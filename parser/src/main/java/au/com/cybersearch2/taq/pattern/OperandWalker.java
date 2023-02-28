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
import au.com.cybersearch2.taq.interfaces.OperandVisitor;

/**
 * OperandWalker
 * For each term in a list, follows left and right operand links to visit all descendent operands.
 * Calls OperandVisitor next() method on each visited operand.
 * Applicable for unification or collection of terms into a map.
 * @author Andrew Bowley
 * 16 Dec 2014
 */
public class OperandWalker 
{
	/** Single operand */
    protected Operand operand;
    /** Term list */
    protected List<Operand> operandList;
    /** Flag set true if navigate all nodes */
    protected boolean isAllNodes;

    /**
     * Construct OperandWalker object to navigate a list of terms.
     * Note Term interface applied to items for compatibility with Template super class Structure, which contains Template terms.
     * @param operandList List of terms to navigate
     */
	public OperandWalker(List<Operand> operandList) 
	{
		this.operandList = new ArrayList<>();
		this.operandList.addAll(operandList);
	}

    /**
     * Construct OperandWalker object to navigate a list of terms.
     * Note Term interface applied to items for compatibility with Template super class Structure, which contains Template terms.
     * @param operandList List of terms to navigate
     * @param isAllNodes Flag set true if navigate all nodes
     */
    public OperandWalker(List<Operand> operandList, boolean isAllNodes) 
    {
        this(operandList);
        this.isAllNodes = isAllNodes;
    }

    /**
     * Construct OperandWalker object
     * @param operand Single term to navigate
     */
	public OperandWalker(Operand operand) 
	{
		this.operand = operand;
	}

	/**
     * @param isAllNodes the isAllNodes to set
     */
    public void setAllNodes(boolean isAllNodes)
    {
        this.isAllNodes = isAllNodes;
    }

    /**
	 * Navigate operand tree for all terms. 
     * The supplied visitor may cut the navigation short causing
     * a false result to be returned.
	 * @param visitor Implementation of OperandVisitor interface 
	 * @return flag set true if entire tree navigated. 
	 */
	public boolean visitAllNodes(OperandVisitor visitor)
	{
		if (operand != null)
			return visit(operand, visitor, 1);
		for (Operand operandItem: operandList)
		{
			if (!visit(operandItem, visitor, 1) && !isAllNodes)
				return false;
		}
		return true;
	}

	/**
	 * Visit a node of the Operand tree. Recursively navigates left and right operands, if any.
	 * @param operand The term being visited
	 * @param visitor Object implementing OperandVisitor interface
	 * @param depth Depth in tree. The root has depth 1.
	 * @return flag set true if entire tree formed by this term is navigated. 
	 */
	public boolean visit(Operand operand, OperandVisitor visitor, int depth)
	{
		if (!visitor.next(operand, depth) && !isAllNodes)
			return false;
		if ((operand.getLeftOperand() != null) &&
		     !visit(operand.getLeftOperand(), visitor, depth + 1) && !isAllNodes)
			return false;
		if ((operand.getRightOperand() != null) &&
			 !visit(operand.getRightOperand(), visitor, depth + 1) && !isAllNodes)
            return false;
        if (operand.getBranch1() != null) 
        {
           if (!visit(operand.getBranch1(), visitor, depth + 1) && !isAllNodes)
               return false;
           if ((operand.getBranch2() != null) &&
                !visit(operand.getBranch2(), visitor, depth + 1) && !isAllNodes)
               return false;
        }
		return true;
	}

	public void reset(Template next) {
		operandList.clear();
		next.forEach(term -> operandList.add(term));
	}

}
