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
package au.com.cybersearch2.taq.expression;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Iterator;

import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Concaten;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.list.ListIndex;
import au.com.cybersearch2.taq.operator.ListOperator;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.ResultList;

/**
 * Item list implemented as an operand. The list can be set by unification or evaluation.
 * An empty list can be of unknown type, and the actual type assigned only when the value is set.
 *
 * @param <T> Type of list item
 */
public class ListOperand<T> extends Operand implements ItemList<T> {

	/** Item list owned by this ListOperand and retained over evaluation cycles */
	protected ItemList<T> ownItemList;
 
	/** Qualified name of own item list. It may not match the name of an assigned list */
	private final QualifiedName qualifiedListName;
	
    /** Defines operations that an Operand performs with other operands. */
	private final ListOperator listOperator;
	
	/**
	 * Construct ListOperand object
	 * @param ownItemList Item list owned by this ListOperand
	 */
	public ListOperand(ItemList<T> ownItemList) {
		this(ownItemList.getName(), ownItemList);
	}
	
	/**
	 * Construct ListOperand object with given name
	 * @param name Term name
	 * @param ownItemList Item list owned by this ListOperand
	 */
	private ListOperand(String name, ItemList<T> ownItemList) {
		super(name);
		this.ownItemList = ownItemList;
		listOperator = new ListOperator();
		qualifiedListName = ownItemList.getQualifiedName();
		// Assign own list to self. This operand will be empty if the list is empty,
		// which allows another list to be assigned by unification
		super.setValue(ownItemList);
		empty = ownItemList.isEmpty();
	}

	public QualifiedName getListName() {
		return qualifiedListName;
	}
	
	public ListType getlistType() {
		if (getOperandType() == OperandType.AXIOM) 
			return ListType.axiom_item;
		else if (getOperandType() == OperandType.TERM) 
			return ListType.term;
		return ListType.basic;
	}
	
	public OperandType getOperandType() {
		return ownItemList.getOperandType();
	}

	@SuppressWarnings("unchecked")
	public QualifiedName getCurrentName() {
		
		return ownItemList == null ? qualifiedListName : 
			( ownItemList == value ? ownItemList.getQualifiedName() : ((ItemList<T>)value).getQualifiedName());
	}

