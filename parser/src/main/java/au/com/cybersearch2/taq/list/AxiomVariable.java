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
import au.com.cybersearch2.taq.expression.ExpressionException;
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
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * AxiomVariable
 * Variable implementation for accessing AxiomLists and AxiomTermLists
 * @author Andrew Bowley
 * 31May,2017
 */
public class AxiomVariable implements ListItemDelegate, ExecutionTracker
{
    /** Helper for working with AxiomLists and AxiomTermLists */
    protected AxiomListSpec axiomListSpec;
    /** List operand to supply AxiomList object on evaluation */
    protected ListOperand<?> listOperand;
    /**Axiom list set only if passed in the constructor */
    protected AxiomList axiomList;
    /** Axiom term list set only if passed in the constructor */
    protected AxiomTermList axiomTermList;
    /** Flag set true if there is to be no congruence check on appended axioms */
    private boolean isAppemderDelegate;
    /** Appends new items to an item list by attaching them to the end of the list */
    private ItemAppender<?> itemAppender;
    /** ExecutionContext */
    private ExecutionContext context;
    
    /**
     * Construct AxiomVariable object using suppled list operand and index data.
     * The list name is taken from the index data.
     * @param listOperand Operand to supply AxiomList object on evaluation
     * @param indexDataArray Index data - 1 or 2 dimensional
     */
    public AxiomVariable(ListOperand<?> listOperand, ListItemSpec[] indexDataArray)
    {
        this.listOperand = listOperand;
        axiomListSpec = new AxiomListSpec(indexDataArray);
    }
    
   /**
     * Construct AxiomVariable object using suppled AxiomList and index data
     * @param axiomList Axiom list
     * @param indexDataArray Index data - 1 or 2 dimensional
     */
    public AxiomVariable(AxiomList axiomList, ListItemSpec[] indexDataArray)
    {
    	this.axiomList = axiomList;
        axiomListSpec = new AxiomListSpec(axiomList.getQualifiedName(), indexDataArray);
    }
 
    /**
     * Construct AxiomVariable object using suppled AxiomTermList and index data
     * @param axiomTermList Axiom term list
     * @param indexDataArray Index data - 1 or 2 dimensional
     */
    public AxiomVariable(AxiomTermList axiomTermList, ListItemSpec[] indexDataArray)
    {
        this.axiomTermList = axiomTermList;
        axiomListSpec = new AxiomListSpec(axiomTermList.getQualifiedName(), indexDataArray);
    }

    /**
     * Set flag for if there is to be no congruence check on appended axioms
     * @param isAppemderDelegate boolean
     */
    public void setAppemderDelegate(boolean isAppemderDelegate) {
		this.isAppemderDelegate = isAppemderDelegate;
	}

    /**
     * Return list being referenced
     * @param dimension Which dimension, 1 or 2
     * @return the itemList or null if not set
     * @see au.com.cybersearch2.taq.interfaces.ListItemDelegate#getItemList()
     */
    @Override
    public ItemList<?> getItemList(int dimension)
    {
    	ItemList<?> returnList = null;
        if (dimension == 1) {
        	if (axiomList != null) 
        		returnList = axiomList;
        	else if (axiomTermList != null)
        		returnList = axiomTermList;
        } else 
        	returnList = axiomListSpec.getAxiomTermList(getAxiomList()); 
        return returnList;
    }

    /**
     * unifyTerm
     */
    @Override
    public Operand getOperand()
    {
        return axiomListSpec.getItemExpression();
    }
    
    /**
     * evaluate
     * @see au.com.cybersearch2.taq.interfaces.ListItemDelegate#evaluate(int)
     */
    @Override
    public ListIndex evaluate(int id)
    {   // Resolve parameters for AxiomList or AxiomTermList
    	if (listOperand != null) {
    		if (listOperand.isEmpty()) {
            		listOperand.setExecutionContext(context);
    			listOperand.evaluate(id);
    		}
    		ItemList<?> newItemList = (ItemList<?>) listOperand.getValue();
			if (newItemList.getOperandType() == OperandType.AXIOM)
			    axiomList = (AxiomList) listOperand.getValue();
			else
				axiomTermList = (AxiomTermList) listOperand.getValue();
     	}
    	if (axiomList != null)
            axiomListSpec.evaluate(axiomList, id);
    	else if (axiomTermList != null)
            axiomListSpec.evaluate(axiomTermList, id);
    	else 
    		return new ListIndex(-1);
        return axiomListSpec.getListIndex();
    }
    
