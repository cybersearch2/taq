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
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Appender;
import au.com.cybersearch2.taq.interfaces.ExecutionTracker;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemDelegate;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.interfaces.SourceInfo;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DelegateType;
import au.com.cybersearch2.taq.operator.TermOperator;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * ListItemVariable
 * A Variable operand which accesses a list. 
 * Allows new items to be inserted in array lists.
 * Allows AxiomTermList terms to be referenced by name.
 * The list is identified by name and binding to the list occurs
 * either within a parser task, after all lists have been created,
 * or later, at evaluation in the case of dynamically generated lists.  
 * @author Andrew Bowley
 * 28May,2017
 */
public class ListItemVariable extends Variable implements ParserRunner, SourceInfo, LocaleListener, Appender
{
	public static ItemList<Object> EMPTY_LIST = new ArrayItemList<>(OperandType.UNKNOWN, QualifiedName.ANONYMOUS);

    /** Index information for value selection  */
    protected ListItemSpec indexData;
    /** Index information for 2 dimension case - select axiom, then select term in axiom */
    protected ListItemSpec arrayData;
    /** Variable containing list which evaluates item selection */
    protected ListItemDelegate delegate;
    /** Source item to be updated in parser task when more information available to form description of operand */
    protected SourceItem sourceItem;
    /** Manages scope transitions of a context list */
    private ContextListHandler contextListHandler;
    /** Additional evaluation branch for index expression */
    private Operand branch1;
    /** Additional evaluation branch for array expression */
    private Operand branch2;

    /**
     * Create ListItemVariable object which uses a single index to select values
     * @param qname Unique operand name
     * @param indexData  Index information for value selection
     * @param expression Optional assignment expression
     */
    public ListItemVariable(QualifiedName qname, ListItemSpec indexData, Operand expression)
    {
        this(Term.ANONYMOUS, qname, indexData, expression);
    }

    /**
     * Create ListItemVariable object which uses a single index to select values
     * @param qname Unique operand name
     * @param ItemList<?> List being referenced
     * @param indexData  Index information for value selection
     * @param expression Optional assignment expression
     */
    public ListItemVariable(QualifiedName qname, ItemList<?> itemList, ListItemSpec indexData, Operand expression)
    {
        this(Term.ANONYMOUS, qname, indexData, expression);
        setDelegate(itemList);
    }

    /**
     * Create ListItemVariable object which uses a single index to select values
     * @param name Term name
     * @param qname Unique operand name
     * @param ItemList<?> List being referenced
     * @param indexData  Index information for value selection
     * @param expression Optional assignment expression
     */
    public ListItemVariable(String name, QualifiedName qname, ItemList<?> itemList, ListItemSpec indexData, Operand expression)
    {
        super(qname, name, expression);
        this.indexData = indexData;
        setDelegate(itemList);
   }
    
    /**
     * Create ListItemVariable object which uses a single index to select values
     * @param name Term name
     * @param qname Unique operand name
     * @param indexData  Index information for value selection
     * @param expression Optional assignment expression
     */
    public ListItemVariable(String name, QualifiedName qname, ListItemSpec indexData, Operand expression)
    {
        super(qname, name, expression);
        this.indexData = indexData;
    }
    
   /**
     * Create ListItemVariable object for 2 dimensional list access
     * @param qname Unique operand name
     * @param indexDataArray  Index information for value selection 
     * @param expression Optional assignment expression
     */
    public ListItemVariable(QualifiedName qname, ListItemSpec[] indexDataArray, Operand expression)
    {
        // For 2-dimension case, the first dimension selects an item in the list
        // and the second dimension selects a term within the item
        this(qname, indexDataArray[indexDataArray.length - 1], expression);
        if (indexDataArray.length > 1)
            arrayData = indexDataArray[0];
    }

    /**
     * Create ListItemVariable object for 2 dimensional list access
     * @param name Name
     * @param qname Unique operand name
     * @param indexDataArray  Index information for value selection 
     * @param expression Optional assignment expression
     */
    public ListItemVariable(String name, QualifiedName qname, ListItemSpec[] indexDataArray, Operand expression)
    {
        // For 2-dimension case, the first dimension selects an item in the list
        // and the second dimension selects a term within the item
        this(name, qname,  indexDataArray[indexDataArray.length - 1], expression);
        if (indexDataArray.length > 1)
            arrayData = indexDataArray[0];
     }

