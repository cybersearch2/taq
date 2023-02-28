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

import java.util.Formatter;
import java.util.Locale;

import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperandType;

/**
 * DefaultTrait
 * Provides text formatting sufficient for most operands, including StringOperand.
 * Also supports setting locale by 2-character country code - useful for currency formatting.
 * @author Andrew Bowley
 * 21Apr.,2017
 */
public class DefaultTrait implements Trait
{
    /** Operand type identifies type of operands supported */
    protected OperandType operandType;
    /** Locale - defaults to default locale */
    protected Locale locale;
    /** Country code */
    protected String country;

    /** 
     * Construct DefaultTrait object 
     * @param operandType Operator type
     */
    public DefaultTrait(OperandType operandType)
    {
        this.operandType = operandType;
        // Locale is lazy-loaded
        country = "";
    }

    /**
     * formatValue
     * @see au.com.cybersearch2.taq.interfaces.TextFormat#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue(Object value)
    {
        if (locale == null)
            return value.toString();
        Formatter localeFormatter = new Formatter(locale);
        localeFormatter.format("%s", value);
        String formatValue = localeFormatter.toString();
        localeFormatter.close();
        return formatValue;
    }

    /**
     * getLocale
     * @see au.com.cybersearch2.taq.interfaces.Trait#getLocale()
     */
    @Override
    public Locale getLocale()
    {
        if (locale == null)
            setLocale(Locale.getDefault());
        return locale;
    }

    /**
     * setLocale
     * @see au.com.cybersearch2.taq.interfaces.Trait#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale)
    {
        this.locale = locale;
        country = getLocale().getCountry();
    }

    /**
     * getOperandType
     * @see au.com.cybersearch2.taq.interfaces.Trait#getOperandType()
     */
    @Override
    public OperandType getOperandType()
    {
        return operandType;
    }

    /**
     * @return the country code or empty string if locale not set
     */
    @Override
    public String getCountry()
    {
        return country;
    }

    /**
     * Returns locale languge and country codes
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        Locale currentLocale = locale;
        if (currentLocale == null)
            currentLocale = Locale.getDefault();
        StringBuilder builder = new StringBuilder(currentLocale.getLanguage());
        String country = currentLocale.getCountry();
        if (!country.isEmpty())
            builder.append('-').append(country);
        return builder.toString();
    }

}
