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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.SolutionFinder;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.ArchiveIndexHelper;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.OperandWalker;
import au.com.cybersearch2.taq.pattern.SolutionPairer;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * LogicQuery
 * Performs logic operation of unification between a sequence of axioms and a template.
 * Each solution is aggregated with the overall solution being prepared by the LogicQueryExecuter.
 * @author Andrew Bowley
 * 30 Dec 2014
 * @see LogicQueryExecuter
 */
public class LogicQuery implements SolutionFinder
{
	/** Handler to call when a solution is found or null if last query in tail */
    protected SolutionHandler solutionHandler;
    /** Query status - start or in_prgress */
    protected QueryStatus queryStatus;
    /** Iterator over axiom sequence */
    protected Iterator<Axiom> axiomIterator;
    /** Source of axiom sequence */
    protected AxiomSource axiomSource;
    /** Axiom listener is notified of axiom sourced each iteration */
    protected List<LocaleAxiomListener> axiomListenerList;
    /** Object to notify of scope change, null if not required */
    protected ScopeNotifier scopeNotifier;
    /** A Calculator can be used in place of a template */
    private Calculator calculator;
    /** Query properties - may be empty */
    private List<Term> properties;
 
    /**
     * Construct QueryLogic object
     * @param axiomSource Source of axiom sequence
     * @param solutionHandler Handler to call when a solution is found 
     */
	public LogicQuery(AxiomSource axiomSource, SolutionHandler solutionHandler) 
	{
		this.axiomSource = axiomSource;
		this.solutionHandler = solutionHandler;
		queryStatus = QueryStatus.start;
		this.properties = ChainQuery.EMPTY_PROPERTIES;
	}

    /**
     * Construct QueryLogic object
     * @param axiomSource Source of axiom sequence
      */
	public LogicQuery(AxiomSource axiomSource) 
	{
		this(axiomSource, null);
	}

    /**
     * Construct QueryLogic object without axiom source
       */
	public LogicQuery()
    {
        this(null, null);
    }

    /**
     * @return the scopeNotifier
     */
    public ScopeNotifier getScopeNotifier()
    {
        return scopeNotifier;
    }

    /**
     * @param scopeNotifier the scopeNotifier to set
     */
    public void setScopeNotifier(ScopeNotifier scopeNotifier)
    {
        this.scopeNotifier = scopeNotifier;
    }

    /**
     * @param queryStatus the queryStatus to set
     */
    public void setQueryStatus(QueryStatus queryStatus)
    {
        this.queryStatus = queryStatus;
    }

    public Calculator getCalculator() {
		return calculator;
	}

