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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomCollection;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * ChainQueryExecuter Executes a chain query and returns a solution
 * 
 * @author Andrew Bowley 23 Jan 2015
 */
public class ChainQueryExecuter {
	/** Logger */
	private final static Logger logger = LogManager.getLogger(ChainQueryExecuter.class);
	
	/**
	 * Template chain passed down chain to manage case of more than one query in
	 * chain eg. repeating same query in different scopes
	 */
	protected final Deque<Template> templateChain;
	/** Query scope */
	protected final Scope queryScope;

	/** Set of axiom listeners referenced by name */
	protected Map<QualifiedName, List<LocaleAxiomListener>> axiomListenerMap;
	/** The solution is a collection of axioms referenced by name */
	protected Solution solution;
	/** Head of optional query chain */
	protected ChainQuery headChainQuery;
	/** Tail of optional query chain */
	private ChainQuery tailChainQuery;

	/**
	 * Construct ChainQueryExecuter object
	 * 
	 * @param queryParams Query parameters
	 */
	public ChainQueryExecuter(QueryParams queryParams) {
		templateChain = new ArrayDeque<>();
		this.queryScope = queryParams.getScope();
		if (queryScope != null) {
			Map<QualifiedName, List<LocaleAxiomListener>> queryScopeMap = queryScope.getAxiomListenerMap();
			if (queryScopeMap != null) { // Create a copy of the axiom listener map and remove entries as axiom
											// listeners are bound to processors
				List<LocaleAxiomListener> axiomListeners = new ArrayList<>();
				Set<String> queryScopes = queryParams.getQueryScopes();
				queryScopeMap.forEach((key, value) -> {
					String scopeName = key.getScope();
					if (scopeName.isEmpty())
						scopeName = QueryProgram.GLOBAL_SCOPE;
					for (LocaleAxiomListener listener : value) {
						if (queryScopes.contains(scopeName))
							axiomListeners.add(listener);
					}
					;
					if (!axiomListeners.isEmpty()) {
						if (axiomListenerMap == null)
							axiomListenerMap = new HashMap<>();
						List<LocaleAxiomListener> copy = new ArrayList<>();
						copy.addAll(axiomListeners);
						axiomListenerMap.put(key, copy);
						axiomListeners.clear();
					}
				});
			}
			queryParams.getTemplateScopes().forEach(scope -> {
				if (scope.getAxiomListenerMap() != null) {
					if (axiomListenerMap == null)
						axiomListenerMap = new HashMap<>();
					axiomListenerMap.putAll(scope.getAxiomListenerMap());
				}
			});
		}
	}

	public void addAxiomListeners(Map<QualifiedName, List<LocaleAxiomListener>> scopeAxiomListenerMap) {
		if (scopeAxiomListenerMap != null) {
			if (axiomListenerMap == null)
				axiomListenerMap = new HashMap<>();
			axiomListenerMap.putAll(scopeAxiomListenerMap);
		}
	}

	/**
	 * Find next solution. Call getSolution() to obtain an unmodifiable version.
	 * 
	 * @return Flag to indicate solution found
	 */
	public boolean execute() {
		if (axiomListenerMap != null)
			bindAxiomListeners(queryScope);
		if (headChainQuery != null) {
			ExecutionContext context;
			ScopeNotifier scopeNotifier = headChainQuery.getScopeNotifier();
			if (scopeNotifier != null) {
				scopeNotifier.notifyScopes();
				context = scopeNotifier.getScope().getExecutionContext();
			} else
				context = queryScope.getExecutionContext();
			// Query chain will add to solution or trigger short circuit
			return (headChainQuery.executeQuery(solution, templateChain, context) == EvaluationStatus.COMPLETE);
		}
		return true;
	}

	/**
	 * Add chain query
	 * 
	 * @param axiomCollection A collection of axiom sources which are referenced by
	 *                        name
	 * @param template        Template
	 * @param scopeNotifier   Scope notifier
	 */
	public void chain(AxiomCollection axiomCollection, Template template, ScopeNotifier scopeNotifier) {
		LogicChainQuery chainQuery = new LogicChainQuery(axiomCollection, template, scopeNotifier);
		if (axiomListenerMap != null)
			// Bind each axiom listener to a query or the solution depending
			// on the referenced axiom being available in the axiom collection
			bindAxiomListener(template, chainQuery);
		addChainQuery(chainQuery);
	}

