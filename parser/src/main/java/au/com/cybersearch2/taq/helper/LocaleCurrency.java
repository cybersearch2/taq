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
package au.com.cybersearch2.taq.helper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

import au.com.cybersearch2.taq.expression.ExpressionException;

/**
 * LocaleCurrency
 * Support locale-specific currency operations
 * @author Andrew Bowley
 * 8 Mar 2015
 */
public class LocaleCurrency
{
	/** Structure to return result of a text conversion operation */
	private class CurrencyResult {
		public String currencyAsText;
		int digitCount;
		boolean isNegative;
	}
	
    /** The locale */
    protected Locale locale;
    /** The currency as specified by the platform */
    protected Currency currency;

    /**
     * Construct LocaleCurrency object for default locale
     */
	public LocaleCurrency() 
    {
    	this(Locale.getDefault());
	}

    /**
     * Construct LocaleCurrency object for specified locale
     * @param locale The locale
     * @throws ExpressionException if the locale country does not have a currency or
     *   does not support ISO 3166 country code
     */
	public LocaleCurrency(Locale locale) 
    {
		setLocale(locale);
		if (currency == null)
			throw new ExpressionException(String.format("Country of locale %s does not support currency", locale.getDisplayCountry()));
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	/**
	 * Returns BigDecimal representation of an amount specified as text.
	 * The major player in the required conversion is the Java Runtime library
	 * Currency flavor of the DecimalFormat class. This has short-comings that require
	 * workarounds. One is that it works only if the format of the text is strictly correct.
	 * This class relaxes the format requirements to allow reasonable variations. Another
	 * problem is the parser returns a Number type which can actually be a Long or a Double.
	 * In the former case, adjustments may be required to achieve a consistent final BigDecimal conversion.
	 * 
	 * @param currencyAsText An amount expressed in the currency of the locale - UTF-8 character encoding essential
	 * @return BigDecimal object representing the amount value, including fractional part, if specified by the currency
	 */
    public BigDecimal parse(String currencyAsText)
    {
    	String originalCurrencyAsText = currencyAsText;
    	DecimalFormat decformat = (DecimalFormat)NumberFormat.getCurrencyInstance(locale);
    	DecimalFormatSymbols symbols = decformat.getDecimalFormatSymbols();
    	// Strip off prefix and suffix leaving just digits and separators, if any
    	CurrencyResult stripResult = stripCurrency(currencyAsText, symbols);
    	// Replace separator characters with those from specification
     	CurrencyResult sepResult = adjustSeperators(stripResult.currencyAsText, symbols, decformat.getMaximumFractionDigits() > 0);
     	currencyAsText = sepResult.currencyAsText;
    	// Now add currency smybol prefix/suffix according to expected format
    	if (stripResult.isNegative) 
    	{
    		if (!decformat.getNegativePrefix().isEmpty())
    			currencyAsText = decformat.getNegativePrefix() + currencyAsText;
    		else
    			currencyAsText += decformat.getNegativeSuffix();
    	} 
    	else 
    	{
    		if (!decformat.getPositivePrefix().isEmpty())
    			currencyAsText = decformat.getPositivePrefix() + currencyAsText;
    		else
    			currencyAsText += decformat.getPositiveSuffix();
    	}
    	// Now parse sanitized amount which can return either a Long or Double type
     	Number amount;
     	//System.out.println(currencyAsText);
		try 
		{
			amount = decformat.parse(currencyAsText);
		} 
		catch (ParseException e) 
		{
			throw new ExpressionException("Locale " + locale.getLanguage() + "-" + locale.getCountry() + " currency format of amount \"" + originalCurrencyAsText + "\" invalid", e);
		}
		String amountString = amount.toString();
		if (amount instanceof Long) 
		{   // Restore trailing zeros
			for (int i = amountString.length(); i < sepResult.digitCount; ++i)
				amountString += "0";
		    if ((decformat.getMaximumFractionDigits() > 0))
			{   // Insert fraction separator
				int fractionSize = decformat.getMaximumFractionDigits();
	            char[] charArray1 = amountString.toCharArray();
	            char[] charArray2 = new char[charArray1.length + 1];
	            int decimalPos = charArray1.length - fractionSize;
	            System.arraycopy(charArray1, 0, charArray2, 0, decimalPos);
	            charArray2[decimalPos] = '.';
	            System.arraycopy(charArray1, decimalPos, charArray2, decimalPos + 1, fractionSize);
	            amountString = String.copyValueOf(charArray2);
			}
		}
    	return new BigDecimal(amountString);
    }

    /**
     * Replace separator characters with those from specification
     * @param strippedCurrency Amount with prefix and suffix removed
     * @param symbols Decimal format symbols
     * @param hasFraction Flag set true if currency specifies a fractional part
     * @return CurrencyResult object
     */
	private CurrencyResult adjustSeperators(String strippedCurrency, DecimalFormatSymbols symbols, boolean hasFraction) {
    	CurrencyResult result = new CurrencyResult();
   	    // Fix grouping character, if different from expected
    	// Move a cursor from first digit until non-digit encountered
    	int cursor = 0;
    	int firstPos = -1;
    	int lastPos = -1;
    	result.digitCount = 0;
    	while (cursor < strippedCurrency.length())
    	{
    	    char currencyChar = strippedCurrency.charAt(cursor);
    	    if (Character.isDigit(currencyChar))
    	    	++result.digitCount;
    	    else
    	    {
	        	if (firstPos == -1)
	        		firstPos = cursor;
	        	else 
	        		lastPos = cursor;
    	    }
    	    ++cursor;
    	}
    	if (firstPos != -1)
    	{
            char[] charArray = strippedCurrency.toCharArray();
            if (lastPos == -1)
            {
            	charArray[firstPos] = symbols.getDecimalSeparator();
            } 
            else 
            {
	            for (int i = 0; i < charArray.length; i++)
	            {
		    	    if (!Character.isDigit(charArray[i]))
		    	    {
	                    if (i < lastPos)
	                    	charArray[i] = symbols.getGroupingSeparator();
	                    else 
	                    {
	                    	if (hasFraction)
	                    		charArray[i] = symbols.getDecimalSeparator();
	                    	else
		                    	charArray[i] = symbols.getGroupingSeparator();
	    	            	break;
	                    }
		    	    }
	            }
            }
            strippedCurrency = String.copyValueOf(charArray);
    	}
    	result.currencyAsText = strippedCurrency;
		return result;
	}

	/**
     * Strip off prefix and suffix leaving just digits and separators, if any
	 * @param currencyAsText Complete amount 
     * @param symbols Decimal format symbols
	 * @return CurrencyResult with stripped amount and minus flag set
	 */
	private CurrencyResult stripCurrency(String currencyAsText, DecimalFormatSymbols symbols) {
    	CurrencyResult result = new CurrencyResult();
    	char minusSign = symbols.getMinusSign();
    	int endPos = currencyAsText.length() - 1;
    	boolean negPrefix = currencyAsText.charAt(0) == minusSign; 
    	if (negPrefix)
    		currencyAsText = currencyAsText.substring(1);
    	boolean negSuffix = currencyAsText.charAt(endPos) == minusSign;
    	if (negSuffix)
    		currencyAsText = currencyAsText.substring(0,endPos);
       	String currencySymbol = symbols.getCurrencySymbol();
      	// If amount has currency symbol, then strip if off
        // so it can be placed to conform with Java's formatter expectations 
    	int mark = currencyAsText.indexOf(currencySymbol);
    	int symLength = currencySymbol.length();
    	if (mark == 0) {
    		currencyAsText = currencyAsText.substring(symLength);
    		if (!Character.isDigit(currencyAsText.charAt(0)))
    			currencyAsText = currencyAsText.substring(1);
    	}
    	else if (mark > 0) {
    		currencyAsText = currencyAsText.substring(0, mark);
    		int lastPos = currencyAsText.length()-1;
    		if (!Character.isDigit(currencyAsText.charAt(lastPos)))
    			currencyAsText = currencyAsText.substring(0, lastPos);
    	}
    	result.isNegative = negPrefix || negSuffix;
    	result.currencyAsText = currencyAsText;
		return result;
	}

	/**
     * Returns text representation of amount specified in text
     * @param amount Currency value in type compatible with NumberFormat eg. BigDecimal
     * @return String
     */
    public String format(Object amount)
    {
    	DecimalFormat numberFormat = (DecimalFormat)NumberFormat.getCurrencyInstance(locale);
    	DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
    	symbols.setCurrencySymbol(currency.getCurrencyCode());
    	numberFormat.setDecimalFormatSymbols(symbols);
    	return numberFormat.format(amount);
    }

    /**
     * Set locale and return associated currency 
     * @param locale The locale
     * @return Currency object or null if the country does not have a currency eg. Antarctica
     * @exception ExpressionException if the country of the given {@code locale}
     * is not a supported ISO 3166 country code.
     */
	public Currency setLocale(Locale locale) 
	{
		this.locale = locale;
		Currency localeCurrency = null;
		try {
			localeCurrency = Currency.getInstance(locale);
		} catch (IllegalArgumentException e) {
			throw new ExpressionException(String.format("Country of locale %s is not a supported ISO 3166 country code", locale.getDisplayCountry()));
		}
    	if (localeCurrency != null)
    		currency = localeCurrency;
    	return localeCurrency;
	}

	/**
	 * Returns fraction digits for locale currency
	 * @return int
	 */
	public int getFractionDigits()
	{
		return currency.getDefaultFractionDigits();
	}
}
