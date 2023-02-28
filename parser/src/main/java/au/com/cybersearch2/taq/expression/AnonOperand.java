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
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;

/**
 * Anonymous operand with value evaluated by given expression
  */
public class AnonOperand extends Operand {

	/**
	 * Construct AnonOperand
	 * @param expression Operand to provide value
	 */
	public AnonOperand(Operand expression) {
		super(Term.ANONYMOUS);
		setLeftOperand(expression);
	}
	
	@Override
	public void assign(Parameter parameter) {
		getLeftOperand().assign(parameter);
	}

	@Override
	public QualifiedName getQualifiedName() {
		return QualifiedName.ANONYMOUS;
	}

	@Override
	public Operand getRightOperand() {
		return null;
	}

	@Override
	public Operator getOperator() {
		return getLeftOperand().getOperator();
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		Operand leftOperand = getLeftOperand();
		leftOperand.setExecutionContext(context);
		EvaluationStatus status = leftOperand.evaluate(id);
	    setId(id);
		if (!leftOperand.isEmpty())
		    setValue(leftOperand.getValue());
		return status;
	}

	@Override
	public void setValue(Object objectValue) {
		getLeftOperand().setValue(objectValue);
		super.setValue(objectValue);
	}

	@Override
	public boolean isEmpty() {
		boolean isEcmpty = getLeftOperand().isEmpty();
		if (!isEcmpty && super.isEmpty()) 
			// Synchronize with wrapped operand
			setValue(getLeftOperand().getValue());
		return isEcmpty;
	}

	@Override
	public int getId() {
		return id == 0 ? getLeftOperand().getId() : id;
	}

	@Override
	public boolean backup(int id) {
		if ((id == 0) || (getId() == id))  {
			Operand operand = getLeftOperand();
			operand.backup(id == 0 ? 0 : operand.getId());
	        return super.backup(id);
		}
		return false;
	}

	@Override
	public Object getValue() {
		// Always update value in case in has changed.
		// For example, left operand is head and shadow has caused it to update
		if (!getLeftOperand().isEmpty())
			// Synchronize with wrapped operand, which have changed since last time
			setValue(getLeftOperand().getValue());
		return super.getValue();
	}

	@Override
	public Class<?> getValueClass() {
		getValue();
		return super.getValueClass();
	}

}
