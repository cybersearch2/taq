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
import au.com.cybersearch2.taq.interfaces.RightOperand;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.ListOperator;

/**
 * References a list by value
 */
public class ReferenceOperand extends Operand implements RightOperand {

	private final QualifiedName qualifiedName;
    /** Defines operations that an Operand performs with other operands. */
	private final ListOperator listOperator;
	/** ListOperand intermediary for the referenced list */
	private final ListOperand<?> subject;

    /** Optional operand for specialization eg. Currency */
    protected Operand rightOperand;
    /** List operand to be assigned subject */
    private ListOperand<?> assignee;

    /**
     * Construct ReferenceOperand object
     * @param subject Operand to reference
     */
	public ReferenceOperand(ListOperand<?> subject) {
		super(Term.ANONYMOUS);
		this.subject = subject;
		leftOperand = subject;
		listOperator = new ListOperator();
		qualifiedName = new QualifiedName(getName(), subject.getQualifiedName());
	}

	public void setAssignee(ListOperand<?> assignee) {
		this.assignee = assignee;
	}

	@Override
	public QualifiedName getQualifiedName() {
		return qualifiedName;
	}

	/**
	 * Execute operation for expression
	 * @param id Identity of caller, which must be provided for backup()
	 * @return EvaluationStatus
	 */
    @Override
	public EvaluationStatus evaluate(int id)
	{
		EvaluationStatus status = EvaluationStatus.COMPLETE;
        if (rightOperand != null) {
        	rightOperand.setExecutionContext(context);
            rightOperand.evaluate(id);
        }
        leftOperand.setExecutionContext(context);
	    status =  leftOperand.evaluate(id);
	    setValue(leftOperand.getValue());
	    if (assignee != null) {
	    	assignee.setValue(getValue());
	    	//assignee.setId(id);
	    }
	    return status;
	}
 
    
    @Override
	public Object getValue() {
		if (value instanceof ListOperand)
			return ((ListOperand<?>)value).getValue();
		return super.getValue();
	}

	/**
     * Backup to initial state if given id matches id assigned on unification or given id = 0. 
     * @param id Identity of caller. 
     * @return boolean true if backup occurred
     * @see au.com.cybersearch2.taq.language.Parameter#unify(Term otherParam, int id)
     */
    @Override
    public boolean backup(int id)
    {
        if ((rightOperand != null) && (getId() != 0))
        	rightOperand.backup(id);
       return false;
    }
    
	@Override
	public void assign(Parameter parameter) {
		if (parameter.getValueClass() == DoubleOperand.class) {
			// Check for NaN which is returned from an out of range cursor
			DoubleOperand doubleoperand = (DoubleOperand)parameter.getValue();
			if (Double.NaN == ((Double)doubleoperand.getValue()).doubleValue())
				subject.clear();
		} else {
			subject.assign(parameter);
		    setValue(leftOperand.getValue());
		}
	    int assignId = parameter.getId();
	    if (assignId != 0) 
	    {
	    	setId(assignId);
	        if (isShadow()) 
	    	    head.castShadow(getValue(), assignId);
	    }
	}

    /**
     * setRightOperand
     * @see au.com.cybersearch2.taq.interfaces.RightOperand#setRightOperand(au.com.cybersearch2.taq.interfaces.Operand)
     */
    @Override
    public void setRightOperand(Operand rightOperand)
    {
        this.rightOperand = rightOperand;
    }
    
	/**
	 * Returns null		
	 * @see au.com.cybersearch2.taq.interfaces.Operand#getRightOperand()
	 */
	@Override
	public Operand getRightOperand() 
	{
		return rightOperand;
	}

	@Override
	public Operator getOperator() {
		return listOperator;
	}
	
}
