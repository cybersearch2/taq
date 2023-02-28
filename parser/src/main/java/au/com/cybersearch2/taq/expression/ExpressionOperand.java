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
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;

/**
 * ExpressionOperand - Named typed Parameter, with optional expression to assign a value
 * @author Andrew Bowley
 * @since 28/09/2010
 */
public abstract class ExpressionOperand<T> extends Operand
{
    /** Qualified name of operand */
    protected QualifiedName qname;
 
    /**
	 * Construct a ExpressionOperand object using given name 
     * @param qname Qualified name
	 * @throws IllegalArgumentException if name is empty
	 */
	protected ExpressionOperand(QualifiedName qname)
	{
		super(qname.getName());
		//if (name.isEmpty())
		//	throw new IllegalArgumentException("Param \"name\" is empty");
		this.qname = qname;
	}

	/**
	 * Construct a non-empty named ExpressionOperand object, callable only from sub class.
     * @param qname Qualified name
	 * @param value Object of generic type T 
	 */
	protected ExpressionOperand(QualifiedName qname, T value) 
	{
		super(qname.toString(), value);
        this.qname = qname;
	}

	/**
	 * Construct a non-empty named ExpressionOperand object
     * @param qname Qualified name
	 * @param expression Operand which evaluates value 
	 */
	protected ExpressionOperand(QualifiedName qname, Operand expression) 
	{
		this(qname, expression, qname.getName());
	}

    /**
     * Construct a non-empty named Variable object
     * @param qname Qualified name
     * @param expression Operand which evaluates value 
     * @param name Term name - should be qname.name or empty string for later name change
     */
    protected ExpressionOperand(QualifiedName qname, Operand expression, String name) 
    {
        super(name);
        this.qname = qname;
        this.leftOperand = expression;
    }

    /**
     * Returns qualified name
     * @return QualifiedName object
     */
	@Override
    public QualifiedName getQualifiedName()
    {
        return qname;
    }

	/**
	 * Returns flag set true if has expression Operand
	 * @return boolean
	 */
	public boolean hasExpression()
	{
	    return leftOperand != null;
	}

    /**
     * @param expression the expression to set
     */
    public void setExpression(Operand expression)
    {
        setLeftOperand(expression);
    }

    /**
     * Set value, mark Term as not empty
     * @param value Object. If null a Null object will be set and empty status unchanged
     */
	@SuppressWarnings("unchecked")
    @Override
    public void setValue(Object value)
    {
        setTypeValue((T) value);
    }

    /**
     * Returns value
     * @return Object of generic type T
     */
    @Override
    public Object getValue()
    {
        return super.getValue();
    }

    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
    {
	    int assignId = parameter.getId();
	    if (assignId != 0) 
	    	setId(assignId);
        setValue(parameter.getValue());
    }

	/**
	 * Backup to intial state if given id matches id assigned on unification or given id = 0. 
	 * @param id Identity of caller. 
	 * @return boolean true if backup occurred
	 * @see au.com.cybersearch2.taq.language.Parameter#unify(Term otherParam, int id)
	 */
	@Override
	public boolean backup(int id)
	{
		if (leftOperand != null)
			leftOperand.backup(id);
		return super.backup(id);
	}
	
	/**
	 * Execute operation for expression
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Flag set true if evaluation is to continue
	 */
	@Override
	public EvaluationStatus evaluate(int id)
	{
		EvaluationStatus status = EvaluationStatus.COMPLETE;
		if ((leftOperand != null) && empty)
		{
         		leftOperand.setExecutionContext(context);
			status = leftOperand.evaluate(id);
			if (!leftOperand.isEmpty())
			{
				setValue(leftOperand.getValue());
			    this.empty = false;
			    setId(id);
			}
		}
		return status;
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
		String valueText = ( value == null ? "null" : value.toString());
		return (!name.isEmpty() ? name + "=" : "") + valueText;
	}

	/**
	 * Returns null		
	 * @see au.com.cybersearch2.taq.interfaces.Operand#getRightOperand()
	 */
	@Override
	public Operand getRightOperand() 
	{
		return null;
	}

    /**
     * Set value type safe
     * @param value Object of generic type T
     */
    protected void setTypeValue(T value)
    {
        if (value == null)
            this.value = new Null();
        else
            this.value = value;
        if (isShadow()) 
    	    head.castShadow(getValue(), id);
        else if (isHead())
        	castShadow(id);
        this.empty = false;
    }

}
