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

import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;

/**
 * AxiomListSpec
 * Assists list variable with evaluation of list operand (AxiomList) and parameter resolution for
 * one or two dimensions. Also unwraps list containing a single item.
  * @author Andrew Bowley
 * 4 Aug 2015
 */
public class AxiomListSpec implements ListItemSpec
{
    /** Name of axiom list */
    private QualifiedName qualifiedListName;
    /** Reference to current array item */
    private ListItemSpec arrayReference;
    /** Reference to current term or null if target is an axiom array list */
    private ListItemSpec termReference;
    /** Item extracted or produced in most recent evaluation. May be empty. */
    private AxiomTermList axiomTermList;
    
    /**
     * Construct AxiomListSpec object using suppled AxiomList and index data
     * @param qualifiedListName Qualified name of axiom list being referenced
     * @param indexDataArray Index data - 1 or 2 dimensional
     */
    public AxiomListSpec(QualifiedName qualifiedListName, ListItemSpec[] indexDataArray)
    {
        this.qualifiedListName = qualifiedListName;
        init(indexDataArray);
    }
    
    /**
     * Construct AxiomListSpec object using suppled list operand and index data.
     * The list name is taken from the index data.
     * @param indexDataArray Index data - 1 or 2 dimensional
     */
    public AxiomListSpec(ListItemSpec[] indexDataArray)
    {
        init(indexDataArray);
    }

    /**
     * Returns item specified by index from given axiom list
     * @param listIndex List index
     * @param itemList Axiom list
     * @return Item as generic type
     */
    public Object getItem(ListIndex listIndex, AxiomList itemList) {
        int position = listIndex.getPosition();
        if ((position == -1) && (termReference != null))
        	// Position not specified so use local value as default
        	position = termReference.getListIndex().getPosition();
        if (position == -1)
        	// Return array item if index is in range, otherwise Null object
            return itemList.hasItem(listIndex) ? itemList.getItem(listIndex) : new Null();
        else
        {
        	int index = listIndex.getIndex();
        	if (index == -1)
        		index = arrayReference.getListIndex().getIndex();
            if ((index == -1) || itemList.isEmpty())
                // Something unexpected has happened - cannot proceed
                return new Null();
            // An AxiomList containing a single item is unwrapped
            Term term = itemList.getItemAsList(index).getItem(new ListIndex(index, position));
            return term != null ? term.getValue() : new Null();
        }
    }

	public void incrementIndex() {
		arrayReference.getListIndex().incrementIndex();
	}

    /**
     * getListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getListName()
     */
    @Override
    public String getListName()
    {
        return qualifiedListName.getName();
    }

    /**
     * getQualifiedListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getQualifiedListName()
     */
    @Override
    public QualifiedName getQualifiedListName()
    {
        return qualifiedListName;
    }

    /**
     * setQualifiedListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#setQualifiedListName(au.com.cybersearch2.taq.language.QualifiedName)
     */
    @Override
    public void setQualifiedListName(QualifiedName qualifiedListName)
    {
        this.qualifiedListName = qualifiedListName;
    }
    
    /**
     * Returns item extracted or produced in most recent evaluation. This is a selection value if termIndex is valid. 
     * @param axiomList Axiom list being referenced
     * @return AxiomTermList object. This may be empty, but never null.
     */
    public AxiomTermList getAxiomTermList(AxiomList axiomList)
    {
    	if (axiomTermList == null) {
            int index = arrayReference.getListIndex().getIndex();
            if ((index == -1) || (axiomList == null) || axiomList.isEmpty())
                return getEmptyAxiomTermList();
            axiomTermList =  axiomList.getItemAsList(index);
    	}
    	return axiomTermList;
    }

    /**
     * backup
     * @param id Modification id
     * @return boolean
     */
    public boolean backup(int id) 
    {  
        return backupIndexExpression(termReference, id) && 
        	   backupIndexExpression(arrayReference, id);
    }

    /**
     * Returns operand to evaluate axiom selection value for either 1 and 2 dimension cases
     * @return Operand object
     */
    public Operand getAxiomExpression()
    {
        return arrayReference.getItemExpression();
    }

	@Override
	public void setListIndex(ListIndex listIndex) {
		if (termReference != null) {
			int index = listIndex.getPosition();
			if (index != -1)
				termReference.setListIndex(new ListIndex(index)); 
		}
		int index = listIndex.getIndex();
		if (index != -1)
			arrayReference.setListIndex(new ListIndex(index)); 
	}

	@Override
	public ListIndex getListIndex() {
		int position = termReference != null ? termReference.getListIndex().getIndex() : -1;
		return new ListIndex(arrayReference.getListIndex().getIndex(), position);
	}
	
    /**
     * getItemExpression
     * Returns operand to evaluate term selection value. 
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getItemExpression()
     */
    @Override
    public Operand getItemExpression()
    {
        return termReference != null ? termReference.getItemExpression() : arrayReference.getItemExpression();
    }

