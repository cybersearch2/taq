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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import org.junit.Test;

import au.com.cybersearch2.taq.ResourceHelper;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.CurrencyOperator;
import au.com.cybersearch2.taq.trait.CurrencyTrait;

/**
 * CurrencyOperandTest
 * 
 * @author Andrew Bowley
 * 10 Mar 2015
 */
public class TestCurrencyOperand 
{
    //Locale.getDefault(/*Category.FORMAT*/)
	final static String NAME = "CurrencyOp";
	static QualifiedName QNAME = QualifiedName.parseName(NAME);
	
	@Test
	public void test_unify_text()
	{
		BigDecimalOperand currencyOperand = new BigDecimalOperand(QNAME);
		currencyOperand.operator = new CurrencyOperator();
		Parameter localAmount = new Parameter(Term.ANONYMOUS, NumberFormat.getCurrencyInstance().format(12345.67));
		assertThat(currencyOperand.unifyTerm(localAmount, 1)).isEqualTo(1);
		assertThat(currencyOperand.evaluate(1)).isEqualTo(EvaluationStatus.COMPLETE);
		// TOSO -Check precision
		assertThat(currencyOperand.getValue()).isEqualTo(new BigDecimal("12345.67"));
	}

	// Fails if code compiled with Java 6
	@Test
	public void test_euro()
	{
		doEuroTest(false);
		doEuroTest(true);
	}
	
	protected void doEuroTest(boolean useCountryOperand)
	{
        //int index = 0;
        BigDecimal expectedResult = new BigDecimal("12345.67");
  		for (Locale locale : getLocalesFromIso4217("EUR")) 
	    {   // Standard format for locale
  		    if (locale.toString().contains("#"))
  		        continue;
            //System.out.println(locale + " ==>" + NumberFormat.getCurrencyInstance(locale).format(12345.67));
  	    	testOperand(locale, NumberFormat.getCurrencyInstance(locale).format(12345.67), expectedResult, useCountryOperand);
	    }
        for (Locale locale : getLocalesFromIso4217("EUR")) 
        {   // Strip currency character and any space
            if (locale.toString().contains("#"))
                continue;
            String euroAmount = NumberFormat.getCurrencyInstance(locale).format(12345.67);
            if (Character.isDigit(euroAmount.charAt(0)))
                euroAmount = euroAmount.substring(0, euroAmount.length() - 2); 
            else if (Character.isDigit(euroAmount.charAt(euroAmount.length() - 1)))
                euroAmount = euroAmount.substring(1, euroAmount.length()).trim(); 
            //System.out.println(locale + " ==>" + euroAmount);
            testOperand(locale, euroAmount, expectedResult, useCountryOperand);
        }
        for (Locale locale : getLocalesFromIso4217("EUR")) 
        {   // Reverse standard format for locale
            if (locale.toString().contains("#"))
                continue;
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
            String euroAmount = numberFormat.format(12345.67);
            String euro = "€";
            if (Character.isDigit(euroAmount.charAt(0)))
                euroAmount = euro + euroAmount.substring(0, euroAmount.length() - 2);
            else if (Character.isDigit(euroAmount.charAt(euroAmount.length() - 1)))
            {
                euroAmount = euroAmount.substring(1, euroAmount.length());
                if (euroAmount.startsWith(" "))
                    euroAmount = euroAmount.trim() + euro;
                else
                    euroAmount = euroAmount.trim() + " " + euro;
             }
            //System.out.println(locale + " ==>" + euroAmount);
            testOperand(locale, euroAmount, expectedResult, useCountryOperand);
        }
        expectedResult = new BigDecimal("12345.00");
  		for (Locale locale : getLocalesFromIso4217("EUR")) 
	    {
            if (locale.toString().contains("#"))
                continue;
  	    	String testAmount = NumberFormat.getCurrencyInstance(locale).format(12345);
  	    	testOperand(locale, testAmount, expectedResult, useCountryOperand);
	    }
	}
	
