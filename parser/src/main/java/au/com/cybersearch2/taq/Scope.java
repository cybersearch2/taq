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
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import au.com.cybersearch2.taq.axiom.ScopePropertiesListener;
import au.com.cybersearch2.taq.compile.AxiomAssembler;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserTask;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.NameParser;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.QueryType;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.provider.ScopeFunctions;
import au.com.cybersearch2.taq.result.ResultList;
import au.com.cybersearch2.taq.scope.ScopeContext;
import au.com.cybersearch2.taq.scope.ScopeManager;

/**
 * Scope
 * A namespace for axioms, templates and queries
 * @author Andrew Bowley
 * 28 Dec 2014
 */
public class Scope
{
    /** scope literal */
    static final public String SCOPE = "scope";
    /** Region locale key literal */
    static final protected String REGION_KEY = "region";
    /** Language locale key literal */
    static final protected String LANGUAGE_KEY = "language";

    static final public Map<String, Object> EMPTY_PROPERTIES;
    static final private List<Callable<Void>> EMPTY_CALLABLES;
    
    static private ScopeFunctions scopeFunctions;

    static
    {
        EMPTY_PROPERTIES = Collections.emptyMap();
        EMPTY_CALLABLES = Collections.emptyList();
    }

    /** Scopes container */
    private final ScopeManager scopeManager;
    /** Scope name - must be unique to all scopes */
    private final String name;
    /** Map QuerySpec objects to query name */
    private Map<String, QuerySpec> querySpecMap;
    /** Local pattern assembler */
    private ParserAssembler parserAssembler;
    /** A scope locale can be different to the system default */
    private Locale locale;
    /** Callbacks to return query results */
    private List<Callable<Void>> queryResultCallbacks;
    /** Name of current outer template - provided to allow ad hoc inner template creation */
    private QualifiedName outerTemplate;
    
    
    /**
     * Construct a Scope object
     * @param scopeManager Creates scopes and provides methods to access them collectively
     * @param name Scope name - must be unique to all scopes
     * @param properties Scope properties - may be empty
     */
    public Scope(ScopeManager scopeManager, String name, Map<String, Object> properties) 
    {
        this.scopeManager = scopeManager;
        this.name = name;
        if (!setLocale(properties))
            // SE7 supported
            locale = Locale.getDefault(Locale.Category.FORMAT);
        querySpecMap = new HashMap<String, QuerySpec>();
        parserAssembler = new ParserAssembler(this);
        if (properties == null)
            properties = EMPTY_PROPERTIES;
        if (!properties.isEmpty())
        	scopeManager.addScopeList(this, properties);
        queryResultCallbacks = EMPTY_CALLABLES;
        if (name.equals(QueryProgram.GLOBAL_SCOPE)) {
        	scopeFunctions = new ScopeFunctions(this); 
    		parserAssembler.registerLocaleListener(scopeFunctions);
        }
        outerTemplate = new QualifiedTemplateName(name, SCOPE);
    }
 
