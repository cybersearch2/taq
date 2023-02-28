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
package au.com.cybersearch2.taq.provider;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.interfaces.LocaleListener;

/**
 * Scope functions available by object interface
 *
 */
public class ScopeFunctions implements LocaleListener {

	private Scope scope; 
	
	public ScopeFunctions(Scope scope) {
		this.scope = scope;
	}

	public String name() {
		return scope.getName();
	}

	public String locale() {
		return scope.getLocale().toString();
	}
	
    /**
     * Returns the language code of this Locale.
     *
     * <p><b>Note:</b> ISO 639 is not a stable standard&mdash; some languages' codes have changed.
     * Locale's constructor recognizes both the new and the old codes for the languages
     * whose codes have changed, but this function always returns the old code.
     * @return The language code, or the empty string if none is defined.
     * @see #getDisplayLanguage
     */
    public String language() {
        return scope.getLocale().getLanguage();
    }

    /**
     * Returns the script for this locale, which should
     * either be the empty string or an ISO 15924 4-letter script
     * code. The first letter is uppercase and the rest are
     * lowercase, for example, 'Latn', 'Cyrl'.
     *
     * @return The script code, or the empty string if none is defined.
     * @see #getDisplayScript
     * @since 1.7
     */
    public String script() {
        return scope.getLocale().getScript();
    }

    /**
     * Returns the country/region code for this locale, which should
     * either be the empty string, an uppercase ISO 3166 2-letter code,
     * or a UN M.49 3-digit code.
     *
     * @return The country/region code, or the empty string if none is defined.
     * @see #getDisplayCountry
     */
    public String country() {
        return scope.getLocale().getCountry();
    }

    /**
     * Returns the country/region code for this locale, which should
     * either be the empty string, an uppercase ISO 3166 2-letter code,
     * or a UN M.49 3-digit code.
     *
     * @return The country/region code, or the empty string if none is defined.
     * @see #getDisplayCountry
     */
    public String region() {
        return scope.getLocale().getCountry();
    }

    /**
     * Returns the variant code for this locale.
     *
     * @return The variant code, or the empty string if none is defined.
     * @see #getDisplayVariant
     */
    public String variant() {
        return scope.getLocale().getVariant();
    }

	@Override
	public boolean onScopeChange(Scope scope) {
		if (this.scope != scope) {
		    this.scope = scope;
		    return true;
		}
		return false;
	}
}
