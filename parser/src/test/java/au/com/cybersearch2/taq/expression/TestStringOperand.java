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
 * TestStringOperand
 * @author Andrew Bowley
 * 24 Aug 2015
 */
public class TestStringOperand extends StringOperand
{

    /**
     * @param QualifiedName.parseName(name)
     * @param expression
     */
    public TestStringOperand(String name, Operand expression)
    {
        super(QualifiedName.parseName(name), expression);

    }

    /**
     * @param QualifiedName.parseName(name)
     * @param value
     */
    public TestStringOperand(String name, String value)
    {
        super(QualifiedName.parseName(name), value);

    }

    /**
     * @param QualifiedName.parseName(name)
     */
    public TestStringOperand(String name)
    {
        super(QualifiedName.parseName(name));

    }

}
