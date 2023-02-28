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

import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.operator.NullOperator;

/**
 * NullOperand
 * Represents null value. Can be assigned and compared for equality.
 * @author Andrew Bowley
 * 8 Dec 2014
 */
public class NullOperand extends ExpressionOperand<Object>
{
    /** Defines operations that an Operand performs with other operands. */
    protected NullOperator operator;
    
    /**
     * Construct anonymous NullOperand object
     */
	public NullOperand()
	{
		super(QualifiedName.ANONYMOUS, new Null());
		init();
	}

    /**
     * Construct named NullOperand object
     * @param qname Qualified name
	 */
	public NullOperand(QualifiedName qname)
	{
		super(qname, new Null());
        init();
	}

    /**
     * Construct named NullOperand object with Null substitute such as Unknown
     * @param qname Qualified name
     * @param substitute Substitute
     */
    public NullOperand(QualifiedName qname, Object substitute)
    {
        super(qname, substitute);
        init();
    }

    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
	{
	}

    @Override
    public Operator getOperator()
    {
        return operator;
    }

    private void init()
    {
        operator = new NullOperator();
    }

}
