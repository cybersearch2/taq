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

import java.util.List;

import au.com.cybersearch2.taq.interfaces.Operand;

/**
 * OperandFinder
 * @author Andrew Bowley
 * 9Jul.,2017
 */
public class OperandFinder
{
    /** Term list */
    protected List<Operand> operandList;
    protected String name;
    private Operand foundOperand;

    /**
     * @param operandList Term list
     * @param name Name
     */
    public OperandFinder(List<Operand> operandList, String name)
    {
        this.operandList = operandList;
        this.name = name;
    }

    /**
     * Navigate operand tree for all terms. 
     * The supplied visitor may cut the navigation short causing
     * a false result to be returned.
     * @return Operand or null if not found 
     */
    public Operand findNode()
    {
        for (Operand operandItem: operandList)
        {
            if (!visit(operandItem, 1))
                return foundOperand;
        }
        return null;
    }

    /**
     * Visit a node of the Operand tree. Recursively navigates left and right operands, if any.
     * @param operand The term being visited
     * @param depth Depth in tree. The root has depth 1.
     * @return flag set true if entire tree formed by this term is navigated. 
     */
    private boolean visit(Operand operand, int depth)
    {
        if (operand.getName().equals(name))
        {
            foundOperand = operand;
            return false;
        }
        if ((operand.getLeftOperand() != null) &&
             !visit(operand.getLeftOperand(), depth + 1))
            return false;
        if ((operand.getRightOperand() != null) &&
             !visit(operand.getRightOperand(), depth + 1))
            return false;
        return true;
    }
}
