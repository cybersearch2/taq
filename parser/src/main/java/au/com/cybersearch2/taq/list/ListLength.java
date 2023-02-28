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

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListContainer;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * ListLength
 * Operand to evaluate length of a list
 * @author Andrew Bowley
 * 16 Jan 2015
 */
public class ListLength extends Variable implements ParserRunner, LocaleListener
{
	/** List name */
	protected QualifiedName listName;
	/** The list object */
    protected ItemList<?> itemList;
    /** Operand containing a list value */
    protected Operand itemListOperand;
    /** Manages scope transitions of a context list */
    private ContextListHandler contextListHandler;

	/**
	 * Construct a ListLength object
	 * @param qname Qualified name of Variable - list name with "_length" appended 
	 * @param listName Qualified list name in text format
	 */
	public ListLength(QualifiedName qname, QualifiedName listName) 
	{
		super(qname);
		this.listName = listName;
	}

	/**
	 * Construct a ListLength object
	 * @param qname Qualified name of Variable - list name with "_length" appended 
	 * @param subject Operand containing a list value
	 */
	public ListLength(QualifiedName qname, Operand subject) 
	{
		super(qname);
		itemListOperand = subject;
	}

	/**
	 * Evaluate list length. 
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Flag set true if evaluation is to continue
	 */
	@Override
	public EvaluationStatus evaluate(int id) 
	{
        if (contextListHandler != null) {
        	Operand dynamicOperand = contextListHandler.getDynamicOperand();
        	if (dynamicOperand != null)
        		dynamicOperand.evaluate(id);
        }
	    if (itemListOperand != null)
	    {
        	if (itemListOperand instanceof ListContainer)
        		itemList = ((ListContainer)itemListOperand).getList();
        	else 
                itemList = (ItemList<?>)itemListOperand.getValue();
	        if ((itemList == null) || (itemList instanceof Null))
                throw new ExpressionException("List variable \"" + itemListOperand.getName() + "\" is not found");
	    }
		setValue(Integer.valueOf(itemList != null ? itemList.getLength() : 0));
		this.id = id;
		return EvaluationStatus.COMPLETE;
	}
	
    @Override
	public boolean backup(int id) {
		boolean backed = super.backup(id);
        if (contextListHandler != null) {
        	Operand dynamicOperand = contextListHandler.getDynamicOperand();
        	if (dynamicOperand != null)
        		dynamicOperand.backup(id);
        }
        return backed;
	}

	@Override
    public boolean onScopeChange(Scope scope)
    {
		boolean isScopeChange = super.onScopeChange(scope);
    	boolean isContextChange = 
    			(contextListHandler != null) &&
    			!scope.getName().equals(contextListHandler.getCurrentScope());
        if (isContextChange) {
			ItemList<?>  contexItemList = contextListHandler.onScopeChange(scope);
            if (contexItemList != null)
            	itemList = contexItemList;
        }
		return isScopeChange;
    }
    
    @Override
    public void run(ParserAssembler parserAssembler)
    {
        ListParserRunner parserRunner = new ListParserRunner(listName);
        parserRunner.run(parserAssembler);
        if (parserRunner.isContextList()) {
        	QualifiedName targetName = parserRunner.getTargetName();
        	if (targetName != null)
        		listName = targetName;
        	contextListHandler = new ContextListHandler(listName.getName());
        	contextListHandler.assembleContextVariable(parserAssembler);
        	parserAssembler.registerLocaleListener(this);
        } else {
	        itemList = parserRunner.getItemList();
	        if (itemList == null)
                throw new ExpressionException("List \"" + listName + "\" cannot be found");
        }
    }

	@Override
	public String toString() {
		return empty ? (listName + ".size()") : getValue().toString();
	}

}
