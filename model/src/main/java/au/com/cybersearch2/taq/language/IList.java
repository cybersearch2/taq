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

public interface IList<T> {


    /**
     * Returns number of items in list
     * @return int
     */
    int getLength();

    /**
     * Returns name of list
     * @return String
     */
    String getName();

	/**
	 * Returns start index
	 * @return int
	 */
	int getOffset();
	
    /**
     * Returns qualified name
     * @return QualifiedName object
     */
    QualifiedName getQualifiedName();

    /**
     * Returns true if list is empty
     * @return boolean
     */
    boolean isEmpty();

    /**
     * Assign value to list item referenced by index. 
     * The list may grow to accommodate new item depending on implementation.
     * @param listIndex References an array list item or term of an axiom list item
     * @param value Object
     */
    //void assignItem(ListIndex listIndex, T value);

    /**
     * Append given value to list
     * @param value Item to append
     */
    void append(T value);
    
    /**
     * Returns item referenced by list index
     * @param listIndex References an array list item or term of an axiom list item
     * @return Object of generic type T 
     */
	//T getItem(ListIndex listIndex);

	/**
	 * Returns OperandType of items in this container
	 * @return OperandType enum
	 */
	OperandType getOperandType();
	
	/**
	 * Returns true if index is valid and item exists reference by that index
     * @param listIndex References an array list item or term of an axiom list item
	 * @return boolean
	 */
    //boolean hasItem(ListIndex index);

    /**
     * Returns implementation of Iterable interface
     * @return Iterable of generice type T
     */
	Iterable<T> getIterable();

	/**
	 * Clear item list
	 */
	void clear();

	/**
     * @return public flag
     */
    boolean isPublic();

    /**
     * @param isPublic Public flag
     */
    void setPublic(boolean isPublic);
}
