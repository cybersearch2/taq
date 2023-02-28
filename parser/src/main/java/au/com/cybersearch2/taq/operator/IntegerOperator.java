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

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.FormatFunctions;
import au.com.cybersearch2.taq.trait.IntegerTrait;

/**
 * IntegerOperator
 * @see DelegateType#INTEGER
 * @see IntegerOperand
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class IntegerOperator implements Operator, LocaleListener
{
    /** Behaviours for localization and specialization of Integer operands */
    private IntegerTrait integerTrait;
    
    /**
     * Construct IntegerOperator object
     */
    public IntegerOperator()
    {
        super();
        integerTrait = new IntegerTrait();
    }

    @Override
    public Trait getTrait()
    {
        return integerTrait;
    }

    @Override
    public void setTrait(Trait trait)
    {
        if (!IntegerTrait.class.isAssignableFrom(trait.getClass()))
            return; //throw new ExpressionException(trait.getClass().getSimpleName() + " is not a compatible Trait");
        integerTrait = (IntegerTrait) trait;
    }
    
    @Override
    public boolean onScopeChange(Scope scope)
    {
    	if (!integerTrait.getLocale().equals(scope.getLocale())) {
            integerTrait.setLocale(scope.getLocale());
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
            OperatorEnum.BIT_AND,
            OperatorEnum.BIT_OR,
            OperatorEnum.XOR,
            OperatorEnum.REM,   
            OperatorEnum.LSHIFT,
            OperatorEnum.RSIGNEDSHIFT,
            OperatorEnum.RUNSIGNEDSHIFT,
            OperatorEnum.PLUSASSIGN,
            OperatorEnum.MINUSASSIGN,
            OperatorEnum.STARASSIGN,
            OperatorEnum.SLASHASSIGN,
            OperatorEnum.ANDASSIGN,
            OperatorEnum.ORASSIGN,
            OperatorEnum.XORASSIGN,
            OperatorEnum.REMASSIGN,       
            OperatorEnum.LSHIFTASSIGN, // "<<="
            OperatorEnum.RSIGNEDSHIFTASSIGN, // ">>="
            OperatorEnum.RUNSIGNEDSHIFTASSIGN, // ">>>="
        };
    }

	@Override
	public OperatorEnum[] getRightUnaryOps() {
        return  new OperatorEnum[]
        { 
            OperatorEnum.INCR,
            OperatorEnum.DECR,
            OperatorEnum.PLUS,
            OperatorEnum.MINUS,
            OperatorEnum.TILDE      
        };
	}

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
                OperatorEnum.BIT_AND,
                OperatorEnum.BIT_OR,
                OperatorEnum.XOR,
                OperatorEnum.REM,       
                OperatorEnum.LSHIFT,
                OperatorEnum.RSIGNEDSHIFT,
                OperatorEnum.RUNSIGNEDSHIFT,
                OperatorEnum.PLUSASSIGN,
                OperatorEnum.MINUSASSIGN,
                OperatorEnum.STARASSIGN,
                OperatorEnum.SLASHASSIGN,
                OperatorEnum.ANDASSIGN,
                OperatorEnum.ORASSIGN,
                OperatorEnum.XORASSIGN,
                OperatorEnum.REMASSIGN,
                OperatorEnum.LSHIFTASSIGN, // "<<="
                OperatorEnum.RSIGNEDSHIFTASSIGN, // ">>="
                OperatorEnum.RUNSIGNEDSHIFTASSIGN // ">>>="
        };
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
        return  new OperatorEnum[]
        { 
            OperatorEnum.INCR,
            OperatorEnum.DECR,
        };
	}

	@Override
     public OperatorEnum[] getConcatenateOps()
     {
         return EMPTY_OPERAND_OPS;
     }

    @Override
    public Number numberEvaluation(OperatorEnum operatorEnum2, Term rightTerm) 
    {
        int right = convertIntObject(rightTerm.getValue(), rightTerm.getValueClass());
        long calc = 0;
        switch (operatorEnum2)
        {
        case PLUS:  calc = +right; break;
        case MINUS: calc = -right; break;  
        case TILDE: calc = ~right; break;
        case INCR: calc = ++right; break;
        case DECR: calc = --right; break;
        default:
        }
        return Long.valueOf(calc);
    }

    @Override
    public Number numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
        long right = convertObject(rightTerm.getValue(), rightTerm.getValueClass());
        long left = convertObject(leftTerm.getValue(), leftTerm.getValueClass());
        long calc = 0;
        switch (operatorEnum2)
        {
        case PLUSASSIGN: // "+="
        case PLUS:  calc = left + right; break;
        case MINUSASSIGN: // "-="
        case MINUS:  calc = left - right; break;
        case STARASSIGN: // "*="
        case STAR:      calc = left * right; break;
        case SLASHASSIGN: // "/="
        case SLASH:     calc = left / right; break;
        case ANDASSIGN: // "&="
        case BIT_AND:   calc = left & right; break;
        case ORASSIGN: // "|="
        case BIT_OR:    calc = left | right; break;
        case XORASSIGN: // "^="
        case XOR:       calc = left ^ right; break;
        case REMASSIGN: // "%="
        case REM:       calc = left % right; break;
        case LSHIFT:
        case LSHIFTASSIGN: calc = left << right; break;
        case RSIGNEDSHIFT: 
        case RSIGNEDSHIFTASSIGN: calc = left >> right; break;
        case RUNSIGNEDSHIFT: 
        case RUNSIGNEDSHIFTASSIGN: calc = left >>> right; break;
        default:
        }
        return Long.valueOf(calc);
    }

    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
    	if ((leftTerm.getValueClass() == Boolean.class) || (rightTerm.getValueClass() == Boolean.class))
    		return false;
        long right = convertObject(rightTerm.getValue(), rightTerm.getValueClass());
        long left = convertObject(leftTerm.getValue(), leftTerm.getValueClass());
        boolean calc = false;
        switch (operatorEnum2)
        {
        case LT:  calc = left < right; break;
        case GT:  calc = left > right; break;
        case EQ:  calc = left == right; break; // "=="
        case LE:  calc = left <= right; break; // "<="
        case GE:  calc = left >= right; break; // ">="
        case NE:  calc = left != right; break; // "!="
        default:
        }
        return calc;
    }

    /**
     * Convert value to long, if not already of this type
     * @param object Value to convert
     * @param clazz Value class
     * @return long
     */
    public long convertObject(Object object, Class<?> clazz)
    {
        if (clazz == Long.class)
            return (Long)object;
        else if (clazz == String.class)
            return integerTrait.parseValue(object.toString());
        else if (Number.class.isAssignableFrom(clazz))
            return ((Number)object).longValue();
        else 
        	return 0L;
    }

    /**
     * Convert value of integer type to long, if not already of this type
     * @param object Value to convert
     * @param clazz Value class
     * @return long
     */
    protected int convertIntObject(Object object, Class<?> clazz)
    {
        if (clazz == Long.class)
            return ((Long)object).intValue();
        else if (clazz == String.class)
            return integerTrait.parseValue(object.toString()).intValue();
        else if (Number.class.isAssignableFrom(clazz))
            return ((Number)object).intValue();
        else return 0;
    }

}
