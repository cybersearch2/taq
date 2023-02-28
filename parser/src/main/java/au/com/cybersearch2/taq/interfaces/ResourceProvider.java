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

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * ResourceProvider
 * Provides external systems used to input and output data
 * @author Andrew Bowley
 */
public interface ResourceProvider
{
    /**
     * Returns Axiom Provider identity
     * @return String
     */
    String getName();
    
    /**
     * Open resource
      */
    void open() throws ExpressionException;

    /**
     * Close to free all resources used by provider
     */
    void close();
    
    /**
     * Returns axiom iterator
     * @param archetype Axiom archetype to define axiom name and term names
     * @return Axiom iterator
     */
    Iterator<Axiom> iterator(AxiomArchetype archetype);

    /** 
     * Returns listener to notify when an axiom is passed to this provider 
     * @param axiomName Axiom key
     * @return LocaleAxiomListener object
     */
    LocaleAxiomListener getAxiomListener(String axiomName);
    
    /** 
     * Returns flag to indicate if no axioms are available from the provider 
     * @return boolean
     */
    boolean isEmpty();
 
    /**
     * Chain given axiom listener ahead of existing listeners
     * @param axiomListener Axiom listener
     * @return flag set true if this feature is supported
     */
    default boolean chainAxiomListener(LocaleAxiomListener axiomListener) {
    	return false;
    }

    /**
     * Set property
     * @param key Key
     * @param value Value cast as Object type
     * @return flag set true if this feature is supported
     */
	default boolean setProperty(String string, Object value) {
    	return false;
	}

}
