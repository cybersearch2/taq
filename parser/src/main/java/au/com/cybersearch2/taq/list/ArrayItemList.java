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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.Concaten;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.result.ResultList;

/**
 * ArrayItemList
 * List implementation. Generic types are String, Integer, Double, BigDecimal and Boolean
 * A single Operator object is shared by dependent ItemListVariable objects which take their values from the list.
 * This object resides in an OperandMap never directly interacts with other operands.
 * @author Andrew Bowley
 * 15 Jan 2015
 */
public class ArrayItemList<T> implements ItemList<T>, Concaten<T> 
{
    /** The list items */
    protected List<T> valueList;
    /** Source item to be updated in parser task */
    protected SourceItem sourceItem;
    /** Qualified name */
    protected QualifiedName qname;
    /** Operand type */
    protected OperandType operandType;
    /** Preset size */
    protected int size;
    /** Offset when list does not start at index of zero */
    protected int offset;
    /** Flag set true if list is exported */
    protected boolean isPublic;

    /**
     * Construct a ArrayItemList object
     * @param operandType Operand type of list items 
     * @param qname Qualified name 
     */
	public ArrayItemList(OperandType operandType, QualifiedName qname) {
	    this(operandType, qname, new ArrayList<T>());
	}

    /**
     * Construct a ArrayItemList object
     * @param operandType Operand type of list items 
     * @param qname Qualified name 
     */
	protected ArrayItemList(OperandType operandType, QualifiedName qname, List<T> valueList) {
	    this.operandType = operandType;
		this.qname = qname;
		this.valueList =valueList;
		// Preset size of -1 means none
		size = Integer.MIN_VALUE;
	}
	
    /**
     * Returns item referenced by list index
     * @param index References an array list item
     * @return Object of generic type T 
     */
	public T getItem(int index) {
		T item = (T) valueList.get(index);
		if (item == null)
			throw new ExpressionException(getName() + " item " + index + " not found");
		return item;
	}

    /**
     * Assign value to list item referenced by index. 
     * The list may grow to accommodate new item depending on implementation.
     * @param index References an array list item
     */
	public void assignItem(int index, T value) {
	    int listSize =  valueList.size();
		if (index < listSize)
			valueList.set(index, value);
		else if ((listSize == index) || (index < size))
		{
			((ArrayList<T>)valueList).ensureCapacity(index + 1);
			for (int i = valueList.size(); i < index; i++)
				valueList.add(null);
			valueList.add(index, value);
	        if (sourceItem != null)
	            sourceItem.setInformation(toString());
	        if (valueList.size() > size)
	            size = valueList.size();
		}
		else
		    throw new ExpressionException("Index " + (index + offset) + " out of range for list \"" + getName() + "\"");
	}

	/**
	 * Preset size
	 * @param size Max permitted number of items in this list
	 */
	public void setSize(int size)
	{
	    this.size = size;
	}

    /**
     * Set start index
     * @param offset Start index
     */
    public void setOffset(int offset)
    {
        this.offset = offset;
    }
    
	/**
	 * Returns start index
	 * @return int
	 */
    @Override
	public int getOffset()
	{
	    return offset;
	}
	
    @SuppressWarnings("unchecked")
    public void assignItem(int index, Term term)
    {
        assignItem(new ListIndex(index), (T) term.getValue());
    }

    @SuppressWarnings("unchecked")
    public void assignObject(int index, Object value)
    {
        assignItem(new ListIndex(index), (T)value);
    }

	/**
	 * Returns number of items in array
	 * @return int
	 */
	@Override
	public int getLength()
	{
		return valueList.size();
	}

	/**
	 * getQualifiedName
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#getQualifiedName()
	 */
    @Override
    public QualifiedName getQualifiedName()
    {
        return qname;
    }

	/**
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#getName()
	 */
	@Override
	public String getName() 
	{
		String name = qname.getName();
		return name.isEmpty()? qname.getTemplate() : name;
	}

	/**
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#isEmpty()
	 */
	@Override
	public boolean isEmpty() 
	{
		return valueList.isEmpty();
	}

	@Override
	public boolean hasItem(ListIndex listIndex) {
		int index = listIndex.getIndex();
		if (index < 0)
			return false;
		return index < valueList.size() ? valueList.get(index) != null : false;
	}
	
	@Override
	public void assignItem(ListIndex listIndex, T value) {
		assignItem(listIndex.getIndex(), value);
	}
	
	@Override
	public T getItem(ListIndex listIndex) {
		return getItem(listIndex.getIndex());
	}
	
	/**
	 * iterator
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() 
	{
		return (Iterator<T>) valueList.iterator();
	}

	/**
	 * getIterable
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#getIterable()
	 */
	@Override
	public Iterable<T> getIterable() 
	{
		/** Copy of the list items so original can be cleared */
		final   ArrayList<T> valueList2 = new ArrayList<T>();
		valueList2.addAll(valueList);

		return new Iterable<T>()
		{
			@Override
			public Iterator<T> iterator() 
			{   // Return iterator pointing to first non-null member of list
				Iterator<T> iter = (Iterator<T>)valueList2.listIterator();
				for (int index = 0; index < valueList2.size(); index++)
				{
					if (valueList2.get(index) != null)
					    break;
					iter.next();
				}
				return  iter;
			}
		};
	}

	/**
	 * clear
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#clear()
	 */
	@Override
	public void clear() 
	{
		valueList.clear();
        if (sourceItem != null)
            sourceItem.setInformation(toString());
	}

    /**
     * @return public flag
     */
    @Override
    public boolean isPublic()
    {
        return isPublic;
    }

    /**
     * @param isPublic Public flag
     */
    @Override
    public void setPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }

    @Override
    public void setSourceItem(SourceItem sourceItem)
    {
        this.sourceItem = sourceItem;
    }

    /**
     * toString
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() 
    {
        return "List <" + operandType.toString().toLowerCase() + ">[" + valueList.size() + "]";
    }

    @Override
    public OperandType getOperandType()
    {
        return operandType;
    }

	@Override
	public void append(T value) {
        assignItem(valueList.size(), value);
	}

	@Override
	public ItemList<T> concatenate(T value) {
        assignItem(valueList.size(), value);
        return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ItemList<T> newInstance() {
		return (ItemList<T>) newInstance(operandType, qname);
	}
	
	@Override
	public ResultList<T> getSolution() {
		ResultList<T> resultList = new ResultList<T>() {

			private List<T> items = valueList;
			
			@Override
			public QualifiedName getQualifiedName() {
				return qname;
			}

			@Override
			public OperandType getOperandType() {
				return operandType;
			}

			@Override
			public List<T> getList() {
				return items;
			}
			
			@Override
			public int getOffset() {
				return offset;
			}
			
		};
		valueList = new ArrayList<T>();
		return resultList;
	}

	public static ItemList<?> newInstance(OperandType operandType, QualifiedName  qname) {
	    switch (operandType)
	    {
        case INTEGER:
            return new ArrayItemList<Long>(operandType, qname);
        case DOUBLE:
            return new ArrayItemList<Double>(operandType, qname);
        case BOOLEAN:
            return new ArrayItemList<Boolean>(operandType, qname);
        case STRING:
            return new ArrayItemList<String>(operandType, qname);
        case DECIMAL:
        case CURRENCY:
            return new ArrayItemList<BigDecimal>(operandType, qname);
        case UNKNOWN:
            return new ArrayItemList<Unknown>(operandType, qname);
        default:
        	throw new UnsupportedOperationException();
	    }
	}
}
