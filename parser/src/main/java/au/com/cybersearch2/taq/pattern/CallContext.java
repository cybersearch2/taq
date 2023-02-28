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
package au.com.cybersearch2.taq.pattern;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Term;

/**
 * CallContext
 * Preserves state of template so it can be reused during query evaluation
 * @author Andrew Bowley
 * 2 Mar 2015
 */
public class CallContext 
{
    /** Names of lists which are empty at time of object construction */
    protected List<String> emptyListNames;
    /** Tree of operand contexts used to save and restore initial state */
    protected List<OperandContext> contextRootList;
    /** Next CallContext object in stack */
    protected CallContext next;
    /** Reference to template properties */
    protected List<Term> properties;
    /** Saved template properties or null if no properties */
    protected List<Term> contextProperties;

	/**
	 * Construct CallContext object
	 * @param template Template to perform call
	 */
	public CallContext(Template template) 
	{
		contextRootList = new ArrayList<OperandContext>();
        OperandContext operandContext = null;
        // Build operand context tree by visiting all operands 
 		for (int i = 0; i < template.getTermCount(); i++)
		{
		    Operand term = template.getTermByIndex(i);
		    OperandContext nextOperandContext = new OperandContext(term);
		    if (i == 0)
	            contextRootList.add(nextOperandContext);
		    else
		        operandContext.setNext(nextOperandContext);
            operandContext = nextOperandContext;
		    visit(term, operandContext);
		}
  		if (!template.getProperties().getProperties().isEmpty())
 		{
  		    // Save template properties reference and contents
  	        properties = template.getProperties().getProperties();
  	        contextProperties = new ArrayList<Term>();
  	        contextProperties.addAll(properties);
 		}
	}

	/**
	 * Returns next CallContext object in the call stack
	 * @return CallContext object or null
	 */
	public CallContext getNext()
    {
        return next;
    }

	/**
	 * Set next CallContext object in the call stack
	 * @param next CallContext object
	 */
    public void setNext(CallContext next)
    {
        this.next = next;
    }

    /**
	 * Restore template operand values and properties
	 */
	public void restoreContext()
	{
	    for (OperandContext  operandContext: contextRootList)
	        operandContext.restore();
	    if (contextProperties != null)
	    {
	        properties.clear();
	        properties.addAll(contextProperties);
	    }
	}

	/**
	 * Visit term in evaluation tree to build context tree
	 * @param operand Operand
	 * @param operandContext Leaf component of tree to preserve Term states
	 */
    protected void visit(Operand operand, OperandContext operandContext)
    {
        Operand left = operand.getLeftOperand();
        if (left != null)
        {
            OperandContext leftOperandContext = new OperandContext(left);
            operandContext.setLeft(leftOperandContext);
            visit(left, leftOperandContext);
        }
        Operand right = operand.getRightOperand();
        if (right != null)
        {
            OperandContext rightOperandContext = new OperandContext(right);
            operandContext.setRight(rightOperandContext);
            visit(right, rightOperandContext);
        }
    }
}
