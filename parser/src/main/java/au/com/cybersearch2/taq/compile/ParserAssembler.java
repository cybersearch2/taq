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
package au.com.cybersearch2.taq.compile;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.ChoiceArtifact;
import au.com.cybersearch2.taq.axiom.ResourceAxiomSource;
import au.com.cybersearch2.taq.axiom.SingleAxiomSource;
import au.com.cybersearch2.taq.compiler.CompilerException;
import au.com.cybersearch2.taq.compiler.ParserFunction;
import au.com.cybersearch2.taq.expression.CallOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ObjectOperand;
import au.com.cybersearch2.taq.expression.ResourceOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.ClassFileLoader;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.OperandVisitor;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.NameParser;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.list.CursorItemVariable;
import au.com.cybersearch2.taq.list.ResourceList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.pattern.OperandWalker;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.provider.CallHandler;
import au.com.cybersearch2.taq.provider.FunctionCallEvaluator;
import au.com.cybersearch2.taq.provider.GlobalFunctions;
import au.com.cybersearch2.taq.provider.ObjectSpec;
import au.com.cybersearch2.taq.provider.ResourceMonitor;
import au.com.cybersearch2.taq.query.InnerQueryLauncher;

/**
 * ParserAssembler Collects and organizes information gathered compiling an TAQ
 * script. Contained are the details gathered from parsing all statements within
 * a specified scoped.
 * 
 * @author Andrew Bowley
 *
 * @since 15/10/2010
 */
public class ParserAssembler implements LocaleListener {
	
	private static final String CLASS_LOAD_ERROR = "Error loading class %s";

	/** Scope */
	private Scope scope;
	/** The operands, which are terms placed in expressions */
	private OperandMap operandMap;
	/** Maps qualified axiom name to resource name */
	private Map<QualifiedName, String> axiomResourceMap;
	/** List of Locale listeners which are notified of change of scope */
	private List<LocaleListener> localeListenerList;
	/** Axioms which are bound to the current scope */
	private Map<QualifiedName, Axiom> scopeAxiomMap;
	/** Axioms used as parameters */
	private List<QualifiedName> parameterList;
	/** Optional ProviderManager for library operands */
	private ProviderManager providerManager;
	/** Lists container */
	private ListAssembler listAssembler;
	/** Template builder and container */
	private TemplateAssembler templateAssembler;
	/** Template builder and container */
	private AxiomAssembler axiomAssembler;
	/** Choice specification map */
	private Map<QualifiedName, ChoiceArtifact> choiceSpecMap;
	/** Qualified name of enclosing scope/template context */
	private QualifiedName qualifiedContextname;
	/** Parser task queue */
	private ParserTaskQueue parserTaskQueue;
	/** Function map */
	private Map<QualifiedName, CallHandler> functionMap;

	/**
	 * Construct a ParserAssembler object
	 * 
	 * @param scope The name of the enclosing scope
	 */
	public ParserAssembler(Scope scope) {
		this.scope = scope;
		qualifiedContextname = new QualifiedName(scope.getAlias(), QualifiedName.EMPTY);
		operandMap = new OperandMap();
		axiomResourceMap = new HashMap<>();
		localeListenerList = new ArrayList<>();
		parameterList = new ArrayList<>();
		listAssembler = new ListAssembler(scope);
		templateAssembler = new TemplateAssembler();
		axiomAssembler = new AxiomAssembler(scope);
		parserTaskQueue = new ParserTaskQueue();
		choiceSpecMap = new HashMap<>();
	}

	/**
	 * Returns object containing all operands and item lists
	 * 
	 * @return OperandMap object
	 */
	public OperandMap getOperandMap() {
		return operandMap;
	}

	/**
	 * Returns qualified name of enclosing scope/template
	 * 
	 * @return QualifiedName object
	 */
	public QualifiedName getQualifiedContextname() {
		return qualifiedContextname;
	}

	/**
	 * Set qualified name of enclosing scope/template context
	 * 
	 * @param qualifiedContextname Qualified context namep
	 */
	public void setQualifiedContextname(QualifiedName qualifiedContextname) {
		this.qualifiedContextname = qualifiedContextname;
	}

	/**
	 * Set function manager
	 * 
	 * @param providerManager ProviderManager object
	 */
	public void setFunctionManager(ProviderManager providerManager) {
		this.providerManager = providerManager;
	}

	/**
	 * Returns enclosing scope
	 * 
	 * @return Scope object
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * Returns Locale of enclosing scope
	 * 
	 * @return Locale object
	 */
	public Locale getScopeLocale() {
		return scope.getLocale();
	}

	public ListAssembler getListAssembler() {
		return listAssembler;
	}

	public TemplateAssembler getTemplateAssembler() {
		return templateAssembler;
	}

	public AxiomAssembler getAxiomAssembler() {
		return axiomAssembler;
	}

	/**
	 * Returns choice specification for given name. If a selection list is appended
	 * in the choice declaration, then the name of this list can also be used to
	 * retrieve the specification.
	 * 
	 * @param choiceName Qualified name of choice
	 * @return ChoiceSpecification object or null if not found
	 */
	public ChoiceArtifact getChoiceSpec(QualifiedName choiceName) {
		return choiceSpecMap.get(choiceName);
	}

