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
import au.com.cybersearch2.taq.operator.IntegerOperator;

/**
 * LongOperand
 * @author Andrew Bowley
 * 1 Dec 2014
 */
public class IntegerOperand extends ExpressionOperand<Long> implements LocaleListener
{
    /** Defines operations that an Operand performs with other operands. */
    protected IntegerOperator operator;

	/**
	 * Construct a variable IntegerOperand object
     * @param qname Qualified name
	 */
	public IntegerOperand(QualifiedName qname) 
	{
		super(qname);
        operator = new IntegerOperator();
	}

    /**
     * Construct a literal IntegerOperand object
     * @param qname Qualified name
     * @param value Long object
     */
    public IntegerOperand(QualifiedName qname, Integer value) 
    {
        this(qname);
        super.setValue(value.longValue());
    }

	/**
	 * Construct a literal IntegerOperand object
     * @param qname Qualified name
	 * @param value Text
	 */
	public IntegerOperand(QualifiedName qname, String value) 
	{
		this(qname);
		this.value = value;
	}

	/**
	 * Construct a literal IntegerOperand object
     * @param qname Qualified name
	 * @param value Long object
	 */
	public IntegerOperand(QualifiedName qname, Long value) 
	{
		this(qname);
        super.setValue(value);
	}

	/**
	 * Long Expression
     * @param qname Qualified name
	 * @param expression Operand which evaluates value
	 */
	public IntegerOperand(QualifiedName qname, Operand expression) 
	{
		super(qname, expression);
        operator = new IntegerOperator();
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		EvaluationStatus status = super.evaluate(id);
         // Perform conversion to Integer, if required
        setValue(operator.convertObject(value, getValueClass()));
        if (isHead())
        	castShadow(id);
		return status;
	}

    /**
     * Set value, mark Term as not empty
     * @param value Object. If null a Null object will be set and empty status unchanged
     */
    @Override
    public void setValue(Object value)
    {
    	if (!(value instanceof Long))
    		super.setTypeValue(operator.convertObject(value, value.getClass()));
    	else
    		super.setTypeValue((Long)value);
    }

	/**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
	{
    	long newValue = operator.convertObject(parameter.getValue(), parameter.getValueClass());
    	Parameter converted = new Parameter(Term.ANONYMOUS, newValue);
    	converted.setId(parameter.getId());
		super.assign(converted);
	}

    @Override
    public boolean onScopeChange(Scope scope)
    {
    	if (!operator.getTrait().getLocale().equals(scope.getLocale()))  {
             operator.getTrait().setLocale(scope.getLocale());
             return true;
    	}
    	return false;
    }

    @Override
    public Operator getOperator()
    {
        return operator;
    }

}
