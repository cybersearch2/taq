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

import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.trait.DefaultTrait;

/**
 * AssignOnlyOperator
 * @see DelegateType#ASSIGN_ONLY
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class AssignOnlyOperator extends NullOperator
{

    /**
     * Construct AssignOnlyOperator object
     */
    public AssignOnlyOperator()
    {
        super();
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
            OperatorEnum.EQ,
            OperatorEnum.NE
        };
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
                OperatorEnum.EQ,
                OperatorEnum.NE
        };
    }

    @Override
    public void setTrait(Trait trait)
    {
        this.trait = (DefaultTrait) trait;
    }
}
