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

import au.com.cybersearch2.taq.interfaces.Operand;

/**
 * OperandContext
 * Leaf component of tree to preserve Term states
 * @author Andrew Bowley
 * 17 Aug 2015
 */
public class OperandContext
{
    protected int id;
    protected Object value;
    protected OperandContext left;
    protected OperandContext right;
    protected Operand term;
    protected OperandContext next;

    /**
     * Create OperandContext object
     * @param term Term to preserve
     */
    public OperandContext(Operand term)
    {
        this.term = term;
        id = term.getId();
        if (!term.isEmpty())
            value = term.getValue();
    }

    /**
     * Returns Term id
     * @return int
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns Term value
     * @return Object or null if term is empty
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Returns left term OperandContext
     * @return OperandContext object or null
     */
    public OperandContext getLeft()
    {
        return left;
    }

    /**
     * Returns right term OperandContext
     * @return OperandContext object or null
     */
    public OperandContext getRight()
    {
        return right;
    }

    /**
     * Returns next term OperandContext
     * @return OperandContext object or null
     */
    public OperandContext getNext()
    {
        return next;
    }

    /**
     * Sets left term OperandContext
     * @param left Left OperandContext
     */
    public void setLeft(OperandContext left)
    {
        this.left = left;
    }

    /**
     * Sets right term OperandContext
     * @param right Right OperandContext
     */
    public void setRight(OperandContext right)
    {
        this.right = right;
    }

    /**
     * Sets next term OperandContext
     * @param next OperandContext
     */
    public void setNext(OperandContext next)
    {
        this.next = next;
    }

    /**
     * Restore values to terms which originally not empty 
     * Propagates to tree and siblings with this OperandContext as root
     */
    public void restore()
    {
        if (value != null)
        {   // Term had value
             term.setValue(value);
             term.setId(id);
        }
        if (left != null)
            left.restore();
        if (right != null)
            right.restore();
        if (next != null)
            next.restore();
    }
}
