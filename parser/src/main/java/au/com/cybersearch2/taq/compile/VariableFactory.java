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

import java.math.BigDecimal;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compiler.CompilerException;
import au.com.cybersearch2.taq.expression.AxiomOperand;
import au.com.cybersearch2.taq.expression.BigDecimalOperand;
import au.com.cybersearch2.taq.expression.BooleanOperand;
import au.com.cybersearch2.taq.expression.ComplexOperand;
import au.com.cybersearch2.taq.expression.CountryOperand;
import au.com.cybersearch2.taq.expression.DoubleOperand;
import au.com.cybersearch2.taq.expression.DynamicListOperand;
import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.StringOperand;
import au.com.cybersearch2.taq.expression.TermOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.interfaces.AxiomListListener;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.RightOperand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.list.ArrayItemList;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.list.DynamicList;
import au.com.cybersearch2.taq.operator.CurrencyOperator;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;

public class VariableFactory {

	/** Contains parameters used to create a new variable */
	private final VariableSpec variableSpec;

	/**
	 * Creates variables according to given specification
	 * @param variableSpec Variable specification
	 */
	public VariableFactory(VariableSpec variableSpec) {
		this.variableSpec = variableSpec;
	}

	/**
	 * Return new Operand instance of this type
     * @param name Name of new variable
     * @param parserAssembler Parser assembler
	 * @return Operand object
	 */
	public Operand getContextInstance(String name, ParserAssembler parserAssembler)	{
	    return getExpressionInstance(parserAssembler.getContextName(name), parserAssembler);
	}

    /**
     * Return new Operand instance of this type with expression operand on left
     * @param qname Qualified name of new variable
     * @param parserAssembler Parser assembler
     * @return Operand object
     */
    public Operand getInstance(QualifiedName qname, ParserAssembler parserAssembler) {
        return getInstance(qname, null, parserAssembler);
    }
    
   /**
     * Return new Operand instance of this type with expression operand on left
     * @param qname Qualified name of new variable
     * @return Operand object
     */
    public Operand getExpressionInstance(QualifiedName qname, ParserAssembler parserAssembler) {
        return getInstance(qname, variableSpec.getExpression(), parserAssembler);
    }
 
    /**
	 * Returns ItemList object for this type in current scope. 
	 * NOTE: AxiomKey property must be set for Term, Axiom or Local type
     * @param listName Name of new variable
	 * @return ItemList object
	 * @throws CompilerException if list type is unknown
	 */
  	public ItemList<?> getItemListInstance(String listName, QualifiedName qualifiedContextname) throws CompilerException {
        QualifiedName qualifiedListName = QualifiedName.parseName(listName, qualifiedContextname);
  	    return getItemListInstance(qualifiedListName, false);
	}
  	
    /**
	 * Returns logic programming type enumeration
	 * @return the operandType
	 */
	public OperandType getOperandType() {
		return variableSpec.getOperandType();
	}

	/**
	 * Set type to UNKNOWN to force Variable instance creation
	 * Returns self
	 * @return VariableType object
	 */
	public VariableFactory setUnknownType()	{
		variableSpec.setUnknownType(); 
	    return this;
	}
	
