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

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.ExecutionTracker;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * A Variable operand which accesses a list via a cursor. 
 *
 */
public class CursorItemVariable extends ListItemVariable {

	/** Encapsulates a cursor and it's associated list */
	private final CursorList cursorList;
	/** Operand created to perform type conversion */
	private final Operand typeOperand;
	/** Flag set true if this variable references the list with a cursor index */
	private boolean isCursorIndex;

	/**
	 * Construct CursorItemVariable object which directly uses given cursor as it's index data
	 * @param qname Qualified name of variable
	 * @param cursorList CursorList object
	 * @param typeOperand Operand created to perform type conversion
	 */
	public CursorItemVariable(QualifiedName qname, CursorList cursorList, Operand typeOperand) {
		this(qname, getListItemSpec(cursorList), cursorList, typeOperand);
		isCursorIndex = true;
	}

	/**
	 * Construct CursorItemVariable object which uses given cursor to calculate the array position
	 * @param qname Qualified name of variable
	 * @param indexDataArray
	 * @param cursorList CursorList object
	 * @param typeOperand Operand created to perform type conversion
	 */
	public CursorItemVariable(QualifiedName qname, ListItemSpec[] indexDataArray, CursorList cursorList, Operand typeOperand) {
		super(qname, indexDataArray, null);
		this.cursorList = cursorList;
		this.typeOperand = typeOperand;
        indexData.setQualifiedListName(cursorList.getQualifiedListName());
        setDelegate(cursorList.getItemList());
	}

	/**
     * Returns fact status flag
     * @return boolean
     */
    public boolean isFact()
    {
        return cursorList.getCursor().isFact();
    }
    
	public Cursor getCursor() {
		return cursorList.getCursor();
	}

	public CursorList getCursorList() {
		return cursorList;
	}

	/**
	 * Returns bound item list, if available, otherwise an empty list
	 * @return ItemList object
	 */
	public ItemList<?> getItemList() {
		ItemList<?> itemList = cursorList.getItemList();
		return itemList != null ? itemList : ListItemVariable.EMPTY_LIST;
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		ContextListHandler contextListHandler = getContextListHandler();
        if (contextListHandler != null) {
        	Operand dynamicOperand = contextListHandler.getDynamicOperand();
        	if (dynamicOperand != null) {
            		dynamicOperand.setExecutionContext(context);
        		dynamicOperand.evaluate(id);
        	}
        }
		// Evaluate cursor ensures it is initialized
		Cursor cursor = cursorList.getCursor();
	    // Get bound item list, if available, otherwise an empty list
		ItemList<?> itemList = ListItemVariable.EMPTY_LIST;
		if (!cursor.isOutofRange()) {
		    itemList = getItemList();
			cursor.evaluate(itemList);
		}
		// Get current cursor index in order to to create the next list index
    	int index = cursor.getIndex();
    	ListIndex newIndex = null;
    	// Check if array part of list index needs updating
		int arrayIndex = cursor.getListIndex().getIndex();
		if (arrayIndex != index) {
			// Update list index if in range
			if ((index >= 0)  && (index < itemList.getLength()))  
		        cursor.setListIndex(new ListIndex(index, -1));
		    else if (isCursorIndex)
		    	// Indicate cursor is out of range. 
		      	newIndex = new ListIndex(-1);
			// Continue when there is an array index available to sum with the cursor index 
		} 
		if (newIndex == null) {
        	if (delegate instanceof ExecutionTracker)
        		((ExecutionTracker)delegate).setExecutionContext(context);
			// Update current list index
			if (isCursorIndex) {
				// Ensure list index is valid only for array access 
				if (cursor.getListIndex().getPosition() != -1)
			        cursor.setListIndex(new ListIndex(index, -1));
				// Need to evaluate delegate even if returned index is not required
				delegate.evaluate(id);
	    		newIndex = cursor.getListIndex();
	        } else {
	        	// Calculate index by summing cursor index and delegate's array index
	        	if (arrayData == null)
	        	    indexData.setOffset(index);
	        	else
	        		arrayData.setOffset(index);
	        	newIndex = delegate.evaluate(id);
	        	// If the calculated index is out of range and the list is not empty, this is an error. 
	            if (newIndex.getIndex() != -1)
			        cursor.setListIndex(newIndex);
	    	}
		}
        if (newIndex.getIndex() != -1)
        {
    		Object assignValue = null;
            if ((leftOperand != null) && (empty || id == this.id)) {
            	EvaluationStatus status =  leftOperand.evaluate(id);
            	if (status == EvaluationStatus.COMPLETE) {
            		assignValue = leftOperand.getValue();
            	}
            }
	        if (assignValue != null) {
	            this.id = id;
	            setTermValue(assignValue);
	            delegate.setItemValue(assignValue);
	        } 
	        else if  (empty && !itemList.isEmpty() && (newIndex.getIndex() < itemList.getLength()))
	        {   // Update value 
	            this.id = id;
	            setValueByIndex(newIndex);
	        }
        }
        else if (!itemList.isEmpty() && !isCursorIndex)
       		throw new ExpressionException(String.format("Cursor %s out of range", cursor.getCursorQname().toString()));
        return EvaluationStatus.COMPLETE;
	}
	
    @Override
	public boolean backup(int id) {
	    ItemList<?> itemList = cursorList.getItemList();
	    if (itemList != null)
	    	cursorList.getCursor().backup(itemList, id);
	    return super.backup(id);
	}

	@Override
	public Object getValue() {
	        // Pass value through type conversion variable, if present
	        Object cursorValue = super.getValue();
	        if ((typeOperand != null) && (cursorValue.getClass() != Null.class))
	        {
	        	typeOperand.setValue(value);
	        	typeOperand.evaluate(id);
	            cursorValue = typeOperand.getValue();
	        }
	        return cursorValue;
	    }

	@SuppressWarnings("unchecked")
	@Override
	public void setDelegate(ItemList<?> itemList) {
        if (arrayData != null) {
        	if (itemList instanceof ListOperand)
                setDelegate(new AxiomVariable((ListOperand<Axiom>)itemList, new ListItemSpec[] { arrayData, indexData }));
        	else
                setDelegate(new AxiomVariable((AxiomList)itemList, new ListItemSpec[] { arrayData, indexData }));
       } else {
        	super.setDelegate(itemVariableInstance(itemList));
       }
	}
	
	private static ListItemSpec[] getListItemSpec(CursorList cursorList) {
		return new ListItemSpec[] {new CursorIndex(cursorList.getCursor(), cursorList.getQualifiedListName())};
	}

}
