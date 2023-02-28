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
package au.com.cybersearch2.taq.operator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.BigDecimalTerm;
import au.com.cybersearch2.taq.language.BooleanTerm;
import au.com.cybersearch2.taq.language.DoubleTerm;
import au.com.cybersearch2.taq.language.IntegerTerm;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.StringTerm;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * DelegateOperator
 * Proxies operator according to delegate type
 * @see DelegateType
 * @author Andrew Bowley
 * 29Apr.,2017
 */
public class DelegateOperator implements Operator
{
    /** Map value class to Operator delegate type */
    protected static Map<Class<?>, DelegateType> delegateTypeMap;

    static
    {
        delegateTypeMap = new HashMap<Class<?>, DelegateType>();
        delegateTypeMap.put(String.class, DelegateType.STRING);
        delegateTypeMap.put(Integer.class, DelegateType.INTEGER);
        delegateTypeMap.put(Long.class, DelegateType.INTEGER);
        delegateTypeMap.put(Boolean.class, DelegateType.BOOLEAN);
        delegateTypeMap.put(Double.class, DelegateType.DOUBLE);
        delegateTypeMap.put(BigDecimal.class, DelegateType.DECIMAL);
        delegateTypeMap.put(AxiomTermList.class, DelegateType.ASSIGN_ONLY);
        delegateTypeMap.put(Unknown.class, DelegateType.ASSIGN_ONLY);
        delegateTypeMap.put(Axiom.class, DelegateType.AXIOM);
        delegateTypeMap.put(AxiomList.class, DelegateType.AXIOM);
        delegateTypeMap.put(ListOperand.class, DelegateType.LIST);
        delegateTypeMap.put(Blank.class, DelegateType.STRING);
        delegateTypeMap.put(Null.class, DelegateType.NULL);
        delegateTypeMap.put(StringTerm.class, DelegateType.STRING);
        delegateTypeMap.put(IntegerTerm.class, DelegateType.INTEGER);
        delegateTypeMap.put(BooleanTerm.class, DelegateType.BOOLEAN);
        delegateTypeMap.put(DoubleTerm.class, DelegateType.DOUBLE);
        delegateTypeMap.put(BigDecimalTerm.class, DelegateType.DECIMAL);
    }

    /** Defines operations that an Operand performs with other operands. */
    protected Operator proxy;
    /** Current operator DelegateType */
    protected DelegateType delegateType;
    protected boolean isProxyAssigned;

    /**
     * Construct DelegateOperator object
     */
    public DelegateOperator()
    {   // Default to type ASSIGN_ONLY
        delegateType = DelegateType.ASSIGN_ONLY;
        proxy = operatorInstance(DelegateType.ASSIGN_ONLY);
    }
    
    /**
     * Delegates operator depending on given value class
     * @param clazz Class
     */
    public void setDelegate(Class<?> clazz)
    {
        DelegateType newDelegateType = delegateTypeMap.get(clazz);
        if (newDelegateType == null)
            newDelegateType = DelegateType.ASSIGN_ONLY;
        if (newDelegateType != delegateType)
        {
            delegateType = newDelegateType;
            if (!isProxyAssigned)
                proxy = operatorInstance(newDelegateType);
        }
    }

    /**
     * @return the proxy
     */
    public Operator getProxy()
    {
        return proxy;
    }

    public void setProxy(Operator proxy)
    {
        this.proxy = proxy;
        isProxyAssigned = true;
    }
    
    /**
     * @return the isProxyAssigned
     */
    public boolean isProxyAssigned()
    {
        return isProxyAssigned;
    }

    /**
     * @return the delegateType
     */
    public DelegateType getDelegateType()
    {
        return delegateType;
    }

    /**
     * Set delegate type
     * @param delegateType Delegate type
     */
    public void setDelegateType(DelegateType delegateType)
    {
        this.delegateType = delegateType;
        proxy = operatorInstance(delegateType);
    }
    
    /**
     * @param clazz Class
     * @return the delegateType
     */
    public DelegateType getDelegateTypeForClass(Class<?> clazz)
    {
        return delegateTypeMap.get(clazz);
    }

    /**
     * getTrait
     * @see au.com.cybersearch2.taq.interfaces.Operator#getTrait()
     */
    @Override
    public Trait getTrait()
    {
        return proxy.getTrait();
    }

    @Override
    public void setTrait(Trait trait)
    { 
        // Don't touch proxy if trait not for any particular operand type
        if (trait.getOperandType() != OperandType.UNKNOWN)
            proxy.setTrait(trait);
    }
    
    @Override
    public OperatorEnum[] getRightBinaryOps() 
    {
        return proxy.getRightBinaryOps();
    }

	@Override
	public OperatorEnum[] getRightUnaryOps() {
        return proxy.getRightUnaryOps();
	}
	
    @Override
    public OperatorEnum[] getLeftBinaryOps() 
    {
        return proxy.getLeftBinaryOps();
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
        return proxy.getLeftUnaryOps();
	}
	
    @Override
    public OperatorEnum[] getConcatenateOps()
    {
        return proxy.getConcatenateOps();
    }

    @Override
    public Object numberEvaluation(OperatorEnum operatorEnum2, Term rightTerm) 
    {
        return proxy.numberEvaluation(operatorEnum2, rightTerm);
    }

    @Override
    public Object numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2,
            Term rightTerm) 
    {
        return proxy.numberEvaluation(leftTerm, operatorEnum2, rightTerm);
    }

    @Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2,
            Term rightTerm) 
    {
        return proxy.booleanEvaluation(leftTerm, operatorEnum2, rightTerm);
    }

    /**
     * Returns new instance of operator for given delegate type
     * @param delegateType Delegate type
     * @return Operator object
     */
    protected static Operator operatorInstance(DelegateType delegateType)
    {
        return delegateType.getOperatorFactory().delegate();
    }

    /**
     * Returns flag set true if give class is a delegate class
     * @param clazz Class to check
     * @return boolean
     */
    public static boolean isDelegateClass(Class<?> clazz)
    {
        return delegateTypeMap.containsKey(clazz);
    }

    public static Operator getOperator(Class<?> clazz)
    {
        DelegateType newDelegateType = delegateTypeMap.get(clazz);
        if (newDelegateType == null)
            newDelegateType = DelegateType.ASSIGN_ONLY;
        return operatorInstance(newDelegateType);
    }

}
