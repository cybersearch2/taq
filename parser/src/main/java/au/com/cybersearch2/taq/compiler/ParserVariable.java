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

import java.util.Currency;
import java.util.Locale;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.VariableArtifact;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.ComplexOperand;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.CountryCode;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.TaqLiteral;
import au.com.cybersearch2.taq.list.ListParserRunner;

/**
 * Parser helper for creating variable artifacts
 */
public class ParserVariable implements VariableArtifact {

	/** Parser context */
	private final ParserContext parserContext;
	
	private ParserCursor parserCursor;

	/**
	 * Construct ParserVariable object
	 * @param parserContext Parser context
	 */
	public ParserVariable(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	@Override
	public IVariableSpec termList() {
		VariableSpec varSpec = new VariableSpec(OperandType.TERM);
		return varSpec;
	}
 
    /**
     * Process Type production
     * @param literal Literal type
     * @param qualifier Optional parameter
     * @param qualifierName Qualifier resolved to qualified name or null
     * @return VariableSpec object
     */
	@Override
    public VariableSpec variableSpec(TaqLiteral literal, String qualifier, QualifiedName qualifierName, String source) {
    	VariableSpec variableSpec = VariableSpec.variableSpec(literal);
        if ((qualifier != null) && !qualifier.isEmpty()) { 
        	String currencyCode = null;
        	String localeString = null;
        	int pos = qualifier.indexOf('@');
        	if (pos != -1) {
        		if (pos == 0)
        			localeString = qualifier.substring(1);
        		else {
        			currencyCode = qualifier.substring(0, pos);
        			localeString = qualifier.substring(pos + 1);
        		}
        	} else
        		currencyCode = qualifier;
        	if (currencyCode != null) {
        	    Currency currency = CountryCode.getCurrency(qualifier);
        	    if (currency == null)
        	    	throw new CompilerException(String.format("Unknown Currency code %s", qualifier));
        	    variableSpec.setCurrency(currency);
        	}
        	if (localeString != null) {
        		String[] parts = localeString.split("_");
        		if (parts.length != 2)
    	    	    throw new CompilerException(String.format("Invalid locale format %s", localeString));
        		variableSpec.setLocale(new Locale(parts[0], parts[1]));
        	}
        } else if (qualifierName != null) {
	      	Operand operand = getOperandMap().addOperand(qualifierName);
	       	variableSpec.setQualifierOperand(operand);
        } 
        variableSpec.setSource(source);
    	return variableSpec;
    }

	/**
	 * Returns list variable or cursor for same depending on "isCursor" flag
	 * @param listName Name of variable
	 * @param varSpec List type specification
	 * @param isCursor Flag set true if list is referenced by a cursor
	 * @param isReverse Flag set true if cursor operates in reverse
	 * @param function Call operand, if assigned, else null
	 * @return Operand object -  either ListOperand or CursorSentinalOperand
	 */
	@Override
	public Operand createListVariable(String listName, IVariableSpec varSpec, boolean isCursor, boolean isReverse, IOperand function) {
    	ParserAssembler parserAssembler = getParserAssembler();
    	VariableFactory variableFactory = new VariableFactory((VariableSpec)varSpec);
    	QualifiedName listQname = QualifiedName.parseName(listName, parserAssembler.getQualifiedContextname());
		ListOperand<?> listVariable;
		if (function != null)
			listVariable = variableFactory.getDynamicListOperandInstance(listQname, (Operand)function);
		else
			listVariable = (ListOperand<?>)variableFactory.getItemListInstance(listQname, true);
 		registerItemList((ItemList<?>)listVariable);
 		parserAssembler.getOperandMap().addOperand(listVariable);
		if (isCursor) {
			if (parserCursor == null)
				parserCursor = new ParserCursor(parserContext);
			Operand operand = parserCursor.createCursorSentinalOperand(null, listVariable.getQualifiedName(), listVariable.getQualifiedName(), isReverse);
		    operand.setPrivate(false);
		    operand.setLeftOperand(listVariable);
		    return operand;
		} else
		    return listVariable;
	}

	/**
	 * Returns cursor sentinel bound to list of given name
	 * @param listName Name of variable
	 * @param varSpec List type specification
	 * @param target Qualified name of bound list
	 * @param isReverse Flag set true if cursor operates in reverse
	 * @return CursorSentinalOperand
	 */
	@Override
	public Operand createCursorSentinel(String listName, IVariableSpec varSpec, QualifiedName target,
			boolean isReverse) {
		Variable var = new Variable(target);
		Operand operand = createListVariable(listName, varSpec, true, isReverse, var);
		// Set Dynamic operand list left operand to variable to receive bound list
		operand.getLeftOperand().setLeftOperand(var);
		return operand;
	}

    /**
     * Returns list variable to access target
	 * @param target Qualified name of actual list
	 * @param isExport Flag set true if list to be exported
     * @return Operand object
     */
	@Override
	public Operand getListVariable(QualifiedName target, boolean isExport) {
    	// Expect early binding to work as referenced list should be declared 
    	// before being referenced
        ListParserRunner parserRunner = new ListParserRunner(target);
        parserRunner.run(getParserAssembler());
        ItemList<?> itemList = parserRunner.getItemList();
        if (itemList == null) 
            throw new CompilerException(String.format("List %s not found", target.toString()));
        if (isExport)
        	itemList.setPublic(true);
		return ListOperand.listOperandInstance(itemList);
    }

    /**
     * Process VariableDeclaration production
     * @param name Name
     * @param expression Optional assignment expression
     * @param isUntyped Flag set true if type is not specified
     * @param varType Variable type or null if untyped
     */
	@Override
    public void variableDeclaration(String name, IOperand expression, boolean isUntyped, IVariableSpec varSpec) {
    	Operand var = null;
    	VariableSpec specification;
    	ParserAssembler parserAssembler = getParserAssembler();
        if (isUntyped)
        	specification = new VariableSpec(OperandType.UNKNOWN);
        else {
        	specification = (VariableSpec)varSpec;
            if (specification.getOperandType() == OperandType.COMPLEX) {
        	    QualifiedName qname = parserAssembler.getContextName(name);
        	    var = new ComplexOperand(qname, (Operand)expression);
            }
        }
        if (var == null) {
	        if (expression != null)  
	        	specification.setExpression((Operand)expression);
	        VariableFactory variableFactory = new VariableFactory(specification);
	        var = variableFactory.getContextInstance(name, parserAssembler);
        }
        getOperandMap().addOperand(var);
        QualifiedName qname = parserContext.getTemplateName();
     	if (qname.getTemplate().equals(Scope.SCOPE))
     		// Scope variables need to be backed out at end of query
    		parserAssembler.getTemplateAssembler().addTemplate(qname, var);
    }

	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

	/**
	 * Returns operand Map
	 * @return OperandMap object
	 */
	private OperandMap getOperandMap() {
		return parserContext.getParserAssembler().getOperandMap();
	}

    /**
     * Add ItemList object to it's assembly container
     * @param itemList ItemList object to add
     */
	private void registerItemList(ItemList<?> itemList) {
       	getParserAssembler().getListAssembler().addItemList(itemList.getQualifiedName(), itemList);
    }

}
