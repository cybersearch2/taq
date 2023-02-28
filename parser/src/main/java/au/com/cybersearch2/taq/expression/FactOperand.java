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
package au.com.cybersearch2.taq.expression;

import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.helper.EvaluationUtils;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.list.CursorList;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * FactOperand
 * @author Andrew Bowley
 * 20 Aug 2015
 */
public class FactOperand extends BooleanOperand
{
    /** Set only if cursor is target of fact check */
	private CursorList cursorList;
    /** Set only if item list is target of fact check */
	private ItemList<?> itemList;
    /** Set only if list operand is target of fact check */
	private ListOperand<?> listOperand;

	/**
	 * Construct Fact object
	 * @param expression Target of fact check 
	 */
    public FactOperand(Operand expression)
    {
        super(getFactName(expression), expression);
    }

	/**
	 * Construct Fact object for given cursor target
	 * @param cursorList Encapsulates a cursor and a reference to the list it operates ont
	 * @param expression Expression to postfix evaluate cursor 
	 */
    public FactOperand(CursorList cursorList, Operand postfixExpression)
    {
        super(CursorList.getPartName("fact", cursorList.getCursor()), postfixExpression);
        this.cursorList = cursorList;
    }

	/**
	 * Construct Fact object for given cursor target and expression
	 * @param cursorList Encapsulates a cursor and a reference to the list it operates ont
	 */
    public FactOperand(CursorList cursorList)
    {
        super(CursorList.getPartName("fact", cursorList.getCursor()), (Operand)null);
        this.cursorList = cursorList;
    }

   /**
     * Construct Fact object for given item list target
     * @param itemList Item list
     */
    public FactOperand(ItemList<?> itemList) {
        super(getPartName("fact", itemList.getQualifiedName()), (Operand)null);
        this.itemList = itemList;
	}
    
	/**
	 * Construct Fact object for give list operand
	 * @param listOperandt List operand
	 */
    public FactOperand(ListOperand<?> listOperand)
    {
        super(getFactName(listOperand), listOperand);
        this.listOperand = listOperand;
     }


	/**
     * Execute operation for expression
     * @param id Identity of caller, which must be provided for backup()
     * @return Flag set true if evaluation is to continue
     */
    @Override
    public EvaluationStatus evaluate(int id)
    {
    	if (itemList != null) {
    		if (itemList.getOperandType() == OperandType.TERM)
    			analyseValue(itemList);
    		else
                setValue(!itemList.isEmpty());
    	} else if (cursorList != null) {
            if (leftOperand != null)
        	    leftOperand.evaluate(id);
            setValue(cursorList.isFact());
        } else if (listOperand != null) {
    		if (listOperand.getOperandType() == OperandType.TERM)
    			setValue(analyseValue(listOperand.getValue()));
    		else
                setValue(!listOperand.isEmpty());
        } else if (leftOperand == null)
            setValue(false);
        else {
        	if (leftOperand.isEmpty())
        	    leftOperand.evaluate(id);
	        if (leftOperand.isEmpty())
	            setValue(false);
	        else
	        {
	        	setValue(analyseValue(leftOperand.getValue()));
	        }
        }
        this.empty = false;
        this.id = id;
        return EvaluationStatus.COMPLETE;
    }
 
    protected static QualifiedName getFactName(Operand expression) {
    	return getPartName(expression.getName(), expression.getQualifiedName());
    }
    
    protected static QualifiedName getPartName(String part, QualifiedName qname)
    {
        return new QualifiedName("is_" + part + "_fact", qname);
    }

    public static boolean analyseValue(Object value) {
        if (value instanceof AxiomTermList)
        {
            AxiomTermList axiomTermList = (AxiomTermList)value;
            Axiom axiom = axiomTermList.getAxiom();
            // isFact() returns true for an empty axiom, which is not what we want
            return axiom.isFact() && (axiom.getTermCount() > 0);
        }
        else if ((value instanceof Number) && value.toString().equals(EvaluationUtils.NAN))
            return false;
        else if ((value instanceof DoubleOperand) && ((Operand)value).getValue().toString().equals(EvaluationUtils.NAN))
            return false;
        else if (value instanceof Null)
            return false;
        else if (value instanceof Blank)
            return false;
        else
           return value.getClass() != Unknown.class;
	}

}
