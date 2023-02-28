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
import au.com.cybersearch2.taq.operator.BooleanOperator;

/**
 * BooleanVariable
 * @author Andrew Bowley
 * 1 Dec 2014
 */
public class BooleanOperand extends ExpressionOperand<Boolean>
{
    /** Defines operations that an Operand performs with other operands. */
    protected BooleanOperator operator;

    /**
	 * Boolean Variable
     * @param qname Qualified name
	 */
	public BooleanOperand(QualifiedName qname) 
	{
		super(qname);
		init();
	}

	/**
	 * Boolean Literal
     * @param qname Qualified name
	 * @param value Value
	 */
	public BooleanOperand(QualifiedName qname, Boolean value) 
	{
		super(qname, value);
        init();
	}

	/**
	 * Boolean Expression
     * @param qname Qualified name
	 * @param expression Operand which evaluates value
	 */
	public BooleanOperand(QualifiedName qname, Operand expression) 
	{
		super(qname, expression);
        init();
	}

	private void init()
    {
	    operator = new BooleanOperator();
	}

    /**
     * Evaluate value if expression exists
     * @param id Identity of caller, which must be provided for backup()
     * @return Flag set true if evaluation is to continue
     * @see au.com.cybersearch2.taq.expression.ExpressionOperand#evaluate(int)
     */
    @Override
    public EvaluationStatus evaluate(int id)
    {
        EvaluationStatus status = super.evaluate(id);
        if ((status == EvaluationStatus.COMPLETE) && !isEmpty())
        {
            // Perform conversion to Double, if required
            setValue(operator.convertObject(value, getValueClass()));
            if (isHead())
            	castShadow(id);
        }
        return status;
    }

    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
	{
		boolean newValue = operator.convertObject(parameter.getValue(), parameter.getValueClass());
    	Parameter converted = new Parameter(Term.ANONYMOUS, newValue);
    	converted.setId(parameter.getId());
		super.assign(converted);
	}

    @Override
    public Operator getOperator()
    {
        return operator;
    }

}
