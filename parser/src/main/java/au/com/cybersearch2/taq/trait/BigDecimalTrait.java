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

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Scanner;

import au.com.cybersearch2.taq.expression.BigDecimalOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;

/**
 * BigDecimalTrait
 * Behaviors for localization and specialization of BigDecimal operands
 * @author Andrew Bowley
 * 26Apr.,2017
 */
public class BigDecimalTrait extends NumberTrait<BigDecimal>
{
    /**
     * Construct BigDecimalTrait object
     */
    public BigDecimalTrait()
    {
        super(OperandType.DECIMAL);
    }

    /**
     * Construct BigDecimalTrait object for OperandType different from DECIMAL
     * @param operandType OperandType enum
     */
    protected BigDecimalTrait(OperandType operandType)
    {
        super(operandType);
    }

    /**
     * parseValue
     * @see au.com.cybersearch2.taq.trait.NumberTrait#parseValue(java.lang.String)
     */
    @Override
    public BigDecimal parseValue(String string)
    {
        BigDecimal bigDecimal = null;
        Scanner scanner = new Scanner(string);
        scanner.useLocale(getLocale());
        if (scanner.hasNextBigDecimal())
            bigDecimal = scanner.nextBigDecimal();
        else if (scanner.hasNextDouble() || scanner.hasNextLong())
            bigDecimal = new BigDecimal(string);
        scanner.close();    
        if (bigDecimal == null)
        	throw new ExpressionException(String.format("%s is not a valid number in locale %s", string, getLocale().toString()));
        return bigDecimal;
    }

    /**
     * cloneFromOperand
     * @see au.com.cybersearch2.taq.interfaces.StringCloneable#cloneFromOperand(Operand)
     */
    @Override
    public BigDecimalOperand cloneFromOperand(Operand stringOperand)
    {
        BigDecimalOperand clone = 
            stringOperand.getLeftOperand() == null ? 
            new BigDecimalOperand(stringOperand.getQualifiedName(), BigDecimal.ZERO) :
            new BigDecimalOperand(stringOperand.getQualifiedName(), stringOperand.getLeftOperand());
        Parameter param = new Parameter(Term.ANONYMOUS, stringOperand.getValue().toString());
        param.setId(stringOperand.getId());
        Locale locale = stringOperand.getOperator().getTrait().getLocale();
        clone.getOperator().getTrait().setLocale(locale);
        clone.assign(param);
        return clone;
    }

}
