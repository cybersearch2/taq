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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.ChoiceArtifact;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.ChoiceOperand;
import au.com.cybersearch2.taq.expression.ListMapper;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Choice;
import au.com.cybersearch2.taq.pattern.ChoiceParameters;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * Parser helper to collect content of select declaration or map term
 *  
 * @author Andrew Bowley
 */
public class ParserChoice implements ChoiceArtifact {

	/** Name of axiom list containing available choices */
    private final QualifiedName qualifiedAxiomName;
	/** The published choice name - either value of 'name' or 'alias' field */
	private final String choiceName;
	/** Applies compiler logic to the token stream */
	private final Compiler compiler;
	/** Flag set true if this specification is for a map - selections are key/value pairs */
	private final boolean isMap;
	
	/** Flag set true if a select is declared in a scope and false if declared in a template or is a map */
	private boolean isScopeContext;
	/** Template to evaluate the selection */
	private ITemplate template;
	/** Name of evaluation template */
	private QualifiedName qualifiedTemplateName;
	/** Choice qualified name */
	private QualifiedName choiceQname;
    /** Term list name for matching to terms in a context-scoped axiom */
    private QualifiedName listQname;
    /** Parser helper for creating axiom artifacts */
    private ParserAxiom parserAxiom;
    /** Sets operand to evaluate value to be mapped */
    private Operand valueOperand;
    

    /**
     * Construct ParserChoice object
	 * @param compiler Applies compiler logic to the parser token stream
     * @param name Name of the term to receive the selection value
     * @param alias Name of template term or empty string if same as 'name'
     * @param isMap Flag set true if this specification is for a map
     */
	public ParserChoice(Compiler compiler, String choiceName, boolean isMap) {
		this.compiler = compiler;
		this.choiceName = choiceName;
		this.isMap = isMap;
        ParserContext parserContext = compiler.getParserContext();
		ParserAssembler parserAssembler = parserContext.getParserAssembler();
		TemplateAssembler templateAssembler = parserAssembler.getTemplateAssembler();
		ListAssembler listAssembler = parserAssembler.getListAssembler();
		// The tenplate name is formed by attaching a suffix to the choice name
		String choiceTemplateName = choiceName + Choice.TEMPLATE_SUFFIX;
		qualifiedTemplateName = parserAssembler.getContextName(choiceTemplateName);
		// The scope and template parts of qualified name scopes conform to the context of the choice declaration
		isScopeContext = qualifiedTemplateName.getTemplate().isEmpty();
        if (isScopeContext)
        	qualifiedTemplateName = new QualifiedTemplateName(qualifiedTemplateName.getScope(), choiceTemplateName);
        // Search for the template and if not found, create it
		template = templateAssembler.getTemplate(qualifiedTemplateName);
		if (template == null) {
			QualifiedName scopeName = 
				new QualifiedTemplateName(qualifiedTemplateName.getScope(), choiceTemplateName);
			if (!isScopeContext) {
			    template = templateAssembler.getTemplate(scopeName);
			}
			if (template == null) {
		        template = templateAssembler.createChoiceTemplate(qualifiedTemplateName, choiceName);
			} else {
				qualifiedTemplateName = scopeName;
				if (!choiceName.equals(template.getKey()))
				     throw new CompilerException(String.format("Scope context choice name is %s not %s", template.getKey(), choiceName));
				isScopeContext = true;
			}
		}
		// The axiom name is formed by attaching a suffix to the choice name
		if (isScopeContext)
			qualifiedAxiomName = new QualifiedName(qualifiedTemplateName.getScope(), choiceName + Choice.AXiOM_SUFFIX);
        else
			qualifiedAxiomName = parserAssembler.getContextName(choiceName + Choice.AXiOM_SUFFIX);
		// Create the axiom list containing available choices
		listAssembler.createAxiomItemList(qualifiedAxiomName, false);
		// Register this object with parser assembler. It is needed to create a choice operand.
		choiceQname = parserAssembler.getContextName(ChoiceArtifact.CHOICE_PREFIX + choiceName);
		parserAssembler.putChoiceSpec(choiceQname, this);
        VariableSpec varSpec = new VariableSpec(OperandType.TERM);
        varSpec.setAxiomKey(choiceQname);
        VariableFactory variableFactory = new VariableFactory(varSpec);
        ItemList<?> itemList = variableFactory.getItemListInstance(choiceQname, false);
        parserAssembler.getListAssembler().addItemList(itemList.getQualifiedName(), itemList);
        if (isMap)
        	addFirstTermName();
	}