    /**
     * Create ListItemVariable object for 2 dimensional list access
     * @param qname Unique operand name
     * @param ItemList<?> List being referenced
     * @param indexDataArray  Index information for value selection 
     * @param expression Optional assignment expression
     */
    public ListItemVariable(QualifiedName qname, ItemList<?> itemList, ListItemSpec[] indexDataArray, Operand expression)
    {
        // For 2-dimension case, the first dimension selects an item in the list
        // and the second dimension selects a term within the item
        this(qname, indexDataArray[indexDataArray.length - 1], expression);
        if (indexDataArray.length > 1)
            arrayData = indexDataArray[0];
        setDelegate(itemList);
    }

    /**
     * Create ListItemVariable object for 2 dimensional list access
     * @param name Name
     * @param qname Unique operand name
     * @param ItemList<?> List being referenced
     * @param indexDataArray  Index information for value selection 
     * @param expression Optional assignment expression
     */
    public ListItemVariable(String name, QualifiedName qname, ItemList<?> itemList, ListItemSpec[] indexDataArray, Operand expression)
    {
        // For 2-dimension case, the first dimension selects an item in the list
        // and the second dimension selects a term within the item
        this(name, qname,  indexDataArray[indexDataArray.length - 1], expression);
        if (indexDataArray.length > 1)
            arrayData = indexDataArray[0];
        setDelegate(itemList);
    }

    /**
     * Returns operand type of list
     * @return OperandType - may be UNKNOWN if list not available
     */
    public OperandType getListOperandType()
    {
        ItemList<?> itemList =  delegate != null ? delegate.getItemList(1) : null;
        return itemList != null ? itemList.getOperandType() : OperandType.UNKNOWN;
    }
    
    /**
     * Set the term value to list item selected by index
     * @param listIndex Selection value - must be valid
     */
    public void setValueByIndex(ListIndex listIndex)
    {   
    	Object value = delegate.getValue(listIndex);
    	if (value instanceof ListOperand) {
    		@SuppressWarnings("rawtypes")
			ListOperand listOperand = (ListOperand)value;
    		if (listOperand.isEmpty())
    			listOperand.evaluate(getId());
    	}
        setTermValue(value);
    }

    /**
     * Append given value to list
     * @param value Object to append
     */
    public void append(Object value)
    {
        delegate.append(value);
    }
    
    /**
     * Returns current item value
     * @return Object
     */
    public Object getItemValue()
    {
        Object oldValue = super.getValue();
        Object itemValue = delegate.getValue();
        if (itemValue instanceof Term)
            itemValue = ((Term)itemValue).getValue();
        if (!itemValue.equals(oldValue))
            setTermValue(itemValue);
        return itemValue;
    }

    
	public ListItemDelegate getDelegate() {
		return delegate;
	}

	@Override
    public void run(ParserAssembler parserAssembler)
    {
        ListParserRunner parserRunner = new ListParserRunner(indexData.getQualifiedListName() ) {
        	
        	@Override
        	protected void handleDynamicList(ListOperand<Axiom> listOperand) {
                ListItemSpec[] indexDataArray = 
                        arrayData == null ?
                        new ListItemSpec[] { indexData } :
                        new ListItemSpec[] { arrayData, indexData };
                    AxiomVariable axiomVariable = new AxiomVariable(listOperand, indexDataArray);
                    setDelegate(axiomVariable);
                    // Note itemList will remain null until evaluation occurs
        	}
        };
        parserRunner.run(parserAssembler);
        ItemList<?> itemList = null;
        if (parserRunner.isContextList()) {
        	QualifiedName targetName = parserRunner.getTargetName();
        	if (targetName != null)
        		indexData.setQualifiedListName(targetName);
        	contextListHandler = new ContextListHandler(indexData.getQualifiedListName().getName());
        	itemList = contextListHandler.assembleContextVariable(parserAssembler);
            setDelegate(itemList);
        } else if (delegate == null) {
	        itemList = parserRunner.getItemList();
	        if (itemList != null) {
	        	if (delegate == null)
	                setDelegate(itemList);
	        	if (indexData instanceof ArrayIndex)  {
	        		ArrayIndex arrayIndex = (ArrayIndex)indexData;
	        		arrayIndex.setOffset(-itemList.getOffset());
	        	}
	        } else {
	        	setUnknownDelegate();
	        }
        }
        // Register this object as a locale listener to handle change of scope
	  	parserAssembler.registerLocaleListener(this);
        if (sourceItem != null)
            sourceItem.setInformation(toString());
       	branch1 = indexData.getItemExpression();
        if (arrayData != null)
	        branch2 = arrayData.getItemExpression();
    }

	@Override
	public ItemList<?> getList() {
		if (delegate != null)
			return delegate.getItemList(1);
		return null;
	}
	
    @Override
    public int unifyTerm(Term otherTerm, int id)
    {
        return 0; 
    }

