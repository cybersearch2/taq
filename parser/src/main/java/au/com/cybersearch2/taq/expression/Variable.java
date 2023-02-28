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

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.RightOperand;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DelegateOperator;
import au.com.cybersearch2.taq.operator.DelegateType;

/**
 * Variable
 * Operand without a generic type and not assigned a value until after construction.
 * When non-empty (and the value is not null), it implements the Operand interface 
 * by delegating to an Operand matching the type of the value. If empty (or the value is null)
 * delegation is to a NullOperand instance.
 * @author Andrew Bowley
 * 11 Dec 2014
 */
public class Variable extends DelegateOperand implements RightOperand
{
    /** Optional operand for specialization eg. Currency */
    protected Operand rightOperand;
    /** Reflexive operator */
    protected OperatorEnum reflexOp;
 
	/**
	 * Construct a Variable object
	 * @param qname Qualified name
	 */
	public Variable(QualifiedName qname) 
	{
		this(qname, qname.getName());
    }

    /**
     * Construct a Variable object with specified term name
     * @param qname Qualified name
     * @param termName Term name
     */
    public Variable(QualifiedName qname, String termName) 
    {
        super(qname, termName);
    }

	/**
	 * Construct a Variable object which uses an Expression operand to evaluate it's value
     * @param qname Qualified name of variable
	 * @param expression Operand to initialize this Variable upon evaluation
	 */
	public Variable(QualifiedName qname, Operand expression) 
	{
		this(qname);
		setLeftOperand(expression);
	}

	/**
	 * Construct a Variable object which uses an Expression operand to evaluate both it's value and 
	 * that of an operand it is shadowing
     * @param target Expression operand
	 * @param expression Operand to initialize this Variable upon evaluation
	 */
	public Variable(Operand target, Operand expression) 
	{
		this(target.getQualifiedName());
		target.addShadow(this);
		setLeftOperand(expression);
	}

   /**
     * Construct a Variable object which uses an Expression operand to evaluate it's value
     * @param qname Qualified name of variable
     * @param termName Term name
     * @param expression Operand to initialize this Variable upon evaluation
     */
    public Variable(QualifiedName qname, String termName, Operand expression) 
    {
        this(qname, termName);
		setLeftOperand(expression);
    }

    public void setExpression(Operand expression) {
		setLeftOperand(expression);
	}

	/**
     * @return the delegateType
     */
    public DelegateType getDelegateType()
    {
        return getDelegateOperator().getDelegateType();
    }

    /**
     * Set delegate type
     * @param delegateType Delegate type
     */
    public void setDelegateType(DelegateType delegateType)
    {
    	DelegateOperator operator = getDelegateOperator();
    	operator.setDelegateType(delegateType);
        // Fix cursor type delegate permanently for special types
        if (delegateType == DelegateType.CURSOR)
            operator.setProxy(operator.getProxy());
    }
    
	/**
     * @param reflexOp the reflexOp to set
     * @param self Operand performing reflex operation
     */
    public void setReflexOp(OperatorEnum reflexOp, Operand self)
    {
        this.reflexOp = reflexOp;
        rightOperand = self;
        setLeftOperand(new Evaluator(self, reflexOp.toString(), leftOperand));
    }

    public OperatorEnum getReflexOp() {
		return reflexOp;
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() || ((reflexOp != null) && (leftOperand != null) && leftOperand.isEmpty());
	}

    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
	{
	    setValue(parameter.getValue());
	    int assignId = parameter.getId();
	    if (assignId != 0) 
	    {
	    	setId(assignId);
	        if (isShadow()) 
	    	    head.castShadow(getValue(), assignId);
	    }
	}

	@Override
	public void setExecutionContext(ExecutionContext context) {
		this.context = context;
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
        if (reflexOp != null) {
        		rightOperand.setExecutionContext(context);
            rightOperand.evaluate(id);
        }
		if (leftOperand != null)
		{
		    if (leftOperand.isEmpty())
		    {
	        		leftOperand.setExecutionContext(context);
		        status =  leftOperand.evaluate(id);
		        if (reflexOp != null)
		            rightOperand.setValue(leftOperand.getValue());
		    } 
		    // Only use expression value if empty or 
		    // same id used as last time value was set on this variable
			if (!leftOperand.isEmpty() && (empty || (this.id == id) || isShadow()))
			{
                if (reflexOp != null)
                	getDelegateOperator().setProxy(rightOperand.getOperator());
			    if (isShadow()) 
			    {
			    	if (head.isEmpty())
			    		head.evaluate(id);
			    	head.castShadow(leftOperand.getValue(), id);
			    } else
				    setValue(leftOperand.getValue());
			    this.id = id;
 			}
		}
		return status;
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
        if ((leftOperand != null) && (getId() != 0))
        	leftOperand.backup(id);
        // Only backup if forced (id = 0) or same id as used for evaluation
        if ((id == 0) || (id == this.id))
        {
            super.backup(id);
            return true;
        }
        return false;
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

    /**
     * Override toString() to report &lt;empty&gt;, null or value
     * @see au.com.cybersearch2.taq.language.Parameter#toString()
     */
    @Override
    public String toString()
    {
        if (empty)
        {
        	boolean isAssign = reflexOp != null;
            if (leftOperand != null)
                return (!name.isEmpty() && !isAssign ? name + "=" : "") + (leftOperand.toString());
            return name;
        }
        String valueText = ( value == null ? "null" : value.toString());
        return (!name.isEmpty() ? name + "=" : "") + valueText;
    }


}