	/**
	 * Add a calculate chain query
	 * 
	 * @param axiom         Optional axiom to initialize Calculator
	 * @param template      Template to unify and evaluate
	 * @param scopeNotifier Object to notify of scope change, null if not required
	 * @return CalculateChainQuery object to execute calculator
	 */
	public CalculateChainQuery chainCalculator(Axiom axiom, Template template, ScopeNotifier scopeNotifier) {
		if (axiom == null) {
			if (template.getKey() == null)
				template.setKey("");
		} else
			template.setKey(axiom.getName());
		CalculateChainQuery chainQuery = new CalculateChainQuery(axiom, template, scopeNotifier);
		if (axiomListenerMap != null)
			bindCalculatorAxiomListener(template);
		addChainQuery(chainQuery);
		return chainQuery;
	}

	/**
	 * Returns query as String. Shows empty terms with "?",
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Iterator<ChainQuery> iterator = chainQueryIterator();
		StringBuilder builder = new StringBuilder();
		boolean firstTime = true;
		while (iterator.hasNext()) {
			if (firstTime)
				firstTime = false;
			else
				builder.append(", ");
			builder.append(iterator.next().toString().replaceAll("\\<empty\\>", "?"))
					.append(System.getProperty("line.separator"));
		}
		return builder.toString();
	}

	/**
	 * Force reset to initial state
	 */
	protected void reset() {
		if (headChainQuery != null) {
			ChainQuery chainQuery = headChainQuery;
			do {
				chainQuery.reset();
				chainQuery = chainQuery.getNext();
			} while (chainQuery != null);
		}
	}

	/**
	 * Returns iterator to walk the query chain
	 * 
	 * @return Iterator of generic type ChainQuery
	 */
	protected Iterator<ChainQuery> chainQueryIterator() {
		List<ChainQuery> chainQueryList = new ArrayList<ChainQuery>();
		ChainQuery query = headChainQuery;
		while (query != null) {
			chainQueryList.add(query);
			query = query.getNext();
		}
		return chainQueryList.iterator();
	}

	/**
	 * Backup to start state
	 */
	protected void backup(boolean isCalculation) {
		if (headChainQuery != null) {
			// Reset all query templates so they can be recycled
			if (isCalculation)
				headChainQuery.backupToStart();
			else
				headChainQuery.reset();
			ChainQuery chainQuery = headChainQuery.getNext();
			while (chainQuery != null) {
				if (chainQuery instanceof CalculateChainQuery)
					chainQuery.backupToStart();
				else
					chainQuery.reset();
				chainQuery = chainQuery.getNext();
			}
		}
	}

	/**
	 * Set initial solution
	 * 
	 * @param solution Solution object - usually empty but can contain initial
	 *                 axioms
	 */
	protected void setSolution(Solution solution) {
		this.solution = solution;
	}

	/**
	 * Returns solution
	 * 
	 * @return Collection of axioms referenced by name. The axioms reference the
	 *         templates supplied to the query.
	 */
	protected Solution getSolution() {
		return solution;
	}

	/**
	 * Add chain query
	 * 
	 * @param chainQuery ChainQuery
	 */
	protected void addChainQuery(ChainQuery chainQuery) {
		if (headChainQuery == null) {
			headChainQuery = chainQuery;
			tailChainQuery = chainQuery;
		} else {
			tailChainQuery.setNext(chainQuery);
			tailChainQuery = chainQuery;
		}
	}

