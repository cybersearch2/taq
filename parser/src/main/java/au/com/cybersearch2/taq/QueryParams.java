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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.cybersearch2.taq.axiom.AxiomMapCollection;
import au.com.cybersearch2.taq.axiom.SingleAxiomSource;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.AxiomCollection;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.NameParser;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.query.Solution;

/**
 * QueryParams
 * Composes parameters for query from QuerySpec and supplied Scope.
 * Note the scope may internally reference the global Scope
 * @author Andrew Bowley
 */
public class QueryParams 
{
    public static SolutionHandler DO_NOTHING;
    
    /** Query specification */
	private final QuerySpec querySpec;
	/** Scope object which provides objects to be passed to the query */
	private final Scope scope;
    /** 1st query parameter - AxiomCollection object */
	private AxiomMapCollection axiomEnsemble;
	/** 2nd query parameter - List of Template objects */ 
	private List<Template> templateList;
	/** Solution handler (optional). Do-nothing handler applied if none supplied */
	private SolutionHandler solutionHandler;
	/** Solution (optional). Source of initialization axioms */
	private Solution initialSolution;
    /** Container for template axiom parameters */
	private Map<QualifiedName, Axiom> parametersMap;
	/** Set of scopes participating in this query */
	private Set<String> queryScopes;

    static
    {
        DO_NOTHING = new SolutionHandler(){

            @Override
            public boolean onSolution(Solution solution)
            {
                return true;
            }};
    }
    
	/**
	 * Construct QueryParams object
	 * @param scope Specified scope
	 * @param querySpec Specifies query parameters as key name sequence(s)
	 */
	public QueryParams(Scope scope, QuerySpec querySpec)
	{
		this.scope = scope;
		this.querySpec = querySpec;
	}

	/**
	 * Initialize these query parameters
	 */
	public void initialize()
	{
	    // Notify locale listeners of change of scope
		scope.notifyChange();
		boolean isStart = (axiomEnsemble == null) || (templateList == null);
		if (isStart)
		{
		    // Collect query axiom sources and templates
		    templateList = new ArrayList<>();
		    axiomEnsemble = new AxiomMapCollection();
		}
		ListAssembler listAssembler = scope.getParserAssembler().getListAssembler();
		// Iterate through list of query specification KeyNames
		if (isStart || (initialSolution != null))
    		for (KeyName keyname: querySpec.getKeyNameList())
    		{
    		    // Collect axiom source
    			QualifiedName axiomKey = keyname.getAxiomKey();
    			if (!axiomKey.getName().isEmpty())
    			{
    			    AxiomSource axiomSource = scope.findAxiomSource(axiomKey);
    			    if ((axiomSource == null) && (initialSolution != null))
    			    {
    			        Axiom axiom = initialSolution.getAxiom(axiomKey.getName());
    			        if (axiom != null)
    			        {
    			            axiomSource = new SingleAxiomSource(axiom);
    			            scope.getParserAssembler().addScopeAxiom(axiom);
    			        }
    			    }
    			    if (axiomSource == null)
    			        // Trigger source not found exception
    			        scope.getAxiomSource(axiomKey);
    			    else // Add axiom source to ensemble using one or two part key as specified
                        axiomEnsemble.put(axiomKey.getSource(), axiomSource);
    			}
        	    if (isStart)
        	    {
            		// Collect template
        	    	TemplateAssembler templateAssembler = scope.getTemplateAssembler(keyname.getTemplateName().getScope());
        			Template template = templateAssembler.getTemplate(keyname.getTemplateName());//scope.findTemplate(keyname.ate(keyname.getTemplateName()());
        			if (template == null)
        				throw new IllegalArgumentException("Template \"" + keyname.getTemplateName().toString() + "\" does not exist");
    			    templateList.add(template);
                    if (!template.isBackedUped())
                    	template.backup(0);
                    if (template.isReplicate())
                    	replicateCopyGlobalListeners(template, listAssembler);
        			if (!axiomKey.getName().isEmpty()) // Empty axiom key indicates no axiom
        			    // Setting template key here facilitates unification
        				// Use one or two part key as specified in the axiom key
        				template.setKey(axiomKey.getSource());
        			else // Reset template key in case it was set in a previous query
        			    template.setKey(template.getQualifiedName().toString());
        	    }
    		}
	}

    /**
	 * Returns object referenced by 1st query parameter
	 * @return AxiomCollection
	 */
	public AxiomCollection getAxiomCollection() 
	{
		return axiomEnsemble;
	}

	/**
	 * Returns object referenced by 2nd query parameter
	 * @return List of Template objects
	 */
	public List<Template> getTemplateList() 
	{
		return templateList;
	}

	/**
	 * Returns solution handler. Return do-nothing handler if none supplied
	 * @return SolutionHandler object
	 */
	public SolutionHandler getSolutionHandler() 
	{
		return solutionHandler != null ? solutionHandler : DO_NOTHING;
	}

	/**
	 * Set the solution handler
	 * @param solutionHandler the solutionHandler to set
	 */
	public void setSolutionHandler(SolutionHandler solutionHandler) 
	{
		this.solutionHandler = solutionHandler;
	}

