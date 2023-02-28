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

import au.com.cybersearch2.taq.list.ListIndex;

/**
 * ListItemDelegate
 * Delegate for ListItemVariable, where operand details are finalized only when the Parser task is performed
 * @author Andrew Bowley
 * 1Jun.,2017
 */
public interface ListItemDelegate
{

    /**
     *Returns the operand
     *@return Operand object
     */
    Operand getOperand();

    /**
     * Evaluate to complete list binding, if using a list operand, and resolve list parameters
     * @param id Identity of caller, which must be provided for backup()
     * @return ListIndex object which references current value
     */
    ListIndex evaluate(int id);

    /**
     * backup
     * @param id Identity of caller
     * @return boolean
     */
    boolean backup(int id);

    /**
     * Append to list
     * @param value Value
     */
    void append(Object value);
    
    /**
     * Set item value
     * @param value Value
     */
    void setItemValue(Object value);

    /**
     * Returns value from item list
     * @return value as object
     */
    Object getValue();

    /**
     * Returns value at specified index in item list
     * @param listIndex References an array list item or term of an axiom list item
     * @return value as object
     */
    Object getValue(ListIndex selection);

    /**
     * Return list being referenced
     * @param dimension Which dimension, 1 or 2
     * @return the itemList or null if not set
     */
    ItemList<?> getItemList(int dimension);

    ListIndex getListIndex();
    
    void setListIndex(int index);
}