	/**
	 * Check the name of the first axiom term name to see if it is a context term list
	 * @param name Term name
	 */
	@Override
	public void analyseFirstTermName(String name) {
	    IVariableSpec varSpec = compiler.getParserContext().getVariableSpec(name);
	    if ((varSpec != null) && (varSpec.getOperandType() == OperandType.TERM))
	    	listQname = new QualifiedName(Scope.SCOPE, QualifiedName.EMPTY, name);
	}

	/**
	 * Returns name of the term to receive the selection value
	 * @return name
	 */
	@Override
	public String getName() {
		return choiceName;
	}

	/**
	 * Returns qualified name of this Choice
	 * @return QualifiedName object
	 */
	@Override
	public QualifiedName getQualifiedName() {
		return choiceQname;
	}

	/**
	 * Returns qualified name of list used for matching to context-scoped axiom terms
	 * @return QualifiedName name object
	 */
	@Override
	public QualifiedName getListQname() {
		return listQname;
	}

	/** Returns flag set true if choice declared in a scope and false if declared in a template 
	 * @return boolean
	 */
	@Override
	public boolean isScopeContext() {
		return isScopeContext;
	}

	/**
	 * Returns qualified name of axiom list containing available choices
	 * @return QualifiedName object
	 */
	@Override
	public QualifiedName getQualifiedAxiomName() {
		return qualifiedAxiomName;
	}

	/**
	 * Returns qualified name of evaluation template 
	 * @return QualifiedName object
	 */
	@Override
	public QualifiedName getQualifiedTemplateName() {
		return qualifiedTemplateName;
	}

	/**
	 * Returns evaluation template
	 * @return Template object
	 */
	@Override
	public ITemplate getTemplate() {
		return template;
	}

	/**
	 * Returns choice name
	 * @return name or alias, if defined
	 */
	@Override
	public String getChoiceName() {
		return choiceName;
	}

	/**
	 * Returns flag set true if choice is map (just key/value pairing)
	 * @return boolean
	 */
	@Override
	public boolean isMap() {
		return isMap;
	}
	
	/**
	 * Sets operand to evaluate value to be mapped
	 * @param valueOperand Operand to provide value
	 */
	@Override
	public void setValueExpression(IOperand valueOperand) {
		this.valueOperand = (Operand) valueOperand;
	}
	