	/**
	 * Maps given choice specification to it's name and also the selection list
	 * name, if it exists.
	 * 
	 * @param choiceName Qualified name of choice
	 * @param coiceSpec  Choice specification
	 */
	public void putChoiceSpec(QualifiedName choiceName, ChoiceArtifact coiceSpec) {
		choiceSpecMap.put(choiceName, coiceSpec);
	}

	/**
	 * Add contents of another ParserAssembler to this object
	 * 
	 * @param parserAssembler Other ParserAssembler object
	 */
	public void addAll(ParserAssembler parserAssembler) {
		axiomResourceMap.putAll(parserAssembler.axiomResourceMap);
		parameterList.addAll(parserAssembler.parameterList);
		listAssembler.addAll(parserAssembler.listAssembler);
	}

	/**
	 * Create Variable to contain inner Tempate values and add to OperandMap
	 * 
	 * @param innerTemplate The inner Template
	 */
	public void addInnerTemplate(ITemplate innerTemplate) {
		// Create Variable to be axiomTermList container. Give it the same name as the
		// inner Template
		// so it is qualified by the name of the enclosing Template
		Variable listVariable = new Variable(innerTemplate.getQualifiedName());
		// Add variable to OperandMap so it can be referenced from script
		operandMap.addOperand(listVariable);
	}

	/**
	 * Add scope-bound axiom. This axiom will be passed to the query by containing
	 * it in the QueryParams initialSolution object. All scope-bound axioms are
	 * removed on scope context reset at the conclusion of a query.
	 * 
	 * @param axiom Axiom object
	 * @see au.com.cybersearch2.taq.QueryParams#initialize()
	 */
	public void addScopeAxiom(Axiom axiom) {
		if (scopeAxiomMap == null)
			scopeAxiomMap = new HashMap<QualifiedName, Axiom>();
		String axiomName = axiom.getName();
		QualifiedName qualifiedAxiomName = QualifiedName.parseGlobalName(axiomName);
		scopeAxiomMap.put(qualifiedAxiomName, axiom);
	}

	/**
	 * Bind resource to axiom provider for given qualified axiom name and remove
	 * internal reference
	 * 
	 * @param resourceName         Resource name
	 * @param qualifiedBindingName Qualified name of axiom or template
	 * @return ResourceProvider
	 * @throws ExpressionException if axiom provider not found
	 */
	public ResourceProvider bindResource(String resourceName, QualifiedName qualifiedBindingName) {
		ResourceProvider resourceProvider = getResourceProvider(resourceName);
		if (resourceProvider == null)
			throw new ExpressionException("Axiom provider \"" + resourceName + "\" not found");
		if (!qualifiedBindingName.isTemplateEmpty()) {
			LocaleAxiomListener axiomListener = resourceProvider.getAxiomListener(qualifiedBindingName.toString());
			if (axiomListener == null)
				throw new CompilerException(
						String.format("Emitter named '%s' not found", qualifiedBindingName.toString()));
			registerAxiomListener(qualifiedBindingName, axiomListener);
		} else {
			// Remove entry from axiomListMap so the axiom is not regarded as internal
			listAssembler.removeAxiomItems(qualifiedBindingName);
			// Axiom header may have no term names, so an archetype needs to be created
			if (axiomAssembler.getAxiomArchetype(qualifiedBindingName) == null)
				axiomAssembler.createAxiomArchetype(qualifiedBindingName);
		}

		// Preserve mapping of qualified axiom name to resource name
		axiomResourceMap.put(qualifiedBindingName, resourceName);
		return resourceProvider;
	}

	/**
	 * Preserve mapping of qualified axiom name to resource name
	 * 
	 * @param qualifiedBindingName Qualified name of axiom or template
	 * @param resourceName         Resource name
	 */
	public void addResourceBinding(QualifiedName qualifiedBindingName, String resourceName) {
		// Place in global scope as it is guaranteed to exist
		scope.getGlobalParserAssembler().getAxiomResourceMap().put(qualifiedBindingName, resourceName);
	}

	/**
	 * Register name of axiom to be supplied as a parameter
	 * 
	 * @param qualifiedAxiomName Qualified name of axiom to which properties apply
	 */
	public void setParameter(QualifiedName qualifiedAxiomName) {
		parameterList.add(qualifiedAxiomName);
		if (!listAssembler.existsKey(ListType.axiom_item, qualifiedAxiomName))
			listAssembler.createAxiomItemList(qualifiedAxiomName, false);
	}

	/**
	 * Returns flag to indicate if the axiom specified by name is a parameter
	 * 
	 * @param qualifedAxiomName Qualified name of axiom
	 * @return boolean
	 */
	public boolean isParameter(QualifiedName qualifedAxiomName) {
		if (parameterList == null)
			return false;
		return parameterList.contains(qualifedAxiomName);
	}

