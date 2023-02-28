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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.StringOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;
import au.com.cybersearch2.taq.result.ResultList;

/**
 * DynamicList
 * List of items evaluated at runtime. Allows lists containing scope-dependent values and/or expressions.
 * The list always starts at an index value of zero and all items must be contiguous.
 * @author Andrew Bowley
 * 7Jun.,2017
 */
public class DynamicList<T> implements ItemList<T>
{
    static class DynamicListIterable<T> implements Iterable<T>
    {
        List<T> itemList;
        
        @SuppressWarnings("unchecked")
        public DynamicListIterable(List<Term> termList)
        {
            itemList = new ArrayList<T>(termList.size());
            for (Term term: termList)
                itemList.add((T) term.getValue());
        }

        @Override
        public Iterator<T> iterator()
        {
            return itemList.iterator();
        }
    }
    
    /** Parameter template evaluates item values */
    private final Template template;
    /** Qualified name */
    private final QualifiedName qname;
    /** Operand type */
    private final OperandType operandType;
    
    /** Source item to be updated in parser task */
    private SourceItem sourceItem;
    /** Flag set true if list is exported */
    private boolean isPublic;
    /** Offset when list does not start at index of zero */
    private int offset;
   
    /**
     * Construct a DynamicList object
     * @param operandType Operand type of list items 
     * @param qname Qualified name 
     * @param template Parameter template evaluates item values
     */
    public DynamicList(OperandType operandType, QualifiedName qname, Template template)
    {
        this.operandType = operandType;
        this.qname = qname;
        this.template = template;
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
	
    @Override
    public Iterator<T> iterator()
    {
        return new DynamicListIterable<T>(template.toArray()).iterator();
    }

    @Override
    public void setSourceItem(SourceItem sourceItem)
    {
        this.sourceItem = sourceItem;
    }

    @Override
    public int getLength()
    {
        return template.getTermCount();
    }

    @Override
    public String getName()
    {
        return qname.getName();
    }

    @Override
    public QualifiedName getQualifiedName()
    {
        return qname;
    }

    @Override
    public boolean isEmpty()
    {
        return (template.getTermCount() == 0) || !template.isFact();
    }

	@Override
	public void append(T value) {
		TermListManager archetype = template.getTemplateArchetype();
		boolean isMutable = archetype.isMutable();
		((TemplateArchetype)archetype).setMutable();
		assignItem(getLength(), value);
		if (!isMutable)
			archetype.clearMutable();
	}

    @Override
    public T getItem(ListIndex listIndex)
    {
    	return getItem(listIndex.getIndex());
    }
    
    @SuppressWarnings("unchecked")
    private T getItem(int index)
    {
        if ((index >= 0) && (index < getLength()))
            return (T) template.getTermByIndex(index).getValue();
        return null;
    }

    @Override
    public OperandType getOperandType()
    {
        return operandType;
    }

    @Override
    public boolean hasItem(ListIndex listIndex)
    {
    	int index = listIndex.getIndex();
        return (index >= 0) && (index < getLength());
    }

    @Override
    public Iterable<T> getIterable()
    {
        return new DynamicListIterable<T>(template.toArray());
    }

    @Override
    public void clear()
    {
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

    /**
     * toString
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() 
    {
        return "List <" + operandType.toString().toLowerCase() + ">[" + getLength() + "]";
    }

	@Override
	public ItemList<T> newInstance() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultList<T> getSolution() {
		ResultList<T> resultList = new ResultList<T>() {

			private ArrayList<T> items;
			
			@Override
			public QualifiedName getQualifiedName() {
				return qname;
			}

			@Override
			public OperandType getOperandType() {
				return operandType;
			}

			@Override
			public ArrayList<T> getList() {
				if (items == null) {
					items = new ArrayList<T>();
					for (int i = 0; i < template.getTermCount(); ++i) {
						@SuppressWarnings("unchecked")
						T value = (T) template.getTermByIndex(i);
						if ((value instanceof Null) || (value instanceof Blank))
							value = null;
					    items.add((value));
					}
				}
				return items;
			}
			
		};
		resultList.getList();
		return resultList;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

    /**
     * Evaluate Terms of this list
     * @param executionContext Execution context
     * @return EvaluationStatus
     */
	@Override
    public EvaluationStatus evaluate(ExecutionContext executionContext)
    {
        return template.evaluate(executionContext);
    }
 
    /**
     * Backup from last evaluation.
     * @param partial Flag to indicate backup to before previous unification or backup to start
     * @return Flag to indicate if this Structure is ready to continue unification
     */
	@Override
    public boolean backup(boolean partial)
    {
        return template.backup(partial);
    }

	@Override
	public void assignItem(ListIndex listIndex, T value) {
		assignItem(listIndex.getIndex(), value);
	}

    /**
     * assignItem -Can only overwrite existing value and append to end of list
     * @see au.com.cybersearch2.taq.interfaces.ItemList#assignItem(int, java.lang.Object)
     */
    private void assignItem(int index, T value)
    {
        if ((index < 0) || (index > getLength()))
            throw new ExpressionException("Index " + index + " invalid");
        if (index == getLength())
        {
        	if (template.getId() == 0) {
	            Variable var = new Variable(new QualifiedName(getName() + qname.incrementReferenceCount(), qname));
	            var.setValue(value);
	            template.addTerm(var);
        	} else {
        		switch (operandType) {
        		case STRING: template.addTerm(new StringOperand(QualifiedName.ANONYMOUS, (String)value)); break;
        		default:
        			template.addTerm(new StringOperand(QualifiedName.ANONYMOUS, value.toString()));
        		}
        	}
            if (sourceItem != null)
                sourceItem.setInformation(toString());
        }
        else
            template.getTermByIndex(index).setValue(value);
    }

}
