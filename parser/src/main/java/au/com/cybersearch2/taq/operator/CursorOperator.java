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

import au.com.cybersearch2.taq.expression.CursorOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.Cursor;
import au.com.cybersearch2.taq.provider.CursorFunctions;
import au.com.cybersearch2.taq.trait.CursorTrait;

/**
 * CursorrOperator
 * @see DelegateType#CURSOR
 * @see Cursor
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class CursorOperator implements Operator
{
    /** Behaviors for localization and specialization of Cursor operands */
    private CursorTrait cursorTrait;
    
    /**
     * Construct IntegerOperator object
     */
    public CursorOperator()
    {
        super();
        cursorTrait = new CursorTrait();
    }

    @Override
	public Class<?> getObjectClass() {
		return CursorFunctions.class;
	}

	@Override
    public Trait getTrait()
    {
        return cursorTrait;
    }

    @Override
    public void setTrait(Trait trait)
    {
        if (!CursorTrait.class.isAssignableFrom(trait.getClass()))
            throw new ExpressionException(trait.getClass().getSimpleName() + " is not a compatible Trait");
        cursorTrait = (CursorTrait) trait;
    }
    
    @Override
    public OperatorEnum[] getRightBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
            OperatorEnum.ASSIGN
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
            OperatorEnum.ASSIGN
        };
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
        return  new OperatorEnum[]
        { 
            OperatorEnum.INCR,
            OperatorEnum.DECR,
            OperatorEnum.SC_AND  // "&&"
        };
	}

     @Override
     public OperatorEnum[] getConcatenateOps()
     {
         return  new OperatorEnum[]
         { 
              OperatorEnum.PLUSASSIGN
         };
     }

    @Override
    public Number numberEvaluation(OperatorEnum operatorEnum2, Term cursorTerm) 
    {
    	CursorOperand cursorOperand = (CursorOperand)cursorTerm;
        int right = cursorOperand.getIndex();
        long calc = 0;
        switch (operatorEnum2)
        {
        case INCR: calc = ++right; break;
        case DECR: calc = --right; break;
        case PLUS: return Long.valueOf(cursorOperand.forward());
        case MINUS: return Long.valueOf(cursorOperand.reverse());
        default:
        }
        cursorOperand.setIndex(right);
        return Long.valueOf(calc);
    }

    @Override
    public Number numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
        if (operatorEnum2 == OperatorEnum.PLUSASSIGN)
        {
        	CursorOperand cursorOperand = (CursorOperand)leftTerm;
            int index = cursorOperand.getIndex();
            index += ((Long)rightTerm.getValue()).intValue();
            cursorOperand.setIndex(index);
            return Long.valueOf(index);
        }
        return Long.valueOf(0L);
    }

    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
        return false;
    }

}
