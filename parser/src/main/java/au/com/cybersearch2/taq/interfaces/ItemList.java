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

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.ListIndex;
import au.com.cybersearch2.taq.result.ResultList;

/**
 * ItemList
 * Object collection accessed in script using square brackets array notation
 * @author Andrew Bowley
 * 19 Jan 2015
 */
public interface ItemList<T> extends Iterable<T>, SourceInfo
{

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
    void assignItem(ListIndex listIndex, T value);

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
	T getItem(ListIndex listIndex);

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
    boolean hasItem(ListIndex index);

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

    /**
     * Returns empty clone of this list
     * @return ItemList object of parametric type
     */
    ItemList<T> newInstance();

    /**
     * Returns shallow copy of this object, leaving is in cleared state
     * @return ResultList object
     */
    ResultList<?> getSolution();
 
    /**
     * Returns flag set true if list is dynamic
     * @return
     */
    default boolean isDynamic() {
    	return false;
    }
    
    /**
     * Evaluate list - applicable only to dynamic lists
     * @param executionContext Execution context
     * @return EvaluationStatus
     */
     default EvaluationStatus evaluate(ExecutionContext executionContext)
     {
         return EvaluationStatus.COMPLETE;
     }

     /**
      * Backup from last evaluation - applicable only to dynamic lists.
      * @param partial Flag to indicate backup to before previous unification or backup to start
      * @return Flag to indicate if this Structure is ready to continue unification
      */
     default boolean backup(boolean partial)
     {
         return true;
     }
     
}