    /**
     * Returns set of scope names
     * @return String set
     */
    public Set<String> getScopeNames()
    {
        return scopeManager.getScopeNames();
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
     */
    public void forEach(Consumer<Scope> action) {
    	scopeManager.forEach(action);
    }

   /**
     * Returns locale of scope
     * @return Locale object
     */
    public Locale getLocale() 
    {
        return locale;
    }

    /**
     * Set locale of this scope
     * @param locale Locale object
     */
    public void setLocale(Locale locale) 
    {
        this.locale = locale;
    }

    /** Returns top template in current stack while compiling TAQ source */
    public QualifiedName getOuterTemplate() {
		return outerTemplate;
	}

    /**
     * Sets top template in current stack while compiling TAQ source
     * @param outerTemplate
     */
	public void setOuterTemplate(QualifiedName outerTemplate) {
		this.outerTemplate = outerTemplate;
	}

	/**
	 * Returns qualified names of all exported lists
	 * @return QualifiedName list
	 */
	public List<QualifiedName> getExportListNames() {
		ListAssembler listAssembler = parserAssembler.getListAssembler();
		return listAssembler.getExportListNames();
	}

	/**
	 * Returns execution context of this scope. 
	 * It may be the context of the global scope if one has not been branched for this scope.
	 * @return ExecutionContext object
	 */
    public ExecutionContext getExecutionContext() {
    	return scopeManager.getExecutionContext(name);
    }

	/**
     * Update scope properties - only applicable to global scope
     * @param properties Key=value pairs
     */
    public void updateProperties(Map<String, Object> properties)
    {
        setLocale(properties);
        if (!properties.containsKey(LANGUAGE_KEY))
            properties.put("language", locale.getLanguage());
        if (!properties.containsKey(REGION_KEY))
            properties.put("region", locale.getCountry());
    	scopeManager.addScopeList(this, properties);
    }

    /**
     * Complete construction of a query specification according to type of query and position in chain. 
     * If the need is detected, a new head query specification will be returned. This is used when a 
     * calculator is found as the head query and a logic query needs to be inserted before it to feed
     * the calculator axioms one by one.
     * Intended only for use by compiler. 
     * @param querySpec Query specification under construction
     * @param firstKeyname Keyname object at head of query chain
     * @param keynameCount Number of keynames in chain so far
     * @param termList Query parameters. Optional, so may be empty
     * @return QuerySpec The query specification object passed as a parameter or a new head query specifiection object  
     */
    public QuerySpec buildQuerySpec(QuerySpec querySpec, KeyName firstKeyname, int keynameCount, List<Term> termList)
    {
       QualifiedName templateName = firstKeyname.getTemplateName();
       ITemplate firstTemplate = findTemplate(templateName);
       if (firstTemplate.isCalculator())
       {
           // Now deal with the specifics of a calculator query
           // Query type
           querySpec.setQueryType(QueryType.calculator);
       }
       // Query parameters specified as properties
       if (termList.size() > 0)
          querySpec.putProperties(firstKeyname, termList);
       return querySpec;
    }
    
    /**
     * Add specification of query Axiom(s) and Template(s) names
     * @param querySpec QuerySpec object
     */
    public void addQuerySpec(QuerySpec querySpec)
    {
        querySpecMap.put(querySpec.getName(), querySpec);
    }

    public void addQueryResultCallback(Callable<Void> callback) {
    	if (queryResultCallbacks.isEmpty())
    		queryResultCallbacks = new ArrayList<>();
    	queryResultCallbacks.add(callback);
    }
    
    /**
     * Returns scope name
     * @return String
     */
    public String getName() 
    {
        return name;
    }

    /**
     * Returns name of scope or alias if global scope
     * @return Alias
     */
    public String getAlias()
    {
        return name.equals(QueryProgram.GLOBAL_SCOPE) ? QualifiedName.EMPTY : name;
    }
    
    /**
     * Returns scope specified by name.
     * @param name Name
     * @return Scope object
     * @throws IllegalArgumentException if scope does not exist
     */
    public Scope getScope(String name)
    {
        return scopeManager.getScope(name);
    }

    public List<Callable<Void>> getQueryResultCallbacks() {
		return queryResultCallbacks;
	}

	/**
     * Returns scope specified by name.
     * @param name Name
     * @return Scope object or null if not foune
     */
    public Scope findScope(String name)
    {
        return scopeManager.findScope(name);
    }

	/**
     * Returns local parser assembler
     * @return ParserAssembler object
     */
    public ParserAssembler getParserAssembler() 
    {
        return parserAssembler;
    }

    /**
     * Returns Query specification referenced by name
     * @param querySpecName Query spec. name
     * @return QuerySpec object
     */
    public QuerySpec getQuerySpec(String querySpecName) 
    {
        return querySpecMap.get(querySpecName);
    }

    /**
     * Returns query specification map
     * @return Map with name keys and QuerySpec values
     */
    public Map<String, QuerySpec> getQuerySpecMap()
    {
        return Collections.unmodifiableMap(querySpecMap);
    }

    /**
     * Returns axiom source for specified axiom name
     * @param axiomKey Axiom qualified name
     * @return AxiomSource object
     */
    public AxiomSource getAxiomSource(QualifiedName axiomKey)
    {
        AxiomSource axiomSource = findAxiomSource(axiomKey);
        if (axiomSource == null)
            throw new IllegalArgumentException("Axiom \"" + axiomKey.toString() + "\" does not exist");
        return axiomSource;
    }

    /**
     * Returns axiom source for specified axiom name
     * @param axiomKey Axiom qualified name
     * @return AxiomSource object or null if it does not exist in scope
     */
    public AxiomSource findAxiomSource(QualifiedName axiomKey)
    {
        String scopeName = axiomKey.getScope();
        boolean isTemplateName = false;
        if (scopeName.isEmpty())
            scopeName = QueryProgram.GLOBAL_SCOPE;
        else
            isTemplateName = isTemplateName(scopeName);
        Scope axiomScope = getScopeByName(scopeName);
        if ((axiomScope == null) && !isTemplateName)
            throw new ExpressionException("Scope \"" + scopeName + "\"  in axiom key \"" + axiomKey.toString() + "\" is not found");
        AxiomSource axiomSource = null;
        if (axiomScope != null)
            axiomSource = axiomScope.getParserAssembler().getAxiomSource(axiomKey);
        if ((axiomSource == null) && (!scopeName.equals(name) || isTemplateName))
            axiomSource = parserAssembler.getAxiomSource(axiomKey);
        if (axiomSource != null)
            return axiomSource;
        QualifiedName qname = QualifiedName.parseName(axiomKey.getName(), parserAssembler.getQualifiedContextname());
        axiomSource = parserAssembler.getAxiomSource(qname);
        if ((axiomSource == null) && !qname.getTemplate().isEmpty())
        {
            qname.clearTemplate();
            axiomSource = parserAssembler.getAxiomSource(qname);
        }
        if ((axiomSource == null) && (!name.equals(QueryProgram.GLOBAL_SCOPE)))
        {
            axiomSource = getGlobalParserAssembler().getAxiomSource(qname);
            if (axiomSource == null)
            {
                qname = QualifiedName.parseGlobalName(axiomKey.getName());
                axiomSource = getGlobalParserAssembler().getAxiomSource(qname);
            }
        }
        return axiomSource;
    }

    /**
     * Returns flag set true if supplied name is the name of a template
     * @param templateName Name to check
     * @return boolean
     */
    public boolean isTemplateName(String templateName)
    {
        boolean isTemplateKey = false;
        for (QualifiedName qame: parserAssembler.getTemplateAssembler().getTemplateNames())
            if (qame.getTemplate().equals(templateName))
            {
                isTemplateKey = true;
                break;
            }
        if (!isTemplateKey && !name.equals(QueryProgram.GLOBAL_SCOPE))
        {
            for (QualifiedName qame: getGlobalTemplateAssembler().getTemplateNames())
                if (qame.getTemplate().equals(templateName))
                {
                    isTemplateKey = true;
                    break;
                }
        }
        return isTemplateKey;
    }

    /**
     * Returns template with specified name
     * @param templateName Template name
     * @return Template object or null if template not found
     */
    public Template findTemplate(QualifiedName templateName)
    {
        String scopeName = templateName.getScope();
        if (!scopeName.isEmpty()) // Ensure scope name is valid
            getScope(scopeName);
        TemplateAssembler templateAssembler = getTemplateAssembler(scopeName);
        Template template = templateAssembler.getTemplate(templateName);
        return template;
    }

    /**
     * Returns template with specified name, creating one if required
     * @param templateName Template name
     * @return Template object
     */
    public Template getKeyTemplate(QualifiedName templateName) {
        TemplateAssembler templateAssembler = getTemplateAssembler(templateName.getScope());
        Template template = templateAssembler.getTemplate(templateName);
        if ((template == null) && !templateName.getScope().isEmpty()) {
            // Use global scope template if scope specified {
            template = getGlobalTemplateAssembler()
                .getTemplate(new QualifiedTemplateName(NameParser.GLOBAL_SCOPE, templateName.getTemplate()));
            if (template != null)
            	// Create replicate
                template = templateAssembler.createTemplate(template, templateName);
            else if (isResourceTemplate(templateName))
                // Create template for resource binding, if one exists
                template = parserAssembler.createResourceTemplate(templateName);
         }
         return template;
    }
 
    /**
     * Returns object containing all axiom listeners belonging to this scope
     * @return  Unmodifiable AxiomListener map object
     */
    public Map<QualifiedName, List<LocaleAxiomListener>> getAxiomListenerMap()
    {
        Map<QualifiedName, List<LocaleAxiomListener>> axiomListenerMap = null;
        if (!name.equals(QueryProgram.GLOBAL_SCOPE))
        {
            ListAssembler globalListAssembler = getGlobalListAssembler(); 
            if (globalListAssembler.getAxiomListenerMap().size() > 0)
                axiomListenerMap = globalListAssembler.getAxiomListenerMap();
        }
        Map<QualifiedName, List<LocaleAxiomListener>> localListenerMap = 
            parserAssembler.getListAssembler().getAxiomListenerMap();
        if (localListenerMap.size() > 0)
        {
            if (axiomListenerMap != null)
            {
                Map<QualifiedName, List<LocaleAxiomListener>> newAxiomListenerMap = new HashMap<>();
                newAxiomListenerMap.putAll(axiomListenerMap);
                axiomListenerMap = newAxiomListenerMap;
                axiomListenerMap.putAll(localListenerMap);
            }
            else
                axiomListenerMap = localListenerMap;
        }
        return axiomListenerMap == null ? null : Collections.unmodifiableMap(axiomListenerMap);
    }

    /**
     * Returns ItemList for specified list name 
     * @param listName List name
     * @return ItemList object
     */
    public ItemList<?> getItemList(String listName) 
    {
        // Look first in local scope, then if not found, try global scope
        String scopeName = name.equals(QueryProgram.GLOBAL_SCOPE) ? QualifiedName.EMPTY : name;
        return parserAssembler.getListAssembler().getItemList(scopeName, listName);
    }

    /**
     * Returns context of this scope
     * @param isFunctionScope Flag to indicate function scope
     * @return ScopeContext object
     */
    public ScopeContext getContext(boolean isFunctionScope) 
    {
        return new ScopeContext(this, isFunctionScope);
    }

    /**
     * Returns global scope
     * @return Scope, which is this object if global scope not already set
     */
    public Scope getGlobalScope() 
    {
        return scopeManager.getGlobalScope();
    }

    /**
     * Returns map which provides access to result lists as iterables
     * @return Container which maps fully qualified name to list iterable
     */
    public Map<QualifiedName, ResultList<?>> getListMap() 
    {
        return parserAssembler.getListAssembler().getListResults();
    }

    /** 
     * Returns container with axioms in scope 
     * @param listMap Maps exported lists to list names
     * @return Map of QualifiedNames to Axioms
     */
    public Map<QualifiedName, AxiomTermList> getAxiomMap(Map<QualifiedName, ResultList<?>> listMap)
    {
        Map<QualifiedName, AxiomTermList> axiomMap = new HashMap<>();
        if (!name.equals(QueryProgram.GLOBAL_SCOPE))
        {
            getGlobalListAssembler().copyAxioms(axiomMap);
            parserAssembler.getListAssembler().copyAxioms(axiomMap);
        }
        else
            parserAssembler.getListAssembler().copyAxioms(axiomMap);
        axiomMap.forEach((name, axiom) -> {
        	if (axiom != null)
	        	axiom.forEach(term -> {
	        		if (term.getValueClass() == ListOperand.class) {
	        			ListOperand<?> listOperand = (ListOperand<?>)term.getValue();
	        			ResultList<?> resultList = listMap.get(listOperand.getQualifiedName());
	        			if (resultList != null) {
	        				List<?> exportList = resultList.getList();
	        				term.setValue( exportList);
	        			}
	        		}
	        	});
        });
        return axiomMap;
    }

    /**
     * Returns list containing all the qualified query names in this scope
     * @return qualified name list
     */
    public List<QualifiedName> getQueryNames() {
    	List<QualifiedName> queryNames = new ArrayList<>();
    	querySpecMap.keySet().forEach(key -> {
    		queryNames.add(new QualifiedName(name, key));
    	});
    	return queryNames;
    }

    /**
     * Evaluate scope template
     * @param context Execution context
     */
	public void evaluateScopeTemplate() {
		QualifiedName qname = new QualifiedTemplateName(name, SCOPE);
		Template template = parserAssembler.getTemplateAssembler().getTemplate(qname);
		if ((template != null) && !template.isEmpty() )
			template.globalEvaluate(getExecutionContext());
	}

	/**
	 * Back up scope template
	 */
	public void backupScopeTemplate() {
		QualifiedName qname = new QualifiedTemplateName(name, SCOPE);
		Template template = parserAssembler.getTemplateAssembler().getTemplate(qname);
		if ((template != null) && !template.isEmpty() ){
			template.backup(false);
		}
	}

    /**
     * Add locale listener for Scope Properties axiom identified by key. The supplied axiom listener is
     * notified of every change of scope.
     * @param qualifiedAxiomName Local axiom qualified name
     * @param axiomListener The local axiom listener
     */
    public void addScopePropertiesListener(QualifiedName qualifiedAxiomName, LocaleAxiomListener axiomListener) 
    {
        // Register locale listener with Global scope in which all local axioms must be declared
        final ParserAssembler parserAssembler = getGlobalScope().getParserAssembler();
        ScopePropertiesListener localeListener = new ScopePropertiesListener(qualifiedAxiomName, axiomListener);
        // If the local is declared inside a scope, then it's scope never changes and a LocaleListener is not required
        if (!name.equals(QueryProgram.GLOBAL_SCOPE))
            localeListener.onScopeChange(this);
        else
        {
            parserAssembler.registerLocaleListener(localeListener); 
            ParserTask parserTask = parserAssembler.addPending(localeListener);
            parserTask.setPriority(ParserTask.Priority.list.ordinal());
        }
    }

    /**
     * Notify change of scope
     */
    public void notifyChange() 
    {
        // Notify Locale listeners in Global scope of scope locale
        if (!name.equals(QueryProgram.GLOBAL_SCOPE))
            getGlobalParserAssembler().onScopeChange(this);
    }

    /**
     * Returns ParserAssembler belonging to the global scope
     * @return ParserAssembler object
     */
    public ParserAssembler getGlobalParserAssembler()
    {
        return scopeManager.getGlobalScope().getParserAssembler();
    }

    /**
     * Returns ParserAssembler belonging to the global scope
     * @return ParserAssembler object
     */
    public ListAssembler getGlobalListAssembler()
    {
        return getGlobalParserAssembler().getListAssembler();
    }

    /**
     * Returns TemplateAssembler belonging to the global scope
     * @return TemplateAssembler object
     */
    public TemplateAssembler getGlobalTemplateAssembler()
    {
        return getGlobalParserAssembler().getTemplateAssembler();
    }

    public TemplateAssembler getTemplateAssembler(String templateScope) {
        Scope scope = getScope(templateScope);
        ParserAssembler parserAssembler = scope.getParserAssembler();
        return parserAssembler.getTemplateAssembler();
    }

    /**
     * Returns ParserAssembler belonging to the global scope
     * @return ParserAssembler object
     */
    public AxiomAssembler getGlobalAxiomAssembler()
    {
        return getGlobalParserAssembler().getAxiomAssembler();
    }

    public static ScopeFunctions getScopeFunctions() {
    	if (scopeFunctions == null)
    		throw new IllegalStateException("Global scope does not exist");
    	return scopeFunctions;
    }
 
    @Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Scope)
		    return name.equals(((Scope)obj).name);
		return false;
	}

    @Override
	public String toString() {
		return name;
	}

	/**
     * Returns Local specified by properties and language code
     * @param properties Script, region and variant values 
     * @param language Language code eg. "de" for Germany
     * @return Locale object
     * @throws ExpressionException if locale parameters are invalid
     */
    Locale getLocale(Map<String, Object> properties, String language)
    {
        // SE7 supported
        Object script = properties.get(QueryProgram.SCRIPT);
        Object region = properties.get(QueryProgram.REGION);
        Object variant = properties.get(QueryProgram.VARIANT);
        Locale locale = null;
        if (script == null)
        {
            if ((region == null) && (variant == null))
                 locale = new Locale(language);
            else if (region != null)
            {
                if (variant == null)
                     locale = new Locale(language, region.toString());
                else
                     locale = new Locale(language, region.toString(), variant.toString());
            }
        }
        else
        {
          Locale.Builder builder = new Locale.Builder();
          builder.setLanguage(language);
          if (region != null)
          {
              builder.setRegion(region.toString());
              if (variant != null)
                  builder.setVariant(variant.toString());
          }
          builder.setScript(script.toString());
          locale = builder.build();
        }
        if (locale == null)
            throw new ExpressionException("Scope \"" + name + "\" invalid Locale settings combination for language " + language);
        return locale;
    }

    Scope getScopeByName(String scopeName) {
		return scopeManager.getScopeByName(scopeName);
	}

    private boolean isResourceTemplate(QualifiedName templateName) {
    	ParserAssembler globalAssmbler =  getGlobalParserAssembler();
        String resourceName = globalAssmbler.getResourceName(templateName);
        return (resourceName != null);
    }

    /**
     * Set scope locale from properties
     * @param properties
     * @return flag set true if properties are valid
     */
    private boolean setLocale(Map<String,Object> properties)
    {
        boolean hasProperties = (properties != null) && !properties.isEmpty();
        if (hasProperties)
        {
            Object language = properties.get(QueryProgram.LANGUAGE);
            if (language != null)
                // SE7 supported
                try 
                {
                    locale = getLocale(properties, language.toString());
                    return true;
                }
                catch (IllformedLocaleException e)
                {
                  throw new ExpressionException("Scope \"" + name + "\" invalid Locale settings", e);
                }
        }
        return false;
    }

}