    @Override
    public EvaluationStatus evaluate(int id)
    {
        if (rightOperand != null) {
            // Specialization
        	rightOperand.setExecutionContext(context);
            rightOperand.evaluate(id);
        }
        if (contextListHandler != null) {
        	Operand dynamicOperand = contextListHandler.getDynamicOperand();
        	if (dynamicOperand != null) {
            		dynamicOperand.setExecutionContext(context);
        		dynamicOperand.evaluate(id);
        	}
        }
        ListIndex newIndex;
        if (delegate != null) {
        	if (delegate instanceof ExecutionTracker)
        		((ExecutionTracker)delegate).setExecutionContext(context);
        	newIndex = delegate.evaluate(id);
        }
        else 
	       	throw new ExpressionException(String.format("List %s not found", indexData.getQualifiedListName().toString()));
        if ((leftOperand != null) && (empty || id == this.id))
            super.evaluate(id);
        else if (empty)
        {  
        	if (newIndex.getIndex() != -1) {
                // Update current index and update value too if index in range
                this.id = id;
                setValueByIndex(newIndex);
        	} else  if (delegate.getItemList(1).getName().equals(Scope.SCOPE)) {
        		// This is a scope axiom term list, so set value to blank so it is non-enpty
                this.id = id;
        		setTermValue(new Blank());
        	}
        }
        return EvaluationStatus.COMPLETE;
    }

    @Override
    public boolean backup(int id) 
    {  
        // Backup everything that may be touched by unification or evaluation
        // Note itemList and index fields are not changed, so getValue() will still return the same value as before.
        // However, this variable will be in a "empty" state, so getValue() should not be invoked.
        if (rightOperand != null)
            rightOperand.backup(id);
        if (contextListHandler != null) {
        	Operand dynamicOperand = contextListHandler.getDynamicOperand();
        	if (dynamicOperand != null)
        		dynamicOperand.backup(id);
        }
        if (delegate != null)
            delegate.backup(id);
        return super.backup(id);
    }
    
    @Override
    public void assign(Parameter parameter)
    {
        setValue(parameter);
    }

    @Override
    public void setValue(Object value)
    {
        // Value is wrapped in a Term if coming from assign()
        Term term = (value instanceof Term) ? (Term)value : null;
        if (term != null)
            value = term.getValue();
        // Set term value - first
        setTermValue(value);
        ItemList<?> itemList = delegate.getItemList(1);
        if (itemList.getOperandType() == OperandType.TERM) {
        	if (itemList instanceof ListOperand) {
        		ListOperand<?> listOperand = (ListOperand<?>)itemList;
	        	if (value instanceof AxiomTermList)
	        		listOperand.setValue((AxiomTermList)value);
	        	else if (value instanceof AxiomList) {
	            	AxiomList axiomList = (AxiomList)value;
	            	if (!axiomList.isEmpty())
	            		listOperand.setValue(axiomList.getItem(0));
	            } else
	                delegate.setItemValue(value);
        	} else {
	        	AxiomTermList axiomTermList = (AxiomTermList)itemList;
	        	if (value instanceof Axiom)
	        		axiomTermList.setAxiom((Axiom)value);
	        	else if (value instanceof AxiomTermList)
	        		axiomTermList.setAxiom(((AxiomTermList)value).getAxiom());
	            else if (value instanceof AxiomList) {
	            	AxiomList axiomList = (AxiomList)value;
	            	if (!axiomList.isEmpty())
	                	axiomTermList.setAxiom(axiomList.getItem(0));
	            } else
	                delegate.setItemValue(value);
        	}
        } else
            delegate.setItemValue(value);
    }
    
    @Override
    public void setSourceItem(SourceItem sourceItem)
    {
        this.sourceItem = sourceItem;
    }

    @Override
    public Object getValue()
    {
        if (!(delegate instanceof ItemVariable))
            return value;
        // Refresh from list item in case it has changed from last update
        return getItemValue();
    }

    @Override
    public boolean onScopeChange(Scope scope)
    {
    	boolean isScopeChange = super.onScopeChange(scope);
    	boolean isContextChange = (contextListHandler != null) &&
    			                  !scope.getName().equals(contextListHandler.getCurrentScope());
        if (isContextChange) {
			ItemList<?>  itemList = contextListHandler.onScopeChange(scope);
            if (itemList != null)
            {
                delegate = null;
                setDelegate(itemList);
                // Set term name of this variable now at last opportunity. Append the suffix of the index used to select the value.
                // The suffix is formed using available data, and may be a name required by an index operand to achieve unification
                if (name.isEmpty())
                    setName(indexData.getSuffix());
                if (sourceItem != null)
                    sourceItem.setInformation(toString());
            }
		}
        return isScopeChange;
    }
    