    /**
     * @return the properties
     */
    public List<Term> getProperties()
    {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(List<Term> properties)
    {
        this.properties = properties;
    }

	public AxiomSource getAxiomSource() {
		return axiomSource;
	}

	public void setAxiomSource(AxiomSource axiomSource) {
		this.axiomSource = axiomSource;
	}

	public void setQueryParameters(Template template) {
	    // Set properties attached to query
	    if (!properties.isEmpty())
	        template.getProperties().setInitData(properties);
	}

	/**
	 * Find a solution for specified calculator
	 * @param solution Container to aggregate results  
	 * @param calculator Structure to pair with axiom sequence
	 * @return Flag to indicate if another solution may be available
	 */
	public boolean iterate(Solution solution, Calculator calculator, Template template, ExecutionContext context)
	{
		if (queryStatus == QueryStatus.start)
		{   // Start from beginning of axiom sequence
		    if (axiomSource != null)
		    {
			    axiomIterator = axiomSource.iterator(null);
    			if ((axiomIterator.hasNext()))
     				queryStatus = QueryStatus.in_progress; 
		    }
		    else {
		        queryStatus =  QueryStatus.complete;
		        return false;
		    }
		}
		this.calculator = calculator;
		boolean ok = false;
		while (axiomIterator.hasNext())
		{
			Axiom axiom = axiomIterator.next();
			if (axiomListenerList != null) {
				Locale locale = scopeNotifier != null ? scopeNotifier.getScope().getLocale() : solution.getGlobalLocale();
				for (LocaleAxiomListener axiomListener: axiomListenerList)
					axiomListener.onNextAxiom(new QualifiedName(axiom.getName()), axiom, locale);
			}
			if (calculator.execute(axiom,  template, solution, context) ) {
				ok = true;
				break;
			}
			template.backup(true);
			Template head = template.getNext();
			while (head != null) {
				head.backup(true);
				head = head.getNext();
			}
		}
		if (!ok)
		    queryStatus = QueryStatus.start;
		if (!axiomIterator.hasNext())
			calculator = null;
		return ok;
	}
	
    /**
	 * Find a solution for specified template
	 * @param solution Container to aggregate results  
	 * @param template Structure to pair with axiom sequence
	 * @return Flag to indicate if another solution may be available
	 */
	@Override
	public boolean iterate(Solution solution, Template template, ExecutionContext context)
	{
	    // An empty template is provided for unification with the incoming axiom, by first
	    // populating it with empty operands to match the axiom terms.
	    // The empty template is located as the first in the chain.
	    boolean emptyTemplate = false;
		if (queryStatus == QueryStatus.start)
		{   // Start from beginning of axiom sequence
		    if (axiomSource != null)
		    {
			    axiomIterator = axiomSource.iterator(context);
    			if ((axiomIterator.hasNext()))
    			{    
    				queryStatus = QueryStatus.in_progress; 
    				emptyTemplate = (template.getTermCount() == 0);
    			}
		    }
		    else 
		        queryStatus =  QueryStatus.complete;
		    if (template.isReplicate() && !template.isBackedUped())
		    	template.backup(0);
			if ((queryStatus == QueryStatus.start) || (queryStatus ==  QueryStatus.complete))
			   // When AxiomSource is absent or empty, allow unification solely with solution
				return unifySolution(solution, template) &&
					    completeSolution(solution, template, context);
		}
		// Iterate through axioms to find solution
		boolean success = false;
		while (axiomIterator.hasNext())
		{
			Axiom axiom = axiomIterator.next();
			if (axiomListenerList != null) {
				Locale locale = scopeNotifier != null ? scopeNotifier.getScope().getLocale() : solution.getGlobalLocale();
				for (LocaleAxiomListener axiomListener: axiomListenerList)
					axiomListener.onNextAxiom(new QualifiedName(axiom.getName()), axiom, locale);
			}
			if (emptyTemplate && template.getName().equals(axiom.getName()) && template.getKey().equals(axiom.getName()))
	        {   // Populate empty template with operands to match the named terms of the axiom
	            for (int i = 0; i < axiom.getTermCount(); i++)
	            {
	                Term term = axiom.getTermByIndex(i);
	                if (!term.getName().equals(Term.ANONYMOUS))
	                    template.addTerm(new Variable(new QualifiedName(term.getName(), template.getQualifiedName())));
	            }
                ArchiveIndexHelper archiveIndexHelper = new ArchiveIndexHelper(template);
                archiveIndexHelper.setOperandTree(1);
	            emptyTemplate = false;
	        }
			if (unify(axiom, template, solution, context)) {
			    Template head = template.getNext();
				if ((head == null) || unifyChain(axiom, head, solution, context)) {
				    if (completeSolution(solution, template, context)) 
					{
						success = true;
						break;
					}
					else if (head != null)
						backupChain(head);
				}
			}
		    template.backup(true);
		}
		if (!success || !axiomIterator.hasNext())
		    queryStatus = QueryStatus.start;
		return success;
	}

	/**
	 * Unify template with axiom and solution
	 * @param axiom The axiom to pair with
	 * @param template Template to pair with
	 * @param solution The solution to pair with
	 * @param context Evaluation context
	 * @return flag set true if unification succeeds
	 */
	boolean unify(Axiom axiom, Template template, Solution solution, ExecutionContext context)
    {   // Unify enclosed templates which will participate in ensuing evaluation
		boolean isCaseInsensititve = ((context != null) && context.isCaseInsensitiveNameMatch());
	    return isCaseInsensititve ? template.unifyCaseInsensitive(axiom, solution) : template.unify(axiom, solution);
    }

	/**
	 * Unify template with axiom and solution
	 * @param axiom The axiom to pair with
	 * @param template Template to pair with
	 * @param solution The solution to pair with
	 * @param context Evaluation context
	 * @return flag set true if unification succeeds
	 */
	boolean unifyChain(Axiom axiom, Template template, Solution solution, ExecutionContext context)
    {   // Unify enclosed templates which will participate in ensuing evaluation
		boolean isCaseInsensititve = ((context != null) && context.isCaseInsensitiveNameMatch());
		boolean success = true;
        while (template != null)
        {
        	if ((context != null) && isCaseInsensititve)
        		success = template.unifyCaseInsensitive(axiom , solution);
        	else
        		success = template.unify(axiom , solution);
            if (!success)
            {
                break;
            }
            template = template.getNext();
        }
        return success;
    }
	
	/**
	 * Backup template chain
	 * @param template Head of template chain
	 * @return flag set true if unification succeeds
	 */
	void backupChain(Template template)
    {   // Unify enclosed templates which will participate in ensuing evaluation
        while (template != null)
        {
        	template.backup(true);
        	template = template.getNext();
        }
    }
	
   List<LocaleAxiomListener> getAxiomListenerList() {
        if (axiomListenerList == null)
            axiomListenerList = new ArrayList<>();
		return axiomListenerList;
	}

	/**
     * Set axiom listener to receive each solution as it is produced
     * @param axiomListener The axiom listener object
     */
    @Override
    public void setAxiomListener(LocaleAxiomListener axiomListener) 
    {
    	getAxiomListenerList().add(axiomListener);
    }

    /**
     * Set query status to "complete" to stop any further query processing
     */
    public void setQueryStatusComplete() 
    {
        queryStatus = QueryStatus.complete;
    }

    /**
	 * Unify template with solution.
	 * @param solution Container to aggregate results  
	 * @param template Structure to pair with axiom sequence
	 * @return Flag to indicate if the query is resolved
	 */
	protected boolean unifySolution(Solution solution, Template template)
    {
		if (solution.size() > 0)
		{
	        boolean success = unify(template, solution);
	        Template chainTemplate = template.getNext();
	        while (chainTemplate != null)
	        {
	            if (!unify(chainTemplate , solution))
	                success = false;
	            chainTemplate = chainTemplate.getNext();
	        }
	        return success;
		}
		return true;
    }

	/**
	 * Unify chain template with solution
	 * @param template The template to pair with
	 * @param solution The solution to pair with
     * @return flag set true if unification succeeds
	 */
	protected boolean unify(Template template, Solution solution)
	{
        OperandWalker walker = template.getOperandWalker();
        walker.setAllNodes(true);
        final SolutionPairer pairer = template.getSolutionPairer(solution);
        return walker.visitAllNodes(pairer);
	}
	
	/**
	 * Complete finding solution following successful unification
	 * @param solution Container to aggregate results  
	 * @param template Structure to pair with axiom sequence
	 * @return Flag to indicate if the query is resolved
	 */
	private boolean completeSolution(Solution solution, Template template, ExecutionContext context)
	{
		try
		{
			// evaluate() may result in a short circuit exit flagged by returning false
			// isfact() flags true if each term of the template is non-empty
			if ((template.evaluate(context) == EvaluationStatus.COMPLETE) && template.isFact())
			{
			    String solutionKey = template.getQualifiedName().toString();
				solution.put(solutionKey, template.toAxiom());
				if ((solutionHandler == null) ||
				     solutionHandler.onSolution(solution))
					return true;
			}
		}
		catch (ExpressionException e)
		{   // evaluate() exceptions are thrown by Evaluator objects 
			throw new QueryExecutionException("Error evaluating: " + template.toString(), e);
		}
		return false;
	}
	
	/**
	 * Returns query status
	 * @return QueryStatus
	 */
	protected QueryStatus getQueryStatus() 
	{
		return queryStatus;
	}

}