	public Object concatenate(Term rightTerm) {
		append(rightTerm.getValue());
		return getValue();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void append(Object item) {
		ItemList<T> itemList = getListFromObject(getValue());
		OperandType operandType = itemList.getOperandType();
		switch (operandType) {
		case INTEGER: ((Concaten<Long>)itemList).concatenate((Long)item);
	        break;
		case BOOLEAN: ((Concaten<Boolean>)itemList).concatenate((Boolean)item);
		    break;
		case DOUBLE: ((Concaten<Double>)itemList).concatenate((Double)item);
	        break;
		case STRING: ((Concaten<String>)itemList).concatenate(item.toString());
	        break;
		case DECIMAL: ((Concaten<BigDecimal>)itemList).concatenate((BigDecimal)item);
	        break;
		case TERM: ((Concaten<Term>)itemList).concatenate((Term)item);
	        break;
		case AXIOM: 
		{
			if (item instanceof AxiomTermList)
			    ((Concaten<Axiom>)itemList).concatenate(((AxiomTermList)item).getAxiom());
			else if (item instanceof AxiomList)
				((Concaten<Axiom>)itemList).concatenate(((AxiomList)item).getItem(0));
			else
			    ((Concaten<Axiom>)itemList).concatenate((Axiom)item);
		    break;
		}
		default:
		    throw new ExpressionException("Append to list not supported for " + operandType.name());
		}
		empty = false;
	}
	
	@Override
	public QualifiedName getQualifiedName() {
		
		return qualifiedListName;
	}

	@Override
	public void assign(Parameter parameter) {
		if (parameter.getValueClass() == ListOperand.class)
			// If the operand list has not been evaluated it's own item list is returned,
			// in which case the operand list is just a passive list container.
		    setValue(((ListOperand<?>)parameter.getValue()).getValue());
		else
	        setValue(parameter.getValue());
	    int assignId = parameter.getId();
	    if (assignId != 0) 
	    {
	    	setId(assignId);
	        if (isShadow()) 
	    	    head.castShadow(getValue(), assignId);
	    }
	}

	@Override
	public void setValue(Object value)
	{
		ItemList<T> itemList = getListFromObject(value);
		OperandType operandType = ownItemList.getOperandType();
		if ((itemList.getOperandType() != operandType) && (operandType != OperandType.UNKNOWN))
			throw new ExpressionException(String.format("Cannot assign list type %s to list variable %s", itemList.getOperandType(), getName()));
		super.setValue(itemList);
	    empty = itemList.isEmpty();
	}

	
	@Override
	public Object getValue() {
		Object value = super.getValue();
		if (value instanceof ListOperand)
			return ((ListOperand<?>)value).getValue();
		return value;
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		if (value instanceof Null) {
			super.setValue(ownItemList);
		    empty = ownItemList.isEmpty();
		} else
			empty = getListFromObject(value).isEmpty();
		return completeEvaluate(id);
	}

	@Override
	public Operand getRightOperand() {
		return null;
	}

	@Override
	public Operator getOperator() {
		
		return listOperator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isEmpty() {
		ItemList<T> itemList = null;
		if (value instanceof ItemList) {
			itemList = (ItemList<T>)value;
		    empty = itemList.isEmpty();
		}
		return empty;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean backup(int id) {
		ItemList<T> itemList = null;
		if (value instanceof ItemList)
			itemList = (ItemList<T>)value;
		if (id != 0) {
			if (id == getId() && (ownItemList != itemList)) {
				super.setValue(ownItemList);
				empty = ownItemList.isEmpty();
				return true;
			} else 
				return false;
		}
		boolean backed = super.backup(id);
		if (backed) {
			if (!ownItemList.isEmpty()) {
				if (ownItemList != itemList)
				     ownItemList.clear();
				else
				     ownItemList = ownItemList.newInstance();
				super.setValue(ownItemList);
				empty = true;
			}
		}
		return backed;
	}

	@Override
	public Iterator<T> iterator() {
		return getItemList().iterator();
	}

	@Override
	public void setSourceItem(SourceItem sourceItem) {
		getItemList().setSourceItem(sourceItem);
	}

	@Override
	public int getLength() {
		return getItemList().getLength();
	}

	@Override
	public int getOffset() {
		return getItemList().getOffset();
	}

	@Override
	public void assignItem(ListIndex listIndex, T value) {
		getItemList().assignItem(listIndex, value);
	}

	@Override
	public T getItem(ListIndex listIndex) {
		return getItemList().getItem(listIndex);
	}

	@Override
	public boolean hasItem(ListIndex listIndex) {
		return getItemList().hasItem(listIndex);
	}

	@Override
	public Iterable<T> getIterable() {
		return getItemList().getIterable();
	}

	@Override
	public void clear() {
		getItemList().clear();
	}

	@Override
	public boolean isPublic() {
		if (super.isPrivate())
			return false;
		return getItemList().isPublic();
	}

	@Override
	public void setPublic(boolean isPublic) {
		getItemList().setPublic(isPublic);
	}

	@Override
	public ItemList<T> newInstance() {
		return getItemList().newInstance();
	}
	
    @Override
	public boolean isDynamic() {
		return getItemList().isDynamic();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResultList<T> getSolution() {
		return (ResultList<T>) getItemList().getSolution();
	}

	/**
	 * Returns an anonymous OperanList object wrapping the given item list
	 * @param itemList ItemList object
	 * @return ListOperand object
	 */
	@SuppressWarnings("unchecked")
	public static ListOperand<?> listOperandInstance(ItemList<?> itemList) {
        switch (itemList.getOperandType())
        {
        case INTEGER:
        	return new ListOperand<Integer>("", (ItemList<Integer>) itemList);
        case DOUBLE:
        	return new ListOperand<Double>("", (ItemList<Double>) itemList);
        case BOOLEAN:
        	return new ListOperand<Boolean>("", (ItemList<Boolean>) itemList);
        case STRING:
        	return new ListOperand<String>("", (ItemList<String>) itemList);
        case DECIMAL:
        	return new ListOperand<BigDecimal>("", (ItemList<BigDecimal>) itemList);
        case CURRENCY:
        	return new ListOperand<Currency>("", (ItemList<Currency>) itemList);
        case AXIOM:
        	return new ListOperand<Axiom>("", (ItemList<Axiom>) itemList);
        case TERM:
        	return new ListOperand<Term>("", (ItemList<Term>) itemList);
        case UNKNOWN:
        default:
        	return new ListOperand<Unknown>("", (ItemList<Unknown>) itemList);
       }
    }
    
	protected EvaluationStatus completeEvaluate(int id) {
		setId(id);
		return super.evaluate(id);
	}

	@SuppressWarnings("unchecked")
	protected ItemList<T> getListFromObject(Object value) {
		ItemList<T> itemList = null;
		if (ItemList.class.isAssignableFrom(value.getClass())) 
			itemList = (ItemList<T>)value;
		else if (value instanceof ListOperand) 
			itemList = (ItemList<T>) ((ListOperand<T>)value).getValue();
		else if (value instanceof Axiom) {
			Axiom axiom = (Axiom)value;
			QualifiedName qname = axiom.getArchetype().getQualifiedName();
			AxiomTermList axiomTermList =  new AxiomTermList(qname, qname);
			axiomTermList.setAxiom(axiom);
			itemList = (ItemList<T>)axiomTermList;
		} if (itemList == null)
		    throw new ExpressionException(String.format("%s is not a list", value.toString()));
		return itemList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ItemList<T> getItemList() {
		ItemList<T> itemList = null;
		Object object = getValue();
		if (ItemList.class.isAssignableFrom(object.getClass())) 
			itemList = (ItemList<T>)object;
		else if (object instanceof ListOperand) 
			itemList = (ItemList<T>) ((ListOperand)object).getValue();
		if (itemList == null)
			itemList = ownItemList;
		return itemList;
    }
    
}
