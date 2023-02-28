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
package au.com.cybersearch2.taq.compile;

import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * Axiom export declaration is saved to this object
 */
public class ProtoAxiom {

	private Axiom axiom;
	private final boolean isExported;

	/**
	 * Create ProtoAxiom object
	 * @param isExported Flag set true if axiom is exported
	 */
	public ProtoAxiom(boolean isExported) {
		this.isExported = isExported;
	}

	public void add(Axiom axiom) {
		this.axiom = axiom;
	}
	
	public Axiom getAxiom() {
		return axiom;
	}

	public boolean isExported() {
		return isExported;
	}


}