	/**
	 * Returns operand to select from key/value mappings
	 * @param qname Qualified name
	 * @param owner Template containing this Map
	 * @param isList Flag set true if list map
	 * @return Operand object
	 */
	@Override
	public Operand getMap(QualifiedName qname, ITemplate owner, boolean isList) {
		Template template = (Template)owner;
		ParserAssembler parserAssembler = compiler.getParserAssembler();
		ListAssembler listAssembler = parserAssembler.getListAssembler();
	    OperandMap operandMap = parserAssembler.getOperandMap();
    	QualifiedName contextName = parserAssembler.getQualifiedContextname();
    	String[] parts = contextName.getScope().split("@");
	    // Create operand list to hold the term operand as the first item, and following if applicable, the selection operands
        Operand operand;
        QualifiedName topName;
    	if (parts.length > 1)
    		topName = new QualifiedName(parts[0], parts[1], getName());
    	else
    		topName = new QualifiedName(getName(), contextName);
    	operand = operandMap.get(topName);
    	if (operand == null)
            operand = operandMap.addOperand(getName(), contextName);
	    List<Operand> operandList = new ArrayList<>();
	    operandList.add(operand);
    	QualifiedName contextListName;
    	if (parts.length > 1)
    		contextListName = new QualifiedName(parts[0], parts[1], qname.getName());
    	else
    		contextListName = new QualifiedName(qname.getName(), contextName);
	    operandList.add(new Variable(QualifiedName.parseName(qname.getName(), contextListName)));
	    // Create object to hold choice parameters
	    ChoiceParameters choiceParams = new ChoiceParameters(parserAssembler.getScope(), template.getQualifiedName(), operandList);
	   	QualifiedName choiceTemplateName = getQualifiedTemplateName();
	    Template choiceTemplate = parserAssembler.getTemplateAssembler().createChoiceTemplate(template, choiceTemplateName);
	    template.setNext(choiceTemplate);
	   	// Create return operand
	   	Choice choice = new Choice(this, choiceParams);
        ChoiceOperand choiceOperand;
	    if (isList) { // Is list map. with each alternative a list reference
            ListOperand<?> listOperand = (ListOperand<?>) operandMap.getOperand(qname);
            if (listOperand == null) {
			   	Map<QualifiedName,ItemList<?>> itemListMap = createItemListMap(choice.getChoiceAxiomList());
		      	OperandType operandType = itemListMap.values().iterator().next().getOperandType();
		    	VariableSpec variableSpec = new VariableSpec(operandType);
		    	variableSpec.setAxiomKey(qname);
		        VariableFactory variableFactory = new VariableFactory(variableSpec);
		   	    QualifiedName choiceOpName = new QualifiedName(qname.getName() + qname.incrementReferenceCount(), qname);
		   	    choiceOperand = new ChoiceOperand(choiceOpName, choiceTemplate, choice);
		   	    QualifiedName mapperOpName = new QualifiedName(qname.getName() + qname.incrementReferenceCount(), qname);
		   	    ListMapper mapper = new ListMapper(mapperOpName, choiceOperand,itemListMap );
		   	    listOperand = variableFactory.getDynamicListOperandInstance(qname, mapper);
		   	    listOperand.setPrivate(true);
		        listAssembler.addItemList(qname, listOperand);
		     	operandMap.addOperand(listOperand);
            }
	   	    return listOperand;
	    } else {
	        Operand target = operandMap.get(qname);
	        if (target != null)
		    	choiceOperand =  new ChoiceOperand(qname, choiceTemplate, choice, target);
	        else {
		        choiceOperand = new ChoiceOperand(qname, choiceTemplate, choice);
		     	operandMap.addOperand(choiceOperand);
		    }
	        choiceOperand.setLeftOperand(getValueOperand());
            return choiceOperand;
	    }
	}

	private Operand getValueOperand() {
		return valueOperand;
	}

	/**
	 * Sets the name of the first axiom term name which implicitly is the 'name' or 'alias' value, as appropriate.
	 * The remaining term names are included in the choice declaration.
	 */
	private void addFirstTermName() {
		String firstTermName = choiceName;
		if (parserAxiom == null)
		    parserAxiom = new ParserAxiom(compiler.getParserContext());
		parserAxiom.addAxiomTermName(qualifiedAxiomName, firstTermName);
	}

    /**
     * Returns map containing qualified name keys and item list values
     * @param choiceAxiomList List of axioms, with each axiom representing one row of the choice
     * @return Map object
     */
    private Map<QualifiedName,ItemList<?>> createItemListMap(List<Axiom> choiceAxiomList) {
    	ParserAssembler parserAssembler = compiler.getParserAssembler();
    	ListAssembler listAssembler = parserAssembler.getListAssembler();
	   	// Establish if handling for an appender term is required
	   	Map<QualifiedName,ItemList<?>> listOpMap = null;
	   	for (Axiom axiom: choiceAxiomList) {
	   		for (int i = 0; i < axiom.getTermCount(); ++i) {
	   			Term term = axiom.getTermByIndex(i);
	   			if (term.getValue() instanceof QualifiedName) {
	   				
		        	if (listOpMap == null)
		        		listOpMap = new HashMap<>();
		        	QualifiedName listName = (QualifiedName)term.getValue();
		        	ItemList<?> itemList = listAssembler.findItemList(listName);
		        	listOpMap.put(listName, itemList);
	   			}
	   		}
	   	};
	   	if (listOpMap == null)
	   		listOpMap = Collections.emptyMap();
       	return listOpMap;
    }

}
