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
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.operator.ComplexOperator;

/**
 * ComplexOperand
 * @author Andrew Bowley
 */
public class ComplexOperand extends ExpressionOperand<double[]> implements LocaleListener
{
    /** Defines operations that an Operand performs with other operands. To be set by super. */
    private final ComplexOperator operator;

	/**
	 * Construct named DoubleOperand object
     * @param qname Qualified name
	 */
	public ComplexOperand(QualifiedName qname) 
	{
		super(qname);
		operator = new ComplexOperator();
	}

	/**
	 * Construct named, non empty DoubleOperand object
     * @param qname Qualified name
	 * @param value Double object
	 */
	public ComplexOperand(QualifiedName qname, double[] value) 
	{
		super(qname, value);
		operator = new ComplexOperator();
	}

	/**
	 * Construct named DoubleOperand object which delegates to an expression to set value
     * @param qname Qualified name
	 * @param expression Operand which evaluates value
	 */
	public ComplexOperand(QualifiedName qname, Operand expression) 
	{
		super(qname, expression);
		operator = new ComplexOperator();
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
            if (isHead())
            	castShadow(id);
        }
        return status;
    }

    /**
     * Set value, mark Term as not empty
     * @param value Object. If null a Null object will be set and empty status unchanged
     */
    @Override
    public void setValue(Object value)
    {
    		super.setTypeValue((double[])value);
    }
    
	/**
	 * onScopeChange
	 * @see au.com.cybersearch2.taq.interfaces.LocaleListener#onScopeChange(au.com.cybersearch2.taq.Scope)
	 */
    @Override
    public boolean onScopeChange(Scope scope)
    {
        return operator.onScopeChange(scope);
    }

    /**
     * getOperator
     * @see au.com.cybersearch2.taq.interfaces.Operand#getOperator()
     */
    @Override
    public Operator getOperator()
    {
        return operator;
    }

	/**
	 * Override toString() to incorporate expression
	 * @see au.com.cybersearch2.taq.language.Parameter#toString()
	 */
	@Override
	public String toString()
	{
		if (empty)
		{
			if (leftOperand != null)
				return (!name.isEmpty() ? name + "=" : "") + (leftOperand.toString());
			return name;
		}
		String valueText = ( value == null ? "null" : valueToString());
		return (!name.isEmpty() ? name + "=" : "") + valueText;
	}

	private String valueToString() {
		return operator.getTrait().formatValue(value);
	}
}
