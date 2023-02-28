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
package au.com.cybersearch2.taq.db;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * DataEmitter
 * Interface for pre-processing of a data object to be emitted as an entity object.
 * The DataEmitter implementation takes a data object
 * and returns an entity object of the Entity type, which
 * can also be the Data type if only one of more fields are to 
 * have values modified.
 * @param <Data>   Data type
 * @param <Entity> Entity type
 */
public abstract class DataEmitter<Data,Entity> {

	private final Class<Data> dataClass;

	/**
	 * Construct DataEmitter object
	 * @param dataClass Data class
	 */
	public DataEmitter(Class<Data> dataClass) {
		this.dataClass = dataClass;
	}

	/**
	 * Returns list of entity objects created from given data object
	 * @param data Data object
	 * @return Entity object list
	 */
	abstract protected List<Entity> emit(Data data);
	
	public Class<Data> getDataClass() {
		return dataClass;
	}

	/**
     * Returns list of entity objects created from given axiom	 
     * @param axiom Data-mapped axiom
     * @param locale Locale
	 * @param axiomConverter Axiom converter creates entity objects
	 * @return Entity object list
	 * @throws ExecutionException
	 */
	public List<Entity> emit(Axiom axiom, Locale locale, AxiomConverter axiomConverter) throws ExecutionException {
		 return emit(axiomConverter.getEntityFromAxiom(axiom, dataClass, locale)); 	
    }
}
