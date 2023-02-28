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

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.ExecutionTracker;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemDelegate;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;

/**
 * ItemVariable
 * Variable implementation for accessing array lists
 * @author Andrew Bowley
 * 1Jun.,2017
 */
public class ItemVariable<T> implements ListItemDelegate, ExecutionTracker
{
    /** Index information for value selection  */
	private final ListItemSpec indexData;
	
    /** The list to reference */
	private ItemList<T> itemList;
	/** Operand which must be evaluated for list to be created */
    private ListOperand<T> itemListOperand;
    /** Appends new items to an item list by attaching them to the end of the list */
    private final ItemAppender<T> itemAppender;
    /** ExecutionContext */
    protected ExecutionContext context;

    /**
     * @param itemList The list to reference 
     * @param indexData List item specification
     */
    public ItemVariable(ItemList<T> itemList, ListItemSpec indexData)
    {
        this.itemList = itemList;
        this.indexData = indexData;
        itemAppender = new ItemAppender<T>(this);
    }

	public void setListOperand(ListOperand<T> itemListOperand) {
		this.itemListOperand = itemListOperand;
	}
	
    @Override
    public Operand getOperand()
    {
       return indexData.getItemExpression();
    }
    
    @Override
    public ItemList<T> getItemList(int dimension)
    {
        return dimension == 1 ? itemList : null;
    }

    @SuppressWarnings("unchecked")
	@Override
    public ListIndex evaluate(int id)
    {   
    	if (itemListOperand != null)
    		itemList = (ItemList<T>)itemListOperand.getValue();
        if (itemList.isDynamic())
            itemList.evaluate(context);
        // Resolve parameters for array list
        indexData.assemble(itemList);
        indexData.evaluate(itemList, id);
        return indexData.getListIndex();
    }

    @Override
    public boolean backup(int id)
    {
        if (itemList.isDynamic())
            itemList.backup(true);
        Operand indexExpression = indexData.getItemExpression();
        if (indexExpression != null)
            return indexExpression.backup(id);
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setItemValue(Object value)
    {
        ListIndex listIndex = indexData.getListIndex();
        int index = listIndex.getIndex();
        if (index != -1)
        {   // Assign value to item list
            // Save old item, if available, to check if same as new value
            Object itemValue = itemList.hasItem(listIndex) ? itemList.getItem(listIndex) : null;
            // Use flag to signal whether update required
            boolean proceed = (itemValue == null);
            if (!proceed)
            {   // Check if update required by comparing new item to old one
                if (itemValue instanceof Term)
                    itemValue = ((Term)itemValue).getValue();
                proceed = !itemValue.equals(value);
            }
            if (proceed)
            {
            	//if ((itemList instanceof AxiomList) && 
            	//		!(value instanceof Axiom))
            	//	throw new ExpressionException("Array list cannot be set to " + value);
                if (itemList.getOperandType() == OperandType.TERM)
                    itemList.assignItem(indexData.getListIndex(), (T) new Parameter(Term.ANONYMOUS, value));
                else
                    itemList.assignItem(indexData.getListIndex(), (T)value);
            }
        }
    }

    @Override
    public Object getValue()
    {
        return getValue(indexData.getListIndex());
    }

    @Override
    public Object getValue(ListIndex selection)
    {
        // Index should be valid, but check for safety
        if (!itemList.hasItem(selection))
            return new Null();
        Object item = itemList.getItem(selection);
        if (selection.getPosition() != -1) {
        	if (item instanceof Term)
                return((Term)item).getValue();
        }
        return item;
    }

    @Override
    public void append(Object value)
    {
    	itemAppender.appendItem(value);
    }

	@Override
	public ListIndex getListIndex() {
		return indexData.getListIndex();
	}

	@Override
	public void setListIndex(int index) {
		indexData.setListIndex(new ListIndex(index));
	}

	@Override
	public void setExecutionContext(ExecutionContext context) {
		this.context = context;
	}
	
}
