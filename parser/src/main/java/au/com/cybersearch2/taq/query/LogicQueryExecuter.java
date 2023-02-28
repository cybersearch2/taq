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
package au.com.cybersearch2.taq.query;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.axiom.SingleAxiomSource;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomCollection;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TermList;

/**
 * LogicQueryExecuter
 * Performs logic operations according to configuration of axiom sequences and templates.
 * Performs chained queries which add to solutions provided by main query.
 * @author Andrew Bowley
 * 30 Dec 2014
 */
public class LogicQueryExecuter extends ChainQueryExecuter
{

    /**
	 * QuerySolutionHander 
	 * Chains to next query following solution found for prior query in the chain
	 */
	class QuerySolutionHander implements SolutionHandler
	{
		/** Query index. The value 0 represents the head of the chain. */
		int index;
		
		public QuerySolutionHander(int index)
		{
			this.index = index;
		}

		/**
		 * Handle solution found event
		 * @param solution The axiom-containing Solution
		 * @return Flag set true if complete solution found
		 */
		@Override
		public boolean onSolution(Solution solution) 
		{
			// Execute the next query in the chain
    		LogicQuery nextQuery = logicQueryList.get(index+1);
    		Template nextTemplate = templateList.get(index + 1);
    		if (nextQuery.getQueryStatus() == QueryStatus.complete)
    		    nextQuery.setQueryStatus(QueryStatus.start);
    		onNextTemplate(nextTemplate, index + 1);
			ExecutionContext context;
            ScopeNotifier scopeNotifier = nextQuery.getScopeNotifier();
			if (scopeNotifier != null) {
				scopeNotifier.notifyScopes();
				context = scopeNotifier.getScope().getExecutionContext();
			} else
				context = queryScope.getExecutionContext();
    		return nextQuery.iterate(solution, nextTemplate, context);
		}
	}

	/** The query components, each working with a sequence of axioms */
    private List<LogicQuery> logicQueryList;
    /** A collection of axiom sources which are referenced by name */
    private AxiomCollection axiomCollection;
    /** The template sequence. Each template is assigned to a LogicQuery object */
    private List<Template> templateList;
    /** Head of SolutionHandler chain. Note all queries except tail are assigned a SolutionHandler */
	protected SolutionHandler headSolutionHandler;

	/**
	 * Construct a LogicQueryExecuter object 
	 * @param queryParams The query parameters
	 */
	public LogicQueryExecuter(QueryParams queryParams) 
	{
		super(queryParams);
		if (queryParams.hasInitialSolution())
		    setSolution(queryParams.getInitialSolution());
	    else
	        setSolution(new Solution(queryScope.getLocale()));
		// A collection of axiom sources which are referenced by name
		this.axiomCollection = queryParams.getAxiomCollection();
		// The template sequence. Each template is assigned to a LogicQuery object
		this.templateList = queryParams.getTemplateList();
		logicQueryList = new ArrayList<LogicQuery>();
		// Populate logicQueryList
		initialize();
	}
	
	public void setProperties(List<Term> properties) {
		logicQueryList.get(0).setProperties(properties);
	}
	
	/**
	 * Find next solution. Call getSolution() to obtain an unmodifiable version.
	 * Warning: 
	 * @return Flag to indicate solution found
	 */
	@Override
	public boolean execute()
    {
		ExecutionContext context;
		// At least one LogicQuery will have been created on construction
		LogicQuery logicQuery = logicQueryList.get(0);
        ScopeNotifier scopeNotifier = logicQuery.getScopeNotifier();
		if (scopeNotifier != null) {
			scopeNotifier.notifyScopes();
			context = scopeNotifier.getScope().getExecutionContext();
		} else {
			context = queryScope.getExecutionContext();
            if (axiomListenerMap != null)
			    bindAxiomListeners(queryScope);
		}
		if (logicQuery.getQueryStatus() == QueryStatus.complete)
		{
			int index = 1;
			while (index < logicQueryList.size())
			{
				if (logicQueryList.get(index).getQueryStatus() != QueryStatus.complete)
				    return executeNext(context);
			}
			return false;
		}
		do
		{
			switch (logicQuery.getQueryStatus())
			{
			case in_progress:
			    if (executeNext(context))
			        return true;
			    break;
			case start:
				Template template = templateList.get(0);
				logicQuery.setQueryParameters(template);
			    Axiom seedAxiom = new Axiom(template.getKey()); 
		        template.getProperties().initialize(seedAxiom, (TermList<Operand>)template);
		        if (seedAxiom.getTermCount() > 0) 
		        {
		        	if (logicQuery.getAxiomSource() == null)
		        	    logicQuery.setAxiomSource(new SingleAxiomSource(seedAxiom));
		        	else
		        		solution.put(template.getKey(), seedAxiom);
		        }
				if (template.isCalculator())
				{
					Calculator calculator = new Calculator();
					logicQuery.getAxiomListenerList().forEach(axiomListener -> calculator.setAxiomListener(axiomListener));
			        templateChain.clear();
			        if (logicQuery.iterate(solution, calculator, template, context))
					{
						if (super.execute())
							return true;
					}
				}
				else 
				{	
					if (logicQuery.iterate(solution, template, context))
					{
						if (super.execute())
						{
							if (logicQuery.getQueryStatus() == QueryStatus.start)
								break;
							return true;
						}
					}
				}
				break;
			default:
			}
		} while(logicQuery.getQueryStatus() != QueryStatus.start && logicQuery.getQueryStatus() != QueryStatus.complete);
		logicQuery.setQueryStatusComplete();
		solution.reset();
		return false;
    }

    /**
	 * Force reset to initial state
	 */
	@Override
	public void reset() 
	{
		for (int i = 0; i < templateList.size(); i++)
		{
			Template template = templateList.get(i);
			template.reset();
		}
		super.reset();
	}

