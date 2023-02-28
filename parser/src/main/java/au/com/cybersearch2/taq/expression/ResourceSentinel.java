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

import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.CursorList;
import au.com.cybersearch2.taq.list.ResourceCursor;
import au.com.cybersearch2.taq.operator.CursorOperator;

/**
 * Operand to reset a resource cursor on backup. Does not interact with other operands.
 */
public class ResourceSentinel extends Operand {

	/** Resource cursor to control */
	private final ResourceCursor cursor;
    /** Qualified name of operand */
	private final QualifiedName qname;
	/** Cursor operator */
    private final Operator operator;

    /**
     * Construct ResourceSentinal object
     * @param cursor Resource cursor to control
     */
	public ResourceSentinel(ResourceCursor cursor) {
		super(CursorList.getPartName(CursorSentinelOperand.SENTINAL, cursor).getName());
        this.cursor = cursor;
        qname = CursorList.getPartName(CursorSentinelOperand.SENTINAL, cursor);
		operator = new CursorOperator();
		setPrivate(true);
	}

	@Override
	public QualifiedName getQualifiedName() {
		return qname;
	}

	@Override
	public void assign(Parameter parameter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Operand getRightOperand() {
		return null;
	}

	@Override
	public Operator getOperator() {
		return operator;
	}

    /**
     * Execute operation for expression
     * @param id Identity of caller, which must be provided for backup()
     * @return Flag set true if evaluation is to continue
     */
    @Override
    public EvaluationStatus evaluate(int id)
    {
    	cursor.evaluate();
    	if (isEmpty())
		    setValue(new Blank());
    	setId(id);
        return EvaluationStatus.COMPLETE;
    }

	@Override
	public boolean backup(int id) {
		if ((id == 0) || (id == getId())) {
			cursor.backup();
		    return true;
		}
		return false;
	}

}