	/**
	 * Returns the scope
	 * @return Scope object
	 */
	public Scope getScope() 
	{
		return scope;
	}

	/**
	 * Returns template scopes named in key name list.
	 * Excludes global scope and query scope.
	 * @return Scope list, possibly empty
	 */
	public List<Scope> getTemplateScopes() 
	{
		List<Scope> templateScopes = null;
		List<KeyName> keynameList = new ArrayList<>();
		querySpec.getKeyNameList().forEach(keyName -> keynameList.add(keyName));
		List<QuerySpec> queryChainList = querySpec.getQueryChainList();
		if (queryChainList != null)
			queryChainList.forEach(chainQuerySpec -> 
		    	chainQuerySpec.getKeyNameList().forEach(keyName -> keynameList.add(keyName))
		);
		for (KeyName keyName: keynameList) {
			String scopeName = keyName.getTemplateName().getScope();
			if (!scopeName.isEmpty() && !scopeName.equals(scope.getName())) {
				Scope templateScope = scope.findScope(scopeName);
				if (templateScope == null)
					throw new ExpressionException(String.format("Scope \"%s\" not found", scopeName));
				if (templateScopes == null)
					templateScopes = new ArrayList<>();
				else if (templateScopes.contains(templateScope))
					continue;
				templateScopes.add(templateScope);
			}
		}
		return templateScopes == null ? Collections.emptyList() : templateScopes;
	}

	/**
	 * Returns query specification
	 * @return QuerySpec object
	 */
	public QuerySpec getQuerySpec() 
	{
		return querySpec;
	}

    /**
     * Add axiom key / template name pair for calculator query, along with optional properties
     * @param qualifiedTemplateName Qualified name of template to which the properties apply
     * @param properties Calculator properties
     */
    public void putProperties(QualifiedName qualifiedTemplateName, Map<String, Object> properties) 
    {
        if ((properties != null) && (properties.size() > 0))
        {
            Axiom calculatorAxiom = new Axiom(qualifiedTemplateName.getName());
            for (Map.Entry<String, Object> entry: properties.entrySet())
                calculatorAxiom.addTerm(new Parameter(entry.getKey(), entry.getValue()));
            if (parametersMap == null)
                parametersMap = new HashMap<QualifiedName, Axiom>();
            parametersMap.put(qualifiedTemplateName, calculatorAxiom);
        }
    }

    /**
     * Returns properties referenced by template name or null if no properties found
     * @param qualifiedTemplateName Qualified name of template to which the properties apply
     * @return Properties object
     */
    public Axiom getParameter(QualifiedName qualifiedTemplateName) 
    {
        return parametersMap == null ? null : parametersMap.get(qualifiedTemplateName);
    }

    /**
     * Returns flag set true if an initial solution has been provided
     * @return boolean
     */
    public boolean hasInitialSolution()
    {
        return initialSolution != null;
    }
 
    /**
     * Returns initial solution, creating one if it does not exist
     * @return Solution object
     */
    public Solution getInitialSolution()
    {
        if (initialSolution == null)
            initialSolution = new Solution(scope.getLocale());
        return initialSolution;
    }

    /**
     * Returns flag set true if first key name specifies an axiom source
     * @return boolean
     */
    public boolean hasAxiomSource() {
        KeyName firstKeyname = querySpec.getKeyNameList().get(0);
        QualifiedName axiomName = firstKeyname.getAxiomKey();
        return !axiomName.getName().isEmpty();
    	
    }

    /**
     * Returns set of names of scopes participating in this query
     * @return names
     */
    public Set<String> getQueryScopes() {
    	if (queryScopes == null) {
    		queryScopes = new HashSet<>();
    		queryScopes.add(scope.getName());
    		addQueryScopes(querySpec.getKeyNameList());
    		List<QuerySpec> chain = querySpec.getQueryChainList();
    		if (chain != null)
    		    chain.forEach(spec -> addQueryScopes(spec.getKeyNameList()));
    	}
		return queryScopes;
	}

    private void addQueryScopes(List<KeyName> keyNameList) {
		keyNameList.forEach(keyName -> {
			if (!keyName.getTemplateName().isScopeEmpty())
				queryScopes.add(keyName.getTemplateName().getScope());
			else
				queryScopes.add(QueryProgram.GLOBAL_SCOPE);
		});

    }
 
    /**
     * Handle case query name matches name of global template returning term list.
     * This is detected by the replicate having no listeners.
     * The replicate needs to copy the listener(s) of the global template.
     * 
     * @param replicate Replicate template
     * @param listAssembler List assembler in scope of replicate
     */
    private void replicateCopyGlobalListeners(Template replicate, ListAssembler listAssembler) {
    	QualifiedName replicateName = replicate.getQualifiedName();
        List<LocaleAxiomListener> listeners = listAssembler.getAxiomListenerMap().get(replicateName);
        if (listeners == null) {
        	QualifiedName queryName = new QualifiedName(NameParser.GLOBAL_SCOPE, replicateName.getTemplate(), QualifiedName.EMPTY);
         	listeners = scope.getGlobalListAssembler().getAxiomListenerMap().get(queryName);
            if (listeners != null) 
            	listeners.forEach(item -> listAssembler.add(replicateName, item));
        }
    }

}
