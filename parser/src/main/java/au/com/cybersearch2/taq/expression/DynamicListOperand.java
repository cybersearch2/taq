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

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.list.CursorItemVariable;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * Construct ListOperand object
 * @param ownItemList Item list owned by this ListOperand
 */
public class DynamicListOperand<T> extends ListOperand<T> {

	/** List operand to supply list object on evaluation */
    private final Operand operand;
	
    public DynamicListOperand(ItemList<T> ownItemList, Operand operand) {
		super(ownItemList);
		this.operand = operand;
	}

    public DynamicListOperand(ListOperand<T> listOperand, Operand operand) {
		super(listOperand.ownItemList);
		this.operand = operand;
	}

	@Override
	public EvaluationStatus evaluate(int id) {
	    super.setValue(evaluateOperand(operand, id));
		empty = operand.isEmpty();
		return completeEvaluate(id);
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
				operand.backup(id);
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
			operand.backup(0);
		}
		return backed;
	}


    @SuppressWarnings("unchecked")
	private ItemList<T> evaluateOperand(Operand operand, int id) {
		if (operand instanceof ListOperand) 
			return (ItemList<T>) operand.getValue();
		else if (operand.isEmpty()) {
				operand.setExecutionContext(context);
            operand.evaluate(id);
		}
        if ((operand.getValueClass() == Null.class) && (operand instanceof CursorItemVariable))
            return (ItemList<T>) ((CursorItemVariable)operand).getItemList();
        else if (operand.getValueClass() == Axiom.class)
            return (ItemList<T>) wrapAxiom((Axiom)operand.getValue());
        else
            return (ItemList<T>)operand.getValue();
    }

    private AxiomTermList wrapAxiom(Axiom axiom)
    {
        Term term = axiom.getTermByIndex(0);
        if (ItemList.class.isAssignableFrom(term.getValueClass()))
            return (AxiomTermList) term.getValue();
        AxiomArchetype archetype = (AxiomArchetype) axiom.getArchetype();
        AxiomTermList axiomTermList = new AxiomTermList(archetype.getQualifiedName(), archetype.getQualifiedName());
        axiomTermList.getAxiomListener().onNextAxiom(archetype.getQualifiedName(), axiom, getOperator().getTrait().getLocale());
        return axiomTermList;
    }

}