	/**
	 * Returns query as String. Shows empty terms with "?",
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return toString((List<? extends TermList<?>>)templateList);
	}

	/**
	 * Execute next iteration of current query
	 * @param context Execution context
	 * @return flag set true if solution found
	 */
	private boolean executeNext(ExecutionContext context)
    {
        int next = logicQueryList.size() - 1;
        LogicQuery nextQuery = null;
        Template nextTemplate = null;
        while (next >= 0)
        {
            nextQuery = logicQueryList.get(next);
            if (nextQuery.getQueryStatus() == QueryStatus.in_progress)
            {
                super.backup(headChainQuery instanceof CalculateChainQuery);
                nextTemplate = templateList.get(next);
                onNextTemplate(nextTemplate, next);
                Calculator calculator = nextQuery.getCalculator();
                if (calculator != null)
                {
			        if (nextQuery.iterate(solution, calculator, nextTemplate, context))
					{
						if ((next == 0) && (nextQuery.getQueryStatus() == QueryStatus.start)) 
							nextQuery.setQueryStatus(QueryStatus.complete);
						if (super.execute())
							return true;
					}
                }
                else if (nextQuery.iterate(solution, nextTemplate, context))
                {
					if ((next == 0) && (nextQuery.getQueryStatus() == QueryStatus.start)) 
					    nextQuery.setQueryStatus(QueryStatus.complete);
                    return super.execute();
                }
                backupToStart(next);
            }
            --next;
        }
        return false;
    }

	private void onNextTemplate(Template nextTemplate, int index) {
        //if (nextTemplate.isReplicate() && !nextTemplate.isBackedUped())
        //	nextTemplate.backup(false);
        //else
            nextTemplate.backup(true);
        solution.remove(nextTemplate.getQualifiedName().toString());
	}

	/**
	 * Initialize the LogicQuery object list. All but the last object requires a solution handler.
	 */
	protected void initialize()
	{
		for (int i = 0; i < templateList.size(); i++)
		{   // Use the template key to reference the corresponding axiom source
			Template template = templateList.get(i);
			String key = template.getKey();
            QualifiedName qname = QualifiedName.parseGlobalName(key);
			LogicQuery logicQuery = null;
			if (i < templateList.size() - 1)
			{   // Create solution handler which causes the next LogicQuery object in the chain
				// to find a solution.
				final int index = i;
				logicQuery = new LogicQuery(axiomCollection.getAxiomSource(key), 
						                    new QuerySolutionHander(index));
			}
			else
				logicQuery = new LogicQuery(axiomCollection.getAxiomSource(key));

	        String scopeName = template.getQualifiedName().getScope();
	        boolean isChangeScope;
	        if (i == 0)
	        	isChangeScope = !scopeName.isEmpty();
	        else 
	        	isChangeScope = !scopeName.equals(templateList.get(i-1).getQualifiedName().getScope());
	        if (isChangeScope)
	        {
	        	Scope templateScope;
	        	if (scopeName.isEmpty()) 
	        		scopeName = QueryProgram.GLOBAL_SCOPE;
	            templateScope = queryScope.findScope(scopeName);
	            logicQuery.setScopeNotifier(new ScopeNotifier((ChainQueryExecuter)this, templateScope));
	        }
			
			logicQueryList.add(logicQuery);
			if (axiomListenerMap != null)
			{
                if (axiomListenerMap.containsKey(qname))
                {
    	        	List<LocaleAxiomListener> axiomListenerList = axiomListenerMap.get(qname);
            		for (LocaleAxiomListener axiomListener: axiomListenerList)
            			logicQuery.setAxiomListener(axiomListener);
	        		axiomListenerMap.remove(qname);
                }
                qname = template.getQualifiedName();
   	        	List<LocaleAxiomListener> axiomListenerList = null;
             	if (axiomListenerMap.containsKey(qname))
             		axiomListenerList = axiomListenerMap.get(qname);
                else 
                {
                	QualifiedName scopeQname = new QualifiedTemplateName(queryScope.getName(), qname.getTemplate());
                    if (axiomListenerMap.containsKey(scopeQname)) 
                    {
                        axiomListenerList = axiomListenerMap.get(scopeQname);
                        
                    }
                }
             	if (axiomListenerList != null)
            	{
             		for (LocaleAxiomListener axiomListener: axiomListenerList)
            			solution.setAxiomListener(qname, axiomListener);
	        		axiomListenerMap.remove(qname);
            	}
			}
		}
		if (templateList.size() > 1)
			headSolutionHandler = new QuerySolutionHander(0);
		/*
		if (axiomListenerMap != null)
		{
			getQueryChainList().forEach(querySpec -> {
				querySpec.
				for (int i = 0; i < templateList.size(); i++)
				{   // Use the template key to reference the corresponding axiom source
					Template template = templateList.get(i);
				}
			});
		}*/
	}

	/**
	 * Backup to start state
	 * @param start Index of first template index to backup
	 */
	protected void backupToStart(int start)
	{
		for (int i = start; i < templateList.size(); i++)
		{
			Template template = templateList.get(i);
			template.backup(false);
		}
	}

	/**
	 * Returns text representation of query where empty terms are expressed as '?' 
	 * @param termLists List of template termss contained in query
	 * @return String
	 */
	static protected String toString(List<? extends TermList<?>> termLists)
	{
		StringBuilder builder = new StringBuilder();
		boolean firstTime = true;
	    for (TermList<?> termList: termLists)
	    {
	    	if (firstTime)
	    		firstTime = false;
	    	else
	    		builder.append(", ");
	    	builder.append(termList.toString().replaceAll("\\<empty\\>", "?"));
	    }
	    return builder.toString();	
	}




}