	/**
	 * Bind axiom listeners to processors. This is a late binding step for any
	 * outstanding outbound list variables.
	 */
	protected void bindAxiomListeners(Scope localScope) {
		if (axiomListenerMap == null)
			return;
		Set<QualifiedName> keys = axiomListenerMap.keySet();
		for (QualifiedName key : keys) {
			if (!key.getTemplate().isEmpty())
				continue; // Templates are output
			AxiomSource axiomSource = localScope.findAxiomSource(key);
			if ((axiomSource == null) && !localScope.getName().equals(QueryProgram.GLOBAL_SCOPE))
				axiomSource = queryScope.getGlobalScope().findAxiomSource(key);
			if (axiomSource != null)
				bindAxiomSource(axiomSource, key);
			else
				logger.warn(String.format("Axiom source %s not found", key.toString()));
		}
		// Delete the axiom listener map as binding is complete
		axiomListenerMap = null;
	}

	/**
	 * Bind axiom source to listener collection identified by name
	 * 
	 * @param axiomSource The axiom source
	 * @param key         Listener collection identity
	 */
	protected void bindAxiomSource(AxiomSource axiomSource, QualifiedName key) {
		List<LocaleAxiomListener> axiomListenerList = axiomListenerMap.get(key);
		for (LocaleAxiomListener axiomListener : axiomListenerList) {
			Iterator<Axiom> iterator = axiomSource.iterator(null);
			if (!iterator.hasNext())
				break;
			Axiom axiom = iterator.next();
			if (axiom != null)
				axiomListener.onNextAxiom(key, axiom, solution.getGlobalLocale());
		}
	}

	/**
	 * Bind axiom listeners to solution for calculator
	 * 
	 * @param template Calculator
	 */
	protected void bindCalculatorAxiomListener(ITemplate template) {
		QualifiedName qname = template.getQualifiedName();
		List<LocaleAxiomListener> axiomListenerList = null;
		if (axiomListenerMap.containsKey(qname))
			axiomListenerList = axiomListenerMap.get(qname);
		else if (template.isReplicate()) {
			qname = new QualifiedTemplateName(QueryProgram.GLOBAL_SCOPE, qname.getTemplate());
			if (axiomListenerMap.containsKey(qname))
				axiomListenerList = axiomListenerMap.get(qname);
		}
		if (axiomListenerList != null) {
			for (LocaleAxiomListener axiomListener : axiomListenerList)
				solution.setAxiomListener(qname, axiomListener);
			axiomListenerMap.remove(qname);
		}
	}

	/**
	 * Bind each axiom listener to a query or the solution depending on the
	 * referenced axiom being available in the axiom collection belonging to the
	 * chain query
	 * 
	 * @param template   Chain template
	 * @param chainQuery Chain query
	 */
	protected void bindAxiomListener(ITemplate template, LogicChainQuery chainQuery) {
		String key = template.getKey();
		QualifiedName qname = QualifiedName.parseGlobalName(key);
		if (axiomListenerMap.containsKey(qname)) {
			AxiomSource axiomSource = chainQuery.getAxiomSource(key);
			List<LocaleAxiomListener> axiomListenerList = axiomListenerMap.get(qname);
			if (axiomSource != null) {
				for (LocaleAxiomListener axiomListener : axiomListenerList)
					chainQuery.setAxiomListener(qname, axiomListener);
				axiomListenerMap.remove(qname);
			}
		} else {
			qname = template.getQualifiedName();
			if (axiomListenerMap.containsKey(qname)) {
				List<LocaleAxiomListener> axiomListenerList = axiomListenerMap.get(qname);
				for (LocaleAxiomListener axiomListener : axiomListenerList)
					solution.setAxiomListener(qname, axiomListener);
				axiomListenerMap.remove(qname);
			}
		}
	}

	/**
	 * Copy all axiom listeners in scope to this executer
	 * 
	 * @param scope Scope object
	 */
	protected void setAxiomListeners(Scope scope) {
		if (scope.getAxiomListenerMap() != null) { 
			// Create a copy of the axiom listener map and remove entries as
			// axiom listeners are bound to processors
			if (axiomListenerMap == null)
				axiomListenerMap = new HashMap<>();
			axiomListenerMap.putAll(scope.getAxiomListenerMap());
		}
	}

	/**
	 * Notify all locale listeners that the scope has changed
	 * 
	 * @param templateScope Notification scope
	 */
	protected void onScopeChange(Scope templateScope) {
		queryScope.getParserAssembler().onScopeChange(templateScope);
	}

}