	/**
	 * Returns axiom source for specified axiom name
	 * 
	 * @param qualifiedAxiomName Qualified axiom name
	 * @return AxiomSource object
	 */
	public AxiomSource getAxiomSource(QualifiedName qualifiedAxiomName) {
		// Scope-bound axioms are passed in query parameters and
		// removed at the end of the query
		if (scopeAxiomMap != null) { // Scope axioms are provided in query parameters.
			Axiom axiom = scopeAxiomMap.get(qualifiedAxiomName);
			if (axiom != null)
				return new SingleAxiomSource(axiom);
		}
		// Look for list defined in the script
		if ((parameterList == null) || !parameterList.contains(qualifiedAxiomName)) { // Axiom is declared in script?
			List<Axiom> axiomList = listAssembler.getAxiomItems(qualifiedAxiomName);
			if (axiomList != null)
				return axiomAssembler.createAxiomSource(qualifiedAxiomName, axiomList);
			else {
				Axiom axiom = listAssembler.getAxiom(qualifiedAxiomName);
				if (axiom != null)
					return axiomAssembler.createAxiomSource(qualifiedAxiomName, axiom);
				else if (!QueryProgram.GLOBAL_SCOPE.equals(scope.getName())) {
					axiom = scope.getGlobalListAssembler().getAxiom(qualifiedAxiomName);
					if (axiom != null)
						return axiomAssembler.createAxiomSource(qualifiedAxiomName, axiom);
				}
			}
			// Look for dynamic axiom list
			Operand operand = operandMap.getOperand(qualifiedAxiomName);
			if (operand != null) {
				AxiomSource axiomSource = templateAssembler.createAxiomSource(operand);
				if (axiomSource != null)
					return axiomSource;
			}
		}
		ParserAssembler globalParserAssembler = scope.getGlobalParserAssembler();
		String resourceName = getAxiomResourceMap().get(qualifiedAxiomName);
		if (resourceName == null) {
			if (!QueryProgram.GLOBAL_SCOPE.equals(scope.getName()))
				resourceName = globalParserAssembler.getAxiomResourceMap().get(qualifiedAxiomName);
		}
		if (resourceName != null) {
			final AxiomArchetype archetype = globalParserAssembler.getAxiomAssembler()
					.getAxiomArchetype(qualifiedAxiomName);
			if (archetype != null) {
				final ResourceProvider resourceProvider = getResourceProvider(resourceName);
				if (resourceProvider != null)
					return new ResourceAxiomSource(resourceProvider, archetype);
			}
		}
		return null;
	}

	/**
	 * Create template for for term list bound to resource. The template terms are
	 * simply named variables. An axiom declaration must exist with same name as
	 * template. Preconditions: A template with specified name does not already
	 * exist and a term name list is defined for axiom declaration.
	 * 
	 * @param templateName Qualified template name
	 * @return Template object
	 */
	public Template createResourceTemplate(QualifiedName templateName) { // Create qualified axiom name
		QualifiedName qualifiedAxiomName = QualifiedName.axiomFromTemplate(templateName);
		// Get axiom archetype
		AxiomArchetype axiomArchetype = axiomAssembler.getAxiomArchetype(qualifiedAxiomName);
		if ((axiomArchetype == null) && !templateName.getScope().equals(QueryProgram.GLOBAL_SCOPE)) {
			qualifiedAxiomName.clearScope();
			axiomArchetype = axiomAssembler.getAxiomArchetype(qualifiedAxiomName);
		}
		if (axiomArchetype != null) {
			List<String> termNameList = axiomArchetype.getTermNameList();
			if (!termNameList.isEmpty()) {
				TemplateAssembler targetAssember = scope.getTemplateAssembler(templateName.getScope());
				Template template = targetAssember.createTemplate(templateName, TemplateType.template);
				for (String termName : termNameList) {
					QualifiedName qname = getContextName(termName);
					template.addTerm(new Variable(qname));
				}
				return template;
			}
		}
		return null;
	}

	/**
	 * Queue task to bind list to it's source which may not yet be declared
	 * 
	 * @param axiomTermList Axiom term list object
	 */
	public void registerAxiomTermList(final AxiomTermList axiomTermList) {
		parserTaskQueue.registerAxiomTermList(axiomTermList, scope);
	}

	/**
	 * Queue task to bind list to it's source which may not yet be declared
	 * 
	 * @param axiomList The axiom list
	 */
	public void registerAxiomList(final AxiomList axiomList) {
		parserTaskQueue.registerAxiomList(axiomList, scope);
	}

	/**
	 * Register axiom term list for scope properties axiom
	 * 
	 * @param axiomTermList The term list
	 */
	public void registerScopeProperties(final AxiomTermList axiomTermList) {
		QualifiedName qualifiedAxiomName = axiomTermList.getKey();
		scope.addScopePropertiesListener(qualifiedAxiomName, axiomTermList.getAxiomListener());
	}

	/**
	 * Register locale listener to be notified when the scope changes
	 * 
	 * @param localeListener LocaleListener object
	 */
	public void registerLocaleListener(LocaleListener localeListener) {
		localeListenerList.add(localeListener);
	}

