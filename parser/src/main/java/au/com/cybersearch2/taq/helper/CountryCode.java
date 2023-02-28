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

import java.util.Locale;
import java.util.Locale.IsoCountryCode;
import java.util.Set;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashSet;

/**
 * Validates Locale country code and currency code.
 * Caches ISO codes obtained using Locale functions
 *
 */
public class CountryCode {

	private static Set<String> PART1_ALPHA2;
	private static Set<String> PART1_ALPHA3;
	private static Set<String> CURRENCIES = new HashSet<>();

	public static boolean isValidCountryCode(String value) {
		if ((value.length() < 2) || (value.length() > 4))
			return false;
		Set<String> codes;
		switch(value.length()) {
		case 2: 
		    if (PART1_ALPHA2 == null)
				PART1_ALPHA2 = Locale.getISOCountries(IsoCountryCode.PART1_ALPHA2);
		    codes = PART1_ALPHA2;
		    break;
		case 3: 
		    if (PART1_ALPHA3 == null)
				PART1_ALPHA3 = Locale.getISOCountries(IsoCountryCode.PART1_ALPHA3);
		    codes = PART1_ALPHA3;
		    break;
		default: 
		    return false;
		}
		return codes.contains(value);
	}

	public static Currency getCurrency(String currencyCode) {
		if (isValidCurrencyCode(currencyCode)) {
			if ((currencyCode.length() == 2)) {
				for (String code3: CURRENCIES)
					if (code3.startsWith(currencyCode))
						return Currency.getInstance(code3);
			}
			return Currency.getInstance(currencyCode);
		}
		return null;
	}
	
	public static boolean isValidCurrencyCode(String value) {
		if ((value.length() != 3) && (value.length() != 2))
			return false;
		if (CURRENCIES.isEmpty())
			 Currency.getAvailableCurrencies()
			 	.forEach(currency ->  CURRENCIES.add(currency.getCurrencyCode()));
		if ((value.length() == 2)) {
			for (String code3: CURRENCIES)
				if (code3.startsWith(value))
					return true;
		}
		return CURRENCIES.contains(value);
	}
	
	public static Locale getLocaleByCountryCode(String countryCode) {
	   	return getLocale(countryCode, null);
	}
	
	public static Locale getLocaleByCurrency(Currency currency) {
		String currencyCode = currency.getCurrencyCode();
		if (currencyCode.equals("EUR"))
			// Euro formated according to German locale
			return Locale.GERMANY;
		if (currencyCode.equals("USD"))
			// Euro formated according to German locale
			return Locale.US;
	   	return getLocale(currencyCode.substring(0,2), currency);
	}
	
	private static Locale getLocale(String countryCode, Currency currency) {
		if (currency == null)
			currency = CountryCode.getCurrency(countryCode);
		Locale currencyLocale = null;
		Locale countryLocale = null;
	    for (Locale locale: NumberFormat.getAvailableLocales()) {
	    	if (locale.getCountry().equals(countryCode) && locale.getVariant().isEmpty()) {
	    		 if ((currency != null) && (Currency.getInstance(locale) == currency)) {
	    			 currencyLocale = locale;
	    			 break;
	    		 } else {
	    			 countryLocale = locale;
	    			 if (currency == null)
	    				 break;
	    		 }
	    	}
	    }
	    if (currencyLocale != null)
			return currencyLocale;
	    else if (countryLocale != null)
			return countryLocale;
	    else
	    	return null;
	}

}
