/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.scope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.AxiomAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.debug.ExecutionConsole;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.language.InitialProperties;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * Creates scopes and provides methods to access them collectively
 */
public class ScopeManager implements ExecutionConsole {

	/** Global scope created during ScopeManager construction */
	private final Scope globalScope;
	/** All scopes */
	private final List<Scope> scopes;
	/** Global Execution Context plus any branched contexts */
	private final Map<String,ExecutionContext> contextMap;

	/**
	 * Construct ScopeManager object
	 */
	public ScopeManager() {
		// Scopes in order of creation
		scopes = new ArrayList<Scope>();
		// Create global scope
		globalScope = new Scope(this, QueryProgram.GLOBAL_SCOPE, Scope.EMPTY_PROPERTIES);
		scopes.add(globalScope);
		contextMap = new HashMap<>();
		contextMap.put(QueryProgram.GLOBAL_SCOPE, new ExecutionContext());
	}

	/**
	 * Returns global scope
	 * @return Scope object
	 */
	public Scope getGlobalScope() {
		return globalScope;
	}

    /**
     * Performs the given action for each element of the {@code Scope}
     * until all elements have been processed or the action throws an
     * exception.  Actions are performed in the order of iteration, if that
     * order is specified.  Exceptions thrown by the action are relayed to the
     * caller.
     * <p>
     * The behavior of this method is unspecified if the action performs
     * side-effects that modify the underlying source of elements, unless an
     * overriding class has specified a concurrent modification policy.
     *
     * @implSpec
     * <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @since 1.8
     */
    public void forEach(Consumer<Scope> action) {
    	scopes.forEach(action);
    }

    /**
     * Returns set of scope names
     * @return String set
     */
    public Set<String> getScopeNames()
    {
    	Set<String> scopeNames = new HashSet<>();
    	scopes.forEach(scope -> scopeNames.add(scope.getName()));
        return scopeNames;
    }
    
    /**
     * Returns scope specified by name.
     * @param name Name
     * @return Scope object
     * @throws IllegalArgumentException if scope does not exist
     */
    public Scope getScope(String name)
    {
    	if (name.isEmpty() || name.equals(QueryProgram.GLOBAL_SCOPE))
    		return globalScope;
        Scope scope = findScope(name);
        if (scope == null)
            throw new IllegalArgumentException("Scope \"" + name + "\" does not exist");
        return scope;
    }
    
	/**
     * Returns scope specified by name.
     * @param name Name
     * @return Scope object or null if not foune
     */
    public Scope findScope(String name)
    {
        return name.isEmpty() ? globalScope : getScopeByName(name);
    }

    public Locale getQualifiedLocale(QualifiedName qname) {
    	return getScope(qname.getScope()).getLocale();
    }
    
    /**
     * Process all query result callbacks
     * @throws ExpressionException
     */
    public void processResults() {
		List<Callable<Void>> callbacksList = new ArrayList<>();
		forEach(item -> {
			if (!item.getQueryResultCallbacks().isEmpty())
				callbacksList.addAll(item.getQueryResultCallbacks());
		});
		if (!callbacksList.isEmpty())
			callbacksList.forEach(callback -> {
				try {
					callback.call();
				} catch (Exception e) {
					throw new ExpressionException("Query result error", e);
				}
			});
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
		Map<String, Object> properties = scopeProperties != null ? scopeProperties.getProperties() : null;
		if (scopeName.equals(QueryProgram.GLOBAL_SCOPE)) { 
			// Global properties are updatable
			if ((properties != null) && !properties.isEmpty())
				globalScope.updateProperties(properties);
			return globalScope;
		} else { 
			// Ensure global scope axiom exists
			QualifiedName qname = new QualifiedName(QueryProgram.GLOBAL_SCOPE, Scope.SCOPE);
			Axiom scopeAxiom = globalScope.getParserAssembler().getListAssembler().getAxiom(qname);
			if (scopeAxiom == null)
				addScopeList(globalScope);
		}
		if (getScopeByName(scopeName) != null)
			throw new ExpressionException("Scope named \"" + scopeName + "\" already exists");
		Scope newScope = new Scope(this, scopeName, properties);
		scopes.add(newScope);
		return newScope;
	}
	
