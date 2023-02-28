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
package au.com.cybersearch2.taq.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.IntegerTerm;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.BooleanOperator;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.operator.StringOperator;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;

/**
 * Compiles a TAQ file and then launches a query contained in the file.
 * The file is located by searching a directory tree, the root of which 
 * defaults to the examples project resources location. This can be 
 * overridden by using TAQ_RESOURCES environmental location set to
 * the absolute location of the target taq resources.
 */
public class Taq {

	private static final class Query implements SolutionHandler, Comparable<Query> {
		private final String name;
		private final String scope;
		private OperandType returnType;
		private Map<String,List<Axiom>> solutionMap;
		
		public Query(String scope, String name) {
			this.name = name;
			this.scope = scope;
			returnType = OperandType.UNKNOWN;
		}

		public String getName() {
			return name;
		}

		public String getQueryName() {
			return scope + "." + name;
		}

		public QualifiedName getQualifiedName() {
			return new QualifiedName(scope, name);
		}
		
		public String getScope() {
			return scope;
		}

		public boolean hasReturnType() {
			return returnType !=  OperandType.UNKNOWN;
		}
		
		public OperandType getReturnType() {
			return returnType;
		}

		public void setReturnType(OperandType returnType) {
			this.returnType = returnType;
		}

		
		public Map<String, List<Axiom>> getSolutionMap() {
			return solutionMap != null ? solutionMap : Collections.emptyMap();
		}

		@Override
		public boolean onSolution(Solution solution) {
			if (solutionMap == null) {
				solutionMap = new TreeMap<>();
			    solution.keySet().forEach(key -> solutionMap.put(key, new ArrayList<>()));
			}
		    solution.keySet().forEach(key -> solutionMap.get(key).add(solution.getAxiom(key)));
			return true;
		}

		@Override
		public int compareTo(Query other) {
			return getQueryName().compareTo(other.getQueryName());
		}

