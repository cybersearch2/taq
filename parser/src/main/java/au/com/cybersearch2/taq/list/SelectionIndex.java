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
package au.com.cybersearch2.taq.list;

import java.util.List;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * SelectionIndex
 * Extends ArrayIndex to provide support for string indexes
 * @author Andrew Bowley
 * 23May,2017
 */
public class SelectionIndex extends ArrayIndex
{
    /** Selection string */
    private String selection;

    /**
     * Construct ListIndex with specified selection value
     * @param qname Qualified list name
     * @param selection Selection value
     */
    public SelectionIndex(QualifiedName qname, String selection)
    {
        super(qname, null, selection);
        this.selection = selection;
    }

    /**
     * Construct ListIndex with specified index operand
     * @param qname Qualified list name
     * @param indexExpression Index evaluation operand
     */
    public SelectionIndex(QualifiedName qname, Operand indexExpression)
    {
        super(qname, indexExpression);
    }
 
    /**
     * Returns selection string
     * @return String
     */
    public String getSelection()
    {
        return selection;
    }
 
    /**
     * assemble
     * @see au.com.cybersearch2.taq.list.ArrayIndex#assemble(au.com.cybersearch2.taq.interfaces.ItemList)
     */
    @SuppressWarnings("rawtypes")
	@Override
    public void assemble(ItemList<?> itemList)
    {
    	if (itemList instanceof ListOperand)
    		itemList = (ItemList<?>) ((ListOperand)itemList).getValue();
        if ((selection != null) && (itemList instanceof AxiomContainer))
            setAxiomTermListIndex((AxiomContainer)itemList);
        super.assemble(itemList);
    }
    
    /**
     * getVariableName
     * @see au.com.cybersearch2.taq.list.ArrayIndex#getVariableName()
     */
    @Override
    public QualifiedName getVariableName()
    {
        return new QualifiedName(getVariableName(qname.getName(), suffix) + qname.incrementReferenceCount(), qname);
    }
    /**
     * Returns variable name given list name and suffix
     * @param listName
     * @param suffix
     * @return String
     */
    protected String getVariableName(String listName, String suffix)
    {
        return listName + "_" + suffix;
    }

    /**
     * setIntIndex
     * @see au.com.cybersearch2.taq.list.ArrayIndex#setIntIndex()
     */
    @Override
    protected int setIntIndex()
    {
        Object object = indexExpression.getValue();
        int index = (object instanceof Long) ? ((Long)object).intValue() : ((Integer)object).intValue();
        if (indexExpression.getName().isEmpty())
            //suffix = getListName() + "." + index;
            suffix = Integer.toString(index);
        else 
            suffix = indexExpression.getName();
        return index;
    }

    /**
     * setStringIndex
     * @see au.com.cybersearch2.taq.list.ArrayIndex#setStringIndex(au.com.cybersearch2.taq.interfaces.ItemList)
     */
    @Override
    protected void setStringIndex(ItemList<?> itemList)
    {
        if (itemList instanceof AxiomTermList)
        {
            suffix = selection = indexExpression.getValue().toString();
            setAxiomTermListIndex((AxiomTermList)itemList);
        }
    }

    /**
     * Set axiom term names
     * @param axiomContainer Axiom list or axiom term list
     */
    protected void setAxiomTermListIndex(AxiomContainer axiomContainer)
    {
        List<String> axiomTermNameList = axiomContainer.getAxiomTermNameList();
        if ((axiomTermNameList != null) && !suffix.isEmpty()) {
            int index = getIndexForName(suffix, axiomTermNameList);
            if (index == -1) {
            	if (!qname.isGlobalName() || !qname.getName().equals("scope")) 
                    throw new ExpressionException("List \"" + getListName() + "\" does not have term named \"" + suffix + "\"");
            }
            setIndex(index);
        } else
            throw new ExpressionException("List \"" + getListName() + "\" term names not available for indexed access");
    }


    /**
     * Returns index of item identified by name
     * @param item Item name
     * @param axiomTermNameList Term names of axiom source
     * @return Index
     */
    private int getIndexForName(String item, List<String> axiomTermNameList) 
    {
        for (int i = 0; i < axiomTermNameList.size(); i++)
        {
            if (item.equals(axiomTermNameList.get(i)))
                return i;
        }
        return -1;
    }



}
