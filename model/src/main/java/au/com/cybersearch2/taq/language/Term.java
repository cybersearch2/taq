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
package au.com.cybersearch2.taq.language;

/**
 * Term
 * Interface for all parameters which participate in iterations of unify, evaluate, backup.
 * @author Andrew Bowley
 * 10 Dec 2014
 */
public interface Term
{
    /** Empty name constant */
	public static final String ANONYMOUS = "";

	/**
	 * Returns Parameter value or null if not assigned
	 * @return Object
	 */
	Object getValue();

	/**
	 * Returns Parameter value class or Null.class if value is null
	 * @return Class object
	 */
    Class<?> getValueClass();

    /**
     * Set Parameter name. 
     * This means the Term is annoymous until a name is assigned to it.
     * @param name String
	 * @throws IllegalStateException if name has already been assigned
     */
    void setName(String name);
    
    /**
     * Returns Parameter name
     * @return String
     */
	String getName();

	/**
	 * Returns true if no value has been assigned to this Parameter
	 * @return boolean true if empty
	 */
	boolean isEmpty();

	/**
	 * Perform unification with other Term. 
	 * One Term must be empty and the other not empty.  
	 * If unification is successful, then the two terms will be equivalent. 
	 * @param otherTerm Term with which to unify
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Identity passed in param "id" or zero if unification failed
	 */
	int unifyTerm(Term otherTerm, int id);
	
    /**
     * Set value, mark Term as not empty
     * @param value Object. If null a Null object will be set and empty status unchanged
     */
	void setValue(Object value);
	
    /**
     * Set value to Null, mark Term as empty and set id to 0
     */
     void clearValue();
	
	/**
     * Returns id
     * @return int
     */
    int getId(); 

}