    /**
     * Return new Operand instance of this type
     * @param qname Qualified name of new variable
     * @param expression Optional expression to set variable on evaluation
     * @param parserAssembler Parser assembler
     * @return Operand object
     */
    public Operand getInstance(QualifiedName qname, Operand expression, ParserAssembler parserAssembler) {
        boolean hasExpression = expression != null;
		Operand operand = null;
		Operand assignExpression = null;
        QualifiedName axiomKey = null;
        AxiomListListener axiomListListener = null;
        ItemList<?> itemList = null;
        if (variableSpec.isAxiom() || variableSpec.isSetList() || variableSpec.isTerm()) {
            axiomKey = variableSpec.getAxiomKey();
            if (axiomKey == null)
                axiomKey = qname;
            axiomListListener = parserAssembler.getListAssembler().axiomListListenerInstance();
            ListAssembler listAssembler = parserAssembler.getListAssembler();
            itemList = listAssembler.getItemList(axiomKey);
        }
        OperatorEnum reflexOp = variableSpec.getReflexOp();
        if (reflexOp != null) {
            assignExpression = expression;
            expression = null;
        }
	    
        switch (variableSpec.getOperandType())
	    {
	    case INTEGER:
	    	operand = !hasExpression ? new IntegerOperand(qname) : new IntegerOperand(qname, expression);
	    	break;
        case DOUBLE:
        	operand = !hasExpression ? new DoubleOperand(qname) : new DoubleOperand(qname, expression);
	    	break;
        case BOOLEAN:
        	operand = !hasExpression ? new BooleanOperand(qname) : new BooleanOperand(qname, expression);
	    	break;
        case STRING:
        	operand = !hasExpression ? new StringOperand(qname) : new StringOperand(qname, expression);
	    	break;
        case DECIMAL:
        	operand = !hasExpression ? new BigDecimalOperand(qname) : new BigDecimalOperand(qname, expression);
	    	break;
        case TERM:
            operand = new TermOperand(new AxiomTermListEvaluator((AxiomTermList)itemList, (Template)variableSpec.getTemplate()));
            break;
        case SET_LIST:
        	if (!variableSpec.isListUnitialized())
            	operand = createSetList((AxiomList)itemList, axiomListListener );
        	else {
        		AxiomList axiomList;
        		if (itemList instanceof AxiomList)
        			axiomList = (AxiomList)itemList; 
        		else {
        			AxiomTermList axiomTermList = (AxiomTermList)itemList;
        			axiomList = new AxiomList(axiomTermList.getQualifiedName(), axiomTermList.getKey());
        			axiomList.append(axiomTermList.getAxiom());
        		}
                operand = new AxiomOperand(axiomList, axiomListListener);
       		}
         	break;
        case AXIOM:
            operand = new AxiomOperand((AxiomList)itemList, axiomListListener);
            break;
        case CURRENCY:
        {
            BigDecimalOperand currencyOperand = !hasExpression ? new BigDecimalOperand(qname) : new BigDecimalOperand(qname, expression);
            currencyOperand.setOperator(getCurrencyOperator(currencyOperand));
            operand = currencyOperand;
	    	break;
        }
        case COMPLEX:
        	operand = new ComplexOperand(qname, expression);
        	break;
        case UNKNOWN: 	
        default:
        	operand = !hasExpression ? new Variable(qname) : new Variable(qname, expression);
	    }
	    if (operand instanceof LocaleListener)
	    	parserAssembler.registerLocaleListener((LocaleListener) operand);
	    Operand literalOperand = variableSpec.getLiteral();
	    if (literalOperand != null)
	        operand.assign(new Parameter(Term.ANONYMOUS, literalOperand.getValue()));
	    if (assignExpression != null) {
	        Variable var = new Variable(qname, assignExpression);
	        var.setReflexOp(reflexOp, operand);
	        operand = var;
	    }
	    return operand;
    }

