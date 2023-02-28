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

import java.math.BigDecimal;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.RightOperand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.BigDecimalOperator;

/**
 * BigDecimalOperand
 * @author Andrew Bowley
 * 3 Dec 2014
 */
public class BigDecimalOperand extends ExpressionOperand<BigDecimal> implements LocaleListener, RightOperand 
{
    /** Defines operations that an Operand performs with other operands. */
    protected BigDecimalOperator operator;
    /** Optional operand for Currency type */
    protected Operand rightOperand;
    
	/**
	 * Construct named, empty BigDecimalOperand object
     * @param qname Qualified name
	 */
	public BigDecimalOperand(QualifiedName qname) 
	{
		super(qname);
		init();
	}

	/**
	 * Construct named, non-empty BigDecimalOperand object
     * @param qname Qualified name
	 * @param value BigDecimal
	 */
	public BigDecimalOperand(QualifiedName qname, BigDecimal value) 
	{
		super(qname, value);
        init();
	}

	/**
	 * Construct named, non-empty BigDecimalOperand object
     * @param qname Qualified name
	 * @param value Text value 
	 */
	public BigDecimalOperand(QualifiedName qname, String value) 
	{
		super(qname);
		this.value = value;
		empty = false;
        init();
	}

	/**
	 * Construct named BigDecimalOperand object which delegates to an expression to set value
     * @param qname Qualified name
	 * @param expression Operand which evaluates value
	 */
	public BigDecimalOperand(QualifiedName qname, Operand expression) 
	{
		super(qname, expression);
        init();
	}

	/**
	 * Override operator to customize behaviour
	 * @param operator Compatible operator
	 */
    public void setOperator(BigDecimalOperator operator)
    {
        this.operator = operator;
    }
    
    /**
     * Evaluate value if expression exists
     * @param id Identity of caller, which must be provided for backup()
     * @return Flag set true if evaluation is to continue
     */
    @Override
    public EvaluationStatus evaluate(int id)
    {
        if (rightOperand != null)
            rightOperand.evaluate(id);
        EvaluationStatus status = super.evaluate(id);
        if ((status == EvaluationStatus.COMPLETE) && !isEmpty())
        {
            // Perform conversion to BigDecimal, if required
            setValue(operator.convertObject(value, getValueClass()));
            if (isHead())
            	castShadow(id);
        }
        return status;
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
        if (rightOperand != null)
            rightOperand.backup(id);
        return super.backup(id);
    }

    /**
     * Set value, mark Term as not empty
     * @param value Object. If null a Null object will be set and empty status unchanged
     */
    @Override
    public void setValue(Object value)
    {
    	if (!(value instanceof BigDecimal))
    		super.setTypeValue(operator.convertObject(value, value.getClass()));
    	else
    		super.setTypeValue((BigDecimal)value);
    }

    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
	{
    	BigDecimal newValue = operator.convertObject(parameter.getValue(), parameter.getValueClass());
    	Parameter converted = new Parameter(Term.ANONYMOUS, newValue);
    	converted.setId(parameter.getId());
		super.assign(converted);
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
    public boolean onScopeChange(Scope scope)
    {
        return operator.onScopeChange(scope);
    }

    @Override
    public Operator getOperator()
    {
        return operator;
    }

    @Override
    public void setRightOperand(Operand rightOperand)
    {
        this.rightOperand = rightOperand;
    }
    
    /**
     * @see au.com.cybersearch2.taq.expression.ExpressionOperand#toString()
     */
    @Override
    public String toString()
    {
        if (operator.getTrait().getOperandType() == OperandType.CURRENCY)
        {
            String country = operator.getTrait().getCountry();
            if (!country.isEmpty())
                return country + " " + super.toString();
            else if (rightOperand != null)
                return rightOperand.toString() + " " + super.toString();
        }
        return super.toString();
    }

    private void init()
    {
        operator = new BigDecimalOperator();
    }

}
