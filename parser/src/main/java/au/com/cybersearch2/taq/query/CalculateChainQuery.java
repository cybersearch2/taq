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
import java.util.Deque;
import java.util.List;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * CalculateChainQuery
 * Chain query to perform calculation
 * @author Andrew Bowley
 * 12 Jan 2015
 */
public class CalculateChainQuery extends ChainQuery 
{

	/** Optional axiom to initialize Calculator */
	protected Axiom axiom;
	/**Template to unify and evaluate */
	protected Template template;
	/** Optional axiom listener to receive each solution as it is produced */
    protected List<LocaleAxiomListener> axiomListenerList;

	/**
	 * Create a CalculateChainQuery object
	 * @param axiom Optional axiom to initialize Calculator
	 * @param template Template to unify and evaluate
	 * @param scopeNotifier Scope notifier
	 */
	public CalculateChainQuery(Axiom axiom, Template template, ScopeNotifier scopeNotifier) 
	{
	    super(scopeNotifier);
		this.axiom = axiom;
		this.template = template;
	}

    /**
 	 * Execute query and if not tail, chain to next.
 	 * Sub classes override this method and call it upon completion to handle the chaining
 	 * @param solution The object which stores the query results
 	 * @param templateChain Template chain to manage same query repeated in different scopes
	 * @return EvaluationStatus enum: SHORT_CIRCUIT, SKIP or COMPLETE
	 */
	@Override
	public EvaluationStatus executeQuery(Solution solution, Deque<Template> templateChain, ExecutionContext context)
	{
	    // Set properties attached to query
	    if (!properties.isEmpty())
	        template.getProperties().setInitData(properties);
	    solution.remove(template.getQualifiedName().toString());
		Calculator calculator = new Calculator();
		if (axiomListenerList != null)
			for (LocaleAxiomListener axiomListener: axiomListenerList)
			    calculator.setAxiomListener(axiomListener);
        if (next != null)
        	pushTemplateChain(templateChain, template);
        else
            templateChain.clear();
        if (!template.isBackedUped())
        	template.backup(false);
		if (axiom == null)
			calculator.iterate(solution, template, context);
		else 
		{
		    Axiom seedAxiom = axiom;
		    if (axiom.getTermCount() == 0)
    		{
    		    // Placeholder axiom to be populated from solution
    		    seedAxiom = solution.getAxiom(axiom.getName());
    		    if (seedAxiom == null)
    		        throw new QueryExecutionException("Calculator \"" + template.getName() + "\" cannot find axiom \"" + axiom.getName() + "\"");
    		}
    		calculator.iterate(seedAxiom, solution, template, context);
		}
		return super.executeQuery(solution, templateChain, context);
 	}

 	/**
	 * Backup to state before previous unification
	 */
	@Override
	protected void backupToStart() 
	{
		template.backup(false);
		Template next = template.getNext();
		while(next != null) {
			next.backup(false);
			next = next.getNext();
		}
	}

    @Override
    protected void backup()
    {
        template.backup(false);
    } 

	/**
	 * Force reset to initial state
	 */
	@Override
	protected void reset() 
	{
		template.reset();
	}

	/**
	 * Set axiom listener to receive each solution as it is produced
	 * @param qname Reference to axiom by qualified name
	 * @param axiomListener The axiom listener object
	 */
	@Override
	void setAxiomListener(QualifiedName qname, LocaleAxiomListener axiomListener) 
	{
		if (axiomListenerList == null)
			axiomListenerList = new ArrayList<>();
		axiomListenerList.add(axiomListener);
	}

}