   /**
    * Returns ItemList object for this type. 
    * NOTE: AxiomKey property must be set for Term, Axiom type
    * @param qname Qualified name of new variable
    * @return ItemList object
    * @throws CompilerException if list type is unknown
    */
   public ItemList<?> getItemListInstance(QualifiedName qname, boolean isOperand) throws CompilerException {
		QualifiedName axiomKey = null;
		if (variableSpec == null)
			throw new CompilerException("Type is not specified for variable " + qname.toString());
        if (variableSpec.isAxiom() || variableSpec.isSetList() || variableSpec.isTerm()) {
        	if (!variableSpec.axiomKeyExists())
                //throw new CompilerException("List " + qname.toString() + " missing axiom key");
        		axiomKey = qname;
        	else
			    axiomKey = variableSpec.getAxiomKey();
        }
		ItemList<?> itemList = null;
		OperandType operandType = variableSpec.getOperandType();
	    switch (operandType)
	    {
        case INTEGER:
        	itemList = createIntegerList(qname, isOperand);
            break; 
        case DOUBLE:
        	itemList = createDoubleList(qname, isOperand);
            break;
        case BOOLEAN:
        	itemList = createBooleanList(qname, isOperand);;
            break; 
        case STRING:
        	itemList = createStringList(qname, isOperand);
            break; 
        case DECIMAL:
        case CURRENCY:
        	itemList = createDecimalList(qname, isOperand, operandType);
            break;
        case TERM:
            itemList = createTermList(qname, axiomKey, isOperand);
            break;
        case AXIOM:
        case SET_LIST:
            itemList = createAxiomList(qname, axiomKey, isOperand);
            break;
        case UNKNOWN:   
        	if (isOperand) {
        		itemList = createEmptyListOperand(qname);
        		break;
        	}
        default:
            throw new CompilerException("List " + qname.toString() + " type unknown");
       }
       return itemList;
   }
   /**
    * Returns ItemList object for this type. 
    * NOTE: AxiomKey property must be set for Term, Axiom type
    * @param qname Qualified name of new variable
    * @return ItemList object
    * @throws CompilerException if list type is unknown
    */
   public DynamicListOperand<?> getDynamicListOperandInstance(QualifiedName qname, Operand operand) throws CompilerException {
		QualifiedName axiomKey = null;
		if (variableSpec == null)
			throw new CompilerException("Type is not specified for variable " + qname.toString());
        if (variableSpec.isAxiom() || variableSpec.isSetList() || variableSpec.isTerm()) {
        	if (!variableSpec.axiomKeyExists())
                //throw new CompilerException("List " + qname.toString() + " missing axiom key");
        		axiomKey = qname;
        	else
			    axiomKey = variableSpec.getAxiomKey();
        }
        DynamicListOperand<?> listOperand = null;
		OperandType operandType = variableSpec.getOperandType();
	    switch (operandType)
	    {
        case INTEGER:
        	listOperand = createIntegerList(qname, operand);
            break; 
        case DOUBLE:
        	listOperand = createDoubleList(qname, operand);
            break;
        case BOOLEAN:
        	listOperand = createBooleanList(qname, operand);;
            break; 
        case STRING:
        	listOperand = createStringList(qname, operand);
            break; 
        case DECIMAL:
        case CURRENCY:
        	listOperand = createDecimalList(qname, operand, operandType);
            break;
        case TERM:
        	listOperand = createTermList(qname, axiomKey, operand);
            break;
        case AXIOM:
        case SET_LIST:
        	listOperand = createAxiomList(qname, axiomKey, operand);
            break;
        case UNKNOWN:   
        default:
            throw new CompilerException("List " + qname.toString() + " type unknown");
       }
       return listOperand;
   }
 	
	
   /**
    * Macro to convert a literal value and return the result in a parameter
    * @param literal Literal value contained in an anonymous parameter
    * @param scope The scope context
    * @return Operand object
    * @throws CompilerException if type not supported
    */
    public Operand getParameter(final Parameter literal, Scope scope) throws CompilerException {
        Operand operand = null;
        final QualifiedName qname = QualifiedName.ANONYMOUS;
 		OperandType operandType = variableSpec.getOperandType();
        switch (operandType)
        {
	       case INTEGER: {
	       	if (literal.getValueClass() == Long.class)
	           operand = new IntegerOperand(qname, (Long)literal.getValue());
	       	else
	           operand = new IntegerOperand(qname, literal.getValue().toString());
	           break;
	       }
	       case DOUBLE: {
		       if (literal.getValueClass() == Double.class)
	               operand = new DoubleOperand(qname, (Double)literal.getValue());
	           else
	               operand = new DoubleOperand(qname, literal.getValue().toString());
	           break;
	       }
	       case BOOLEAN: {
	           operand = new BooleanOperand(qname, getBoolean(literal));
	           break;
	       }
	       case STRING: {
	           operand = new StringOperand(qname, literal.getValue().toString());
	           break;
	       }
	       case DECIMAL: {
	    	   if ((literal.getValueClass() == String.class) ||
	    		   (literal.getValueClass() == Double.class))
		           operand = new BigDecimalOperand(qname, literal.getValue().toString());
	    	   else
	               operand = new BigDecimalOperand(qname, getBigDecimal(literal));
	           break;
	       }
	       case CURRENCY: {
	           BigDecimalOperand currencyOperand = 
	        		literal.getValueClass() == String.class ?
	        			new BigDecimalOperand(qname, literal.getValue().toString()) :
				        new BigDecimalOperand(qname, getBigDecimal(literal));
	           currencyOperand.setOperator(getCurrencyOperator(currencyOperand));
	           operand = currencyOperand;
	           break;
	       }
	       default:
	           throw new CompilerException(operandType.toString() + " is not a literal type");
        }
        if ((operandType != OperandType.CURRENCY) || !variableSpec.existsCurrency())
            ((LocaleListener)operand).onScopeChange(scope);
            operand.evaluate(0);
        return operand;
    }