    /**
     * backup
     * @see au.com.cybersearch2.taq.interfaces.ListItemDelegate#backup(int)
     */
    @Override
    public boolean backup(int id) 
    {  
    	if ((axiomList != null) && axiomList.isDynamic())
    		axiomList.backup(true);
        axiomListSpec.backup(id);
        if (listOperand != null) {
        	if ((id == 0) || (id == listOperand.getId())) {
	            listOperand.backup(id);
	            axiomList = null;
	            axiomTermList = null;
        	}
        }
        return true;
    }
    
    /**
     * setItemValue
     * @see au.com.cybersearch2.taq.interfaces.ListItemDelegate#setItemValue(java.lang.Object)
     */
    @Override
    public void setItemValue(Object value)
    {
    	checkListAvaliable();
        if (value instanceof Null) 
            throw new ExpressionException("Axion value of null invalid");
        ListIndex listIndex = axiomListSpec.getListIndex(); 
        int index = listIndex.getIndex();
        if (listIndex.getPosition() == -1)
        {
            if (index == -1)
                throw new ExpressionException("List is not ready to receive first item");
            AxiomTermList lermList = axiomTermList;
            if (lermList != null) {
            	Term term = lermList.getAxiom().getTermByIndex(index);
            	if (term == null)
            		throw new ExpressionException(String.format("Index %d out of range  for axiom term list %s", index, lermList.getName()));
            	term.setValue(value);
            } else {
	            // Assign value to item list
	            if (value instanceof AxiomTermList)
	                (getAxiomList()).assignItem(listIndex, ((AxiomTermList)value).getAxiom());
	            else
	                (getAxiomList()).assignItem(listIndex, (Axiom)value);
            }
        }
        else
        {   
            // An AxiomList containing a single item is unwrapped
            AxiomTermList termList = axiomTermList;
            if (termList == null)
            	termList = axiomListSpec.getAxiomTermList(getAxiomList());
            if (termList.isEmpty())
            {
                AxiomArchetype archetype = new AxiomArchetype(termList.getKey());
                Axiom axiom = new Axiom(archetype);
                axiom.addTerm(new Parameter(Term.ANONYMOUS, value));
                termList.setAxiom(axiom);
            }
            else
            {
                // Update term in axiom referenced by list
                Term term = termList.getItem(listIndex);
                term.setValue(value);
            }
        }
    }
    
	/**
     * getValue
     * @see au.com.cybersearch2.taq.interfaces.ListItemDelegate#getValue()
     */
    @Override
    public Object getValue()
    {
        ListIndex listIndex = axiomListSpec.getListIndex();
        if (listIndex.getIndex() == -1)
            // Something unexpected has happened - cannot proceed
            return new Null();
         return getValue(listIndex);
    }

    /**
     * Returns object at given list index
     */
    @Override
    public Object getValue(ListIndex listIndex)
    {
    	int itemIndex = listIndex.getIndex();
    	if ((listIndex.getPosition() == -1) && (axiomTermList != null)) 
    		return axiomTermList.getAxiom().getTermByIndex(itemIndex);
    	else if ((axiomList != null) && !axiomList .isEmpty())
            return axiomListSpec.getItem(listIndex, getAxiomList());
    	else
    		return new Null();
    }

    /**
     * append
     * @see au.com.cybersearch2.taq.interfaces.ListItemDelegate#append(java.lang.Object)
     */
    @Override
    public void append(Object value)
    {
    	if (isAppemderDelegate) {
    		if (itemAppender == null)
    			itemAppender = getItemList(1).getOperandType() == OperandType.AXIOM ? 
    				new ItemAppender<AxiomList>(this) : 
    				new ItemAppender<AxiomTermList>(this);
    		itemAppender.appendItem(value);
    	} else {
	        if (value instanceof AxiomTermList)
	            axiomList.concatenate(((AxiomTermList)value).getAxiom());
	        else if (value instanceof AxiomList)
	            axiomList.concatenate((AxiomList)value);
	        else if (value instanceof Axiom) 
	        	axiomList.concatenate((Axiom)value);
	        else
	        	throw new ExpressionException(String.format("Value '%s' cannot be appended", value.toString()));
	        axiomListSpec.incrementIndex();
    	}
    }

	@Override
	public ListIndex getListIndex() {
		return axiomListSpec.getListIndex();
	}

	@Override
	public void setListIndex(int index) {
		axiomListSpec.setListIndex(new ListIndex(index));
	}

	@Override
	public void setExecutionContext(ExecutionContext context) {
		this.context = context;
	}
	
	private AxiomList getAxiomList() {
		if (axiomList == null)
		    throw new ExpressionException("Axiom list not available for variable operation");
		return axiomList;
	}
	
    private void checkListAvaliable() {
		if ((axiomList == null) && (axiomTermList == null) /*&& (itemList == null)*/)
			throw new ExpressionException("Axiom list not available");
	}
}
