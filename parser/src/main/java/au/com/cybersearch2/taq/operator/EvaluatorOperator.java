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

import static au.com.cybersearch2.taq.helper.EvaluationUtils.isNaN;

import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;

/**
 * EvaluatorOperator
 * Sub classes DelegateOperator to escape number evaluation with NaN
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class EvaluatorOperator extends DelegateOperator
{
    /**
     * Construct EvaluatorOperator object
     */
    public EvaluatorOperator()
    {
        super();
    }

    @Override
    public Object numberEvaluation(OperatorEnum operatorEnum2, Term rightTerm) 
    {
        if (isNaN(rightTerm.getValue()))
            return Double.valueOf("NaN");
        return super.numberEvaluation(operatorEnum2, rightTerm);
    }

    @Override
    public Object numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
        if (isNaN(rightTerm.getValue()) || isNaN(leftTerm.getValue()))
            return Double.valueOf("NaN");
        return super.numberEvaluation(leftTerm, operatorEnum2, rightTerm);
    }

}
