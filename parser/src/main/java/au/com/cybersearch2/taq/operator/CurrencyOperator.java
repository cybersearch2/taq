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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.helper.CountryCode;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.trait.BigDecimalTrait;
import au.com.cybersearch2.taq.trait.CurrencyTrait;

/**
 * CurrencyOperator
 * Operator for BigDecimalOperand which holds an amount in a particular currency
 * @see BigDecimalOperator
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class CurrencyOperator extends BigDecimalOperator implements LocaleListener
{
    /** Behaviors for localization and specialization of currency operands */
    private CurrencyTrait currencyTrait;
    private Currency currency;
    private Locale region;

    /**
     * Construct CurrencyOperator object
     */
    public CurrencyOperator()
    {
        super();
        currencyTrait = new CurrencyTrait();
        // CurrencyTrait is a sub class of BigDecimalTrait
        bigDecimalTrait = currencyTrait;
    }

    /**
     * Construct CurrencyOperator object for given locale
     */
    public CurrencyOperator(Locale locale)
    {
    	this();
        currencyTrait.setLocale(locale);
    }
    
    /**
     * getTrait
     * @see au.com.cybersearch2.taq.operator.BigDecimalOperator#getTrait()
     */
    @Override
    public Trait getTrait()
    {
        return currencyTrait;
    }
 
    /**
     * onScopeChange
     * @see au.com.cybersearch2.taq.operator.BigDecimalOperator#onScopeChange(au.com.cybersearch2.taq.Scope)
     */
    @Override
    public boolean onScopeChange(Scope scope) 
    {
    	if ((currency == null) && (region == null)) {
    		if (!currencyTrait.getLocale().equals(scope.getLocale())) {
                currencyTrait.setLocale(scope.getLocale());
                return true;
    		}
    	}
    	return false;
    }

    /**
     * Returns country code
     * @return String
     */
    public String getCountry()
    {
        return currencyTrait.getCountry();
    }

    /**
     * Returns BigDecimal representation of an amount specified as text.
     * Relaxes Java's strict format requirements to allow reasonable variations.
     * @param value Text to parse
     * @return BigDecimal object
     */
    public BigDecimal parseValue(String value)
    {
        return currencyTrait.parseValue(value);
    }
    
    /**
     * Returns text representation of amount specified in text
     * @param value Currency value in type compatible with NumberFormat eg. BigDecimal
     * @return String
     * @see au.com.cybersearch2.taq.trait.DefaultTrait#formatValue(java.lang.Object)
     */
    public String formatValue(Object value)
    {
        return currencyTrait.formatValue(value);
    }
    
    public void setCurrency(Currency currency) {
		this.currency = currency;
		Locale locale = CountryCode.getLocaleByCurrency(currency);
        currencyTrait.setLocale(locale);
	}

    public void setRegion(Locale region) {
	     this.region = region;
         currencyTrait.setLocale(region);
    }

	/**
     * Binary multiply. Override to adjust rounding. 
     * @param right BigDecimal object left term
     * @param left BigDecimal object reight term
     * @return BigDecimal object
     * @see au.com.cybersearch2.taq.operator.BigDecimalOperator#calculateTimes(java.math.BigDecimal, java.math.BigDecimal)
     */
    @Override
    protected BigDecimal calculateTimes(BigDecimal right, BigDecimal left)
    {
        BigDecimal newAmount = left.multiply(right);
        // Gets the default number of fraction digits used with this currency.
        // For example, the default number of fraction digits for the Euro is 2,
        // while for the Japanese Yen it's 0.
        // In the case of pseudo-currencies, such as IMF Special Drawing Rights,
        // -1 is returned.
        int scale = currencyTrait.getFractionDigits();
        if (scale >= 0)
            newAmount = newAmount.setScale(scale, RoundingMode.HALF_EVEN);
        return newAmount;
    }

    /**
     * Binary divide. Override to adjust rounding. 
     * @param right BigDecimal object left term
     * @param left BigDecimal object reight term
     * @return BigDecimal object
     * @see au.com.cybersearch2.taq.operator.BigDecimalOperator#calculateDiv(java.math.BigDecimal, java.math.BigDecimal)
     */
    @Override
    protected BigDecimal calculateDiv(BigDecimal right, BigDecimal left)
    {
        return left.divide(right, RoundingMode.HALF_EVEN);
    }

    @Override
    public void setTrait(Trait trait)
    {
        if (!CurrencyTrait.class.isAssignableFrom(trait.getClass()))
            return; //throw new ExpressionException(trait.getClass().getSimpleName() + " is not a compatible Trait");
        bigDecimalTrait = (BigDecimalTrait) trait;
        currencyTrait = (CurrencyTrait) trait;
    }
}
