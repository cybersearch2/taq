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
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListContainer;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.AppenderSpec;
import au.com.cybersearch2.taq.list.ListItemVariable;

/**
 * AppenderVariable
 * Performs concatenation
 * @author Andrew Bowley
 * 3Aug.,2017
 */
public class AppenderVariable extends Variable implements ListContainer
{
    boolean doConcatenate;
    
    /**
     * Construct AppenderVariable object
     * @param qname Qualified name of variable
     * @param termName Term name
     * @param operator Assign or concatenate - "=" or "+="
     * @param expression Concatenation expression
     */
    public AppenderVariable(QualifiedName qname, String termName, AppenderSpec appenderSpec)
    {
        super(qname, termName, appenderSpec.getExpression());
        doConcatenate = "+=".equals(appenderSpec.getOperator());
    }

    /**
     * Set expression to evaluate value to append to list
     * @param expression Operand object
     */
    public void setExpression(Operand expression)
    {
        this.leftOperand = expression;
    }
    
    /**
     * @param doConcatenate the doConcatenate to set
     */
    public void setDoConcatenate(boolean doConcatenate)
    {
        this.doConcatenate = doConcatenate;
    }

    /**
     * evaluate
     * @see au.com.cybersearch2.taq.expression.Variable#evaluate(int)
     */
    @Override
    public EvaluationStatus evaluate(int id)
    {
    	ListItemVariable appender = (ListItemVariable)rightOperand;
        if (appender.isEmpty()) {
        		rightOperand.setExecutionContext(context);
            appender.evaluate(id);
        }
        EvaluationStatus status = super.evaluate(id);
        if (leftOperand == null)
        {   // Not used in concatenation expression, so just return appender value
            setValue(appender.getValue());
            return EvaluationStatus.COMPLETE;
        }
        if (status == EvaluationStatus.COMPLETE) 
        {
            if (doConcatenate)
                appender.append(value);
            else
            {
                appender.assign(this);
                appender.setId(id);
            }
        }
        return status;
    }

	@Override
	public ItemList<?> getList() {
    	ListItemVariable appender = (ListItemVariable)rightOperand;
        if (!appender.isEmpty())
        	return appender.getList();
		return null;
	}

	@Override
	public boolean backup(int id) {
		if (rightOperand != null)
			rightOperand.backup(id);
		return super.backup(id);
	}

}