    @Override
    public void setSuffix(String suffix)
    {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * getSuffix
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getSuffix()
     */
    @Override
    public String getSuffix()
    {
    	if (termReference != null)
    		return termReference.getSuffix();
    	else
    		return arrayReference.getSuffix();
    }

    /**
     * getVariableName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getVariableName()
     */
    @Override
    public QualifiedName getVariableName()
    {
    	if (termReference != null)
    		return termReference.getVariableName();
    	else
    		return arrayReference.getVariableName();
    }

    /**
     * assemble
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#assemble(au.com.cybersearch2.taq.interfaces.ItemList)
     */
    @Override
    public void assemble(ItemList<?> itemList)
    {   
    	if (termReference != null)
    		// Note arrayData is assumed to not require assembly as it only deals with integer selection values
    		termReference.assemble(itemList);
    	else
    		arrayReference.assemble(itemList);
    }

    /**
     * evaluate
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#evaluate(au.com.cybersearch2.taq.interfaces.ItemList, int)
     */
    @SuppressWarnings("rawtypes")
	@Override
    public boolean evaluate(ItemList<?> itemList, int id)
    {
        // The itemList object may be AxiomList or AxiomTermList depending on usage
        boolean isTermList = itemList instanceof AxiomTermList;
        AxiomList axiomList = null;
		if (!isTermList) 
        {
        	if (itemList instanceof ListOperand) {
        		if (itemList.getOperandType() == OperandType.AXIOM)
        			axiomList  = (AxiomList)((ListOperand)itemList).getValue();
        		else {
        			isTermList = true;
                    axiomTermList = (AxiomTermList)((ListOperand)itemList).getValue();;
        		}
        	} else {
                axiomTermList = null;
                axiomList = (AxiomList)itemList;
        	}
        }
        if (isTermList) 
        {
        	if (!(itemList instanceof ListOperand))
                axiomTermList = (AxiomTermList)itemList;
            if (axiomList == null) {
                axiomList = new AxiomList(axiomTermList.getQualifiedName(), axiomTermList.getKey());
                axiomList.setAxiomTermNameList(axiomTermList.getAxiomTermNameList());
            }
        }
        if (termReference != null)
        {   // 2 - dimensional if AxiomList passed, otherwise invalidate axiom index
            if (axiomTermList == null) {
                arrayReference.evaluate(itemList, id);
                int index = arrayReference.getListIndex().getIndex();
                int listSize = axiomList.getLength();
                if ((index != -1) && (index < listSize))
                	axiomTermList = axiomList.getItemAsList(index);
            }
        }
        else if ((axiomTermList == null) && !axiomList.isEmpty())
            // The item is only a sample if isAxiomList is true
            axiomTermList = axiomList.getItemAsList(0);
        if (axiomTermList != null) {
	        // Note: assemble() invoked because referenced axiom may local axiom which changes when scope changes
	        if ((isTermList ) || (termReference == null)) {
	        	arrayReference.assemble(axiomTermList);
	            arrayReference.evaluate(axiomTermList, id);
	        } else {
	    		termReference.assemble(axiomTermList);
	            termReference.evaluate(axiomTermList, id);
	        }
        }
        return true;
    }

	@Override
	public void setOffset(int offset) {
		arrayReference.setOffset(offset);
	}

	@Override
	public int getOffset() {
		return arrayReference.getOffset();
	}
    /**
     * Returns item index given name of item
     * @param itemName
     * @param axiomTermNameList List of axiom term names
     * @return int
     */
    protected int getIndexByName(String itemName, List<String> axiomTermNameList)
    {
        for (int i = 0; i < axiomTermNameList.size(); i++)
        {
            // Strip term name down to name only
            String termName = axiomTermNameList.get(i);
            if (itemName.equals(termName))
                return i;
        }
        return -1;
    }

    /**
     * Returns empty axiom term list
     * @return AxiomTermList object
     */
    private AxiomTermList getEmptyAxiomTermList()
    {
        QualifiedName qname = new QualifiedName(getListName() + "_item", qualifiedListName);
        return new AxiomTermList(qname, qname);
    }

    private void init(ListItemSpec[] indexDataArray)
    {
    	arrayReference = indexDataArray[0];
    	if (indexDataArray.length > 1) 
    		termReference = indexDataArray[1];
    	if (qualifiedListName == null) {
    	    if (termReference != null)
    		    qualifiedListName = termReference.getQualifiedListName();
    	    else
    		    qualifiedListName = arrayReference.getQualifiedListName();
    	}
    }

    private boolean backupIndexExpression(ListItemSpec listItemSpec, int id) {
    	if (listItemSpec != null) {
    		Operand itemExpression = listItemSpec.getItemExpression();
    		return itemExpression != null ? itemExpression.backup(id) : true;
    	}
		return true;
    }
    
}
