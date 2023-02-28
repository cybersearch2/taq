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
package au.com.cybersearch2.taq.interfaces;

import java.util.Iterator;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * AxiomGenerator
 * Provides Axiom iterators which navigate collections identified by name
 * @author Andrew Bowley
 *
 * @since 06/10/2010
 */
public interface AxiomSource
{
	/**
	 * Returns axiom iterator
	 * @param context Execution context
	 * @return Iterator of generic type Axiom
	 */
	Iterator<Axiom> iterator(ExecutionContext context);
	/**
	 * Return axiom archetype
	 * @return Archetype for axiom
	 */
	Archetype<Axiom,Term> getArchetype();
}
