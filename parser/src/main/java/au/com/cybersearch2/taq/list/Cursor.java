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

import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Cursor
 * List index that increments, decrements and can set be set to an arbitrary value. 
 * The index may not necessarily always be in range for the list to which it is bound.
 * @author Andrew Bowley
 */
public class Cursor
{
	/** Direction to progress enum: forward or reverse */
	public enum Direction{ forward, reverse }
    /** Qualified name of cursor */
	private final QualifiedName cursorQname;
	/** Optional type of list item */
	private final VariableSpec variableSpec;
	/** Flag set true if initial direction is reverse */
	private final Direction initialDirection;
	
	/** Index - if out of range, isFact = false */
	private int cursorIndex;
	/** List index for current list item. This is not updated if the cursor index changes to an out of range value. */
	private ListIndex listIndex;
    /** Flag set false when cursor index is out of range */
	protected boolean isFact;
	/** Flag set false on backup and true on next evaluation */
	protected boolean isActive;
    /** Direction to progress -  indicates whether stepping in reverse or forwards */
    protected Direction direction;
	
	/**
	 * Construct a Cursor object
	 * @param cursorQname Cusror qualified name
     * @param varSpec Variable specification or null
	 * @param isReverse Flag set true if initial direction is reverse
	 */
	public Cursor(QualifiedName cursorQname, VariableSpec variableSpec, boolean isReverse) 
	{
		this.cursorQname = cursorQname;
		this.variableSpec = variableSpec;
		initialDirection = isReverse ? Direction.reverse : Direction.forward;
		// Initial state is updated on fist evaluation which is first opportunity 
		// to access the bound list
		cursorIndex = -1;
		listIndex = new ListIndex(-1);
		isFact = false; 
	}

	/**
     * Returns fact status flag
     * @return boolean
     */
    public boolean isFact()
    {
        return isFact;
    }

    /**
     * Returns out-of-range flag. Not initialized is excluded.
     * When the flag is set, any item list variable bound to this cursor
     * should act as though the list is empty.
     * @return boolean
     */
    public boolean isOutofRange() {
		return (direction != null) && isActive && !isFact;
	}

	/**
     * Set list index for currently selected item
     * @param liatIndex List index for current list item
     * @see CursorItemVariable
     */
    public void setListIndex(ListIndex liatIndex) {
    	this.listIndex = liatIndex;
    }
    
    /**
     * Returns current list index.
     * @return ListIndex object
     */
    public ListIndex getListIndex()
    {
        return listIndex;
    }

    /**
     * Returns cursor index. Can be out of valid range for bound list.
     * @return int
     */
    public int getIndex()
    {
        return cursorIndex;
    }

    public QualifiedName getCursorQname() {
		return cursorQname;
	}

	public VariableSpec getVariableSpec() {
		return variableSpec;
	}

	/**
     * Sets index 
     * @param itemList Bound list
     * @param index Next cursor index
     */
    public void setIndex(int index, ItemList<?> itemList)
    {
        cursorIndex = index;
        int size = itemList.getLength();
        if ((size == 0) || // Cannot set index of empty array
        	(index >= size) || (index < 0))	
            isFact = false;
    }

    /**
     * Reset cursor to navigate forward from start of list
     * @param itemList Bound list
     * @return previous cursor position
     */
    public long forward(ItemList<?> itemList)
    {
    	return forward(itemList, 0);
    }
    
    /**
     * Reset cursor to navigate forward from given list position
     * @param itemList Bound list
     * @param position  Index to start from
     * @return cursor position or -1 if cursor out of range
     */
    public long forward(ItemList<?> itemList, int position)
    {
        direction = Direction.forward;
        int size = itemList.getLength();
        cursorIndex = position;
        if (position < size)
        {
            isFact = true;
            return position;
        }
        else // Cannot set index - array too short
        {
            isFact = false;
            return -1;
        }
    }

    /**
     * Reset cursor to navigate in reverse from end of list
     * @param itemList Bound list
     * @return cursor position or -1 if cursor out of range
     */
    public long reverse(ItemList<?> itemList)
    {
    	return reverse(itemList, itemList.getLength() - 1);
    }
    
    /**
     * Reset cursor to navigate in reverse from end of list
     * @param itemList Bound list
     * @return previous cursor position
     */
    public long reverse(ItemList<?> itemList, int position)
    {
        direction = Direction.reverse;
        int size = itemList.getLength();
        cursorIndex = position;
        if (position < size) 
        {
            isFact = true;
            return position;
        }
        else // Cannot set index - array too short
        {
            isFact = false;
            return -1;
        }
    }
    
	public void evaluate(ItemList<?> itemList) 
	{
        if (direction == null) { // First time initialization
        	isActive = true;
			if (initialDirection == Direction.reverse)
				reverse(itemList);
			else
				forward(itemList);
        } else if (!isActive) {
        	if (!itemList.isEmpty()) {
        		// Sync after backup
        	    isActive = true;
        	    if (direction == Direction.forward) {
        	    	if (cursorIndex == 0)
        	    	    forward(itemList);
        	    	else
        	    		isFact = true;
        	    } else 
    				reverse(itemList);
        	}
        }
	}

	/**
	 * Backup to initial state if full backup required
     * @param itemList Bound list
	 * @param id Modification id , 0 means full backup
	 * @return true
	 */
    public boolean backup(ItemList<?> itemList, int id) 
    {  
        // Id ignored if non-zero to avoid problems with detecting navigation
    	// to the end of the list
        if ((id == 0) && isActive)
        {
            if (itemList == null)
            	throw new ExpressionException("Referenced list is missing");
         	int endPos = itemList.getLength() - 1;
            if ((direction == Direction.forward) && (cursorIndex != 0))
                forward(itemList);
            else if ((direction == Direction.reverse) && (cursorIndex != endPos))
                reverse(itemList, endPos);
            isActive = false;
        }
        return true;
    }
 
    /**
     * Move cursor to position specified by given parameter
     * @param parameter Parameter containing integer value
     * @param itemList Bound list
     */
    public void assign(Parameter parameter, ItemList<?> itemList)
    {
        int index = ((Long)parameter.getValue()).intValue();
        setIndex(index, itemList);
    }

    @Override
    public String toString()
    {
        if (!isFact)
            return Boolean.FALSE.toString();
        return super.toString();
    }

	protected Direction getDirection() {
        if (direction == null)  // First time initialization
        	return initialDirection;
        return direction;
	}

}
