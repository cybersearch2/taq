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

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.Cursor;
import au.com.cybersearch2.taq.list.CursorItemVariable;
import au.com.cybersearch2.taq.list.CursorList;
import au.com.cybersearch2.taq.operator.DelegateOperator;
import au.com.cybersearch2.taq.operator.DelegateType;

/**
 * CursorOperand Performs operations using a cursor that do not involve a list
 * offset
 */
public class CursorOperand extends ExpressionOperand<Object> implements ParserRunner, LocaleListener {

	private static Operand EMPTY_VALUE = new DoubleOperand(QualifiedName.ANONYMOUS, Double.NaN);

	/** The cursor */
	private final Cursor cursor;
	/** Variable to access the associated list */
	private final CursorItemVariable cursorItemVariable;
	/** Defines operations that this Operand performs with other operands. */
	private final DelegateOperator delegateOperator;
	/** Defines operations that the value Operand performs with other operands */
	private DelegateType valueDelegateType;

	/**
	 * Construct CursorOperand object
	 * 
	 * @param cursorList  Encapsulates a cursor and it's associated list
	 * @param typeOperand Operand to perform type conversion or null
	 */
	public CursorOperand(CursorList cursorList, Operand typeOperand) {
		super(cursorList.getCursor().getCursorQname());
		this.cursor = cursorList.getCursor();
		// Override regular delegate operator so operations are split
		// according to whether they are binary or unary
		delegateOperator = new DelegateOperator() {

			@Override
			public OperatorEnum[] getRightBinaryOps() {
				if ((valueDelegateType != null) && !isEmpty())
					setValueOperand();
				return super.getRightBinaryOps();
			}

			@Override
			public OperatorEnum[] getRightUnaryOps() {
				setCursorDelegate();
				return super.getRightUnaryOps();
			}

			@Override
			public OperatorEnum[] getLeftBinaryOps() {
				if ((valueDelegateType != null) && !isEmpty())
					setValueOperand();
				return super.getLeftBinaryOps();
			}

			@Override
			public OperatorEnum[] getLeftUnaryOps() {
				setCursorDelegate();
				return super.getLeftUnaryOps();
			}

		};
		setCursorDelegate();
		QualifiedName cursorQname = cursor.getCursorQname();
		QualifiedName qname = new QualifiedName(cursorQname.getName() + "_var" + cursorQname.incrementReferenceCount(),
				cursorQname);
		cursorItemVariable = new CursorItemVariable(qname, cursorList, typeOperand);
		leftOperand = cursorItemVariable;
	}

	public boolean isFact() {
		return cursor.isFact();
	}

	/**
	 * Returns cursor index. Can be out of valid range for bound list.
	 * 
	 * @return int
	 */
	public int getIndex() {
		return cursor.getIndex();
	}

	public CursorItemVariable getCursorItemVariable() {
		return cursorItemVariable;
	}

	/**
	 * Sets index
	 * 
	 * @param index Next cursor index
	 */
	public void setIndex(int index) {
		cursor.setIndex(index, cursorItemVariable.getItemList());
	}

	/**
	 * Reset cursor to navigate forward from start of list
	 * 
	 * @return start cursor position
	 */
	public long forward() {
		long position = cursor.forward(cursorItemVariable.getItemList());
		if (cursor.isFact()) {
			cursorItemVariable.setValueByIndex(cursor.getListIndex());
			setValue(cursorItemVariable.getValue());
		} else if (isEmpty())
			setValue(EMPTY_VALUE);
		return position;
	}

	/**
	 * Reset cursor to navigate in reverse from end of list
	 * 
	 * @return previous cursor position
	 */
	public long reverse() {
		long position = cursor.reverse(cursorItemVariable.getItemList());
		if (cursor.isFact()) {
			cursorItemVariable.setValueByIndex(cursor.getListIndex());
			setValue(cursorItemVariable.getValue());
		} else if (isEmpty())
			setValue(EMPTY_VALUE);
		return position;
	}

	@Override
	public Operator getOperator() {
		return delegateOperator;
	}

	@Override
	public void run(ParserAssembler parserAssembler) {
		cursorItemVariable.run(parserAssembler);
	}

	@Override
	public int unify(Term otherTerm, int id) {
		// CursorOperand does not participate in unification.
		// The operand is only set by evaluation.
		return id;
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		EvaluationStatus status = super.evaluate(id);
		if (isEmpty())
			setValue(EMPTY_VALUE);
		setId(id);
		return status;
	}

	@Override
	public boolean backup(int id) {
		if (id == 0) {
			cursor.backup(cursorItemVariable.getItemList(), id);
			setCursorDelegate();
		}
		return super.backup(id);
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		delegateOperator.setDelegate(getValueClass());
		valueDelegateType = delegateOperator.getDelegateType();
	}

	@Override
	public void assign(Parameter parameter) {
		int index = ((Long) parameter.getValue()).intValue();
		if (index < 0) {
			index = -index;
			cursor.reverse(cursorItemVariable.getItemList(), index);
		} else
			cursor.forward(cursorItemVariable.getItemList(), index);
		setIndex(index);
		if (cursor.isFact()) {
			cursorItemVariable.setValueByIndex(cursor.getListIndex());
			setValue(cursorItemVariable.getValue());
		} else
			setValue(EMPTY_VALUE);
	}

	/**
	 * Handle notification of change of scope
	 * 
	 * @param scope The new scope which will assigned a particular locale
	 */
	@Override
	public boolean onScopeChange(Scope scope) {
		return ((LocaleListener) delegateOperator).onScopeChange(scope);
	}

	private void setCursorDelegate() {
		delegateOperator.setDelegateType(DelegateType.CURSOR);
	}

	protected void setValueOperand() {
		delegateOperator.setDelegateType(valueDelegateType);
	}

}
