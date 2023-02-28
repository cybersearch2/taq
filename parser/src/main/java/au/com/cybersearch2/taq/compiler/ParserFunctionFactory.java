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
package au.com.cybersearch2.taq.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.ChoiceArtifact;
import au.com.cybersearch2.taq.artifact.FunctionArtifact;
import au.com.cybersearch2.taq.artifact.FunctionFactoryArtifact;
import au.com.cybersearch2.taq.compile.AxiomAssembler;
import au.com.cybersearch2.taq.compile.Library;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.ParserTask;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.db.DatabaseSupport;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.CallOperand;
import au.com.cybersearch2.taq.expression.ChoiceOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.ObjectOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.CallEvaluator;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.list.ListItemVariable;
import au.com.cybersearch2.taq.list.ListLength;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Choice;
import au.com.cybersearch2.taq.pattern.ChoiceParameters;
import au.com.cybersearch2.taq.pattern.ReceiverHandler;
import au.com.cybersearch2.taq.pattern.SelectCallEvaluator;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;
import au.com.cybersearch2.taq.provider.CallHandler;
import au.com.cybersearch2.taq.provider.FunctionCallEvaluator;
import au.com.cybersearch2.taq.provider.FunctionException;
import au.com.cybersearch2.taq.provider.ObjectSpec;
import au.com.cybersearch2.taq.provider.ScopeFunctions;
import au.com.cybersearch2.taq.query.InnerQueryLauncher;
import au.com.cybersearch2.taq.query.QueryEvaluator;

/**
 * Singleton parser helper to create function artifacts
 *
 */
public class ParserFunctionFactory implements FunctionFactoryArtifact {

	/** Parser context */
	private final ParserContext parserContext;
	
	private ParserListFactory parserListFactory;