	/**
	 * Apply locale of given scope to the scope templates of this scope
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.LocaleListener#onScopeChange(au.com.cybersearch2.taq.Scope)
	 * @param localeScope Scope with locale to be applied
	 */
	public void changeScopeTemplate(Scope localeScope) {
		Template scopeTemplate = templateAssembler.getTemplate(new QualifiedTemplateName(scope.getAlias(), "scope"));
		if (scopeTemplate != null) {
			boolean[] scopeChanged = new boolean[] { false };
			OperandVisitor operandVisitor = new OperandVisitor() {

				@Override
				public boolean next(Operand operand, int depth) {
					if (operand instanceof LocaleListener) {
						LocaleListener localeListener = (LocaleListener) operand;
						if (localeListener.onScopeChange(localeScope))
							scopeChanged[0] = true;
					}
					return true;
				}
			};
			while (scopeTemplate != null) {
				if (!scopeTemplate.isEmpty()) {
					OperandWalker walker = scopeTemplate.getOperandWalker();
					walker.visitAllNodes(operandVisitor);
					if (scopeChanged[0]) {
						scopeTemplate.backup(true);
						scopeTemplate.evaluate(localeScope.getExecutionContext());
						scopeChanged[0] = false;
					}
				}
				scopeTemplate = scopeTemplate.getNext();
			}
		}
	}

	/**
	 * Notify all locale listeners that the scope has changed
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.LocaleListener#onScopeChange(au.com.cybersearch2.taq.Scope)
	 */
	@Override
	public boolean onScopeChange(Scope scope) {
		boolean isGlobal = QueryProgram.GLOBAL_SCOPE.equals(scope.getName());
		changeScopeTemplate(scope);
		// Evaluate global scope template, if not already done so
		if (isGlobal && (this.scope != scope))
			changeScopeTemplate(scope.getScope(QueryProgram.GLOBAL_SCOPE));
		notifyScopeChange(scope);
		if (this.scope != scope)
			scope.getParserAssembler().notifyScopeChange(scope);
		return true;
	}

	/**
	 * Remove all scope-bound axioms
	 */
	public void clearScopeAxioms() {
		if (scopeAxiomMap != null)
			scopeAxiomMap.clear();
	}

	/**
	 * Bind listener and term names for axiom container - AxiomList or
	 * AxiomTermList. This method is intended to be invoked in a ParserTask post
	 * compilation so the listener target is guaranteed to be parsed.
	 * 
	 * @param axiomContainer The axiom container
	 */
	public void bindAxiomList(AxiomContainer axiomContainer) {
		LocaleAxiomListener axiomListener = axiomContainer.getAxiomListener();
		QualifiedName axiomKey = axiomContainer.getKey();
		// Axiom key identifies the listener target and may point to an axiom source or
		// a query solution.
		QualifiedName qualifiedAxiomName = null;
		QualifiedName qualifiedTemplateName = null;
		List<Axiom> internalAxiomList = null;
		Scope targetScope = null;
		boolean isTemplateKey = !axiomKey.isTemplateEmpty();
		if (isTemplateKey && !axiomKey.isNameEmpty() && axiomKey.getScope().equals(scope.getAlias())) { 
			// This is an attached list
			qualifiedAxiomName = new QualifiedName(scope.getAlias(), axiomKey.getName());
			internalAxiomList = getListAssembler().getAxiomItems(qualifiedAxiomName);
			if (internalAxiomList != null) {
				AxiomArchetype archetype = axiomAssembler.getAxiomArchetype(qualifiedAxiomName);
				if (archetype != null)
					axiomContainer.setAxiomTermNameList(archetype.getTermNameList());
				listAssembler.setAxiomContainer(axiomContainer, internalAxiomList);
				return;
			}
			targetScope = scope;
			qualifiedTemplateName = QualifiedTemplateName.templateFromAxiom(axiomKey);
		}
		if (isTemplateKey) {
			if (qualifiedAxiomName == null)
				qualifiedAxiomName = QualifiedName.axiomFromTemplate(axiomKey);
		} else
			// Create copy of key to allow editing
			qualifiedAxiomName = new QualifiedName(axiomKey);
		// A key in axiom form may point to a query solution, so analysis required.
		// Where there is ambiguity, precedence is given to match on an axiom source
		if (targetScope == null) {
			targetScope = findSourceScope(qualifiedAxiomName);
			if (targetScope != null) {
				qualifiedTemplateName = QualifiedName.templateFromAxiom(qualifiedAxiomName);
				isTemplateKey = false;
			} else if (isTemplateKey)
				qualifiedTemplateName = axiomKey;
			else
				qualifiedTemplateName = QualifiedName.templateFromAxiom(axiomKey);
		}
		if (targetScope == null) {
			// Scope may need to change to Global scope if that is where axiom source with
			// same name found
			targetScope = templateAssembler.findTemplateScope(scope, qualifiedTemplateName);
			if (targetScope == null) {
				qualifiedAxiomName = QualifiedName.axiomFromTemplate(qualifiedTemplateName);
				if ((functionMap != null) && functionMap.keySet().contains(qualifiedAxiomName))
					targetScope = scope;
				else
					throw new ExpressionException("Axiom list binding failed for target \"" + axiomKey.toString());
			}
			isTemplateKey = true;
		}
		Template template = null;
		if (!isTemplateKey) {
			// A choice is detected using a key in template form
			template = targetScope.getParserAssembler().templateAssembler.getTemplate(qualifiedTemplateName);
			isTemplateKey = (template != null) && template.isChoice();
		}
		if (!isTemplateKey) { // The final analysis is axiom (term) list which can be set now
			AxiomArchetype archetype = axiomAssembler.getAxiomArchetype(qualifiedAxiomName);
			if (archetype != null)
				axiomContainer.setAxiomTermNameList(archetype.getTermNameList());
			ListAssembler listAssembler = targetScope.getParserAssembler().getListAssembler();
			internalAxiomList = listAssembler.getAxiomItems(qualifiedAxiomName);
			if (internalAxiomList != null) {
				listAssembler.setAxiomContainer(axiomContainer, internalAxiomList);
				return;
			} else {
				Axiom axiom = listAssembler.getAxiom(qualifiedAxiomName);
				listAssembler.setAxiomContainer(axiomContainer, Collections.singletonList(axiom));
				return;
			}
		} else {
			if (template == null)
				template = targetScope.getParserAssembler().templateAssembler.getTemplate(qualifiedTemplateName);
			if ((template == null) && (targetScope.getName() != QueryProgram.GLOBAL_SCOPE)) {
				QualifiedName globalName = new QualifiedTemplateName(QueryProgram.GLOBAL_SCOPE,
						qualifiedTemplateName.getTemplate());
				template = scope.getGlobalTemplateAssembler().getTemplate(globalName);
			}
			if (template != null)
				axiomContainer.setAxiomTermNameList(template.getTemplateArchetype().getTermNameList());
		}
		listAssembler.add(qualifiedTemplateName, axiomListener);
	}

