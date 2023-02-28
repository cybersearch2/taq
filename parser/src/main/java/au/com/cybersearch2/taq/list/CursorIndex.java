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

import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 *List item specification of a cursor
 *
 */
public class CursorIndex implements ListItemSpec {

	/** Cursor providing index value */
	private final Cursor cursor;
    /** Qualified name of list */
	private final QualifiedName qname;

	/**
	 * Construct CursorIndex object
	 * @param cursor Cursor providing index value
	 * @param qname Qualified name of list
	 */
	public CursorIndex(Cursor cursor, QualifiedName qname) {
		this.cursor = cursor;
		this.qname = qname;
	}

    /**
     * getListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getListName()
     */
    @Override
    public String getListName()
    {
        return qname.getName();
    }

    /**
     * getQualifiedListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getQualifiedListName()
     */
    @Override
    public QualifiedName getQualifiedListName()
    {
        return qname; 
    }


	@Override
	public void setQualifiedListName(QualifiedName qualifiedListName) {
	}

	@Override
	public QualifiedName getVariableName() {
        return new QualifiedName((qname.getName() + "_cursor") + qname.incrementReferenceCount(), qname);
	}

	@Override
	public void setListIndex(ListIndex index) {
		// Cursor list index is used
	}

	@Override
	public ListIndex getListIndex() {
		return cursor.getListIndex();
	}

	@Override
	public Operand getItemExpression() {
		return null;
	}

	@Override
	public String getSuffix() {
		return "cursor";
	}

	@Override
	public void setSuffix(String suffix) {
	}
	
	@Override
	public void assemble(ItemList<?> itemList) {
	}

	@Override
	public boolean evaluate(ItemList<?> itemList, int id) {
		return true;
	}

	@Override
	public void setOffset(int offset) {
		// Offset is constant 0
	}

	@Override
	public int getOffset() {
		return 0;
	}

}
