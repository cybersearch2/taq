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
package au.com.cybersearch2.taq.trait;

import java.text.NumberFormat;

import au.com.cybersearch2.taq.interfaces.StringCloneable;
import au.com.cybersearch2.taq.language.OperandType;

/**
 * NumberTrait
 * Base class for Number operands - Integer, Double and Decimal
 * @author Andrew Bowley
 * 21Apr.,2017
 */
public abstract class NumberTrait<T extends Number> extends DefaultTrait implements StringCloneable
{

    /**
     * Construct NumberTrait object
     * @param operandType OperandType enum
     */
    public NumberTrait(OperandType operandType)
    {
        super(operandType);
    }

    /**
     * formatValue
     * @see au.com.cybersearch2.taq.trait.DefaultTrait#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue(Object value)
    {
        NumberFormat numberFormat = NumberFormat.getInstance(getLocale());
        String formatValue = numberFormat.format(value);
        return formatValue;
    }

    /**
     * Sub classes to implement parsing of text values to return a Number object
     * @param string Value to parse
     * @return Number=type object
     */
    public abstract T parseValue(String string);

}
