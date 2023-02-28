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

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.SolutionFinder;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.OperandWalker;
import au.com.cybersearch2.taq.pattern.SolutionPairer;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TermList;

/**
 * Calculator
 * Query with extra evaluation features beyond LogicQuery.
 * Because loop evaluation is permitted, a 2 second timer operates to break an infinite loop.
 * @author Andrew Bowley
 * 11 Jan 2015
 */
public class Calculator implements SolutionFinder
{
    /** Optional Axiom to initialize calculation */
	protected Axiom axiom;
    /** Axiom listener is notified of axiom source each iteration */
    protected LocaleAxiomListener axiomListener;

	/**
	 * Find a solution for specified template
	 * @param solution Container to aggregate results  
	 * @param template Template used on each iteration
     * @return Always true to indicate the query is resolved
	 */
	@Override
	public boolean iterate(Solution solution, Template template, ExecutionContext context)
	{
		Axiom axiom = new Axiom(template.getKey()); 
		template.getProperties().initialize(axiom, (TermList<Operand>)template);
		if ((axiom != null) && (axiom.getTermCount() > 0))
		    execute(axiom, template, solution, context);
		else
		    execute(template, solution, context);
		return true;
	}
	
	/**
	 * Find a solution for specified template and initializer axiom.
	 * @param axiom Initializer axiom
	 * @param solution Resolution of current query managed by LogicQueryExecuter up to this point  
	 * @param template Template used on each iteration
	 * @param context Execution context
     * @return Flag to indicate whether or not the query is resolved
	 */
	public boolean iterate(Axiom axiom, Solution solution, Template template, ExecutionContext context)
	{
		return execute(axiom, template, solution, context);
	}
	
	/**
	 * Set axiom listener to receive each solution as it is produced
	 * @param axiomListener The axiom listener object
	 */
	@Override
	public void setAxiomListener(LocaleAxiomListener axiomListener) 
	{
		this.axiomListener = axiomListener;
	}

	/**
	 * Execute calculation using specified solution and template.
	 * An axiom to seed the calculation is optional, but it will be set if this is called from iterate().
	 * @param template Template used on each iteration
	 * @param solution Container to aggregate results  
	 * @param context Execution context
	 */
	public void execute(Template template, Solution solution, ExecutionContext context)
	{
		execute(null, template, solution, context);
	}
	
	/**
	 * Execute calculation using specified solution and template.
	 * An axiom to seed the calculation is optional, but it will be set if this is called from iterate().
	 * @param seedAxiom Seed axiom - may be null
	 * @param template Template used on each iteration
	 * @param solution Container to aggregate results 
	 * @param context Execution context 
     * @return Flag to indicate whether or not the query is resolved
	 */
	public boolean execute(Axiom seedAxiom, Template template, Solution solution, ExecutionContext context)
	{
		if (seedAxiom != null) 
		{
			if (!template.getKey().endsWith(seedAxiom.getName()))
				throw new QueryExecutionException("Axiom key \"" + seedAxiom.getName() + "\" does not match Template key \"" + template.getKey() + "\"");
			axiom = seedAxiom;
		}
		else
		    axiom = solution.getAxiom(template.getKey());
		boolean unificationSuccess = true;
		boolean caseInsensitive = context != null && context.isCaseInsensitiveNameMatch();
		if ((axiom != null) && (axiom.getTermCount() > 0))
		{
			unificationSuccess = caseInsensitive ? template.unifyCaseInsensitive(axiom, solution) : template.unify(axiom, solution);
			if (unificationSuccess)
			{   // Unify enclosed templates which will participate in ensuing evaluation
				Template chainTemplate = template.getNext();
				while (chainTemplate != null)
				{
					if (caseInsensitive)
						chainTemplate.unifyCaseInsensitive(axiom , solution);
					else
						chainTemplate.unify(axiom , solution);
					chainTemplate = chainTemplate.getNext();
				}
			}
		}
		else 
            unifySolution(solution, template);
		// Short circuit when solution not available
		return unificationSuccess && completeSolution(solution, template, context);
	}

    /**
	 * Complete finding solution following successful unification
	 * @param solution Container to aggregate results  
	 * @param template Template used on each iteration
	 * @return Flag to indicate if the query is resolved
	 */
	protected boolean completeSolution(Solution solution, Template template, ExecutionContext context)
	{
		EvaluationStatus evaluationStatus = EvaluationStatus.SHORT_CIRCUIT;
		try
		{
			// evaluate() may result in a short circuit exit flagged by returning false
			// isfact() flags true if each term of the template is non-empty
			evaluationStatus = template.evaluate(context);
			if (evaluationStatus == EvaluationStatus.COMPLETE)
			{
				axiom = template.toAxiom();
				solution.put(template.getQualifiedName().toString(), axiom);
				return true;
			}
			else
			    return evaluationStatus != EvaluationStatus.SKIP;
		}
		catch (ExpressionException e)
		{   // evaluate() exceptions are thrown by Evaluator objects 
			throw new QueryExecutionException("Error evaluating: " + template.toString(), e);
		}
	}
	
	/**
	 * Unify template with solution.
	 * @param solution Container to aggregate results  
	 * @param template Structure to pair with axiom sequence
	 */
	protected void unifySolution(Solution solution, Template template)
    {
		if (solution.size() > 0)
		{
			Template chainTemplate = template;
			while (chainTemplate != null)
			{
				OperandWalker walker = chainTemplate.getOperandWalker();
				walker.setAllNodes(true);
	            final SolutionPairer pairer = template.getSolutionPairer(solution);
				walker.visitAllNodes(pairer);
				chainTemplate = chainTemplate.getNext();
			}
		}
    }


}
