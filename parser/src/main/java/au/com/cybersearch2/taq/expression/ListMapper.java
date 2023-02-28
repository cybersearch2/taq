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

import java.util.Collections;
import java.util.Map;

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.operator.AssignOnlyOperator;

/**
 * Performs selection of a list using a term map
 */
public class ListMapper extends Operand {
	private final ChoiceOperand choiceOperand;
	private final QualifiedName qualifiedName;
	private final Map<QualifiedName,ItemList<?>> itemListMap; 
	private final AssignOnlyOperator assignOnlyOperator = new AssignOnlyOperator();

	/**
	 * Construct ListMapper object
	 * @param qualifiedName Qualified name
	 * @param choiceOperand Map term
	 * @param itemListMap Maps list name to list 
	 */
	public ListMapper(QualifiedName qualifiedName, ChoiceOperand choiceOperand, Map<QualifiedName,ItemList<?>> itemListMap) {
		super(qualifiedName.getName());
		this.qualifiedName = qualifiedName;
		this.choiceOperand = choiceOperand;
		this.itemListMap = itemListMap;
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		choiceOperand.setExecutionContext(context);
		choiceOperand.evaluate(id);
		if (!choiceOperand.isEmpty()) {
			setValue(itemListMap.get(choiceOperand.getValue()));
		} else {
			setValue(Collections.emptyList());
		}
		setId(id);
		return super.evaluate(id);
	}

	@Override
	public boolean backup(int id) {
		choiceOperand.backup(id);
		return super.backup(id);
	}
	@Override
	public boolean isEmpty() {
		return super.isEmpty() || ((ItemList<?>)getValue()).isEmpty();
	}

	@Override
	public QualifiedName getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public void assign(Parameter parameter) {
	}

	@Override
	public Operand getRightOperand() {
		return null;
	}

	@Override
	public Operator getOperator() {
		return assignOnlyOperator;
	}
}
