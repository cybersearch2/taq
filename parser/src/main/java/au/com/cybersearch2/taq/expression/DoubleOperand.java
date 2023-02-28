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
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DoubleOperator;

/**
 * DoubleOperand
 * @author Andrew Bowley
 * 1 Dec 2014
 */
public class DoubleOperand extends ExpressionOperand<Double> implements LocaleListener
{
    /** Defines operations that an Operand performs with other operands. To be set by super. */
    protected DoubleOperator operator;

	/**
	 * Construct named DoubleOperand object
     * @param qname Qualified name
	 */
	public DoubleOperand(QualifiedName qname) 
	{
		super(qname);
		init();
	}

	/**
	 * Construct named, non empty DoubleOperand object
     * @param qname Qualified name
	 * @param value Double object
	 */
	public DoubleOperand(QualifiedName qname, Double value) 
	{
		super(qname, value);
        init();
	}

	/**
	 * Construct named, non empty DoubleOperand object
     * @param qname Qualified name
	 * @param value Text
	 */
	public DoubleOperand(QualifiedName qname, String value) 
	{
		super(qname);
		this.value = value;
        init();
	}

	/**
	 * Construct named DoubleOperand object which delegates to an expression to set value
     * @param qname Qualified name
	 * @param expression Operand which evaluates value
	 */
	public DoubleOperand(QualifiedName qname, Operand expression) 
	{
		super(qname, expression);
	    init();
	}

	/**
	 * Complete object construction
	 */
    private void init()
    {
        operator = new DoubleOperator();
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
     * Set value, mark Term as not empty
     * @param value Object. If null a Null object will be set and empty status unchanged
     */
    @Override
    public void setValue(Object value)
    {
    	if (!(value instanceof Double))
    		super.setTypeValue(operator.convertObject(value, value.getClass()));
    	else
    		super.setTypeValue((Double)value);
    }
    
    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
	{
		double newValue = operator.convertObject(parameter.getValue(), parameter.getValueClass());
    	Parameter converted = new Parameter(Term.ANONYMOUS, newValue);
    	converted.setId(parameter.getId());
		super.assign(converted);
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
}
