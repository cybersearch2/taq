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
package au.com.cybersearch2.taq.interfaces;

import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;

/**
 * Operator
 * Defines operations that an Operand performs with other operands. It has left-hand and right-hand associations.
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public interface Operator
{
    /** OperatorEnum const array for no operations permitted */
    final static OperatorEnum[] EMPTY_OPERAND_OPS = new OperatorEnum[0];
    /** OperatorEnum const array for assignment only */
    final static OperatorEnum[] ASSIGN_OPERAND_OP = { OperatorEnum.ASSIGN };

    /**
     * Returns OperatorEnum values for which this Term is a valid right operand
     * @return OperatorEnum[]
     */
    OperatorEnum[] getRightBinaryOps();
    
    /**
     * Returns OperatorEnum values for which this Term is a valid right operand
     * @return OperatorEnum[]
     */
    OperatorEnum[] getRightUnaryOps();
    
    /**
     * Returns OperatorEnum values for which this Term is a valid left operand
     * @return OperatorEnum[]
     */
     OperatorEnum[] getLeftBinaryOps();

     /**
      * Returns OperatorEnum values for which this Term is a valid left operand
      * @return OperatorEnum[]
      */
     OperatorEnum[] getLeftUnaryOps();
      
    /**
     * Returns OperatorEnum values for which this Term is a valid concatenate operand
     * @return OperatorEnum[]
     */
     OperatorEnum[] getConcatenateOps();

    /**
     * Evaluate a unary expression 
     * @param operatorEnum2 OperatorEnum for one of +, -, ~. ++ or -- 
     * @param rightTerm The term, always on right except for post inc/dec
     * @return Class derived from Number.
     */
    Object numberEvaluation(OperatorEnum operatorEnum2, Term rightTerm);
    
    /**
     * Evaluate a binary expression
     * @param leftTerm Term on left
     * @param operatorEnum2 OperatorEnum for one of +, -, *, /, &amp;, |, ^ or % 
     * @param rightTerm Term on right
     * @return Class derived from Number.
     */
    Object numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm);
    
    /**
     * Evaluate comparison 
     * @param leftTerm Term on left
     * @param operatorEnum2 OperaorEnum.LT or OperaorEnum.GT
     * @param rightTerm Term on right
     * @return BooleanOperand result
     */
    Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm);

    /**
     * Returns trait for localization and specialization
     * @return Trait object
     */
    Trait getTrait();

    /**
     * Set trait - incompatible traits may be ignored for number traits
     * @param trait Trait object
     */
    void setTrait(Trait trait);
 
    /**
     * Returns object class associated with operands that use this operator
     * @return Class
     */
	default Class<?> getObjectClass() {
		return null;
	}

}
