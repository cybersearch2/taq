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

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.NullOperand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.trait.DefaultTrait;

/**
 * NullOperator
 * @see DelegateType#NULL
 * @see NullOperand
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class NullOperator implements Operator
{
    /** Localization and specialization */
    protected DefaultTrait trait;

    /**
     * Construct NullOperator object
     */
    public NullOperator()
    {
        super();
        trait = new DefaultTrait(OperandType.UNKNOWN);
    }
    
    @Override
    public Trait getTrait()
    {
        return trait;
    }

    @Override
    public void setTrait(Trait trait)
    {
        if (trait.getOperandType() != OperandType.UNKNOWN)
            throw new ExpressionException(trait.getOperandType().toString() + " is not a compatible operand type");
        this.trait = (DefaultTrait) trait;
    }
    
    @Override
    public OperatorEnum[] getRightBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
            OperatorEnum.ASSIGN,
            OperatorEnum.EQ, // "=="
            OperatorEnum.NE // "!="
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
         return EMPTY_OPERAND_OPS;
     }

    @Override
    public Number numberEvaluation(OperatorEnum operatorEnum2, Term rightTerm) 
    {
        return Integer.valueOf(0);
    }

    @Override
    public Number numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
        throw new ExpressionException(
        	String.format("Cannot evaluate %s%s%s", 
        			leftTerm.getValue().toString(), operatorEnum2.toString(), rightTerm.getValue().toString()));
    }

    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
        boolean calc = false;
        switch (operatorEnum2)
        {
        case EQ:  calc = (leftTerm.getValueClass() == getValueClass()) && (rightTerm.getValueClass() == getValueClass()); break; // "=="
        case NE:  calc = !((leftTerm.getValueClass() == getValueClass()) && (rightTerm.getValueClass() == getValueClass())); break; // "!="
        default:
        }
        return calc;
    }

    /**
     * Returns Null class
     * @return class object
     */
    private Class<?> getValueClass()
    {
        return Null.class;
    }


}
