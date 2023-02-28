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
package au.com.cybersearch2.taq.operator;

import java.math.BigDecimal;

import au.com.cybersearch2.taq.artifact.LiteralArtifact;
import au.com.cybersearch2.taq.expression.BooleanOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.FormatFunctions;
import au.com.cybersearch2.taq.trait.BooleanTrait;

/**
 * BooleanOperator
 * @see DelegateType#BOOLEAN
 * @see BooleanOperand
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class BooleanOperator implements Operator
{
    /** Localization and specialization */
    protected BooleanTrait trait;

    /** 
     * Construct BooleanOperator object
     */
    public BooleanOperator()
    {
        trait = new BooleanTrait();
    }

    /**
     * Convert value to boolean, if not already of this type. If type conversion
     * is required and fails, then returns false.
     * @param object Value to convert
     * @param clazz Value class
     * @return boolean 
     */
    public boolean convertObject(Object object, Class<?> clazz)
    {
        if (clazz == Boolean.class)
            return (Boolean)object;
        else if (clazz == String.class)
        	return LiteralArtifact.isMatch(Boolean.TRUE, object.toString());
        else 
        	return false;
    }
    /**
     * Returns object class associated with operands that use this operator
     * @return Class
     */
    @Override
	public Class<?> getObjectClass() {
		return FormatFunctions.class;
	}

    /**
     * getTrait
     * @see au.com.cybersearch2.taq.interfaces.Operator#getTrait()
     */
    @Override
    public Trait getTrait()
    {
        return trait;
    }

    @Override
    public void setTrait(Trait trait)
    {
        if (trait.getOperandType() != OperandType.BOOLEAN)
            throw new ExpressionException(trait.getOperandType().toString() + " is not a compatible operand type");
        this.trait = (BooleanTrait) trait;
    }
    
    /**
     * getRightOperandOps
     * @see au.com.cybersearch2.taq.interfaces.Operator#getRightOperandOps()
     */
    @Override
    public OperatorEnum[] getRightBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
            OperatorEnum.EQ, // "=="
            OperatorEnum.NE, // "!="
            OperatorEnum.ASSIGN,
            OperatorEnum.SC_OR, // "||"
            OperatorEnum.SC_AND, // "&&"
            OperatorEnum.STAR // * true == 1.0, false = 0.0
        };
    }

	@Override
	public OperatorEnum[] getRightUnaryOps() {
        return  new OperatorEnum[]
        { 
            OperatorEnum.NOT,    // !
            OperatorEnum.HOOK
        };
	}

    /**
     * getLeftOperandOps
     * @see au.com.cybersearch2.taq.interfaces.Operator#getLeftOperandOps()
     */
    @Override
    public OperatorEnum[] getLeftBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
            OperatorEnum.EQ, // "=="
            OperatorEnum.NE, // "!="
            OperatorEnum.ASSIGN, // "="
            OperatorEnum.SC_OR,  // "||"
            OperatorEnum.SC_AND,  // "&&"
            OperatorEnum.STAR // * true == 1.0, false = 0.0
        };
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
        return  new OperatorEnum[]
        { 
            OperatorEnum.SC_OR,  // "||"
            OperatorEnum.SC_AND  // "&&"
        };
	}

	/**
     * Returns OperatorEnum values for which this Term is a valid String operand
     * @return OperatorEnum[]
     * @see au.com.cybersearch2.taq.interfaces.Operator#getConcatenateOps()
     */
     @Override
     public OperatorEnum[] getConcatenateOps()
     {
         return EMPTY_OPERAND_OPS;
     }

    /**
     * numberEvaluation - unary
     * @see au.com.cybersearch2.taq.interfaces.Operator#numberEvaluation(OperatorEnum, Term)
     */
    @Override
    public Number numberEvaluation(OperatorEnum operatorEnum2, Term rightTerm) 
    {   // There is no valid evaluation involving a boolean resulting in a number
        return Integer.valueOf(0);
    }

    /**
     * Evaluate a binary expression using this Term as the left term
     * @param leftTerm Term on left
     * @param operatorEnum OperatorEnum for one of +, -, *, /, &amp;, |, ^ or % 
     * @param rightTerm Term on right
     * @return sub class of Number with result
     * @see au.com.cybersearch2.taq.interfaces.Operator#numberEvaluation(Term, OperatorEnum, Term)
     */
    @Override
    public Number numberEvaluation(Term leftTerm, OperatorEnum operatorEnum, Term rightTerm) 
    {   // There is no valid evaluation involving a boolean and another term resulting in a number except *
        boolean leftIsBool = leftTerm.getValueClass() == Boolean.class; 
        boolean rightIsBool = rightTerm.getValueClass() == Boolean.class; 
        BigDecimal right;
        BigDecimal left;
        if (leftIsBool)
            left =  ((Boolean)(leftTerm.getValue())).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
        else
            left = convertObject(leftTerm.getValue());
        if (rightIsBool)
            right =  ((Boolean)(rightTerm.getValue())).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
        else
            right = convertObject(rightTerm.getValue());
        return left.multiply(right);
    }

    /**
     * Evaluate boolean operator using this Boolean as the left term
     * @param operatorEnum OperaorEnum.LT or OperaorEnum.GT
     * @param rightTerm Term on right
     * @return Boolean object
     * @see au.com.cybersearch2.taq.interfaces.Operator#booleanEvaluation(Term, OperatorEnum, Term)
     */
    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum, Term rightTerm) 
    {   
        boolean right = ((Boolean)(rightTerm.getValue())).booleanValue();
        boolean left = ((Boolean)(leftTerm.getValue())).booleanValue();
        switch (operatorEnum)
        {
        case SC_OR:  return right || left; // "||"
        case SC_AND: return right && left; // "&&"
        case EQ:  return left == right; // "=="
        case NE:  return left != right; // "!="
        default:
        }
        return Boolean.FALSE;
    }

    /**
     * Convert value to BigDecimal, if not already of this type
     * @param object Value to convert
     * @return BigDecimal object
     */
    protected BigDecimal convertObject(Object object)
    {
            if (object instanceof BigDecimal)
                return (BigDecimal)(object);
            else
                return new BigDecimal(object.toString());
    }

}
