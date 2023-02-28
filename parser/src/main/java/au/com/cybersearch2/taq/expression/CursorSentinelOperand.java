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

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.ContextListHandler;
import au.com.cybersearch2.taq.list.Cursor;
import au.com.cybersearch2.taq.list.CursorList;
import au.com.cybersearch2.taq.list.ListParserRunner;
import au.com.cybersearch2.taq.operator.CursorOperator;

/**
 * Operand to initialize cursor. Does not interact with other operands.
 * Encapsulates a cursor and a reference to the list it operates on
 */
public class CursorSentinelOperand extends Operand  implements CursorList, LocaleListener, ParserRunner {

	protected static final String SENTINAL = "sentinal";
	
    /** Qualified name of operand */
	private final QualifiedName qname;
	/** Cursor operator */
    private final Operator operator;

    /** Cursor to navigate list */
	private final Cursor cursor;
	/** Qualified name of list tied to cursor */
	private final QualifiedName qualifiedListName;
	/** List tied to cursor */
	private ItemList<?> itemList;
	/** Operand list tied to cursor */
    private ListOperand<?> listOperand;
	
	/** Manages scope transitions of a context list */
    private ContextListHandler contextListHandler;
    /** Context list proxy */
    private ListOperand<?> contextListOperand;

    /**
     * Construct CursorSentinalOperand object
	 * @param cursor Cursor object
	 * @param qualifiedListName Qualified name of list tied to cursor
     */
	public CursorSentinelOperand(Cursor cursor, QualifiedName qualifiedListName) {
		super(CursorList.getPartName(SENTINAL, cursor).getName());
        this.cursor = cursor;
        this.qualifiedListName = qualifiedListName;
        qname = CursorList.getPartName(SENTINAL, cursor);
		operator = new CursorOperator();
		setPrivate(true);
	}

	/**
	 * Construct CursorList object for list variable
	 * @param cursor Cursor object
	 * @param itemList List tied to cursor
	 */
	public CursorSentinelOperand(Cursor cursor, ItemList<?> itemList) {
        this(cursor, itemList.getQualifiedName());
        this.itemList = itemList;
    	if (itemList instanceof ListOperand)
    		listOperand = (ListOperand<?>)itemList;
	}

	/**
	 * Construct CursorList object for list variable
	 * @param cursor Cursor object
	 * @param listOperand Operand list tied to cursor
	 */
	public CursorSentinelOperand(Cursor cursor, ListOperand<?> listOperand) {
        this(cursor, listOperand.getListName());
        this.listOperand = listOperand;
        itemList = (ItemList<?>)listOperand;
	}

    /**
     * Resolve list, if required
     * @param parserAssembler Parser assembler
     * @return left hand operand for list evaluation or null if not required 
     */
    public Operand assembleList(ParserAssembler parserAssembler)
    {
    	if ((listOperand != null))
    		return listOperand;
    	if (itemList == null) {
	        ListParserRunner parserRunner = new ListParserRunner(qualifiedListName);
	        parserRunner.run(parserAssembler);
	        if (!parserRunner.isContextList())
	        {
		        itemList = parserRunner.getItemList();
		        if (itemList == null)
		        {
		        	Operand listOperand = parserRunner.getListOperand();
		        	if (listOperand == null)
		                throw new ExpressionException("List \"" + qualifiedListName.toString() + "\" cannot be found");
			    	VariableSpec varSpec = new VariableSpec(OperandType.UNKNOWN);
			    	VariableFactory variableFactory = new VariableFactory(varSpec);
		        	itemList = variableFactory.getItemListInstance(qualifiedListName, true);
		        	return (ListOperand<?>)itemList;
		        }
	        } else {
	        	contextListHandler = new ContextListHandler(qualifiedListName.getName());
	        	itemList = contextListHandler.assembleContextVariable(parserAssembler);
	        	parserAssembler.registerLocaleListener(this);
	        	contextListOperand = (ListOperand<?>)itemList;
	        	return contextListOperand;
	        }
    	}
        return null;
    }


	@Override
    public boolean isFact() {
    	return cursor.isFact();
    }
 
	@Override
    public QualifiedName getCursorQname() {
		return cursor.getCursorQname();
	}

	@Override
	public QualifiedName getQualifiedListName() {
		return qualifiedListName;
	}

	@Override
	public Cursor getCursor() {
		return cursor;
	}
	
	@Override
	public int getIndex() {
		return cursor.getIndex();
	}
	
	@Override
	public ItemList<?> getItemList() {
		return itemList == null ? (ItemList<?>)listOperand : itemList;
	}
	
	@Override
	public QualifiedName getQualifiedName() {
		return qname;
	}

    /**
     * Execute operation for expression
     * @param id Identity of caller, which must be provided for backup()
     * @return Flag set true if evaluation is to continue
     */
    @Override
    public EvaluationStatus evaluate(int id)
    {
        if (contextListHandler != null) {
        	Operand dynamicOperand = contextListHandler.getDynamicOperand();
        	if (dynamicOperand != null) {
	        		dynamicOperand.setExecutionContext(context);
        		dynamicOperand.evaluate(id);
        	}
        }
     	if (leftOperand != null) {
        		leftOperand.setExecutionContext(context);
    		leftOperand.evaluate(id);
     	}
    	evaluateList(id);
		setValue(new Blank());
    	setId(id);
        return EvaluationStatus.COMPLETE;
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

	@Override
	public boolean backup(int id) {
        if (contextListHandler != null) {
        	Operand dynamicOperand = contextListHandler.getDynamicOperand();
        	if (dynamicOperand != null)
        		dynamicOperand.backup(id);
        }
		if ((id == 0) || (id == getId())) {
			boolean backed = backupList(0);
	    	if (leftOperand != null)
	    		leftOperand.backup(id);
		    return backed;
		}
		return false;
	}

    /**
     * Resolve list, if required
     * @param parserAssembler Parser assembler
     * @return left hand operand for list evaluation or null if not required 
     */
    @Override
    public void run(ParserAssembler parserAssembler)
    {
    	assembleList(parserAssembler);
    }

	@Override
	public boolean onScopeChange(Scope scope) {
    	boolean isContextChange = 
    		!scope.getName().equals(contextListHandler.getCurrentScope());
        if (isContextChange) {
			ItemList<?> contextList = contextListHandler.onScopeChange(scope);
			if (contextList != null) {
				itemList = contextList;
				contextListOperand.setValue(itemList);
				cursor.backup(contextList, 0);
			}
        }
		return isContextChange;
	}
	
	@Override
	public void setExecutionContext(ExecutionContext context) {
		this.context = context;
	}
	
	/**
     * Execute operation for expression
     * @param id Identity of caller, which must be provided for backup()
     * @return Flag set true if evaluation is to continue
     */
    private void evaluateList(int id)
    {
    	if (listOperand != null) {
       		listOperand.setExecutionContext(context);
   		    listOperand.evaluate(id);
    	}
    	cursor.evaluate(getItemList());
    }

    private boolean backupList(int id) {
    	if (listOperand != null) 
    		listOperand.backup(getId());
		return cursor.backup(getItemList(), id);
    }
    
}
