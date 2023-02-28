/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.interfaces;

import java.util.Locale;

import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * Receives notifications when an axiom provider has produced an axiom.
 * Locale is included for correct text to number conversions.
 */
public interface LocaleAxiomListener {

	/**
	 * Handle next axiom loaded by processor
	 * @param qname Qualified name of axiom
	 * @param axiom Axiom object
	 * @param locale Locale for text to number conversions
	 * @return flag set true if the axiom should be passed to the next listener
	 */
	boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale);
	
	default QualifiedName getName() {
		return QualifiedName.ANONYMOUS;
	}
}
