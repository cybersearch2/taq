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
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DelegateOperator;

/**
 * DelegateOperand
 * Variable base class which delegates an operator according to value type
 * @author Andrew Bowley
 * 25 Dec 2014
 */
public abstract class DelegateOperand extends Operand implements LocaleListener 
{
	/** Qualified name of operand */
	protected QualifiedName qname;

	/**
     * Construct empty DelegateOperand object
     * @param qname Qualified name of variable
	 */
	protected DelegateOperand(QualifiedName qname) 
	{
		this(qname, qname.getName());
	}

    /**
     * Construct empty DelegateOperand object with specified term name
     * @param qname Qualified name of variable
     * @param termName Term name
     */
    protected DelegateOperand(QualifiedName qname, String termName) 
    {
        super(termName, new DelegateOperator());
        this.qname = qname;
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
     * Returns object which defines operations that an Operand performs with other operands
     * @return Operator object
     */
    @Override
    public Operator getOperator()
    {
        return getDelegateOperator().getProxy();
    }
    
    /**  
     * Handle notification of change of scope
     * @param scope The new scope which will assigned a particular locale
     */
    @Override
    public boolean onScopeChange(Scope scope)
    {
    	DelegateOperator delegateOperator = getDelegateOperator();
        if (delegateOperator.isProxyAssigned() && (delegateOperator.getProxy() instanceof LocaleListener))
            return ((LocaleListener)delegateOperator.getProxy()).onScopeChange(scope);
        return false;
    }
	
    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
    {
         setValue(parameter.getValue());
    }

	/**
	 * Delegate to perform actual unification with other Term. If successful, two terms will be equivalent. 
	 * @param otherTerm Term with which to unify
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Identity passed in param "id" or zero if unification failed
	 */
    @Override
	public int unify(Term otherTerm, int id)
	{
		int result = super.unify(otherTerm, id);
		getDelegateOperator().setDelegate(getValueClass());
		return result;
	}

}
