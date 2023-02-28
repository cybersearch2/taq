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

import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.ListIndex;

/**
 * Interface for object which tracks current list item coordinates. In addition to having an item index, 
 * it must have a term index too for the case of referencing a term of an axiom list item,
 * The term index is persisted as either an integer position or as a term name.  The term index d has value -1 if 
 * if a term is not referenced. 
 *
 */
public interface ListItemSpec
{

    /**
     * Returns name of axiom list
     * @return String
     */
    String getListName();

    /**
     * Returns name of axiom list
     * @return String
     */
    QualifiedName getQualifiedListName();
    
    /**
     * Sets name of axiom list
     * @param qualifiedListName Qualified name
     */
    void setQualifiedListName(QualifiedName qualifiedListName);

    /**
     * Returns unique name for variable to reference axiom list
     * @return String
     */
    QualifiedName getVariableName();

    /**
     * Set reference to an array list item or term of an list item 
     * @param index Index to set
     */
    void setListIndex(ListIndex index);
 
    /**
     * Returns reference to an array list item or term of an list item
     * @return ListIndex object
     */
    ListIndex getListIndex();

    /**
     * Returns Compiler operand for term selection
     * @return Operand object
     */
    Operand getItemExpression();

    /**
     * Returns Text to append to name of variable
     * @return String
     */
    String getSuffix();

    /**
     * Sets text to append to name of variable
     * @param suffix Suffix
     */
    void setSuffix(String suffix);

    /**
     * Complete binding to item list
     * @param itemList The item list
     */
    void assemble(ItemList<?> itemList);
    
    /**
     * Evaluate index used to select value
     * @param itemList The item list
     * @param id Modification id
     * @return flag set true if evaluation successful
     */
    boolean evaluate(ItemList<?> itemList, int id);

    /**
     * Set cursor offset
     * @param offset Offset value
     */
    void setOffset(int offset);
    
    int getOffset();
}
