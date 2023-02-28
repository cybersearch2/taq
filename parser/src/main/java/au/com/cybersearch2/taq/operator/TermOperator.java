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

import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.trait.DefaultTrait;

/**
 * TermOperator
 * Operator for Operand which populates an axiom from a parameter list after the list operands have been evaluated
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class TermOperator implements Operator
{
    static Trait AXIOM_PARAMETER_TRAIT;
    
    static
    {
        AXIOM_PARAMETER_TRAIT = new DefaultTrait(OperandType.TERM);
    }

    /**
     * getTrait
     * @see au.com.cybersearch2.taq.interfaces.Operator#getTrait()
     */
    @Override
    public Trait getTrait()
    {
        return AXIOM_PARAMETER_TRAIT;
    }

    @Override
    public void setTrait(Trait trait)
    {
        // The trait for this class is static, so immutable
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
         };
    }

	@Override
	public OperatorEnum[] getRightUnaryOps() {
        return EMPTY_OPERAND_OPS;
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
        };
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
        return EMPTY_OPERAND_OPS;
	}

	/**
     * Returns Value for no concatenation operations
     * @return OperatorEnum[]
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
    {   // There is no valid evaluation involving an axiom resulting in a number
        return Integer.valueOf(0);
    }

    /**
     * numberEvaluation - binary
     * @see au.com.cybersearch2.taq.interfaces.Operator#numberEvaluation(Term, OperatorEnum, Term)
     */
    @Override
    public Number numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm)
    {   // There is no valid evaluation involving an axiom resulting in a number
        return Integer.valueOf(0);
    }

    /**
     * booleanEvaluation
     * @see au.com.cybersearch2.taq.interfaces.Operator#booleanEvaluation(Term, OperatorEnum, Term)
     */
    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm)
    {   // There is no valid evaluation involving an axiom resulting in a boolean
        return Boolean.FALSE;
    }

}
