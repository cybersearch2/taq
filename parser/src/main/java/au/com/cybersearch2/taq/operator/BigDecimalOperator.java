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
import java.math.RoundingMode;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.expression.BigDecimalOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.FormatFunctions;
import au.com.cybersearch2.taq.trait.BigDecimalTrait;

/**
 * BigDecimalOperator
 * @see DelegateType#DECIMAL
 * @see BigDecimalOperand
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class BigDecimalOperator implements Operator, LocaleListener
{
    /** Behaviors for localization and specialization of Decimal operands */
    protected BigDecimalTrait bigDecimalTrait;

    /** Construct BigDecimalOperator object */
    public BigDecimalOperator()
    {
        bigDecimalTrait = new BigDecimalTrait();
    }

    /**
     * getTrait
     * @see au.com.cybersearch2.taq.interfaces.Operator#getTrait()
     */
    @Override
    public Trait getTrait()
    {
        return bigDecimalTrait;
    }
 
    @Override
    public void setTrait(Trait trait)
    {
        if (!BigDecimalTrait.class.isAssignableFrom(trait.getClass()))
            return; //throw new ExpressionException(trait.getClass().getSimpleName() + " is not a compatible Trait");
        bigDecimalTrait = (BigDecimalTrait) trait;
    }
    
    /**
     * onScopeChange
     * @see au.com.cybersearch2.taq.interfaces.LocaleListener#onScopeChange(Scope)
     */
    @Override
    public boolean onScopeChange(Scope scope)
    {
    	if (!bigDecimalTrait.getLocale().equals(scope.getLocale())) {
            bigDecimalTrait.setLocale(scope.getLocale());
            return true;
    	}
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
     * getRightOperandOps
     * @see au.com.cybersearch2.taq.interfaces.Operator#getRightOperandOps()
     */
    @Override
    public OperatorEnum[] getRightBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
            OperatorEnum.ASSIGN,
            OperatorEnum.LT, // "<"
            OperatorEnum.GT ,// ">"
            OperatorEnum.EQ, // "=="
            OperatorEnum.LE, // "<="
            OperatorEnum.GE, // ">="
            OperatorEnum.NE, // "!="
            OperatorEnum.PLUS,
            OperatorEnum.MINUS,
            OperatorEnum.STAR,
            OperatorEnum.SLASH,
            OperatorEnum.REM,       
            OperatorEnum.PLUSASSIGN,
            OperatorEnum.MINUSASSIGN,
            OperatorEnum.STARASSIGN,
            OperatorEnum.SLASHASSIGN,
            OperatorEnum.REMASSIGN          
        };
    }

	@Override
	public OperatorEnum[] getRightUnaryOps() {
        return  new OperatorEnum[]
        { 
                OperatorEnum.PLUS,
                OperatorEnum.MINUS
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
                OperatorEnum.ASSIGN,
                OperatorEnum.LT, // "<"
                OperatorEnum.GT ,// ">"
                OperatorEnum.EQ, // "=="
                OperatorEnum.LE, // "<="
                OperatorEnum.GE, // ">="
                OperatorEnum.NE, // "!="
                OperatorEnum.PLUS,
                OperatorEnum.MINUS,
                OperatorEnum.STAR,
                OperatorEnum.SLASH,
                OperatorEnum.REM,       
                OperatorEnum.PLUSASSIGN,
                OperatorEnum.MINUSASSIGN,
                OperatorEnum.STARASSIGN,
                OperatorEnum.SLASHASSIGN,
                OperatorEnum.REMASSIGN          
        };
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
        return EMPTY_OPERAND_OPS;
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
    {
        BigDecimal right = convertObject(rightTerm.getValue(), rightTerm.getValueClass());
        BigDecimal calc = BigDecimal.ZERO;
        switch (operatorEnum2)
        {
        case PLUS:  calc = right.plus(); break;
        case MINUS: calc = right.negate(); break;  
        default:
        }
        return calc;
    }

    /**
     * numberEvaluation - binary
     * @see au.com.cybersearch2.taq.interfaces.Operator#numberEvaluation(Term, OperatorEnum, Term)
     */
    @Override
    public Number numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
        BigDecimal right = convertObject(rightTerm.getValue(), rightTerm.getValueClass());
        BigDecimal left = convertObject(leftTerm.getValue(), leftTerm.getValueClass());
        BigDecimal calc = BigDecimal.ZERO;
        switch (operatorEnum2)
        {
        case PLUSASSIGN: // "+="
        case PLUS:  calc = left.add(right); break;
        case MINUSASSIGN: // "-="
        case MINUS:     calc = left.subtract(right); break;
        case STARASSIGN: // "*="
        case STAR:      calc = calculateTimes(left, right); break;
        case SLASHASSIGN: // "/="
        case SLASH:     calc = calculateDiv(left, right); break;
        case REMASSIGN: // "%="
        case REM:       calc = left.remainder(right); break;
        default:
        }
        return calc;
    }

    /**
     * booleanEvaluation
     * @see au.com.cybersearch2.taq.interfaces.Operator#booleanEvaluation(Term, OperatorEnum, Term)
     */
    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
    	if ((leftTerm.getValueClass() == Boolean.class) || (rightTerm.getValueClass() == Boolean.class))
    		return false;
        boolean calc = false;
        BigDecimal leftBigDec = convertObject(leftTerm.getValue(), leftTerm.getValueClass());
        BigDecimal righttBigDec = convertObject(rightTerm.getValue(), rightTerm.getValueClass());
        switch (operatorEnum2)
        {
        case EQ:  calc = leftBigDec.compareTo(righttBigDec) == 0; break; // "=="
        case NE:  calc = leftBigDec.compareTo(righttBigDec) != 0; break; // "!="
        case LT:  calc = leftBigDec.compareTo(righttBigDec) < 0; break; // "<"
        case GT:  calc = leftBigDec.compareTo(righttBigDec) > 0; break; // ">"
        case LE:  calc = leftBigDec.compareTo(righttBigDec) <= 0; break; // "<="
        case GE:  calc = leftBigDec.compareTo(righttBigDec) >= 0; break; // ">="
        default:
        }
        return calc;
    }

    /**
     * Convert value to BigDecimal, if not already of this type
     * @param object Value to convert
     * @param clazz Value class
     * @return BigDecimal object
     */
    public BigDecimal convertObject(Object object, Class<?> clazz)
    {
        if (clazz == BigDecimal.class)
            return (BigDecimal)(object);
        else if (clazz == String.class)
            return bigDecimalTrait.parseValue(object.toString());
        else
            try
            {
                return new BigDecimal(object.toString());
            }
            catch (NumberFormatException e)
            {
                throw new ExpressionException(object.toString() + " is not convertible to a Decimal type");    
            }
    }

    /**
     * Binary multiply. Override to adjust rounding. 
     * @param right BigDecimal object left term
     * @param left BigDecimal object reight term
     * @return BigDecimal object
     */
    protected BigDecimal calculateTimes(BigDecimal right, BigDecimal left)
    {
        return left.multiply(right);
    }

    /**
     * Binary divide. Override to adjust rounding. 
     * @param right BigDecimal object left term
     * @param left BigDecimal object reight term
     * @return BigDecimal object
     */
    protected BigDecimal calculateDiv(BigDecimal right, BigDecimal left)
    {
        return left.divide(right, RoundingMode.FLOOR);
    }


}
