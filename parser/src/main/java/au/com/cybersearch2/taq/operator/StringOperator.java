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
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.StringOperand;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.trait.DefaultTrait;

/**
 * StringOperator
 * @see DelegateType#STRING
 * @see StringOperand
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class StringOperator implements Operator, LocaleListener
{
    /** Localization and specialization */
    protected DefaultTrait trait;

    /**
     * Construct StringOperator object
     */
    public StringOperator()
    {
        super();
        trait = new DefaultTrait(OperandType.STRING);
    }

    @Override
    public Trait getTrait()
    {
        return trait;
    }

    @Override
    public void setTrait(Trait trait)
    {
        if (trait.getOperandType() != OperandType.STRING)
            throw new ExpressionException(trait.getOperandType().toString() + " is not a compatible operand type");
        this.trait = (DefaultTrait) trait;
    }
    
    @Override
    public boolean onScopeChange(Scope scope)
    {
    	if (!trait.getLocale().equals(scope.getLocale())) {
    		trait.setLocale(scope.getLocale());
    		return true;
    	}
    	return false;
    }

    @Override
    public OperatorEnum[] getRightBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
            OperatorEnum.ASSIGN,
            OperatorEnum.EQ, // "=="
            OperatorEnum.NE
        };
    }

	@Override
	public OperatorEnum[] getRightUnaryOps() {
		return EMPTY_OPERAND_OPS;
	}

    @Override
    public OperatorEnum[] getLeftBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
            OperatorEnum.ASSIGN,
            OperatorEnum.EQ, // "=="
            OperatorEnum.NE // "!="
    };
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
		return EMPTY_OPERAND_OPS;
	}

     @Override
     public OperatorEnum[] getConcatenateOps()
     {
        return  new OperatorEnum[]
        { 
            OperatorEnum.PLUS,
            OperatorEnum.PLUSASSIGN
        };
     }

    @Override
    public Number numberEvaluation(OperatorEnum operatorEnum, Term rightTerm) 
    {
        return Integer.valueOf(0);
    }

    @Override
    public Number numberEvaluation(Term leftTerm, OperatorEnum operatorEnum, Term rightTerm) 
    {
        return Integer.valueOf(0);
    }

    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum, Term rightTerm) 
    {
        boolean calc = false;
        Object left = leftTerm.getValueClass() == Blank.class ? 
        		Blank.BLANK : leftTerm.getValue();
        Object right = rightTerm.getValueClass() == Blank.class ? 
        		Blank.BLANK : rightTerm.getValue();
        switch (operatorEnum)
        {
        case EQ:  calc = left.equals(right); break; // "=="
        case NE:  calc = !left.equals(right); break; // "!="
        default:
        }
        return calc;
    }

}