	/**
	 * Construct ParserFunctionFactory object
	 * @param parserContext Parser context
	 */
	public ParserFunctionFactory(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

    /**
     * Process Function production
     * @param library Library containing the function
     * @param name Name of function
     * @param termList Function arguments
     * @return Parameter object
     */
	@Override
    public Parameter function(String library, String name, List<Term> termList) {
        Axiom axiom = getParserAssembler().callFunction(library, name, termList);
        return (Parameter)axiom.getTermByIndex(0);
    }	

	/**
	 * Pop receiver off receiver stack
	 */
	@Override
	public void popReceiver(FunctionArtifact functionArtifact) {
		ParserFunction parserFunction = (ParserFunction)functionArtifact;
		ReceiverHandler receiverHandler =  parserFunction.getReceiverHandler();
       	TemplateAssembler templateAssembler = getParserAssembler().getTemplateAssembler();
      	List<Template> receiverLeaf = templateAssembler.getTemplateList(receiverHandler.getQualifiedName());
      	if (!receiverLeaf.isEmpty())
      		receiverHandler.setReceiverLeaf(receiverLeaf);
       	templateAssembler.popReceiver();
	}

	/**
	 * Returns ObjectOperand for scope operand identified by name 
	 * @param name Qualified name of operand
	 * @param function Method signature
	 * @return
	 */
	@Override
	public Operand createScopeFunction(QualifiedName name, String function) {
		ObjectSpec objectSpec = new ObjectSpec(function, Collections.emptyList(), null);
		objectSpec.setObject(Scope.getScopeFunctions());
		objectSpec.setObjectClass(ScopeFunctions.class);
		//objectSpec.setReflexive(isReflexive);
		ObjectOperand objectOperand = new ObjectOperand(objectSpec);
		//if ((parserTerm != null) && !isReflexive)
		//	objectOperand.setLeftOperand(target);
		return objectOperand;
	}

    /**
     * Process function production
     * @param functionName Function name
     * @param operandType Function return type or null if none specified
     * @param returnsList Flag set true if list returned
     * @param archetype Archetype name if operand type is AXIOM, otherwise null
     */
	@Override
	public void function(String functionName, OperandType operandType, boolean returnsList, String archetype) {
        ParserAssembler parserAssembler = getParserAssembler();
		String scope = parserAssembler.getScope().getName();
		if (QueryProgram.GLOBAL_SCOPE.equals(scope))
			throw new ExpressionException(String.format("Function %s is not found in global scope"));
		File classesBase = null;
		File resourceBase = null;
		File libraries = null;
        FunctionProvider functionProvider = parserAssembler.getFunctionProvider(scope, classesBase, libraries, resourceBase);
        if (functionProvider == null)
            throw new ExpressionException("Function library \"" + scope + "\" not found");
        CallHandler callHandler = functionProvider.getCallEvaluator(functionName);
        if (callHandler == null)
            throw new ExpressionException("Function \"" + functionName + "\" not supported");
        if (callHandler instanceof DatabaseSupport) {
        	((DatabaseSupport)callHandler).setDatabaseProviders(parserContext.getQueryProgram().getDatabaseProviders());
        }
        QualifiedName qname = new QualifiedName(scope, functionName);
        parserAssembler.putFunction(qname, callHandler);
        // Template name required for solution
        qname = new QualifiedName(scope, functionName, Term.ANONYMOUS);
        try {
    		if (operandType == OperandType.AXIOM) {
	    		QualifiedTemplateName qualifiedTemplateName = 
	    			new QualifiedTemplateName(QueryProgram.GLOBAL_SCOPE, archetype);
	        	Template template = 
	        		parserContext
	        			.getScope()
	        			.getGlobalTemplateAssembler()
	        			.getTemplate(qualifiedTemplateName);
	        	if (template != null) {
	        		qualifiedTemplateName = 
	    	    			new QualifiedTemplateName(scope, archetype);
	        		callHandler.setSolutionTemplate(new Template(template, qualifiedTemplateName));
	        		operandType = template.getTemplateArchetype().getOperandType(); 
	        	} else
	        		throw new ExpressionException(String.format("Archetype for %s not found", qualifiedTemplateName.toString()));
	        	TemplateArchetype templateArchetype = template.getTemplateArchetype();
		        VariableSpec varSpec = new VariableSpec(templateArchetype.getOperandType());
		        varSpec.setAxiomKey(qname);
		        VariableFactory variableFactory = new VariableFactory(varSpec);
		        ItemList<?> itemList = variableFactory.getItemListInstance(qname, false);
		        if (operandType == OperandType.AXIOM)
		        	parserAssembler.registerAxiomList((AxiomList) itemList);
		        else
		        	parserAssembler.registerAxiomTermList((AxiomTermList) itemList);
		        itemList.setPublic(true);
		        parserAssembler.getListAssembler().addItemList(itemList.getQualifiedName(), itemList);
		        AxiomContainer axiomContainer = (AxiomContainer)itemList;
		        axiomContainer.setAxiomTermNameList(templateArchetype.getTermNameList());
		        callHandler.setAxiomContainer(axiomContainer);
		        callHandler.applyReturnType(varSpec.getOperandType() == OperandType.AXIOM ? OperandType.SET_LIST : OperandType.TERM);
	        } else if (operandType != null) {
	        	if (returnsList)
	        	    callHandler.applyListReturnType(operandType);
	        	else
	        	    callHandler.applyReturnType(operandType);
	        }
		} catch (FunctionException e) {
    		throw new CompilerException(String.format("Function '%s' declaraton error", qname.toString()), e);
		}
	}

    /**
     * Returns operand which invokes an external function call. 
     * The function name must consist of 2 parts. The first is the name of a library.
     * The type of object returned from the call depends on the library.
     * 
     * @param parserFunction Helper to collect function details
     * @return Operand object
     */
	@Override
	public Operand createCallOperand(FunctionArtifact functionArtifact) {
		ParserFunction parserFunction = (ParserFunction) functionArtifact;
		ParserAssembler parserAssembler = getParserAssembler();
		QualifiedName qname = parserFunction.getName();
		Template parametersTemplate = parserFunction.getParametersTemplate();
		Operand callOperand = parserAssembler.getCallOperand(parserFunction);
		if (callOperand == null) {
			boolean isSelect = parserFunction.getLibrary().equals(QueryProgram.GLOBAL_SCOPE);
			String functionName = isSelect ? parserFunction.getFunctionName() : parserFunction.getLibrary();
			QualifiedName choiceName = parserAssembler.getContextName(ChoiceArtifact.CHOICE_PREFIX + functionName);
			ChoiceArtifact choiceSpec = parserAssembler.getChoiceSpec(choiceName);
		    if (choiceSpec == null) {
		    	choiceName = new QualifiedName(parserAssembler.getScope().getName(), choiceName.getName());
		    	choiceSpec = parserAssembler.getChoiceSpec(choiceName);
		    }
		    if (choiceSpec != null) {
		    	int termCount = parametersTemplate == null ? 0 : parametersTemplate.getTermCount();
		    	if (isSelect) {
			    	if (termCount == 0) 
			    	    throw new CompilerException(String.format("Select %s requires at least one parameter", functionName));
			        callOperand = createSelectOperand(choiceSpec, parametersTemplate, parserFunction);
			        qname = choiceName;
			        parserFunction.setName(qname);
		    	} else {
		    	   	QualifiedName choiceOperandName = new QualifiedName(functionName, choiceName);
					callOperand = createObjectOperand(choiceOperandName, parserFunction, parserAssembler);
					if (callOperand == null)
			    	    throw new CompilerException(
			    	    	String.format("Select %s.%s with %d parameters is not valid",
			    	    			functionName, parserFunction.getFunctionName(), termCount));
		    	}
		    }
		}
		if (callOperand == null)
			callOperand = createObjectOperand(qname, parserFunction, parserAssembler);
		if ((callOperand == null) && (parserFunction.getFunctionName().equals("size"))) {
			ListAssembler listAssembler = parserAssembler.getListAssembler();
	     	OperandMap operandMap = parserAssembler.getOperandMap();
	     	String listName = parserFunction.getLibrary();
	     	QualifiedName qualifiedListName = name(listName, true, false);
            if (operandMap.existsName(listName)) {
       	        Operand operand = operandMap.getOperand(qname);
       	        if (operand instanceof ListOperand)
    	     		callOperand = new ListLength(QualifiedName.ANONYMOUS, (ListOperand<?>)operand);
       	    }
            if (callOperand == null) {
            	ListLength listLength = null;
		     	if (parserContext.getVariableSpec(listName) != null) {
		     		qualifiedListName = new QualifiedName(Scope.SCOPE, QualifiedName.EMPTY, listName);
		     		listLength = new ListLength(QualifiedName.ANONYMOUS, qualifiedListName);
		     	} else {
			     	ListType listType = listAssembler.getListType(qualifiedListName);
					if ((listType != ListType.none) && (listType != ListType.cursor)) {
				         boolean isList = 
				             (listType == ListType.basic) || 
				             (listType == ListType.term) || 
				             (listType == ListType.axiom_item) || 
				             (listType == ListType.axiom_dynamic);
				         if (isList)
				        	 listLength = new ListLength(QualifiedName.ANONYMOUS, qualifiedListName);
				    }
	            }
		     	if (listLength != null) {
		     		callOperand = listLength;
			        ParserTask parserTask = parserAssembler.addPending(listLength);
			        parserTask.setPriority(ParserTask.Priority.variable.ordinal());
		     	}
	        }
		}
		if (callOperand == null)
			callOperand = flowQuery(parserFunction);
		return callOperand;
	}
	
	/**
	 * Set optional list reference to extract an item from a returned list
	 * @param callOperand Function operand
	 * @param listReference List reference 
	 * @return List reference operand
	 */
	public IOperand setListReference(IOperand callOperand, ListReference listReference) {
		QualifiedName qname = ((Operand)callOperand).getQualifiedName();
		VariableSpec varSpec = new VariableSpec(OperandType.TERM);
	   	VariableFactory variableFactory = new VariableFactory(varSpec);
	   	QualifiedName axiomName = new QualifiedName(qname.toString());
	   	ListOperand<?> listOperand = variableFactory.getDynamicListOperandInstance(axiomName, (Operand)callOperand);
    	int reference = qname.incrementReferenceCount();
		QualifiedName listOpName = new QualifiedName(qname.getName() + reference, qname);
		if (parserListFactory == null)
			parserListFactory = new ParserListFactory(parserContext);
        ListItemSpec[] listItemSpec = parserListFactory.indexExpression(axiomName, listReference);
        ListItemVariable listItemVariable;
        if (listItemSpec.length == 1)
        	listItemVariable = new ListItemVariable("", listOpName, (ItemList<?>)listOperand, listItemSpec[0], null);
        else
        	listItemVariable = new ListItemVariable("", listOpName, (ItemList<?>)listOperand, listItemSpec, null);
        listItemVariable.setRightOperand(listOperand);
        return listItemVariable;
	}

    /**
     * Returns receiver template which is created given it's name and outer template name
     * @param receiverName Qualified name of receiver
     * @return Template object
     */
	@Override
    public ITemplate createReceiverTemplate(QualifiedName receiverName, FunctionArtifact functionArtifact) {
    	QualifiedName outerTemplateName = parserContext.getTemplateName();
    	// Use ParserAssembler in receiver name scope
    	ParserAssembler pasrserAssembler = parserContext.getScope().getScope(receiverName.getScope()).getParserAssembler();
       	TemplateAssembler templateAssembler = pasrserAssembler.getTemplateAssembler();
        Template template = (Template)templateAssembler.createReceiverTemplate(outerTemplateName, receiverName.getName());
        template.setKey(outerTemplateName.getTemplate() + "." + receiverName);
    	String listName = template.getQualifiedName().getName();
    	QualifiedName listQname = new QualifiedName(listName, functionArtifact.getName());
    	ListAssembler listAssembler = pasrserAssembler.getListAssembler();
     	ParserFunction parserFunction = (ParserFunction)functionArtifact;
    	CallEvaluator callEvaluator = parserFunction.getCallEvaluator();
    	AxiomTermList axiomTerms = listAssembler.getAxiomTerms(listQname);
    	Scope scope = pasrserAssembler.getScope();
    	Locale locale = scope.getLocale();
    	ExecutionContext context = scope.getExecutionContext();
    	ReceiverHandler receiverHandler = new ReceiverHandler(template, axiomTerms, locale, context);
     	if (callEvaluator instanceof SelectCallEvaluator)
    	    ((SelectCallEvaluator)callEvaluator).setReceiverHandler(receiverHandler);
    	else if (callEvaluator instanceof FunctionCallEvaluator)
        	((FunctionCallEvaluator)callEvaluator).setReceiverHandler(receiverHandler);
     	parserFunction.setReceiverHandler(receiverHandler);
        return template;
    }

	/**
	 * Returns select operand
	 * @param choiceSpec Choice specification
	 * @param parameterTemplate selection term and optional parameters
	 * @param parserFunction ParserFunction object
	 * @return Operand object
	 */
    private Operand createSelectOperand(ChoiceArtifact choiceSpec, Template parameterTemplate, ParserFunction parserFunction) {
	    ParserAssembler parserAssembler = getParserAssembler();
	    OperandMap operandMap = parserAssembler.getOperandMap();
	    // Choice identity is alias, if it has one
	    String choiceName = choiceSpec.getName();
	   	QualifiedName choiceQname = parserAssembler.getContextName(choiceName);
	   	QualifiedName choiceTemplateName = choiceSpec.getQualifiedTemplateName();
	   	Template template = parserAssembler.getTemplateAssembler().getTemplate(parserContext.getTemplateName());
	    Template choiceTemplate = parserAssembler.getTemplateAssembler().createChoiceTemplate(template, choiceTemplateName);
	    template.setNext(choiceTemplate);
	    AxiomAssembler axiomAssembler = parserAssembler.getAxiomAssembler();
	    // Create operand list to hold the term operand as the first item, and following if applicable, the selection operands
	    List<Operand> operandList = new ArrayList<Operand>();
        operandList.add(new Variable(QualifiedName.parseName(choiceSpec.getName(), parserAssembler.getQualifiedContextname())));
        List<String> termNames = axiomAssembler.getTermNameList(choiceSpec.getQualifiedAxiomName());
    	// Selection operands are employed
	    for (int i = 1; i < termNames.size(); ++i) {
	    	String termName = termNames.get(i);
	    	if (termName.isEmpty()) {
	    		// An empty term name represents the selection term 
	    		break;
	    	}
        	QualifiedName contextName = parserAssembler.getQualifiedContextname();
        	String[] parts = contextName.getScope().split("@");
        	QualifiedName contextListName;
        	if (parts.length > 1)
        		contextListName = new QualifiedName(parts[0], parts[1], termName);
        	else
        		contextListName = new QualifiedName(termName, contextName);
        	Operand operand = operandMap.addOperand(contextListName);
        	operandList.add(operand);
		}
        ChoiceParameters choiceParameters = new ChoiceParameters(parserAssembler.getScope(), template.getQualifiedName(), operandList);
       	choiceParameters.setParameterTemplate(parameterTemplate);
        // Selection term if the choice is declared at scope level
        String choiceScope = choiceTemplateName.getScope();
        if (choiceSpec.isScopeContext())
        	choiceParameters.setScopeOperand(operandMap.getOperand(new QualifiedName(choiceScope, choiceSpec.getName())));
	   	Choice choice = new Choice(choiceSpec, choiceParameters);
	   	if (!choiceSpec.isMap())	   	
	       	// Add choice to, or update, operand map. This allow more than one select to occur in a flow or template
	   	    operandMap.putOperand(choice);
	   	// Create return operand
	    if (choiceSpec.isMap()) {
	        Operand choiceOperand = new ChoiceOperand(choiceQname, choiceTemplate, choice);
	     	// Ensure term operand is placed in the operand map
	        boolean hasTermName = operandMap.hasOperand(choiceQname);
	     	if (!hasTermName)
		  	    operandMap.addOperand(choiceOperand);
		    return choiceOperand;
	    } else {
	    	SelectCallEvaluator callHandler = new SelectCallEvaluator(choice, template, choiceTemplate);
	    	parserFunction.setCallEvaluator(callHandler);
	    	AxiomTermList returnList = parserAssembler.getListAssembler().getAxiomTermList(choiceSpec.getQualifiedName());
	    	callHandler.setAxiomContainer(returnList);
		    return new CallOperand(choiceQname, parameterTemplate, callHandler);
        }
    }  

    private Operand createObjectOperand(QualifiedName qname, ParserFunction parserFunction, ParserAssembler parserAssembler) {
		QualifiedName targetName;
		Operand callOperand = null;
		Operand target = null;
		OperandMap operandMap = parserAssembler.getOperandMap();
		if (!qname.isScopeEmpty() && !qname.isTemplateEmpty()) {
			String scope = parserAssembler.getScope().getName();
			targetName= new QualifiedName(scope, qname.getScope(), qname.getTemplate());
			target = operandMap.getOperand(targetName);
			if (target == null)
				target = operandMap.addOperand(targetName);
			qname = targetName;
		} else {
			targetName= name(parserFunction.getLibrary(), true, false);
			target = operandMap.getOperand(targetName);
			if (target == null) {//&& qname.isGlobalName()) {
				if (!parserAssembler.getScope().getName().equals(QueryProgram.GLOBAL_SCOPE))
					target = parserAssembler.getScope().getGlobalParserAssembler().getOperandMap().getOperand(targetName);
				if (target == null) {
					QualifiedName scopeName;
					if (qname.isGlobalName())
					    scopeName = new QualifiedName(qname).toScopeName();
					else
					    scopeName = new QualifiedName(parserAssembler.getScope().getName(),parserFunction.getLibrary()); 
					target = operandMap.getOperand(scopeName);
					if (target != null) 
						qname = scopeName;
				}
			}
		}
		if (target != null) {
			Operand head = target.getHead();
			if (head == null)
				head = target;
			Class<?> objectClass = head.getOperator().getObjectClass();
			if (objectClass != null) {
	    		ObjectSpec objectSpec = new ObjectSpec(parserFunction.getFunctionName(), parserFunction.getParametersTemplate(), target);
	    		objectSpec.setObjectClass(objectClass);
	    		objectSpec.setReflexive(false);
	    		callOperand = new ObjectOperand(objectSpec);
			}
		}
		return callOperand;
    }

    /**
     * Process FlowQuery production
     * @param parserFunction Helper to collect function details
     * @return Operand object
     */
    private Operand flowQuery(ParserFunction parserFunction) {
        ParserAssembler parserAssembler = getParserAssembler();
        QualifiedName functionName = parserFunction.getName();
        Library library = new Library(functionName, parserAssembler.getScope());
        InnerQueryLauncher queryLauncher;
        ParserTask parserTask;
        QueryEvaluator queryEvaluator = 
            new QueryEvaluator(functionName.toString(), library);
        parserTask = parserAssembler.addPending(queryEvaluator);
        queryLauncher = queryEvaluator;
        parserTask.setPriority(ParserTask.Priority.list.ordinal());
        CallOperand callOperand = parserAssembler.getQueryOperand(queryLauncher, parserFunction);
        if (parserFunction.getReceiver() == null) {
        	Runnable unresolvedHandler = new Runnable() {

				@Override
				public void run() {
					Variable var = new Variable(parserAssembler.getContextName(parserFunction.getLibrary()));
			    	ObjectSpec objectSpec = new ObjectSpec(parserFunction.getFunctionName(), parserFunction.getParametersTemplate(), var);
			    	objectSpec.setReflexive(false);
			    	callOperand.setLeftOperand(new ObjectOperand(objectSpec));
			    	callOperand.setRightOperand(var);
				}
        	};
        	queryEvaluator.setUnresolvedHandler(unresolvedHandler);
        } 
        return callOperand;
    }

	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

	/**
	 * Process Name production
	 * @param name Identifier
	 * @param isContextName Flag set true to incorporate context details
	 * @param isDelaration Is part of a declaration
	 * @return QualifiedName object
	 */
	private QualifiedName name(String name, boolean isContextName, boolean isDeclaration) {
	    return getParserAssembler().name(name, isContextName, isDeclaration);
	}

}
