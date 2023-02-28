/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.ParserTask;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.compiler.Compiler;
import au.com.cybersearch2.taq.compiler.ParserResource;
import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.db.EntityCollector;
import au.com.cybersearch2.taq.db.EntityEmitter;
import au.com.cybersearch2.taq.debug.ExecutionConsole;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.PatternFactory;
import au.com.cybersearch2.taq.expression.ResourceOperand;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.InitialProperties;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.model.TaqParser;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.provider.ProviderAgent;
import au.com.cybersearch2.taq.provider.ResourceFunctions;
import au.com.cybersearch2.taq.query.QueryExecutionException;
import au.com.cybersearch2.taq.query.QueryLauncher;
import au.com.cybersearch2.taq.result.Result;
import au.com.cybersearch2.taq.result.ResultList;
import au.com.cybersearch2.taq.scope.ScopeContext;
import au.com.cybersearch2.taq.scope.ScopeManager;
import au.com.cybersearch2.taq.service.WorkerService;

/**
 * QueryProgram Collects the results of parsing a TAQ program. Contains scopes,
 * each scope encompassing operands, axioms, templates and queries in a single
 * namespace. A global scope contains everything, including all other scopes, if
 * any.
 * 
 * @author Andrew Bowley 27 Dec 2014
 */
public class QueryProgram extends QueryLauncher implements ExecutionConsole {
	
	/** The global scope is accessible from all scopes */
	static public final String GLOBAL_SCOPE = "global";
	/**
	 * Sets the Locale language which must be well-formed or an exception is thrown.
	 *
	 * <p>
	 * The typical language value is a two or three-letter language code as defined
	 * in ISO639.
	 */
	static public final String LANGUAGE = "language";
	/**
	 * Sets the Locale script which must be well-formed or an exception is thrown.
	 *
	 * <p>
	 * The typical script value is a four-letter script code as defined by ISO
	 * 15924.
	 */
	static public final String SCRIPT = "script";
	/**
	 * Sets the Locale region which must be well-formed or an exception is thrown.
	 *
	 * <p>
	 * The typical region value is a two-letter ISO 3166 code or a three-digit UN
	 * M.49 area code.
	 *
	 * <p>
	 * The country value in the <code>Locale</code> created by the Locale
	 * <code>Builder</code> is always normalized to upper case.
	 */
	static public final String REGION = "region";
	/**
	 * Sets the Locale variant which must consist of one or more well-formed
	 * subtags, or an exception is thrown.
	 *
	 * <p>
	 * <b>Note:</b> The Locale <code>Builder</code> checks if <code>variant</code>
	 * satisfies the IETF BCP 47 variant subtag's syntax requirements, and
	 * normalizes the value to lowercase letters.
	 */
	static public final String VARIANT = "variant";

	private static final char DOT = '.';
	private static final String NULL_KEY_MESSAGE = "Null key passed to name parser";
	
    /** Maintains a pool of work threads scaled according to available processors */
    private static final WorkerService workerService; 
	/** Logger */
	private static final Logger logger;


	static {
		LogManager.initialize();
		logger = LogManager.getLogger(QueryProgram.class);
		workerService= QueryProgram.getSingleton(WorkerService.class); 
	}

	/** Single scope manager instance */
	private final ScopeManager scopeManager;
	/** Provides indirect access to optional ProviderManager */
	private final ProviderAgent providerAgent;
	
	/** Resource path base */
	private File resourceBase;
	/** Resource function objects mapped by name */
	private List<ResourceFunctions> resourcesList;
	/** Regular expression PatternFactory objects */
	private Map<String, PatternFactory> patternFactoryMap;
	/** Head of initialization template linked list */
	private Template headTemplate;
	/** Compiler used to parse TAQ which has pending tasks to complete */
	private Compiler compiler;

	/**
	 * Default QueryProgram constructor
	 */
	public QueryProgram() {
		this(null);
	}

	/**
	 * Construct QueryProgram object for resource binding
	 * 
	 * @param providerManager Resource provider aggregator
	 */
	public QueryProgram(ProviderManager providerManager) {
		scopeManager = new ScopeManager();
		providerAgent = providerManager == null ? new ProviderAgent() : new ProviderAgent(providerManager);
		resourceBase = providerAgent.getResourceBase();
		injectScope(scopeManager.getGlobalScope());
	}

	/**
	 * Returns File set to resource path
	 * 
	 * @return File object
	 */
	public File getResourceBase() {
		return resourceBase;
	}

	/**
	 * Returns external classes path
	 * 
	 * @return File object
	 */
	public File getClassesBase() {
		return providerAgent.getClassesBase();
	}

