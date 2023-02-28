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

import au.com.cybersearch2.taq.expression.DoubleOperand;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;

/**
 * DoubleTrait
 * Behaviors for localization and specialization of Double operands
 * @author Andrew Bowley
 * 26Apr.,2017
 */
public class DoubleTrait extends NumberTrait<Double>
{

    /**
     * Construct DoubleTrait object
     */
    public DoubleTrait()
    {
        super(OperandType.DOUBLE);
    }

    /**
     * parseValue
     * @see au.com.cybersearch2.taq.trait.NumberTrait#parseValue(java.lang.String)
     */
    @Override
    public Double parseValue(String string)
    {
        Double value = Double.NaN;
        Scanner scanner = new Scanner(string);
        scanner.useLocale(getLocale());
        if (scanner.hasNextDouble())
            value = scanner.nextDouble();
        else if (scanner.hasNextLong())
            value = Double.valueOf(string);
        scanner.close();    
        return value;
    }

    /**
     * cloneFromOperand
     * @see au.com.cybersearch2.taq.interfaces.StringCloneable#cloneFromOperand(Operand)
     */
    @Override
    public DoubleOperand cloneFromOperand(Operand stringOperand)
    {
        DoubleOperand clone = 
            stringOperand.getLeftOperand() == null ? 
            new DoubleOperand(stringOperand.getQualifiedName(), 0D) :
            new DoubleOperand(stringOperand.getQualifiedName(), stringOperand.getLeftOperand());
        Parameter param = new Parameter(Term.ANONYMOUS, stringOperand.getValue().toString());
        param.setId(stringOperand.getId());
        Locale locale = stringOperand.getOperator().getTrait().getLocale();
        clone.getOperator().getTrait().setLocale(locale);
        clone.assign(param);
        clone.setArchetypeId(stringOperand.getArchetypeId());
        clone.setArchetypeIndex(stringOperand.getArchetypeIndex());
        clone.setId(stringOperand.getId());
        return clone;
    }

}
