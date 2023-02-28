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
import au.com.cybersearch2.taq.operator.StringOperator;

/**
 * StringOperand
 * @author Andrew Bowley
 * 8 Dec 2014
 */
public class StringOperand  extends ExpressionOperand<String> implements LocaleListener
{
    /** Defines operations that an Operand performs with other operands. To be set by super. */
    protected StringOperator operator;

	/**
	 * Construct StringOperand with given expression Operand
     * @param qname Qualified name
	 * @param expression The Operand which evaluates value
	 */
	public StringOperand(QualifiedName qname, Operand expression) 
	{
		super(qname, expression);
		init();
	}

    /**
     * Construct StringOperand with given value
     * @param qname Qualified name
	 * @param value The value
	 */
	public StringOperand(QualifiedName qname, String value) 
	{
		super(qname, value);
        init();
	}

	/**
     * Construct StringOperand
     * @param qname Qualified name
	 */
	public StringOperand(QualifiedName qname) 
	{
		super(qname);
        init();
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		EvaluationStatus status = super.evaluate(id);
        {
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
    	String newValue = parameter.getValue().toString();
    	Parameter converted = new Parameter(Term.ANONYMOUS, newValue);
    	converted.setId(parameter.getId());
		super.assign(converted);
	}

    @Override
    public boolean onScopeChange(Scope scope)
    {
        return operator.onScopeChange(scope);
    }

    @Override
    public Operator getOperator()
    {
        return operator;
    }

	/**
	 * Override toString() to add double quotes
	 */
	@Override
	public String toString()
	{
		if (empty)
		    return super.toString();
		String valueText = ( value == null ? "null" : "\"" + value.toString() + "\"");
		return (!name.isEmpty() ? name + "=" : "") + valueText;
	}
	
    private void init()
    {
        operator = new StringOperator();
    }

}
