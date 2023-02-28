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
import au.com.cybersearch2.taq.expression.DoubleOperand;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.FormatFunctions;
import au.com.cybersearch2.taq.trait.DoubleTrait;

/**
 * DoubleOperator
 * @see DelegateType#DOUBLE
 * @see DoubleOperand
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class DoubleOperator implements Operator, LocaleListener
{
    /** Behaviours for localization and specialization of Double operands */
    private DoubleTrait doubleTrait;
    
    /**
     * 
     */
    public DoubleOperator()
    {
        doubleTrait = new DoubleTrait();
    }

    /**
     * Convert value to double, if not already of this type
     * @param object Value to convert
     * @param clazz Value class
     * @return double
     */
    public double convertObject(Object object, Class<?> clazz)
    {
        if (clazz == Double.class)
            return (Double)object;
        else if (clazz == String.class)
            return doubleTrait.parseValue(object.toString());
        else if (Number.class.isAssignableFrom(clazz))
            return ((Number)object).doubleValue();
        else return Double.NaN;
    }
    
    @Override
    public Trait getTrait()
    {
        return doubleTrait;
    }
    
    @Override
    public void setTrait(Trait trait)
    {
        if (!DoubleTrait.class.isAssignableFrom(trait.getClass()))
            return; //throw new ExpressionException(trait.getClass().getSimpleName() + " is not a compatible Trait");
        doubleTrait = (DoubleTrait) trait;
    }
    
    @Override
    public boolean onScopeChange(Scope scope)
    {
    	if (!doubleTrait.getLocale().equals(scope.getLocale())) {
    		doubleTrait.setLocale(scope.getLocale());
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
            OperatorEnum.PLUSASSIGN,
            OperatorEnum.MINUSASSIGN,
            OperatorEnum.STARASSIGN,
            OperatorEnum.SLASHASSIGN
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
                OperatorEnum.PLUSASSIGN,
                OperatorEnum.MINUSASSIGN,
                OperatorEnum.STARASSIGN,
                OperatorEnum.SLASHASSIGN
        };
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
        return EMPTY_OPERAND_OPS;
	}

	@Override
     public OperatorEnum[] getConcatenateOps()
     {
         return EMPTY_OPERAND_OPS;
     }

    @Override
    public Number numberEvaluation(OperatorEnum operatorEnum2, Term rightTerm) 
    {
        double right = convertObject(rightTerm.getValue(), rightTerm.getValueClass());
        double calc = 0;
        switch (operatorEnum2)
        {
        case PLUS:  calc = +right; break;
        case MINUS: calc = -right; break;  
        default:
        }
        return Double.valueOf(calc);
    }

    @Override
    public Number numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
        double right = convertObject(rightTerm.getValue(), rightTerm.getValueClass());
        double left = convertObject(leftTerm.getValue(), leftTerm.getValueClass());
        double calc = 0;
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
        default:
        }
        return Double.valueOf(calc);
    }

    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
    	if ((leftTerm.getValueClass() == Boolean.class) || (rightTerm.getValueClass() == Boolean.class))
    		return false;
        double right = convertObject(rightTerm.getValue(), rightTerm.getValueClass());
        double left = convertObject(leftTerm.getValue(), leftTerm.getValueClass());
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
}