	/**
	 * Returns external libraries path
	 * 
	 * @return File object
	 */
	public File getLibraries() {
		return providerAgent.getLibraries();
	}

	/**
	 * Set resource path
	 * 
	 * @param resourceBase Resource path
	 */
	public void setResourceBase(File resourceBase) {
		this.resourceBase = resourceBase;
	}

	/**
	 * Set compiler.
	 * 
	 * @param compiler Compiler used to parse TAQ which has pending tasks to
	 *                 complete
	 * @see QueryPrograParserm#loadScript(String)
	 */
	public void setCompiler(Compiler compiler) {
		this.compiler = compiler;
	}

	/**
	 * Compile TAQ program
	 * 
	 * @param script TAQ program
	 * @return ParserContext object
	 * @throws ExpressionException if parse error encountered
	 */
	public ParserContext parseScript(String script) {
		try {
			InputStream stream = new ByteArrayInputStream(script.getBytes("UTF-8"));
			TaqParser queryParser = new TaqParser(stream, "UTF-8");
			ParserContext context = new ParserContext(this);
			compiler = new Compiler(queryParser.publish(), context);
			compiler.compile();
			return context;
		} catch (UnsupportedEncodingException e) {
			throw new ExpressionException("Error reading UTF-8 encoded script: " + e.getMessage(), e);
		}
	}

	/**
	 * Returns new Scope instance
	 * 
	 * @param scopeName  Scope name
	 * @param properties Optional properties eg. Locale
	 * @return Scope object
	 * @throws ExpressionException if global scope name requested or a scope exists
	 *                             with the same name
	 */
	public Scope scopeInstance(String scopeName, InitialProperties scopeProperties) {
		Scope newScope = scopeManager.scopeInstance(scopeName, scopeProperties);
		injectScope(newScope);
		return newScope;
	}

	/**
	 * Returns global scope
	 * 
	 * @return Scope
	 */
	public Scope getGlobalScope() {
		return scopeManager.getGlobalScope();
	}

	/**
	 * Returns scope specified by name.
	 * 
	 * @param name Name
	 * @return Scope object
	 * @throws IllegalArgumentException if scope does not exist
	 */
	public Scope getScope(String name) {
		return getGlobalScope().getScope(name);
	}

	/**
	 * Returns unmodifiable map containing all scopes with key equals scope name
	 * 
	 * @return Scope map
	 */
	public Map<String, Scope> getScopes() {
		Map<String, Scope> scopeMap = new HashMap<>();
		scopeManager.forEach(scope -> scopeMap.put(scope.getName(), scope));
		return Collections.unmodifiableMap(scopeMap);
	}

	/**
	 * Returns QueryParams object for name in global scope
	 * 
	 * @param queryName Name of query in scope
	 * @return QueryParams object
	 */
	public QueryParams getQueryParams(String queryName) {
		return getQueryParams(GLOBAL_SCOPE, queryName);
	}

	/**
	 * Returns QueryParams object for specified by scope and name
	 * 
	 * @param scopeName The scope the query applies to
	 * @param queryName Name of query in scope
	 * @return QueryParams object
	 */
	public QueryParams getQueryParams(String scopeName, String queryName) {
		Scope scope = getScope(scopeName);
		QuerySpec querySpec = scope.getQuerySpec(queryName);
		if (querySpec == null)
			throw new IllegalArgumentException("Query \"" + queryName + "\" does not exist");
		return new QueryParams(scope, querySpec);
	}

	/**
	 * Execute query identified by name in named scope.
	 * 
	 * @param scopeName       Scope name
	 * @param queryName       Query name
	 * @param solutionHandler Handler to process each Solution generated by the
	 *                        query
	 * @return Result object containing any result lists generated by the query
	 */
	public Result executeQuery(String scopeName, String queryName, SolutionHandler solutionHandler) {
		QueryParams queryParams = getQueryParams(scopeName, queryName);
		queryParams.setSolutionHandler(solutionHandler);
		return executeQuery(queryParams);
	}

