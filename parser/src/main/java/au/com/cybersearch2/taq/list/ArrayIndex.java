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

import au.com.cybersearch2.taq.expression.CursorOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * ArrayIndex - Tracks index of current array list item
 * @author Andrew Bowley
 * 23May,2017
 */
public class ArrayIndex implements ListItemSpec
{
	/** List index extended to include offset which only parent class supports */
	static class ArrayListIndex extends ListIndex {

	    /** Optional offset */
		private int offset;

		public ArrayListIndex(int index) {
			super(index);
		}

		public ArrayListIndex(int index, int offset) {
			super(index);
			this.offset = offset;
		}

		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		@Override
		public int getIndex() {
			return index + offset;
		}
	}
	
    /** Qualified name of list */
	protected QualifiedName qname;
	/** Index operand - literal if not empty, may be null */
	protected Operand indexExpression;
    /** Name to identify item - deduced from operand components if not specified */
	protected String suffix;

    /** Selection value */
	private ArrayListIndex listIndex;

    /**
     * Construct ArrayIndex object using supplied index operand
     * @param qname Qualified name of list
     * @param indexExpression Index evaluation operand
     */
    public ArrayIndex(QualifiedName qname, Operand indexExpression)
    {
        this.qname = qname;
        this.indexExpression = indexExpression;
        listIndex = new ArrayListIndex(-1);
        OperandType operandType = indexExpression.getOperator().getTrait().getOperandType();
        if (!indexExpression.isEmpty())
        {   // Set index according to literal type, either integer or string
            if (operandType == OperandType.INTEGER)
                setIndex(setIntIndex());
            else if (operandType == OperandType.STRING)
                // NOTE: Override setStringIndex() if string is a valid index type 
                suffix = indexExpression.getValue().toString();
            else
                throw new ExpressionException("List \"" + getListName() + "\" has invalid index \"" + indexExpression.getValue().toString() + "\"");
        }
        if (suffix == null)
        {   // Deduce suffix from first non-empty name navigating left operand branch
            suffix = indexExpression.getName();
            if (suffix.isEmpty())
            {
                Operand operand = indexExpression.getLeftOperand();
                while (operand != null)
                {
                    if (!operand.getName().isEmpty())
                    {
                        suffix = operand.getName();
                        break;
                    }
                    operand = operand.getLeftOperand();
                }
                if (suffix.isEmpty())
                    // This is not expected
                    suffix = indexExpression.toString();
            }
        }
    }
 
    /**
     * Construct ArrayIndex object using supplied index operand and suffix
     * @param qname Qualified name of list
     * @param indexExpression Index evaluation operand
     * @param suffix Name to identify item
     */
    public ArrayIndex(QualifiedName qname, Operand indexExpression, String suffix)
    {
        this.qname = qname;
        this.indexExpression = indexExpression;
        this.suffix = suffix;
        listIndex = new ArrayListIndex(-1);
    }

    /**
     * Construct ArrayIndex object using supplied index and suffix
     * @param qname Qualified name of list
     * @param index Index value to select item
     * @param suffix Name to identify item
     */
    public ArrayIndex(QualifiedName qname, int index, String suffix)
    {
        this.qname = qname;
        this.suffix = suffix;
        listIndex = new ArrayListIndex(index);
    }

    /**
     * assemble
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#assemble(au.com.cybersearch2.taq.interfaces.ItemList)
     */
    @Override
    public void assemble(ItemList<?> itemList)
    {
    }
 
    /**
     * evaluate
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#evaluate(au.com.cybersearch2.taq.interfaces.ItemList, int)
     */
    @Override
    public boolean evaluate(ItemList<?> itemList, int id)
    {
        if (indexExpression != null)
        {   // Evaluate index. The resulting value must be a sub class of Number to be usable as an index.
            indexExpression.evaluate(id);
            if (indexExpression.isEmpty())
                throw new ExpressionException("Index for list \"" + getListName() + "\" is empty" );
            int index = -1;
            if (indexExpression.getValueClass() == Parameter.class) {
            	Object object = indexExpression.getValue();
            	if (object instanceof Long) {
                	indexExpression.setValue(object);
                	index = setIntIndex();
            	} else if (object instanceof String) {
                	indexExpression.setValue(object);
                    setStringIndex(itemList);
            	} else
                    throw new ExpressionException(String.format("List \"%s\" has invalid index \"%s\"", getListName(), indexExpression.toString()));
            } else {
	            OperandType operandType = indexExpression.getOperator().getTrait().getOperandType();
	            if (operandType == OperandType.CURSOR)
	            {
	            	CursorItemVariable itemVariable;
	            	if (indexExpression instanceof CursorOperand)
	            		itemVariable = ((CursorOperand)indexExpression).getCursorItemVariable();
	            	else
	            		itemVariable = (CursorItemVariable) indexExpression;
	                operandType = itemVariable.getListOperandType();
	            }
	            if (operandType == OperandType.STRING)
	                setStringIndex(itemList);
	            else if (operandType == OperandType.INTEGER)
	                index = setIntIndex();
	            if (index == -1)    
	                throw new ExpressionException(String.format("List \"%s\" has invalid index \"%s\"", getListName(), indexExpression.getValue().toString()));
            }
            setIndex(index);
            return true;
        }
        return false;
    }

    /**
     * getListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getListName()
     */
    @Override
    public String getListName()
    {
        return qname.getName();
    }

    /**
     * getQualifiedListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getQualifiedListName()
     */
    @Override
    public QualifiedName getQualifiedListName()
    {
        return qname; 
    }

    /**
     * setQualifiedListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#setQualifiedListName(au.com.cybersearch2.taq.language.QualifiedName)
     */
    @Override
    public void setQualifiedListName(QualifiedName qualifiedListName)
    {
        qname = new QualifiedName(qualifiedListName.getScope(), qualifiedListName.getTemplate(), qualifiedListName.getName());
    }

	@Override
	public void setListIndex(ListIndex listIndex) {
		setIndex(listIndex.getIndex());
	}

	@Override
	public ListIndex getListIndex() {
		return listIndex;
	}

	/**
     * getItemExpression
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getItemExpression()
     */
    @Override
    public Operand getItemExpression()
    {
        return indexExpression;
    }

    /**
     * setSuffix
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#setSuffix(java.lang.String)
     */
    @Override
    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    /**
     * getSuffix
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getSuffix()
     */
    @Override
    public String getSuffix()
    {
        return suffix;
    }

    /**
     * getVariableName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getVariableName()
     */
    @Override
    public QualifiedName getVariableName()
    {
        return new QualifiedName(getVariableName(qname.getName(), suffix) + qname.incrementReferenceCount(), qname);
    }
    
	@Override
	public void setOffset(int offset) {
		listIndex.setOffset(offset);
	}

	@Override
	public int getOffset() {
		return listIndex.getOffset();
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
     * Returns index extracted from index expression, converting from long to int at same time.
     * Also sets suffix according to whether expression term name is empty.
     * @return index
     */
    protected int setIntIndex()
    {
        int index = ((Long)indexExpression.getValue()).intValue();
        if (indexExpression.getName().isEmpty())
            suffix = getListName() + "." + index;
        else 
            suffix = indexExpression.getName();
        return index;
    }

    /**
     * Default setStringIndex implementation does nothing in this base class
     * @param itemList Target list
     */
    protected void setStringIndex(ItemList<?> itemList)
    {
        // Default index to 0 
        // Override if string indexes supported
        setIndex(0);
    }

	protected void setIndex(int index) {
		int offset = this.listIndex.getOffset();
		this.listIndex = new ArrayListIndex(index, offset);
	}

}