    @Override
    public Operand getRightOperand() 
    {
        return rightOperand;
    }

    @Override
    public Operand getBranch1()
    {
        return branch1;
    }

    @Override
    public Operand getBranch2()
    {
        return branch2;
    }

    @Override
    public String toString()
    {
        if (!empty)
            // Item value available to report   
            return delegate.getValue().toString();
        StringBuilder builder;
        if (name.isEmpty())
        {   // Use index data to build 2 or 3 part name to report empty state
        	builder = new StringBuilder(indexData.getListName());
            if (arrayData != null)
                builder.append('.').append(arrayData.getSuffix());
            builder.append('.').append(indexData.getSuffix());
        } else
        	builder = new StringBuilder(name);
        // Use default name plus assigned value if expression field not null
        if (leftOperand != null)
        	builder.append("=").append(leftOperand.toString());
        return builder.toString();
    }

    /** 
     * Set variable containing list which evaluates item selection
     * @param delegate ListItemDelegate object
     */
    protected void setDelegate(ListItemDelegate delegate) {
		this.delegate = delegate;
	}

   /**
     * Set value with possible operator change for axiom term list assignment
     * @param value
     */
    protected void setTermValue(Object value)
    {
        // Preset operator if value type is AxiomTermList, or wrong operator will be set
        DelegateType delegateType = getDelegateType();
        // Change of operator for axiom term list is blocked if permanent delegate in place (ie. is cursor)
        if (!getDelegateOperator().isProxyAssigned() && (delegateType == DelegateType.ASSIGN_ONLY) && (value instanceof AxiomTermList))
        	getDelegateOperator().setProxy(new TermOperator());
        super.setValue(value);
    }
    
    /**
     * Set delegate to handle evaluate events
     * @param itemList Item list to be accessed by this variable
     */
    public void setDelegate(ItemList<?> itemList)
    {
        // Ensure if a name index is provided, it is valid.
        // This operation is repeated at evaluation in case the list is modified from now 
        // eg. local axiom, where term location may change with scope.
        if ((arrayData != null) && (delegate == null)) {
            // Two dimension case requires AxiomList helper
        	if (itemList instanceof AxiomList)
                setDelegate(new AxiomVariable((AxiomList)itemList, new ListItemSpec[] { arrayData, indexData }));
        	else if (itemList instanceof AxiomTermList)
                setDelegate(new AxiomVariable((AxiomTermList)itemList, new ListItemSpec[] { arrayData, indexData }));
        	else
        		throw new ExpressionException(String.format("ItemList for %s must be Axiom List or Axiom Term List", indexData.getQualifiedListName().getName()));
        }
        if (delegate == null)
        	setDelegate(itemVariableInstance(itemList));
    }
    
    /**
     * Set delegate to handle evaluate events
     */
    public void setUnknownDelegate()
    {
    	OperandType operandType = arrayData != null ? OperandType.AXIOM : OperandType.UNKNOWN;
    	VariableSpec varSpec = new VariableSpec(operandType);
    	VariableFactory variableFactory = new VariableFactory(varSpec);
         // Ensure if a name index is provided, it is valid.
        // This operation is repeated at evaluation in case the list is modified from now 
        // eg. local axiom, where term location may change with scope.
        if (arrayData != null) {
        	@SuppressWarnings("unchecked")
			ListOperand<Axiom> valueList = (ListOperand<Axiom>) variableFactory.getItemListInstance(indexData.getQualifiedListName(), true);
            setDelegate(new AxiomVariable(valueList, new ListItemSpec[] { arrayData, indexData }));
            setRightOperand((Operand)valueList);
       } else {
    	    ItemList<?> valueList = variableFactory.getItemListInstance(indexData.getQualifiedListName(), true);
        	setDelegate(itemVariableInstance(valueList));
            setRightOperand((Operand)valueList);
       }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected ItemVariable<?> itemVariableInstance(ItemList<?> itemList)
    {
        switch (itemList.getOperandType())
        {
        case INTEGER:
        case DOUBLE:
        case BOOLEAN:
        case STRING:
        case DECIMAL:
        case CURRENCY:
        case AXIOM:
        case TERM:
        case UNKNOWN:
        	return new ItemVariable(itemList, indexData);
        default:
       }
        // Not expected
       throw new ExpressionException("List " + qname.toString() + " type " + itemList.getOperandType() + " not supported");
    }

	protected ContextListHandler getContextListHandler() {
		return contextListHandler;
	}

}
