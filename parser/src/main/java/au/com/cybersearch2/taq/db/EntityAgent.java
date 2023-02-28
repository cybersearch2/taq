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

import java.util.Locale;

import au.com.cybersearch2.taq.pattern.Axiom;

/**
 *  performs database resource operations relating to entity classes
 * @param <C> Entity collector customized for database implementation
 * @param <E> Entity emitter customized for database implementation
 */
public interface EntityAgent<C extends EntityCollector<?>,E extends EntityEmitter<?>> {

	/**
	 * Returns flag set true if collector with given axiom name has been added
	 * @param axiomName
	 * @return boolean
	 */
	boolean hasCollector(String axiomName);
	
	/**
	 * Returns flag set true if emitter with given axiom name has been added
	 * @param axiomName
	 * @return boolean
	 */
	boolean hasEmitter(String axiomName);
	
	/**
	 * Emit given axiom
	 * @param axiom Axiom
	 * @param locale Locale
	 */
	void emit(Axiom axiom, Locale locale);
	
	/**
	 * Returns object which translates a database row selected by primary key to an entity object
	 * @param name Axiom name of collector which provides the selector
	 * @return ObjectSelector object
	 */
	ObjectSelector<?> getObjectSelector(String name);

	/**
	 * Associate Entity Class with axiom name 
	 * @param axiomName Axiom name
	 * @param entityClass Entity class
	 */
	<T> void addCollectorEntity(String axiomName, Class<T> entityClass);

	/**
	 * Associate Entity Class with axiom name 
	 * @param axiomName Axiom name
	 * @param entityClassName Entity class name
	 */
	void addCollectorEntity(String axiomName, String entityClassName);
	
	/**
	 * Associate Entity Collector with axiom name
	 * @param axiomName Axiom name
	 * @param entityCollector EntityCollector object
	 */
	void addCollector(String axiomName, C entityCollector);

	/**
	 * Associate Entity Class with axiom name 
	 * @param axiomName Axiom name
	 * @param entityClass Entity class
	 */
	<T> E addEmitterEntity(String axiomName, Class<T> entityClass);

	/**
	 * Associate Entity Class with axiom name 
	 * @param axiomName Axiom name
	 * @param entityClassName Entity class name
	 */
	E addEmitterEntity(String axiomName, String entityClassName);

	/**
	 * Associate Entity Emitter with axiom name
	 * @param axiomName Axiom name
	 * @param entityEmitter EntityEmitter object
	 */
	void addEmitter(String axiomName, E entityEmitter);

}