	/**
	 * Returns scope of axiom source specified by qname
	 * 
	 * @param qname Qualified name
	 * @return Scope object or null if axiom source not found
	 */
	public Scope findSourceScope(QualifiedName qname) {
		if (!qname.isTemplateEmpty())
			// qname must be in axiom form
			return null;
		if (isQualifiedAxiomName(qname))
			return scope;
		if (!qname.isScopeEmpty())
			qname.clearScope();
		if (scope.getGlobalParserAssembler().isQualifiedAxiomName(qname))
			return scope.getGlobalScope();
		return null;
	}

	/**
	 * Returns flag set true if given qualified name identifies an axiom source
	 * 
	 * @param qname Qualified axiom name
	 * @return boolean
	 */
	public boolean isQualifiedAxiomName(QualifiedName qname) {
		if ((scopeAxiomMap != null) && (scopeAxiomMap.get(qname) != null))
			return true;
		if (listAssembler.existsKey(ListType.axiom_item, qname))
			return true;
		if (listAssembler.getAxiom(qname) != null)
			return true;
		if (axiomResourceMap.get(qname) != null)
			return true;
		return false;
	}

	/**
	 * Register axiom list by adding it's axiom listener to this ParserAssembler
	 * object
	 * 
	 * @param qname         The qualified name of the axioms inserted into the list
	 * @param axiomListener The axiom listener
	 */
	public void registerAxiomListener(QualifiedName qname, LocaleAxiomListener axiomListener) { 
		// Note to listen for solution notifications, qname must be template name
		String scopeName = qname.getScope();
		if (!scopeName.equals(scope.getAlias())) {
			if (scopeName.isEmpty())
				scopeName = QueryProgram.GLOBAL_SCOPE;
			Scope listenerScope = scope.findScope(scopeName);
			listenerScope.getParserAssembler().getListAssembler().add(qname, axiomListener);
		} else
			listAssembler.add(qname, axiomListener);
	}

	/**
	 * Returns operand which invokes an external function call or object method.
	 * 
	 * @param parserFunction Helper to collect function details
	 * @return CallOperand object or null if function not found
	 */
	public Operand getCallOperand(ParserFunction parserFunction) {
		CallOperand callOperand = null;
		QualifiedName qname = parserFunction.getName();
		String library = parserFunction.getLibrary();
		String name = parserFunction.getFunctionName();
		if (!library.equals(QueryProgram.GLOBAL_SCOPE)) {
			QualifiedName functionQname = new QualifiedName(library, name);
			CallHandler callHandler = getCallHandler(library, functionQname);
			if (callHandler != null) {
				FunctionCallEvaluator callHEvaluator = new FunctionCallEvaluator(callHandler);
				callOperand = new CallOperand(QualifiedName.parseName(functionQname.getSource(), qname),
						parserFunction.getParametersTemplate(), callHEvaluator);
				parserFunction.setCallEvaluator(callHEvaluator);
			}
		} else if (GlobalFunctions.isMethod(name)) {
			ObjectSpec objectSpec = new ObjectSpec(name, parserFunction.getParametersTemplate());
			objectSpec.setObject(new GlobalFunctions());
			objectSpec.setObjectClass(GlobalFunctions.class);
			objectSpec.setReflexive(false);
			return new ObjectOperand(objectSpec);
		}
		return callOperand;
	}

