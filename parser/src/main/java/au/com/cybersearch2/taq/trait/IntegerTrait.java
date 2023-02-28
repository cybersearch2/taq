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

import java.util.Locale;
import java.util.Scanner;

import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;

/**
 * IntegerTrait
 * Behaviours for localization and specialization of Integer operands
 * @author Andrew Bowley
 * 26Apr.,2017
 */
public class IntegerTrait extends NumberTrait<Long>
{
    /**
     * Construct IntegerTrait object
     */
    public IntegerTrait()
    {
        super(OperandType.INTEGER);
     }

    /**
     * Construct IntegerTrait object with specified operand type
     * @param operandType Operand type
     */
    protected IntegerTrait(OperandType operandType)
    {
        super(operandType);
    }

    /**
     * parseValue
     * @see au.com.cybersearch2.taq.trait.NumberTrait#parseValue(java.lang.String)
     */
    @Override
    public Long parseValue(String string)
    {
        // Fail gracefully
        Long value = Long.valueOf(0L);
        Scanner scanner = new Scanner(string);
        scanner.useLocale(getLocale());
        if (scanner.hasNextLong())
            value = scanner.nextLong();
        scanner.close();    
        return value;
    }

    /**
     * cloneFromOperand
     * @see au.com.cybersearch2.taq.interfaces.StringCloneable#cloneFromOperand(Operand)
     */
    @Override
    public IntegerOperand cloneFromOperand(Operand stringOperand)
    {
        IntegerOperand clone = 
            stringOperand.getLeftOperand() == null ? 
            new IntegerOperand(stringOperand.getQualifiedName(), 0) :
            new IntegerOperand(stringOperand.getQualifiedName(), stringOperand.getLeftOperand());
        Parameter param = new Parameter(Term.ANONYMOUS, stringOperand.getValue().toString());
        param.setId(stringOperand.getId());
        Locale locale = stringOperand.getOperator().getTrait().getLocale();
        clone.getOperator().getTrait().setLocale(locale);
        clone.assign(param);
        return clone;
    }

}