    /**
     * Returns Dynamic ItemList object for this type. 
     * @param qname Qualified name of new variable
     * @param template Initialization template
     * @return ItemList object
     * @throws CompilerException if list type does not permit initialization with list of values
     */
    public ItemList<?> getDynamicListInstance(QualifiedName qname, Template template) throws CompilerException {
        DynamicList<?> dynamicListList = null;
        OperandType operandType = variableSpec.getOperandType();
        switch (operandType)
        {
        case INTEGER:
            dynamicListList = new DynamicList<Long>(operandType, qname, template);
            break; 
        case DOUBLE:
            dynamicListList = new DynamicList<Double>(operandType, qname, template);
            break;
        case BOOLEAN:
            dynamicListList = new DynamicList<Boolean>(operandType, qname, template);
            break; 
        case STRING:
            dynamicListList = new DynamicList<String>(operandType, qname, template);
            break; 
        case DECIMAL:
        case CURRENCY:
            dynamicListList = new DynamicList<BigDecimal>(operandType, qname, template);
            break;
        default:
            throw new CompilerException("List " + qname.toString() + " type does not permit initialization with list of values");
        }
        return dynamicListList;
    }

    private ItemList<?> createEmptyListOperand(QualifiedName qname) {
 		return new ListOperand<Unknown>(new ArrayItemList<Unknown>(OperandType.UNKNOWN, qname));
	}

    private ItemList<Long> createIntegerList(QualifiedName qname, boolean isOperand) {
   	    ArrayItemList<Long> arrayItemList = new ArrayItemList<Long>(OperandType.INTEGER, qname);
     	if (isOperand)
   	    	return new ListOperand<Long>(arrayItemList);
      	else
   	    	return arrayItemList;
	}

    private ItemList<Double> createDoubleList(QualifiedName qname, boolean isOperand) {
	   	ArrayItemList<Double> arrayItemList = new ArrayItemList<Double>(OperandType.DOUBLE, qname);
	   	if (isOperand)
	   		return new ListOperand<Double>(arrayItemList);
	   	else
	   		return arrayItemList;
	}

    private ItemList<Boolean> createBooleanList(QualifiedName qname, boolean isOperand) {
	   	ArrayItemList<Boolean> arrayItemList = new ArrayItemList<Boolean>(OperandType.BOOLEAN, qname);
	   	if (isOperand)
	   		return new ListOperand<Boolean>(arrayItemList);
	   	else
	   		return arrayItemList;
		}

   private ItemList<String> createStringList(QualifiedName qname, boolean isOperand) {
	   	ArrayItemList<String> arrayItemList = new ArrayItemList<String>(OperandType.STRING, qname);
	   	if (isOperand)
	   		return new ListOperand<String>(arrayItemList);
	   	else
	   		return arrayItemList;
		}

   private ItemList<BigDecimal> createDecimalList(QualifiedName qname, boolean isOperand, OperandType operandType) {
	   	ArrayItemList<BigDecimal> arrayItemList = new ArrayItemList<BigDecimal>(operandType, qname);
	   	if (isOperand)
	   		return new ListOperand<BigDecimal>(arrayItemList);
	   	else
	   		return arrayItemList;
		}

   private ItemList<Term> createTermList(QualifiedName qname, QualifiedName axiomKey, boolean isOperand) {
	   	ItemList<Term> axiomTermList= new AxiomTermList(qname, axiomKey);
	   	if (isOperand)
	   		return new ListOperand<Term>(axiomTermList);
	   	else
	   		return axiomTermList;
		}

    private ItemList<Axiom> createAxiomList(QualifiedName qname, QualifiedName axiomKey, boolean isOperand) {
	   	ItemList<Axiom> axiomList= new AxiomList(qname, axiomKey);
	   	if (isOperand)
	   		return new ListOperand<Axiom>(axiomList);
	   	else
	   		return axiomList;
		}

    private DynamicListOperand<Long> createIntegerList(QualifiedName qname, Operand operand) {
    	ArrayItemList<Long> arrayItemList = new ArrayItemList<Long>(OperandType.INTEGER, qname);
    	return new DynamicListOperand<Long>(arrayItemList, operand);
	}