	/**
	 * Returns function object specified by name
	 * 
	 * @param library      Name of library
	 * @param classesBase  Classes path
	 * @param libraries    Libraries path
	 * @param resourceBase Resource root location
	 * @return Function object implementing CallEvaluator interface
	 * @throws ExpressionException if provider not found
	 */
	public FunctionProvider getFunctionProvider(String library, File classesBase, File libraries, File resourceBase) {
		FunctionProvider functionProvider = null;
		if (providerManager == null)
			providerManager = new ProviderManager();
		functionProvider = providerManager.findFunctionProvider(library);
		if (functionProvider != null)
			return functionProvider;
		QualifiedName libraryScopeName = new QualifiedName(library, "scope");
		if (listAssembler.existsKey(ListType.term, libraryScopeName))
			functionProvider = loadProviderClass(library, classesBase, libraries, resourceBase, libraryScopeName);
		return (functionProvider != null) ? functionProvider : providerManager.getFunctionProvider(library);
	}

	/**
	 * Returns function library specified by name
	 * 
	 * @param library The library name
	 * @return FunctionProvider implementation or null if not found
	 */
	public FunctionProvider findFunctionProvider(String library) {
		if (providerManager == null)
			providerManager = new ProviderManager();
		return providerManager.findFunctionProvider(library);
	}

	/**
	 * Macro to invoke function and return value in axiom
	 * 
	 * @param library  Function library
	 * @param name     Function name
	 * @param termList List of call parameters, may be empty
	 * @return Parameter object
	 */
	public Axiom callFunction(String library, String name, List<Term> termList) {
		QualifiedName qname = new QualifiedName(library, name);
		CallHandler callHandler = getCallHandler(library, qname);
		if (callHandler == null)
			throw new ExpressionException("Function \"" + name + "\" not supported");
		if (callHandler.getReturnType() == null)
			throw new ExpressionException("Function \"" + name + "\" does not return a single value");
		if (!callHandler.evaluate(termList))
			throw new ExpressionException("Function \"" + name + "\" did not complete successfully");
		Axiom axiom = callHandler.getSolution();
		if (axiom.getTermCount() == 0)
			throw new ExpressionException("Function \"" + name + "\" did not return a value");
		return axiom;
	}

	/**
	 * Returns operand which invokes a flow query.
	 * 
	 * @param queryLauncher  Query launcher for query/function call
	 * @param parserFunction Helper to collect function details
	 * @return CallOperand object
	 */
	public CallOperand getQueryOperand(InnerQueryLauncher queryLauncher, ParserFunction parserFunction) {
		QualifiedName qname = parserFunction.getName();
		FunctionCallEvaluator evaluator = new FunctionCallEvaluator(queryLauncher.getCallHandler());
		CallOperand callOperand = new CallOperand(qname, parserFunction.getParametersTemplate(), evaluator);
		callOperand.setPrivate(true);
		parserFunction.setCallEvaluator(evaluator);
		return callOperand;
	}

	/**
	 * Returns axiom provider specified by resource name
	 * 
	 * @param resourceName Resource name
	 * @return ResourceProvider object or null if object not found
	 */
	public ResourceProvider getResourceProvider(String resourceName) {
		QualifiedName qname = new QualifiedName(resourceName);
		Operand operand = scope.getGlobalParserAssembler().getOperandMap().get(qname);
		if (operand instanceof ResourceOperand) 
			return ((ResourceOperand) operand).getProvider();
		throw new ExpressionException(String.format("Resource %s not found", resourceName));
	}

	/**
	 * Returns qualified name for name in current context
	 * 
	 * @param name Name
	 * @return QualifiedName object
	 */
	public QualifiedName getContextName(String name) {
		return QualifiedName.parseName(name, qualifiedContextname);
	}

	/**
	 * Returns operand identified by name
	 * 
	 * @param operandName Operand name
	 * @return Operand object from same scope or global scope or null if not found
	 */
	public Operand findOperandByName(String operandName) {
		QualifiedName qualifiedOperandName = QualifiedName.parseName(operandName, qualifiedContextname);
		Operand operand = operandMap.get(qualifiedOperandName);
		if ((operand == null) && !qualifiedOperandName.isTemplateEmpty()) {
			qualifiedOperandName.clearTemplate();
			operand = operandMap.get(qualifiedOperandName);
		}
		if ((operand == null) && !qualifiedOperandName.isScopeEmpty()) {
			qualifiedOperandName.clearScope();
			operand = operandMap.get(qualifiedOperandName);
		}
		return operand;
	}

	/**
	 * Returns qualified name for resource specified by qualified binding name
	 * 
	 * @param qname Qualified name of axiom or template bound to resource
	 * @return QualifiedName object or null if not found
	 */
	public String getResourceName(QualifiedName qname) {
		return axiomResourceMap.get(qname);
	}

	public void putResourceName(QualifiedName qname, String resourceName) {
		axiomResourceMap.put(qname, resourceName);
	}

	/**
	 * Add Runnable to pending list
	 * 
	 * @param pending Runnable to execute parser task
	 * @return ParserTask
	 */
	public ParserTask addPending(ParserRunner pending) {
		return parserTaskQueue.addPending(pending, scope);
	}

	/**
	 * Collect pending parser tasks into priority queue
	 * 
	 * @param priorityQueue Priority queue
	 */
	public void getPending(PriorityQueue<ParserTask> priorityQueue) {
		parserTaskQueue.getPending(priorityQueue);
	}

