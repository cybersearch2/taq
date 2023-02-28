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
import java.util.Currency;
import java.util.Locale;

import au.com.cybersearch2.taq.expression.BigDecimalOperand;
import au.com.cybersearch2.taq.helper.LocaleCurrency;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.StringCloneable;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.CurrencyOperator;

/**
 * CurrencyTrait
 * Behaviours for localization and specialization of Currency operands
 * @author Andrew Bowley
 * 21Apr.,2017
 */
public class CurrencyTrait extends BigDecimalTrait implements StringCloneable
{
    /** Currency implementation for specific locale */
    private LocaleCurrency localeCurrency;

    /**
     * Construct CurrencyTrait object
     */
    public CurrencyTrait()
    {
        super(OperandType.CURRENCY);
        localeCurrency = new LocaleCurrency();
    }

    /**
     * Returns fraction digits for locale currency
     * @return int
     */
    public int getFractionDigits()
    {
        return localeCurrency.getFractionDigits();
    }

	public void setCurrency(Currency currency) {
		localeCurrency.setCurrency(currency);
	}

	/**
     * Returns BigDecimal representation of an amount specified as text.
     * Relaxes Java's strict format requirements to allow reasonable variations.
     * @param value Text to parse
     * @return BigDecimal object
     */
    @Override
    public BigDecimal parseValue(String value)
    {
        return localeCurrency.parse(value);
    }
    
    /**
     * Returns text representation of amount specified in text
     * @param value Currency value in type compatible with NumberFormat eg. BigDecimal
     * @return String
     * @see au.com.cybersearch2.taq.trait.DefaultTrait#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue(Object value)
    {
        return localeCurrency.format(value);
    }

    /**
     * @see au.com.cybersearch2.taq.trait.DefaultTrait#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale)
    {
        super.setLocale(locale);
        localeCurrency.setLocale(locale);
    }

    /**
     * getOperandType
     * @see au.com.cybersearch2.taq.trait.DefaultTrait#getOperandType()
     */
    @Override
    public OperandType getOperandType()
    {
        return operandType;
    }

    /**
     * cloneFromOperand
     * @see au.com.cybersearch2.taq.trait.BigDecimalTrait#cloneFromOperand(au.com.cybersearch2.taq.interfaces.Operand)
     */
    @Override
    public BigDecimalOperand cloneFromOperand(Operand stringOperand)
    {
        Locale locale = stringOperand.getOperator().getTrait().getLocale();
        CurrencyOperator currencyOperator = new CurrencyOperator(locale);
        BigDecimalOperand clone = 
                stringOperand.getLeftOperand() == null ? 
                new BigDecimalOperand(stringOperand.getQualifiedName(), BigDecimal.ZERO) :
                new BigDecimalOperand(stringOperand.getQualifiedName(), stringOperand.getLeftOperand());
        Parameter param = new Parameter(Term.ANONYMOUS, stringOperand.getValue().toString());
        param.setId(stringOperand.getId());
        clone.setOperator(currencyOperator);
        clone.assign(param);
        return clone;
    }


}