	@Test
	public void test_format() throws IOException
	{
        //BigDecimal testAmount = new BigDecimal("1234567");
        File worldCurrencyList = ResourceHelper.getTestResourceFile("world-amount.lst");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(worldCurrencyList), "UTF-8"));
        for (Locale locale: Locale.getAvailableLocales())
		{
			if (!locale.getCountry().isEmpty() && 
				 locale.getVariant().isEmpty())
			{
				String country = locale.getCountry();
				if (locale.getCountry().equals("LU")) // Luxenburg has French and German formats
					country = locale.getLanguage() + "_" + country;
		    	DecimalFormat decformat = (DecimalFormat)NumberFormat.getCurrencyInstance(locale);
		    	int fractionDigits = decformat.getMaximumFractionDigits();
		    	String amountString = "1234567";
				if (fractionDigits > 0) {
		            char[] charArray1 = amountString.toCharArray();
		            char[] charArray2 = new char[charArray1.length + 1];
		            int decimalPos = charArray1.length - fractionDigits;
		            System.arraycopy(charArray1, 0, charArray2, 0, decimalPos);
		            charArray2[decimalPos] = '.';
		            System.arraycopy(charArray1, decimalPos, charArray2, decimalPos + 1, fractionDigits);
		            amountString = String.copyValueOf(charArray2);
				}
				BigDecimalOperand currencyOperand = new BigDecimalOperand(QNAME, new BigDecimal(amountString));
		        currencyOperand.operator = new CurrencyOperator();
		        try 
		        {
		        	currencyOperand.operator.getTrait().setLocale(locale);
		        } catch (ExpressionException e) {
		        	continue;
		        }
				//System.out.println(currencyOperand.operator.getTrait().formatValue(currencyOperand.getValue()));
	            String line = reader.readLine();
	            assertThat(currencyOperand.operator.getTrait().formatValue(currencyOperand.getValue())).isEqualTo(line);
			}
		}
		reader.close();
	}
	
	private void testOperand(Locale locale, String amount, BigDecimal expectedResult, boolean useCountryOperand)
	{
		String country = locale.getCountry();
		if (locale.getCountry().equals("LU")) // Luxenburg has French and German formats
			country = locale.getLanguage() + "_" + country;
		//    countryOperand = new StringOperand(QualifiedName.ANONYMOUS, country);
        BigDecimalOperand currencyOperand = new BigDecimalOperand(QNAME);
		CurrencyOperator currencyOperator = new CurrencyOperator();
		CurrencyTrait trait = (CurrencyTrait) currencyOperator.getTrait();
		if (useCountryOperand)
		{
		    Operand countryExpression = new ParseNameVariable("CC", new TestStringOperand("EvalCC", country));
		    CountryOperand countryOperand = new CountryOperand(new QualifiedName(NAME + QNAME.incrementReferenceCount(),QNAME ), trait, countryExpression);
		    currencyOperand.setRightOperand(countryOperand);
		}
		//else
       //     trait.setLocale(trait.getLocaleByCode(country));
		currencyOperand.operator = currencyOperator;
		Parameter localAmount = new Parameter(Term.ANONYMOUS, amount);
		assertThat(currencyOperand.unifyTerm(localAmount, 1)).isEqualTo(1);
		if (currencyOperand.getRightOperand() != null)
		    assertThat(currencyOperand.getRightOperand().evaluate(1)).isEqualTo(EvaluationStatus.COMPLETE);
		assertThat(currencyOperand.evaluate(1)).isEqualTo(EvaluationStatus.COMPLETE);
		assertThat(currencyOperand.getValue()).isEqualTo(expectedResult);
	}
	
	public static Collection<Locale> getLocalesFromIso4217(String iso4217code) 
    {
        Collection<Locale> returnValue = new LinkedList<Locale>();
        for (Locale locale : NumberFormat.getAvailableLocales()) 
        {
            String code = NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode();
            if (iso4217code.equals(code)) 
                returnValue.add(locale);
        }  
        return returnValue;
    }

}