	/**
	 * Add operand to operand map, handling special case for self template variable
	 * 
	 * @param qualifiedName Qualified name - 2-part is template name
	 * @return Operand object
	 */
	public Operand addOperand(QualifiedName qualifiedName) {
		// Logic depends on how many parts are in qualified name and what they contain
		// Extract actual scope if 'scope@template' encountered
		String scopeName = qualifiedName.getScope();
		String[] parts = scopeName.split("@");
		String actualScope;
		if (parts.length > 1)
			actualScope = parts[0];
		else
			actualScope = scopeName;
		// Template name is taken directly f
		String templateName = qualifiedName.getTemplate();
		// Check for case qualifiedName is in global scope or current scope
		if (scopeName.isEmpty() || actualScope.equals(scope.getName())) {
			if (templateName.isEmpty()) { // A single name part is converted to a context name
				qualifiedName = QualifiedName.parseName(qualifiedName.getName(), qualifiedContextname);
				return operandMap.addOperand(qualifiedName);
			} else if (templateName.equals(qualifiedContextname.getTemplate())) {
				// Name in current template
				return operandMap.addOperand(qualifiedName, qualifiedName);
			}
		}
		// Name in different namespace, so use distinct key in operand map as this
		// variable is to be stored in local object map
		qualifiedName = new QualifiedName(scopeName, templateName, qualifiedName.getName());
		QualifiedName key = null;
		// Decompose context scope into parts
		String[] contextParts = qualifiedContextname.getScope().split("@");
		if (parts.length > 1) { // Check for same enclosing template, in which case, use qualifiedName as key
			if ((contextParts.length > 1) && contextParts[0].equals(parts[0]) && contextParts[1].equals(parts[1]))
				key = qualifiedName;
		} else if (contextParts.length > 1) { // Check for check for same enclosing template based on current context
			String contextScope = contextParts[0].equals("global") ? "" : contextParts[0];
			if (contextScope.equals(scopeName) && contextParts[1].equals(templateName))
				key = qualifiedName;
		}
		if (key == null)
			// Name is outside current context, so make one up that is unique in this scope
			key = new QualifiedName(scopeName + "." + qualifiedName.getTemplate(), templateName,
					qualifiedName.getName());
		return operandMap.addOperand(key, qualifiedName);
	}

	/**
	 * Add given choice to choice map
	 * 
	 * @param functionQname Function qualified name
	 * @param callHandler   Call evaluator
	 */
	public void putFunction(QualifiedName functionQname, CallHandler callHandler) {
		if (functionMap == null)
			functionMap = new HashMap<>();
		functionMap.put(functionQname, callHandler);
	}

	/**
	 * Returns function call evaluator specified by qualified name
	 * 
	 * @param functionQname Function qualified name
	 * @return CallHandler object or null if not found
	 */
	public CallHandler getFunction(QualifiedName functionQname) {
		return functionMap != null ? functionMap.get(functionQname) : null;
	}

	/**
	 * Process Name production
	 * 
	 * @param name          Identifier
	 * @param isContextName Flag set true to incorporate context details
	 * @param isDelaration  Is part of a declaration
	 * @return QualifiedName object
	 */
	public QualifiedName name(String name, boolean isContextName, boolean isDeclaration) {
		NameParser parsed = new NameParser(name);
		String scopePart = parsed.getScope();
		String templatePart = parsed.getTemplate();
		if (!scopePart.isEmpty())
			isContextName = false;
		if (isContextName && (scopePart.isEmpty() || templatePart.isEmpty()) && !name.endsWith("@")) {
			boolean isOnePartName = scopePart.isEmpty() && templatePart.isEmpty();
			if (isOnePartName) {
			   QualifiedName qname = analyseOnePartName(name, isContextName, isDeclaration);
			   if (qname != null)
				   return qname;
			}
			QualifiedName listName = findListName(name);
			if (listName != null)
				return listName;
			if (isOnePartName)
				// Return qualified name based on context for creation of a new variable
				return new QualifiedName(name, getQualifiedContextname());
		}
		return parsed.getQualifiedName();
	}

	/**
	 * Returns qualified name of list given a name in text format
	 * 
	 * @param listName List name. Must to 1-part for success.
	 * @return Qualified name of an existing list or null if none found
	 */
	public QualifiedName findListName(String listName) {
		if (listName.indexOf('.') == -1) {
			QualifiedName contextName = getQualifiedContextname();
			String[] parts = contextName.getScope().split("@");
			QualifiedName contextListName;
			if (parts.length > 1)
				contextListName = new QualifiedName(parts[0], parts[1], listName);
			else
				contextListName = new QualifiedName(listName, contextName);
			if (listAssembler.existsKey(ListType.basic, contextListName)
					|| listAssembler.existsKey(ListType.term, contextListName)
					|| listAssembler.existsKey(ListType.cursor, contextListName)
					|| listAssembler.existsKey(ListType.axiom_item, contextListName)
					|| listAssembler.existsKey(ListType.axiom_dynamic, contextListName)
					|| listAssembler.existsKey(ListType.context, contextListName))
				return contextListName;
			else {
				Operand operand = getOperandMap().getOperand(contextListName);
				if (operand instanceof CursorItemVariable)
					return contextListName;
				contextListName.clearTemplate();
				if (listAssembler.existsKey(ListType.basic, contextListName)
						|| listAssembler.existsKey(ListType.axiom_item, contextListName)
						|| listAssembler.existsKey(ListType.term, contextListName)
						|| listAssembler.existsKey(ListType.axiom_dynamic, contextListName))
					return contextListName;
			}
		}
		if (scope != scope.getGlobalScope()) {
			ListAssembler global = scope.getGlobalParserAssembler().getListAssembler();
			QualifiedName globalListName = new QualifiedName(listName);
			if (global.existsKey(ListType.basic, globalListName) || global.existsKey(ListType.term, globalListName)
					|| global.existsKey(ListType.axiom_item, globalListName)
					|| global.existsKey(ListType.axiom_dynamic, globalListName))
				return globalListName;
		}
		return null;
	}

