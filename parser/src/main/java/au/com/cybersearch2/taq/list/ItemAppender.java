package au.com.cybersearch2.taq.list;
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

import java.util.Iterator;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemDelegate;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * Appends new items to an item list by attaching them to the end of the list,
 * It applies to all basic list types, so in the case of an Axiom list, 
 * there is no Term Name Congruence check.
 */
public class ItemAppender<T> {

	/** Delegate to item list to be appended */
	private final ListItemDelegate delegate;

	/**
	 * Construct ItemAppender object
	 * @param delegate Delegate to item list to be appended
	 */
	public ItemAppender(ListItemDelegate delegate) {
		this.delegate = delegate;
	}

    @SuppressWarnings("unchecked")
	public void appendItem(Object value) {
    	ItemList<T> itemList = (ItemList<T>) delegate.getItemList(1); 
        ListIndex appendIndex = new ListIndex(itemList.getLength());
        if (itemList.getOperandType() == OperandType.TERM) {
        	boolean success = false;
        	if (value instanceof Term) {
                itemList.assignItem(appendIndex, (T)value);
                success = true;
        	} else if (value instanceof Axiom) {
        		Axiom axiom = (Axiom)value;
        		for (int i = 0; i < axiom.getTermCount(); ++i)
                    itemList.assignItem(appendIndex, (T) axiom.getTermByIndex(i));
                success = true;
        	} else {
        	    Term item = (Term) itemList.getItem(delegate.getListIndex());
        	    if (item == null)
        	    	throw new ExpressionException(String.format("Index %d ut of range", delegate.getListIndex()));
        	    if (item.getValue() instanceof ListOperand) {
        	    	ListOperand<T> listOperand = (ListOperand<T>)item.getValue();
                    success = append(listOperand, value);
        	    }
         	}
        	if (!success)
       		    throw new ExpressionException(String.format("Cannot append %s to a term list", value.toString()));
       } else if (itemList.getOperandType() == OperandType.AXIOM) {
    	   if (value instanceof Axiom)
               itemList.assignItem(appendIndex, (T)(Axiom)value);
    	   else if (value instanceof AxiomTermList)
               itemList.assignItem(appendIndex, (T)((AxiomTermList)value).getAxiom());
    	   else if (value instanceof AxiomList) {
    		   AxiomList axiomList = (AxiomList)value;
    		   Iterator<Axiom> iterator = axiomList.getIterable().iterator();    
    		   while(iterator.hasNext()) {
    			   itemList.assignItem(appendIndex, (T)iterator.next());
    			   appendIndex.incrementIndex();
    		   }
    	   } else
               itemList.assignItem(appendIndex, (T)value);
       } else
            itemList.assignItem(appendIndex, (T)value);
       if (delegate.getListIndex().getIndex() == -1)
    	   // Prepare to receive first item
    	   delegate.setListIndex(0);
	}

	private boolean append(ListOperand<T> listOperand, Object value)
    {
    	OperandType operandType = listOperand.getOperandType();
    	if (operandType == OperandType.AXIOM) {
 			if (value instanceof AxiomList) {
				AxiomList axiomList = (AxiomList)value;
				axiomList.forEach(axiom ->  {
					listOperand.append(axiom);
				});
			} else if (value instanceof AxiomTermList)
		    	listOperand.append(((AxiomTermList) value).newInstance());
			return true;
    	}
    	return false;
    }

}
