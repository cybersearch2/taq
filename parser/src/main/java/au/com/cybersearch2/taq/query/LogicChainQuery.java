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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.AxiomCollection;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TermList;

/**
 * LogicChainQuery
 * Chain query to perform logic operation
 * @author Andrew Bowley
 * 12 Jan 2015
 */
public class LogicChainQuery extends ChainQuery
{
	/** Template to unify and evaluate */
	protected Template template;
	/** A set of AxiomSource objects referenced by name */
 	protected AxiomCollection axiomCollection;
	/** Optional axiom listener to receive each solution as it is produced */
	protected Map<QualifiedName, List<LocaleAxiomListener>> axiomListenerMap;

	/**
	 * Create LogicChainQuery object
	 * @param axiomCollection A set of AxiomSource objects referenced by name
	 * @param template Ttemplate to unify and evaluate
	 * @param scopeNotifier Scope notifier
	 */
 	public LogicChainQuery(AxiomCollection axiomCollection,	 Template template, ScopeNotifier scopeNotifier) 
    {
 	    super(scopeNotifier);
		this.template = template;
		this.axiomCollection = axiomCollection;
	}

    /**
     * Returns axiom source referenced by name
     * @param name
     * @return AxiomSource object
     */
    AxiomSource getAxiomSource(String name)
    {
        return axiomCollection.getAxiomSource(name);
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
		if (!template.isBackedUped())
			template.backup(false);
		String key = template.getKey();
		AxiomSource axiomSource = axiomCollection.getAxiomSource(key);
		LogicQuery query = (axiomSource == null) ? new LogicQuery() : new LogicQuery(axiomSource);
		if ((axiomListenerMap != null) && axiomListenerMap.containsKey(new QualifiedName(template.getKey())))
			for (LocaleAxiomListener axiomListener: axiomListenerMap.get(new QualifiedName(template.getKey())))
				query.setAxiomListener(axiomListener);
		if (!query.iterate(solution, template, context))
			return EvaluationStatus.SHORT_CIRCUIT;
		return super.executeQuery(solution, templateChain, context);
 	}

	/**
	 * Returns query as String. Shows empty terms with "?",
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return LogicQueryExecuter.toString(((List<? extends TermList<?>>)Collections.singletonList(template)));
	}

    /**
     * Backup to state before previous unification
     */
    @Override
    protected void backup() 
    {
        template.backup(true);
    }

 	/**
	 * Backup to state before previous unification
	 */
	@Override
	protected void backupToStart() 
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
		List<LocaleAxiomListener> axiomListenerList = null;
		if (axiomListenerMap == null)
			axiomListenerMap = new HashMap<>();
		else
			axiomListenerList = axiomListenerMap.get(qname);
		if (axiomListenerList == null)
		{
			axiomListenerList = new ArrayList<>();
			axiomListenerMap.put(qname, axiomListenerList);
		}
		axiomListenerList.add(axiomListener);
	}
	
}