	/**
	 * Returns AxiomList linked to a resource provider with a data-source role
	 * 
	 * @param resourceProvider Resource provider
	 * @return ResourceList object
	 */
	public ResourceList getResourceList(ResourceProvider resourceProvider) {
		if (!(resourceProvider instanceof ResourceMonitor))
			throw new CompilerException("RsourceProvider must extend class ResourceMonitor");
		// Map from resource name to qualified axiom name in resource declaration
		Set<Entry<QualifiedName, String>> entrySet = axiomResourceMap.entrySet();
		for (Entry<QualifiedName, String> entry : entrySet) {
			if (entry.getValue().equals(resourceProvider.getName())) {
				if (entry.getKey().isGlobalName() && entry.getKey().getTemplate().isEmpty()) {
					// Get axiom archetype and list for ResourceList constructor
					QualifiedName axiomName = entry.getKey();
					ParserAssembler globalAssmbler = scope.getGlobalParserAssembler();
					AxiomArchetype archetype = globalAssmbler.getAxiomAssembler().getAxiomArchetype(axiomName);
					List<Axiom> axiomList = globalAssmbler.getListAssembler().getAxiomItems(axiomName);
					return new ResourceList((ResourceMonitor) resourceProvider, archetype, axiomList);
				}
			}
		}
		throw new ExpressionException(
				String.format("Resource provider named '%s' not found", resourceProvider.getName()));
	}

	public void notifyScopeChange(Scope toScope) {
		for (LocaleListener localeListener : localeListenerList)
			localeListener.onScopeChange(toScope);
	}

	private CallHandler getCallHandler(String library, QualifiedName qname) {
		if (providerManager == null)
			providerManager = new ProviderManager();
		Scope libraryScope = scope;
		if (!scope.getAlias().equals(library))
			libraryScope = scope.findScope(library);
		CallHandler callHandler = null;
		if (libraryScope != null)
			callHandler = libraryScope.getParserAssembler().getFunction(qname);
		if (callHandler == null) {
			FunctionProvider functionProvider = providerManager.findFunctionProvider(library);
			if (functionProvider != null)
				callHandler = functionProvider.getCallEvaluator(qname.getName());
		}
		return callHandler;
	}

	private Map<QualifiedName, String> getAxiomResourceMap() {
		return axiomResourceMap;
	}

	private FunctionProvider loadProviderClass(
			String library, 
			File classesBase, 
			File libraries, 
			File resourceBase,
			QualifiedName libraryScopeName) {
		FunctionProvider functionProvider = null;
		Axiom scopeParameters = listAssembler.getAxiom(libraryScopeName);
		Term term = scopeParameters.getTermByName("provider");
		if (term != null) {
			String classname = term.getValue().toString();
			ClassFileLoader loader = new ClassFileLoader(classesBase, libraries, resourceBase);
			Class<?> clazz = loader.loadClass(classname);
			try {
				functionProvider = (FunctionProvider) clazz.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException e) {
				throw new ProviderException(String.format(CLASS_LOAD_ERROR, classname), e);
			}
			providerManager.putFunctionProvider(library, functionProvider);
		}
		return functionProvider;
	}

	private QualifiedName analyseOnePartName(String name, boolean isContextName, boolean isDeclaration) {
		// One-part name may refer to variable on template path, so navigate path
		// to see if any variable with given name exists
		QualifiedName contextName = getQualifiedContextname();
		String scope = contextName.getScope();
		String[] parts = scope.split("@");
		if (name.charAt(0) == '/') { // Path references variable in enclosing template
			if (parts.length > 1)
				return new QualifiedName(parts[0], parts[1], name.substring(1));
			else
				return new QualifiedName(name.substring(1), contextName);
		}
		if (parts.length < 2) {
			// Create parts for outer template
			parts = new String[] { contextName.getScope(), contextName.getTemplate() };
			if (parts[0].isEmpty())
				parts[0] = QueryProgram.GLOBAL_SCOPE;
		}
		int index = 0;
		StringBuilder scopeBuilder = new StringBuilder(parts[0]);
		OperandMap operandMap = getOperandMap();
		QualifiedName qname = null;
		while (++index <= parts.length) {
			if (index < parts.length)
				scopeBuilder.append('@').append(parts[index]);
			else
				scopeBuilder.append('@').append(contextName.getTemplate());
			qname = operandMap.findTemplateScopeName(name, scopeBuilder.toString());
			if (qname != null)
				return qname;
		}
		if (!isDeclaration)
			qname = operandMap.findScopeName(name, parts[0]);
		return qname;
	}

}