		@Override
		public int hashCode() {
			return getQueryName().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Query)
				return ((Query)obj).getQueryName().equals(getQueryName());
			return super.equals(obj);
		}
	}

	private static final class Export {

		private String scopeName;
		private List<QualifiedName> exportListNames;

		public Export(String scopeName, List<QualifiedName> exportListNames) {
			this.scopeName = scopeName;
			this.exportListNames = exportListNames;
		}

		public String getScopeName() {
			return scopeName;
		}

		public List<QualifiedName> getExportListNames() {
			return exportListNames;
		}
		
	}
	
	static public String CLASSES_ROOT;
	static public String RESOURCES_ROOT;
	static public String LIBRARIES;
	static public String WORKSPACE;
	
	static private String CLASSES_PATH = "/examples/target/classes";
	static private String LIBRARIES_PATH = "/examples/lib";
	static private String EXAMPLES_PATH = "/examples/src/main/resources";
	static private String WORKSPACE_PATH = "/examples/workspace/";
	
	static public String USAGE = "Taq <taq-file-name>";
	static private boolean quietMode = false;

	private final List<String> args;
	private String taqFile;
    private QueryProgramParser queryProgramParser;
    private QueryProgram queryProgram;
    private ProviderManager providerManager;
    private Set<Query> queries;
    private List<Export> exports;
    private List<String> captureList;
    
	
	public Taq(List<String> args) {
		this(args, null);
	}

	public Taq(List<String> args, ProviderManager providerManager) {
		this.args = args;
		exports = Collections.emptyList();
		if (providerManager == null) {
			providerManager = new ProviderManager();
			initPropertyManager(providerManager);
		}
		this.providerManager = providerManager;
	}

	public QueryProgram getQueryProgram() {
		if (queryProgram == null)
			throw new IllegalStateException("QueryProgram object not available");
		return queryProgram;
	}

	public void printArgs() {
		System.out.println(args.toString());
	}

	public List<String> getCaptureList() {
		return captureList = new ArrayList<>();
	}
	
	public void findFile() throws IOException {
		String filename = args.get(0);
		int pos = filename.lastIndexOf('.');
		if (pos == -1)
			filename += ".taq";
		Path startDir = Paths.get(RESOURCES_ROOT);
		//System.out.println(startDir.toString());
		FileFinder fileFinder = new FileFinder(startDir, filename);
	    Files.walkFileTree(startDir, fileFinder);
	    String string = fileFinder.getFile().toString();
	    if (string.length() <= RESOURCES_ROOT.length())
	    	throw new IOException(String.format("File %s not found", filename));
	    taqFile = string.substring(RESOURCES_ROOT.length());
	}

	public boolean compile() {
        queryProgramParser = new QueryProgramParser(providerManager);
        queryProgram = queryProgramParser.loadScript(taqFile);
        queryProgram.runPreLaunchTasks();
        queries = new TreeSet<>();
        queryProgram.getScopes().forEach((key, value) -> {
        	value.getQuerySpecMap().keySet().forEach(queryName -> queries.add(new Query(key, queryName)) );
        });
        queries.forEach(query -> {
        	Scope scope = queryProgram.getScopes().get(query.getScope());
        	ListAssembler listAssembler = scope.getParserAssembler().getListAssembler();
        	ItemList<?> itemList = listAssembler.findAxionContainer(query.getQualifiedName());
        	if (itemList != null)
        		query.setReturnType(itemList.getOperandType());
        });
        queryProgram.getScopes().values().forEach(scope -> {
        	List<QualifiedName> exportListNames = scope.getExportListNames();
        	if (!exportListNames.isEmpty()) {
        		if (exports.isEmpty())
        			exports = new ArrayList<>();
        		exports.add(new Export(scope.getName(), exportListNames));
        	}
        });
		return true;
	}

	public boolean execute() {
		return execute("*");
	}
	
	public boolean execute(String queryName) {
		boolean done = false;
		if ("*".equals(queryName)) {
			if (queries.isEmpty())
				evaluateTemplates();
			else {
				for (Query query: queries) {
	        	    doQuery(query);
	            }
				done = true;
			}
		} else {
			for (Query query: queries) {
				int pos = queryName.indexOf('.');
				if (pos != -1 ) {
					String scopeName = queryName.substring(0, pos);
				    String name = queryName.substring(pos + 1);
					if (query.getScope().equals(scopeName) && query.getName().equals(name)) {
	        	        doQuery(query);
	        	        done = true;
	        	        break;
					}
				} else {
					if (query.getScope().equals(QueryProgram.GLOBAL_SCOPE) && query.getName().equals(queryName)) {
	        	        doQuery(query);
	        	        done = true;
	        	        break;
					}
				}
            }
		}
        //Result result = queryProgram.executeQuery("euro_megacities");
		return done;
	}

	private void printAxiomListResult(Result result, QualifiedName name) {
    	Iterator<Axiom> iterator = result.axiomIterator(name);
    	if (iterator.hasNext())
    	    iterator.forEachRemaining(item -> write(axiomToString(name, item)));
	}

	private void printAxiomResult(Result result, QualifiedName name) {
		Axiom axiom = result.getAxiom(name);
		if (!axiom.isEmpty())
            write(axiomToString(name, axiom));
	}

	private void printListResult(Result result, QualifiedName name) {
		Iterator<?> iterator = null;
		OperandType operandType = result.getListType(name);
		if (operandType != null) {
			switch (operandType) {
	        case INTEGER:
	        	iterator = result.integerIterator(name.toString());
	            break; 
	        case DOUBLE:
	        	iterator = result.doubleIterator(name.toString());
	            break;
	        case BOOLEAN:
	        	iterator = result.booleanIterator(name.toString());
	           break; 
	        case STRING:
	        	iterator = result.stringIterator(name.toString());
	            break; 
	        case DECIMAL:
	        case CURRENCY:
	        	iterator = result.decimalIterator(name.toString());
	             break;
	        default:
			}
		}
		if (iterator != null)
			iterator.forEachRemaining(item -> write(item.toString()));
	}

	private void write() {
		if (captureList != null) {
			captureList.add("");
		} else
		System.out.println();
	}

	private void write(String line) {
		if (captureList != null) {
			captureList.add(line);
		} else
			System.out.println(line);
	}
	
	private void write(List<String> lines) {
		lines.forEach(line -> write(line));
	}
	
	private void evaluateTemplates() {
        queryProgramParser.loadProgram(taqFile);
        queryProgram.getScopes().values().forEach(scope -> {
        	ParserAssembler parserAssembler = scope.getParserAssembler();
        	ListAssembler listAssembler = parserAssembler.getListAssembler();
        	Set<QualifiedName> dynamicLists = new HashSet<>();
			TemplateAssembler templateAssembler = parserAssembler.getTemplateAssembler();
			Set<QualifiedName> templateNames = new TreeSet<QualifiedName>();
			templateNames.addAll(templateAssembler.getTemplateNames());
			templateNames.forEach(templateName -> {
				if (!templateName.isTemplateScope()) {
					QualifiedName axiomName = 
						new QualifiedName(templateName.getScope(), QualifiedName.EMPTY, templateName.getTemplate());
	        	    if (listAssembler.existsKey(ListType.axiom_dynamic, axiomName)) {
	        	    	dynamicLists.add(templateName);
	        	    }
				}
        	});
			if (!dynamicLists.isEmpty()) {
				ExecutionContext context = new ExecutionContext();
				dynamicLists.forEach(name -> {
					templateAssembler.getTemplate(name).evaluate(context);
				});
			}
			templateNames.forEach(name -> {
				if (!name.isTemplateScope() &&
					!name.getName().startsWith("scope") && 
					!name.getTemplate().equals("scope") &&
					!dynamicLists.contains(name)) {
					Template template = templateAssembler.getTemplate(name);
					if (!template.isInnerTemplate() && !template.isChoice() && !template.isCalculator()) {
						evaluateTemplate(template);
					}
				}
			});
		});
	}

	private void evaluateTemplate(Template template) {
        template.evaluate(queryProgram.getGlobalScope().getExecutionContext());
        Axiom axiom = template.toAxiom();
        System.out.println();
        System.out.println(String.format("Template: %s", axiom.getName()));
        for (int i = 0; i < axiom.getTermCount(); ++i) {
            Term term = axiom.getTermByIndex(i);
            System.out.println(term.toString());
        }
	}

	private List<String> axiomToString(QualifiedName qname, Axiom axiom) {
		return axiomToString(qname, axiom, 0);
	}
	
	private List<String> axiomToString(QualifiedName qname, Axiom axiom, int indent) {
        StringBuilder builder = new StringBuilder();
        if (!axiom.isEmpty())
        {
        	for (int i = 0; i < axiom.getTermCount(); ++i) {
                Term term  = axiom.getTermByIndex(i);
            	if (!(term instanceof Operand) || !((Operand)term).isPrivate()) {
	                String value = termtoString(term);
	                boolean firstTime = builder.length() == 0;
	                if (firstTime) 
	                    builder.append(getIndent(indent)).append(axiom.getName()).append('(');
	                if (!firstTime && (value != null))
	                    builder.append(", ");
	                if  (value != null)
	                  	builder.append(value);
	                if (builder.length() >= 80)
	                	break;
            	}
            }
            builder.append(')');
         }
        //else
        //    builder.append("()");
        if (builder.length() < 80)
        	return Collections.singletonList(builder.toString());
        List<String> lines = new ArrayList<>();
        lines.add(getIndent(indent) + axiom.getName() + "(");
        builder.setLength(0);
        boolean[] firstTime = new boolean[] {true};
         axiom.forEach( term -> {
        	if (!(term instanceof Operand) || !((Operand)term).isPrivate()) {
        		if (term.getValueClass() == Axiom.class) {
        			if (builder.length() > 0) {
         		        builder.append(',');
        		        lines.add(builder.toString());
        		        builder.setLength(0);
        			} 
        			if (firstTime[0]) 
	                	firstTime[0] = false;
        			lines.addAll(axiomToString(axiom.getArchetype().getQualifiedName(), (Axiom)term.getValue(), indent + 1));
        		} else {
	                String value = termtoString(term);
	                if (firstTime[0]) {
	                	firstTime[0] = false;
	                    if  (value != null) {
	                	    builder.append(getIndent(indent)).append(' ');
	                    	builder.append(value);
	                    }
	                } else {
	                	if (builder.length() > 0) {
	                        builder.append(",");
	                        lines.add(builder.toString());
        		            builder.setLength(0);
	                	}
	                    if  (value != null) {
	                    	builder.append(getIndent(indent)).append(' ');
	                    	builder.append(value);
	                    }
	                }
        		}
        	}
        });
        if (builder.length() > 0)
            lines.add(builder.toString());
        lines.add(getIndent(indent) + ")");
        return lines;
	}

	private String getIndent(int count) {
		StringBuilder builder = new StringBuilder();
		while (count > 0) {
			builder.append(' ');
			--count;
		}
		return builder.toString();
	}
	
	private String termtoString(Term term) {
		String text = null;
		ItemList<?> itemlist = null;
		if (term.getValue() instanceof ListOperand) {
			ListOperand<?> listOperand = (ListOperand<?>)term.getValue();
			itemlist = (ItemList<?>)listOperand;
		} 
		if ((itemlist == null) && (term.getValue() instanceof ItemList))
			itemlist = (ItemList<?>)term.getValue();
		if (itemlist != null) {
			StringBuilder builder = new StringBuilder();
			itemlist.forEach(item -> {
                if (builder.length() == 0)
                    builder.append('(');
                else
                    builder.append(", ");
                builder.append(item.toString());
			});
            builder.append(')');
            text = term.getName() + builder.toString();
		} else if (term.getValueClass() != Blank.class)
			text = term.toString();
		return text;
	}

	private List<Term> getParameters() {
        List<Term> terms = new ArrayList<>();
        for (int i = 1; i < args.size() ; ++i) {
        	String parameter = args.get(i);
        	if (parameter.startsWith("-") || parameter.startsWith("^") || parameter.startsWith("+"))
        		continue;
        	int pos = parameter.indexOf('=');
        	boolean isTrue = pos == -1;
        	Object value;
        	String key;
        	Operator operator;
        	if (isTrue) {
	        	value = Boolean.TRUE;
	        	key = parameter;
	        	operator = new BooleanOperator();
        	} else {
            	value = parameter.substring(pos + 1);
            	key = parameter.substring(0, pos);
            	if (value.toString().startsWith("0x")) {
            		terms.add(new IntegerTerm(value.toString()));
            		continue;
            	} else
            	    operator = new StringOperator();
        	}
        	terms.add(new OperatorTerm(key, value, operator));
        }
        return terms;
	}

	private void doQuery(Query query) {
    	QueryParams queryParams = null;
    	boolean skip = false;
        if (args.size() > 1) {
        	for (String arg: args) {
        		if (arg.startsWith("-") && arg.substring(1).equals(query.getQueryName())) {
        			skip = true;
        			break;
        		} else if (arg.startsWith("^")) {
    				String queryName = arg.substring(1);
    				boolean isMatch = queryName.equals(query.getName());
    				if (!isMatch)
    					isMatch = queryName.equals(query.getQueryName());
        			if (!isMatch) {
              			skip = true;
              			break;
          			}
        		} else if (arg.startsWith("+")) {
    				String option = arg.substring(1);
    				if (option.equals("ci") || option.equals("case-insensitive"))
    					queryProgram.setCaseInsensitiveNames(true);
    				else
    					System.err.println(String.format("Unknown option \"%s\"", option));
        		}
        	}
        	if (skip)
        		return;
        	queryParams = queryProgram.getQueryParams(query.getScope(), query.getName());
            Solution initialSolution = queryParams.getInitialSolution();
            KeyName keyname = queryParams.getQuerySpec().getKeyNameList().get(0);
            String key = query.getName();
            QualifiedName axiomKeyName = keyname.getAxiomKey();
            if (!axiomKeyName.equals(KeyName.EMPTY_QNAME))
                key = axiomKeyName.getName();
            AxiomArchetype archetype = new AxiomArchetype(new QualifiedName(key));
            List<Term> terms = getParameters();
            if (terms.size() > 0) {
                Axiom axiom = archetype.itemInstance(terms);
                initialSolution.put(axiom.getName(), axiom);
            }
        }
        if (!quietMode) {
    	    System.out.println();
    	    System.out.println(String.format("Running query %s in %s scope ", query.getName(), query.getScope()));
        }
        boolean hasParams = true;
        if (queryParams == null) {
        	hasParams = false;
        	queryParams = queryProgram.getQueryParams(query.getScope(), query.getName());
        }
        List<String> otherQueries = new ArrayList<>();
        queries.forEach(item -> {
        	if (!item.getName().equals(query.getName()))
        		otherQueries.add(item.getName());
        });
     	if (query.hasReturnType()) {
        	Result result;
            if (hasParams && !quietMode)
      		    System.out.println(String.format("Parameters %s", args.toString()));
      		result = queryProgram.executeQuery(queryParams);
         	if (query.getReturnType() == OperandType.AXIOM) {
	        	Iterator<Axiom> iterator = result.axiomIterator(query.getName());
	        	if (iterator.hasNext()) {
	        	    iterator.forEachRemaining(item -> { 
	        	    	if (!item.isEmpty())
	        	    	    write(axiomToString(item.getArchetype().getQualifiedName(), item)); 
	        	    });
	        	}
        	} /*
        	    else if (query.getReturnType() == OperandType.TERM) {
        		Axiom axiom = result.getAxiomByKey(query.getName());
        		if (axiom == null) {
	        		axiom = result.getAxiom(query.getScope(), query.getName());
        		}
        		if (!axiom.isEmpty())
                    write(axiomToString(axiom.getArchetype().getQualifiedName(), axiom));
        	} */
        	if (!exports.isEmpty())
            	exports.forEach(export -> printExports(otherQueries, export, result));
        } else if (!exports.isEmpty()) {
        	Result result;
            if (hasParams && !quietMode)
      		    System.out.println(String.format("Parameters %s", args.toString()));
       		result = queryProgram.executeQuery(queryParams);
         	exports.forEach(export -> printExports(otherQueries, export, result));
        } else {
        	if (hasParams && !quietMode)
        		System.out.println(String.format("Parameters %s", args.toString()));
        	queryProgram.executeQuery(queryParams);
        	List<KeyName> keyNameList = queryParams.getQuerySpec().getKeyNameList();
        	List<Iterator<Axiom>> iteratorList = new ArrayList<>();
        	keyNameList.forEach(keyname -> {
        		String templateName = keyname.getTemplateName().getTemplate();
        		List<Axiom> axiomList = query.getSolutionMap().get(templateName);
        		if (axiomList == null) 
        			axiomList = Collections.emptyList();
        	    iteratorList.add(axiomList.iterator());
        	});
        	while(iteratorList.get(0).hasNext()) {
            	write();
            	iteratorList.forEach(iterator -> write(iterator.next().toString()));
        	};
        }
	}

	private void printExports( List<String> otherQueries, Export export, Result result) {
		List<QualifiedName> nameList = new ArrayList<>();
		export.getExportListNames().forEach(qname ->{
			if (!otherQueries.contains(qname.getName()))
				nameList.add(qname);
		});
		printExport(export.getScopeName(), nameList, result);
	}
	
	private void printExport(String scopeName, List<QualifiedName> nameList, Result result) {
		ListAssembler listAssembler = 
    			queryProgram.getScopes()
    				.get(scopeName)
    				.getParserAssembler()
    				.getListAssembler();
		nameList.forEach(name -> {
			if (listAssembler.existsKey(ListType.axiom_dynamic, name))
				printAxiomListResult(result, name);
			else if (listAssembler.existsKey(ListType.term, name))
				printAxiomResult(result, name);
			else if (listAssembler.existsKey(ListType.basic, name))
				printListResult(result, name);
		});
   	}

	public static void setQuietMode() {
		quietMode = true;
		FileFinder.setQuietMode();
	}

	public static void initPropertyManager(ProviderManager providerManager) {
		File workspace = new File(WORKSPACE);
        if (!workspace.exists())
        	workspace.mkdir();
		providerManager.setProperty("workspace", workspace);
		providerManager.setProperty("classes_base", new File(CLASSES_ROOT));
		providerManager.setProperty("libraries", new File(LIBRARIES));
		providerManager.setProperty("resource_base", new File(RESOURCES_ROOT));
	}
	
	public static void initialize() throws IOException {
		File cwd = null;
		File jarFile = null;
		cwd = Paths.get("").toFile();
		jarFile = new File(cwd, "taq.jar");
		boolean useCurrentWDir = jarFile.exists();

		RESOURCES_ROOT = System.getenv("TAQ_RESOURCES");
		if (RESOURCES_ROOT == null) {
			if (useCurrentWDir)
			    RESOURCES_ROOT = cwd.getAbsolutePath() + EXAMPLES_PATH;
			else
				RESOURCES_ROOT = 
					new File(cwd.getAbsolutePath() + "/.." + EXAMPLES_PATH).getCanonicalFile().getAbsolutePath();
		}
		CLASSES_ROOT = System.getenv("TAQ_JAVA_CLASSES");
		if (CLASSES_ROOT == null) {
			if (useCurrentWDir)
				CLASSES_ROOT = cwd.getAbsolutePath() + CLASSES_PATH;
			else
				CLASSES_ROOT = 
					new File(cwd.getAbsolutePath() + "/.." + CLASSES_PATH).getCanonicalFile().getAbsolutePath();
		}
		LIBRARIES = System.getenv("TAQ_LIBRARIES");
		if (LIBRARIES == null) {
			if (useCurrentWDir)
				LIBRARIES = cwd.getAbsolutePath() + LIBRARIES_PATH;
			else
				LIBRARIES = 
					new File(cwd.getAbsolutePath() + "/.." + LIBRARIES_PATH).getCanonicalFile().getAbsolutePath();
		}
		WORKSPACE = System.getenv("TAQ_WORKSPACE");
		if (WORKSPACE == null) {
			if (useCurrentWDir)
				WORKSPACE = cwd.getAbsolutePath() + WORKSPACE_PATH;
			else
				WORKSPACE = 
					new File(cwd.getAbsolutePath() + "/.." + WORKSPACE_PATH).getCanonicalFile().getAbsolutePath();
		}
	}
	
	public static void main(String[] args) throws IOException {
		initialize();
		if (args.length == 0)
			System.out.println(USAGE + System.lineSeparator());
		else {
			Taq taq = new Taq(Arrays.asList(args));
			//taq.printArgs();
			taq.findFile();
			taq.compile();
			try {
			    taq.execute();
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				System.exit(0);
			}
		}
	}

}