    public Scope getScopeByName(String scopeName) {
		for (Scope scope: scopes)
			if (scope.getName().equals(scopeName))
				return scope;
		return null;
	}

	/**
     * Add empty "scope" builtin term list to access scope properties
     * @param scope Scope to target
     */
    public void addScopeList(Scope scope) {
    	addScopeList(scope, Scope.EMPTY_PROPERTIES);
    }

	/**
     * Add "scope" builtin term list to access scope properties
     * @param scope Scope to target
     * @param properties Scope properties
     */
    public void addScopeList(Scope scope, Map<String, Object> properties)
    {   // Add axiom to ParserAssembler
        QualifiedName qname = new QualifiedName(scope.getName(), Scope.SCOPE);
        ParserAssembler parserAssembler = scope.getParserAssembler();
        Axiom scopeAxiom = parserAssembler.getListAssembler().getAxiom(qname);
        AxiomTermList localList = null;
        Axiom localAxiom = null;
        if (scopeAxiom == null)
        {
            parserAssembler.getListAssembler().createAxiom(qname, false, false);
            AxiomAssembler axiomAssembler = parserAssembler.getAxiomAssembler();
            if (properties.isEmpty())
            	axiomAssembler.createAxiom(qname);
            else
                for (String termName: properties.keySet())
                    axiomAssembler.addAxiom(qname, new Parameter(termName, properties.get(termName)));
            localAxiom = axiomAssembler.saveAxiom(qname);
            localList = new AxiomTermList(qname, qname);
            localList.setAxiom(localAxiom);
        }
        else
        {
            localAxiom = scopeAxiom;
            for (String termName: properties.keySet())
            {
                Term term = localAxiom.getTermByName(termName);
                if (term != null)
                    term.setValue(properties.get(termName));
                else
                    localAxiom.addTerm(new Parameter(termName, properties.get(termName)));
            }
            localList = parserAssembler.getListAssembler().getAxiomTermList(qname);
            localList.getAxiomListener().onNextAxiom(qname, localAxiom, scope.getLocale());
            return;
        }
        parserAssembler.registerScopeProperties(localList);
        parserAssembler.getListAssembler().addItemList(qname, localList);
    }
 
    public ExecutionContext getExecutionContext(String name) {
    	ExecutionContext context = contextMap.get(name);
    	if (context == null) {
    		// Ensure name is valid
    		getScope(name);
    		context = contextMap.get(QueryProgram.GLOBAL_SCOPE);
    	}
    	return context;
    }
    
	@Override
	public void setCaseInsensitiveNames(boolean flag) {
		contextMap.forEach((key,value) -> value.setCaseInsensitiveNameMatch(flag));
	}

	@Override
	public void setCaseInsensitiveNames(String scopeName, boolean flag) {
		ensureScopeExeContext(scopeName);
		contextMap.get(scopeName).setCaseInsensitiveNameMatch(flag);
	}

	@Override
	public void setLoopTimeout(int timeout) {
		contextMap.forEach((key,value) -> value.setLoopTimeout(timeout));
	}

	@Override
	public void setLoopThreshold(int threshold) {
		contextMap.forEach((key,value) -> value.setLoopThreshold(threshold));
	}

	@Override
	public void setLoopTimeout(String scopeName, int timeout) {
		ensureScopeExeContext(scopeName);
		contextMap.get(scopeName).setLoopTimeout(timeout);
	}

	@Override
	public void setLoopThreshold(String scopeName, int threshold) {
		ensureScopeExeContext(scopeName);
		contextMap.get(scopeName).setLoopThreshold(threshold);
	}

	private void ensureScopeExeContext(String scopeName) {
		ExecutionContext glbobalCpntext = contextMap.get(QueryProgram.GLOBAL_SCOPE);
		contextMap.computeIfAbsent(scopeName, k -> { return new ExecutionContext(glbobalCpntext); } );
	}
}
