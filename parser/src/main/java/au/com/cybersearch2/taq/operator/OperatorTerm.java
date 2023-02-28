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
import au.com.cybersearch2.taq.language.Parameter;

/**
 * OperatorTerm
 * Parameter with Operator field for recreating variables such as Currency 
 * which depend on operator for correct behavior
 * @author Andrew Bowley
 * 28Aug.,2017
 */
public class OperatorTerm extends Parameter
{
    protected Operator operator;
    
    /**
     * Construct OperatorTerm object
     * @param name Name
     * @param value Value
     * @param operator Operator
     */
    public OperatorTerm(String name, Object value, Operator operator)
    {
        super(name, value);
        this.operator = operator;
    }

    /**
     * @return the operator
     */
    public Operator getOperator()
    {
        return operator;
    }

}