	/**
	 * Execute query framed with query parameters
	 * 
	 * @param queryParams The query parameters, including the query specification
	 * @return Result object containing any result lists generated by the query
	 */
	public Result executeQuery(QueryParams queryParams) {
		Scope scope = queryParams.getScope();
		ScopeContext scopeContext = scope.getContext(false);
		Map<QualifiedName, ResultList<?>> listMap = new HashMap<>();
		Map<QualifiedName, AxiomTermList> axiomMap = new HashMap<>();
		boolean isWorkerServiceActive = false;
		try {
			String sourceDocument = "";
			if (providerAgent.activate()) {
				workerService.addClient();
				isWorkerServiceActive = true;
			}
				
			// Evaluate resource templates
			if (compiler != null) {
				List<String> docList = compiler.getSourceTracker().getSourceDocumentList();
				if (!docList.isEmpty())
					sourceDocument = docList.get(0);
				runPreLaunchTasks();
			}
			scope.getGlobalScope().evaluateScopeTemplate();
			scopeManager.forEach(item -> {
				if (!GLOBAL_SCOPE.equals(item.getName()))
					item.evaluateScopeTemplate();
			});
			logger.trace(String.format("Launching query %s", sourceDocument));
			// Launch query
			launch(queryParams);
			// Collect results
			queryParams.getQueryScopes().forEach(scopeName -> {
				Scope queryScope = scope.findScope(scopeName);
				listMap.putAll(queryScope.getListMap());
				axiomMap.putAll(queryScope.getAxiomMap(listMap));
			});
			scopeManager.processResults();
		} catch (ExpressionException e) {
			e.printStackTrace();
		} finally {
			providerAgent.close();
			scopeContext.resetScope();
			if (resourcesList != null)
				resourcesList.forEach(functionObject -> {
					try {
						// Call close() for all resources to cover exception thrown
						functionObject.close();
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				});
			scopeManager.forEach(item -> {
				if (!GLOBAL_SCOPE.equals(item.getName()))
					item.backupScopeTemplate();
			});
			scope.getGlobalScope().backupScopeTemplate();
			if (isWorkerServiceActive)
				workerService.removeClient();
		}
		return new Result(listMap, axiomMap);
	}

	/**
	 * Execute query identified by name, potentially qualified with scope. Use
	 * provided solution handler
	 * 
	 * @param queryName       Query name
	 * @param solutionHandler Handler to process each Solution generated by the
	 *                        query
	 * @return Result object containing any result lists generated by the query
	 */
	public Result executeQuery(String queryName, SolutionHandler solutionHandler) {
		return executeQuery(getScopePart(queryName), getNamePart(queryName), solutionHandler);
	}

	/**
	 * Execute query identified by name, potentially qualified with scope.
	 * 
	 * @param queryName Query name
	 * @return Result object
	 */
	public Result executeQuery(String queryName) {
		return executeQuery(queryName, QueryParams.DO_NOTHING);
	}

	/**
	 * Execute query identified by name in named scope.
	 * 
	 * @param scopeName Scope name
	 * @param queryName Query name
	 * @return Result object
	 */
	public Result executeQuery(String scopeName, String queryName) {
		return executeQuery(scopeName, queryName, QueryParams.DO_NOTHING);

	}

	/**
	 * Open resource with properties, if supplied
	 * 
	 * @param parserResource Parser resource
	 * @param properties     Properties specific to the resource. May be empty.
	 * @return ResourceOperand object
	 * @throws ExpressionException if open of provider fails
	 */
	public ResourceOperand createResourceOperand(ParserResource parserResource, Map<String, Object> properties)
			throws ExpressionException {
		ResourceProvider resourceProvider = providerAgent.getResourceProvider(parserResource, properties);
		// Add resource object to list used to initialize and close resources
		if (resourcesList == null)
			resourcesList = new ArrayList<>();
		ResourceOperand resourceOperand = new ResourceOperand(parserResource, resourceProvider);
		resourcesList.add(new ResourceFunctions(resourceOperand));
		return resourceOperand;
	}

	/**
	 * Run all parser tasks, which completes compilation after entire program has
	 * been input
	 */
	public void runPending() {
		PriorityQueue<ParserTask> priorityQueue = new PriorityQueue<ParserTask>();
		scopeManager.forEach(scope -> 
			scope.getParserAssembler().getPending(priorityQueue)
	    );
		Scope globalScope = scopeManager.getGlobalScope();
		ParserTask parserTask = priorityQueue.poll();
		while (parserTask != null) {
			Scope scope = globalScope.getScopeByName(parserTask.getScopeName());
			ParserAssembler parserAssembler = scope.getParserAssembler();
			QualifiedName savedQName = parserAssembler.getQualifiedContextname();
			parserAssembler.setQualifiedContextname(parserTask.getQualifiedContextname());
			parserTask.run(parserAssembler);
			parserAssembler.setQualifiedContextname(savedQName);
			parserTask = priorityQueue.poll();
		}
		// Run parser task for every non-empty template. For each operand in the template,
		// the task walks the operand tree and adds all terms to the archetype which
		// belong to the template name space.
		scopeManager.forEach(scope -> {
			TemplateAssembler templateAssembler = scope.getParserAssembler().getTemplateAssembler();
			templateAssembler.doParserTask();
		});
	}

	/**
	 * Adds pattern factory
	 * 
	 * @param patternFactory Pattern factory
	 * @throws ExpressionException if another factory of the same name exists
	 */
	public void addPatternFactory(PatternFactory patternFactory) {
		if (patternFactoryMap == null)
			patternFactoryMap = new HashMap<>();
		else if (patternFactoryMap.values().contains(patternFactory))
			throw new ExpressionException(
					String.format("Regular expression pattern '%s' duplicate encountered", patternFactory.getName()));
		patternFactoryMap.put(patternFactory.getName(), patternFactory);
	}

	/**
	 * Returns pattern factory identified by name
	 * 
	 * @param name Name
	 * @return PatternFactory object
	 * @throws ExpressionException if another factory not found
	 */
	public PatternFactory getPatternFactory(String name) {
		if (patternFactoryMap != null) {
			PatternFactory patternFactory = patternFactoryMap.get(name);
			if (patternFactory != null)
				return patternFactory;
		}
		throw new ExpressionException(String.format("Regular expression pattern '%s' not found", name));
	}

	/**
	 * Add a template to pre-launch evaluation chain
	 * 
	 * @param template Template object
	 */
	public void addInitTemplate(Template template) {
		if (headTemplate == null)
			headTemplate = template;
		else {
			Template next = headTemplate;
			while (next.getNext() != null) {
				next = next.getNext();
				if (next == template)
					return;
			}
			next.setNext(template, 0);
		}
	}

	/**
	 * Run pre-launch tasks
	 */
	public void runPreLaunchTasks() {
		if (headTemplate != null) {
			Template template = headTemplate;
			while (template != null) {
				template.evaluate(scopeManager.getGlobalScope().getExecutionContext());
				template = template.getNext();
			}
		}
		// Open auto resources
		if (resourcesList != null)
			resourcesList.forEach(functionObject -> {
				if (functionObject.resourceOperand.isAutoConnect())
					functionObject.open();
			});
		compiler.runPending();
		compiler = null;
	}

	/**
	 * Return list of all database providers
	 * 
	 * @return DatabaseProvider list
	 */
	public List<DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>> getDatabaseProviders() {
		return providerAgent.getDatabaseProviders();
	}

	@Override
	public void setCaseInsensitiveNames(boolean flag) {
		scopeManager.setCaseInsensitiveNames(flag);
		
	}

	@Override
	public void setCaseInsensitiveNames(String scopeName, boolean flag) {
		scopeManager.setCaseInsensitiveNames(scopeName, flag);
	}
	
	@Override
	public void setLoopTimeout(int timeout) {
		scopeManager.setLoopTimeout(timeout);
	}

	@Override
	public void setLoopThreshold(int threshold) {
		scopeManager.setLoopThreshold(threshold);
	}

	@Override
	public void setLoopTimeout(String scopeName, int timeout) {
		scopeManager.setLoopTimeout(scopeName, timeout);
	}

	@Override
	public void setLoopThreshold(String scopeName, int threshold) {
		scopeManager.setLoopThreshold(scopeName, threshold);
	}

	/**
	 * Returns fixed thread pool service
	 */
	public static WorkerService getWorkerService() {
		return workerService;
	}

	/**
	 * Returns name part of 1 or 2 part key
	 * 
	 * @param key Key
	 * @return name
	 */
	public static String getNamePart(String key) {
		if (key == null)
			throw new IllegalArgumentException(NULL_KEY_MESSAGE);
		int dot = key.lastIndexOf(DOT);
		if (dot != -1)
			return key.substring(dot + 1);
		return key;
	}

	/**
	 * Returns scope part of key or name of Global Scope if not in key
	 * 
	 * @param key Key
	 * @return String
	 */
	public static String getScopePart(String key) {
		if (key == null)
			throw new IllegalArgumentException(NULL_KEY_MESSAGE);
		int dot = key.indexOf(DOT);
		if (dot != -1)
			return key.substring(0, dot);
		return QueryProgram.GLOBAL_SCOPE;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getSingleton(Class<T> clazz) {
		for (Singleton singleton : Singleton.values())
			if (singleton.getMyClass() == clazz)
				return (T) singleton.getObject();
		throw new QueryExecutionException(String.format("Singleton for class %s does not exist", clazz.getName()));
	}

	/**
	 * Set scope resource binding and function support aggregators
	 * 
	 * @param scope Scope object to set
	 */
	private void injectScope(Scope scope) {
		providerAgent.injectScope(scope);
	}

}
