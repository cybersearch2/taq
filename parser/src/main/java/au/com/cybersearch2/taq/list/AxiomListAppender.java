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
package au.com.cybersearch2.taq.list;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * AxiomListAppender
 * Operand to navigate an axiom list
 * @author Andrew Bowley
 */
public class AxiomListAppender extends ListItemVariable {

	/**
	 * Construct an AxiomListAppender object
	 * @param qname Qualified name of Variable - list name with "_appender" appended 
	 * @param arrayIndex Array index
	 */
	public AxiomListAppender(QualifiedName qname, ArrayIndex arrayIndex) {
		super(qname, arrayIndex, null);
	}

	/**
	 * Construct an AxiomListAppender object
	 * @param qname Qualified name of Variable - list name with "_appender" appended 
     * @param ItemList<?> List being referenced
	 * @param arrayIndex Array index
	 */
	public AxiomListAppender(QualifiedName qname, ItemList<?> itemList, ArrayIndex arrayIndex) {
		super(qname, itemList, arrayIndex, null);
	}

	@Override
    public void append(Object value)
    {
		if (value instanceof AxiomList) {
			AxiomList axiomList = (AxiomList)value;
			axiomList.forEach(axiom ->  {
				super.append(axiom);
			});
		} else if (value instanceof Axiom)
			super.append((Axiom) value);
	    else if (value instanceof AxiomTermList)
		    super.append(((AxiomTermList) value).newInstance());
		else
			throw new ExpressionException(String.format("Cannot append value '%s'", value.toString()));
    }
	
}