    private DynamicListOperand<Double> createDoubleList(QualifiedName qname, Operand operand) {
    	ArrayItemList<Double> arrayItemList = new ArrayItemList<Double>(OperandType.DOUBLE, qname);
   		return new DynamicListOperand<Double>(arrayItemList, operand);
	}

    private DynamicListOperand<Boolean> createBooleanList(QualifiedName qname, Operand operand) {
    	ArrayItemList<Boolean> arrayItemList = new ArrayItemList<Boolean>(OperandType.BOOLEAN, qname);
   		return new DynamicListOperand<Boolean>(arrayItemList, operand);
    }
   
    private DynamicListOperand<String> createStringList(QualifiedName qname, Operand operand) {
	   ArrayItemList<String> arrayItemList = new ArrayItemList<String>(OperandType.STRING, qname);
   		return new DynamicListOperand<String>(arrayItemList, operand);
	}

    private DynamicListOperand<BigDecimal> createDecimalList(QualifiedName qname, Operand operand, OperandType operandType) {
	   ArrayItemList<BigDecimal> arrayItemList = new ArrayItemList<BigDecimal>(operandType, qname);
   		return new DynamicListOperand<BigDecimal>(arrayItemList, operand);
	}

    private DynamicListOperand<Term> createTermList(QualifiedName qname, QualifiedName axiomKey, Operand operand) {
	   ItemList<Term> axiomTermList= new AxiomTermList(qname, axiomKey);
		return new DynamicListOperand<Term>(axiomTermList, operand);
	}

    private DynamicListOperand<Axiom> createAxiomList(QualifiedName qname, QualifiedName axiomKey, Operand operand) {
    	ItemList<Axiom> axiomList= new AxiomList(qname, axiomKey);
   		return new DynamicListOperand<Axiom>(axiomList, operand);
	}

    private BigDecimal getBigDecimal(final Parameter literal) {
    	Class<?> clazz = literal.getValueClass();
    	if (clazz == Double.class)
    		return new BigDecimal(((Double)literal.getValue()).doubleValue());
    	else if (clazz == Long.class)
        	return new BigDecimal(((Long)literal.getValue()).doubleValue());
    	String text = literal.getValue().toString();
    	if (text.equals("0"))
    		return BigDecimal.ZERO;
    	else if (text.equals("1"))
    		return BigDecimal.ONE;
    	else if (text.equals("10"))
    		return BigDecimal.TEN;
       	return new BigDecimal(text);
    }
 
    private Boolean getBoolean(final Parameter literal) {
    	if (literal.getValueClass() == Boolean.class)
    	    return (Boolean)literal.getValue();
    	String text = literal.getValue().toString();
    	if (text.equalsIgnoreCase(Boolean.TRUE.toString()))
    		return Boolean.TRUE;
    	return Boolean.FALSE;
    }
    
    private Operand createSetList(AxiomList itemList, AxiomListListener axiomListListener) {
    	AxiomListEvaluator axiomListEvaluator =
                new AxiomListEvaluator(itemList, variableSpec.getTemplateList(), variableSpec.getTemplate());
        if (variableSpec.isExport())
            axiomListEvaluator.setPublic(true);
        return new AxiomOperand(axiomListEvaluator, axiomListListener);
	}

    /**
     * Returns operator for Currency Operand with country code set up according to
     * setting of QUALIFIER_STRING and QUALIFIER_OPERAND properties.
     * @param currencyOperand Currency operand, which has right operand assigned if QUALIFIER_OPERAND is set
     * @return CurrencyOperator object
     */
    private CurrencyOperator getCurrencyOperator(RightOperand currencyOperand) {
        CurrencyOperator currencyOperator = new CurrencyOperator();
        if (variableSpec.existsCurrency()) {
            currencyOperator.setCurrency(variableSpec.getCurrency());
        }
        if (variableSpec.existsLocale()) {
        	currencyOperator.setRegion(variableSpec.getLocale());
        } else {   // Country code set by evaluating a right operand assigned to the Currency Operand
            Operand countryOperand = variableSpec.getQualifierOperand();
            if (countryOperand != null)
            {
                currencyOperand.setRightOperand(
                    new CountryOperand(
                        countryOperand.getQualifiedName(), //countryQname, 
                        currencyOperator.getTrait(), 
                        countryOperand));
            }
        }
        return currencyOperator;
    }

}
