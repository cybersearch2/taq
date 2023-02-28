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
package au.com.cybersearch2.taq.expression;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * TestEvaluator
 * @author Andrew Bowley
 * 24 Aug 2015
 */
public class ParseNameEvaluator extends Evaluator
{

    /**
     * @param leftTerm
     * @param operator
     */
    public ParseNameEvaluator(Operand leftTerm, String operator)
    {
        super(leftTerm, operator, Orientation.unary_postfix);

    }

    /**
     * @param operator
     * @param rightTerm
     */
    public ParseNameEvaluator(String operator, Operand rightTerm)
    {
        super(rightTerm, operator, Orientation.unary_prefix);

    }

    /**
     * @param leftTerm
     * @param operator
     * @param rightTerm
     */
    public ParseNameEvaluator(Operand leftTerm, String operator, Operand rightTerm)
    {
        super(leftTerm, operator, rightTerm);

    }

    /**
     * @param QualifiedName.parseName(name)
     * @param leftTerm
     * @param operator
     * @param rightTerm
     */
    public ParseNameEvaluator(String name, Operand leftTerm, String operator, Operand rightTerm)
    {
        super(QualifiedName.parseName(name), leftTerm, operator, rightTerm);

    }

}
