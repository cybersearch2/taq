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
package au.com.cybersearch2.taq.compiler;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.ListFactoryArtifact;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.ParserTask;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.DualIndex;
import au.com.cybersearch2.taq.language.ExpressionIndex;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.NameParser;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.SyntaxException;
import au.com.cybersearch2.taq.list.AppenderSpec;
import au.com.cybersearch2.taq.list.ArrayIndex;
import au.com.cybersearch2.taq.list.ArrayItemList;
import au.com.cybersearch2.taq.list.Cursor;
import au.com.cybersearch2.taq.list.CursorIndex;
import au.com.cybersearch2.taq.list.CursorItemVariable;
import au.com.cybersearch2.taq.list.CursorList;
import au.com.cybersearch2.taq.list.ListItemAppender;
import au.com.cybersearch2.taq.list.ListItemVariable;
import au.com.cybersearch2.taq.list.SelectionIndex;

/**
 * Parser helper to create list artifacts
 *
 */
public class ParserListFactory implements ListFactoryArtifact {

	/** Parser context */
	private final ParserContext parserContext;
	
	/** Cursor parser helper created on demand */
	private ParserCursor parserCursor;

	public ParserListFactory(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	/**
	 * Returns list name for given qualified name
	 * @param qname Qualified name
	 * @return QualifiedName object
	 */
	public QualifiedName getListName(QualifiedName qname) {
		ParserAssembler parserAssembler = getParserAssembler();
		if (parserAssembler.getOperandMap().getOperand(qname) instanceof ListOperand)
			return qname;
		else if (parserAssembler.getListAssembler().existsKey(ListType.cursor, qname))
			return qname;
	    return getListName(qname.toString());
	}

	/**
	 * Returns list name given both qualified name and name in original format
	 * @param qname Qualified name
	 * @return QualifiedName object
	 */
	public QualifiedName getListName(QualifiedName qname, String originalName) {
		ParserAssembler parserAssembler = getParserAssembler();
		if (parserAssembler.getOperandMap().getOperand(qname) instanceof ListOperand)
			return qname;
		else if (parserAssembler.getListAssembler().existsKey(ListType.cursor, qname))
			return qname;
		else if (parserContext.getVariableSpec(originalName) != null)
			return new QualifiedName(Scope.SCOPE, QualifiedName.EMPTY, originalName);
	    return getListName(originalName);
	}

    /**
     * Returns list item appender
     * @param listName Qualified list name
     * @param listType One of 4 types
     * @param indexDataArray One or two list item specifications in an array
     * @param operator "+=" or "="
     * @param expression Operand to set appender
     * @return Variable operand 
     */
    public Operand getListItemAppender(QualifiedName listName, ListType listType, ListItemSpec[] indexDataArray, String operator, Operand expression) {
    	ParserAssembler parserAssembler = getParserAssembler();
    	if (indexDataArray == null)
    		indexDataArray = new ListItemSpec[] {new ArrayIndex(listName, 0, "appender")};
    	AppenderSpec appenderSpec = new AppenderSpec(listName, indexDataArray, listType, operator, expression);
    	ListItemAppender listItemAppender = new ListItemAppender(appenderSpec, indexDataArray, parserAssembler);
	    ParserTask parserTask = parserAssembler.addPending(listItemAppender);
	    parserTask.setPriority(ParserTask.Priority.list.ordinal());
	    return listItemAppender;
    }

    /**
     * Returns list item appender
     * @param listOperand List operand
     * @param indexDataArray One or two ist item specifications in an array
     * @param operator "+=" or "="
     * @param expression Operand to set appender
     * @return Variable operand 
     */
    public Operand getListItemAppender(ListOperand<?> listOperand, ListItemSpec[] indexDataArray, String operator, Operand expression) {
    	ParserAssembler parserAssembler = getParserAssembler();
    	if (indexDataArray == null)
    		indexDataArray = new ListItemSpec[] {new ArrayIndex(listOperand.getQualifiedName(), 0, "appender")};
    	AppenderSpec appenderSpec = new AppenderSpec(listOperand.getQualifiedName(), indexDataArray, listOperand.getlistType(), operator, expression);
    	appenderSpec.setItemList(listOperand);
    	ListItemAppender listItemAppender = new ListItemAppender(appenderSpec, indexDataArray, parserAssembler);
    	listItemAppender.run(parserAssembler);
	    return listItemAppender;
    }

    /**
     * Process List reference production
     * @param listName List name
     * @param listReference List item reference
     * @return Operand object
     */
    public Operand listReference(QualifiedName listName, ListReference listReference) {
    	ParserAssembler parserAssembler = getParserAssembler();
        ListAssembler listAssembler = parserAssembler.getListAssembler();
        boolean isCursor = false;
		boolean is2d = false;
     	if (listReference.dimension() == 1) {
     		DualIndex parserListIndex = listReference.getListItemSpec1();
    		isCursor = listAssembler.existsKey(ListType.cursor, listName);
    		if (parserListIndex.hasName()) {
    			if (isCursor)
    				is2d = true;
    			else {	
	    			if (!listName.getScope().equals(Scope.SCOPE))
	    			    is2d = !listAssembler.existsKey(ListType.term, listName);
	    			else {
	            	   IVariableSpec varSpec = parserContext.getVariableSpec(listName.getName());
	            	   is2d = (varSpec != null) && varSpec.getOperandType() != OperandType.TERM;
	    			}
    			}
            }
            if (is2d && !isCursor) {
     			ExpressionIndex expressionIndex = 
        				new ExpressionIndex(new IntegerOperand(
        					new QualifiedName(listName.getName() + 
        						listName.incrementReferenceCount(), listName), 0L));
				DualIndex arrayIndex = new DualIndex(expressionIndex);
       			listReference = new ListReference(arrayIndex, parserListIndex);
        	}
    	}
        ListItemSpec[] listItemSpec = null;
        
		listItemSpec = indexExpression(listName, listReference);
        if (is2d && isCursor) {
			Cursor cursor = listAssembler.getCursor(listName);
			listItemSpec = new ListItemSpec[] {new CursorIndex(cursor, listName), listItemSpec[0]};
			return axiomContainerOperand(listName.getName(), listName, listItemSpec, null);
        }
		if (listAssembler.existsKey(ListType.context, getParserAssembler().getContextName(listName.getName()))) 
			listItemSpec[0].getQualifiedListName().toContextName();
        return (listItemSpec.length == 1) ? 
        		listItemOperand(listName.getName(), listItemSpec[0], null) : 
        		axiomContainerOperand(listName.getName(), listName, listItemSpec, null);
    }

    /**
     * Returns ListItemVariable for given index data and expression
     * @param name Operand name
     * @param listName Qualified list name
     * @param indexData  Index information for value selection 
     * @param expression Optional assignment expression
     * @return ListItemVariable or CursorItemVariable object
     */
    public Operand axiomContainerOperand(String name,
    		                      QualifiedName listName,
								  ListItemSpec[] indexData,
								  Operand expression) {
	    QualifiedName qname = new QualifiedName(listName.getName() + "_var" + listName.incrementReferenceCount(), listName);
	    ListItemVariable operand;
    	ParserAssembler parserAssembler = parserContext.getParserAssembler();
    	ListAssembler listAssembler = parserAssembler.getListAssembler();
	    if (listAssembler.existsKey(ListType.cursor, listName)) {
	    	CursorList cursorList = listAssembler.getCursorList(listName);
            if (listAssembler.existsKey(ListType.axiom_dynamic, listName)) {
                QualifiedName targetName = listAssembler.getAxiomListMapping(listName);
                if (targetName.getScope().equals("scope")) {
            	    if (indexData[0].getQualifiedListName().equals(listName))
            	    	indexData[0].setQualifiedListName(targetName);
            	    if ((indexData.length > 1) && indexData[1].getQualifiedListName().equals(listName))
            	    	indexData[1].setQualifiedListName(targetName);
                	listName = targetName;
                }
            }
	    	operand = cursorItemVariable(cursorList, listName, indexData);
	    	operand.setLeftOperand(expression);
	    } else
            operand = name == null ? 
            	new ListItemVariable(qname, indexData, expression) : 
            	new ListItemVariable(name, qname, indexData, expression);
	    ParserTask parserTask = parserAssembler.addPending(operand);
	    parserTask.setPriority(ParserTask.Priority.variable.ordinal());
	    return operand;
    }

    /**
     * Returns ListItemVariable for given index data and expression
     * @param indexData  Index information for value selection 
     * @param expression Optional assignment expression
     * @return ListItemVariable or CursorItemVariable object
     */
    public Operand listItemOperand(ListItemSpec indexData,
							Operand expression) throws CompilerException {
        return listItemOperand(null, indexData, expression);
    }

    /**
     * Returns list item variable or cursor item variable
     * @param name List or cursor one-part name
     * @param indexData Index specification
     * @param expression Optional assignment expression
     * @return ListItemVariable or CursrItemVariable object
     * @throws CompilerException
     */
    public Operand listItemOperand(String name,
							       ListItemSpec indexData,
							       Operand expression) throws CompilerException {
    	ParserAssembler parserAssembler = parserContext.getParserAssembler();
    	ListAssembler listAssembler = parserAssembler.getListAssembler();
	    QualifiedName listName = indexData.getQualifiedListName();
	    QualifiedName qname = new QualifiedName(listName.getName() + "_var" + listName.incrementReferenceCount(), listName);
	    ListItemVariable operand = null;
	    Operand listOperand = null;
	    if (listAssembler.existsKey(ListType.cursor, listName)) {
	    	CursorList cursorList = listAssembler.getCursorList(listName);
            if (listAssembler.existsKey(ListType.axiom_dynamic, listName)) {
                QualifiedName targetName = listAssembler.getAxiomListMapping(listName);
                if (targetName.getScope().equals("scope")) {
            	    if (indexData.getQualifiedListName().equals(listName))
            	    	indexData.setQualifiedListName(targetName);
                	listName = targetName;
                }
            }
	    	operand = cursorItemVariable(cursorList, listName, new ListItemSpec[] {indexData});
	    	operand.setExpression(expression);
	    } else {
	    	listOperand = parserAssembler.getOperandMap().get(indexData.getQualifiedListName());
	    	if (!(listOperand instanceof ListOperand))
	    		listOperand = null;
	    }
	    if (operand == null) {
	    	if (listOperand == null) {
		    	if (parserContext.getVariableSpec(listName.getName()) != null)
		    		indexData.setQualifiedListName(new QualifiedName(Scope.SCOPE, QualifiedName.EMPTY, listName.getName()));
		        operand = name == null ? 
		        	new ListItemVariable(qname, indexData, expression) : 
		        	new ListItemVariable(name, qname, indexData, expression);
			    ParserTask parserTask = parserAssembler.addPending(operand);
			    parserTask.setPriority(ParserTask.Priority.variable.ordinal());
	    	} else {
        		ItemList<?> itemList =  (ItemList<?>)listOperand;
		        operand = name == null ? 
			        	new ListItemVariable(qname, itemList, indexData, expression) : 
			        	new ListItemVariable(name, qname, itemList, indexData, expression);
	    	}
	    }
	    parserAssembler.registerLocaleListener(operand);
        return operand;
    }

    /**
     * Process IndexExpression production
     * @param listName Name of list
     * @param listReference Reference to list item
     * @return ListItemSpec array
     */
    public ListItemSpec[] indexExpression(QualifiedName listName, ListReference listReference) {
        if ((listReference.dimension() == 2) && listReference.getListItemSpec1().hasName())
            throw new CompilerException("Axiom list \"" + listName.getName() + "\" axiom cannot be selected by name");
        DualIndex index1 = listReference.getListItemSpec1();
        if (listReference.dimension() == 1) {
        	
        	if (index1.hasName())
        		return new ListItemSpec[] { new SelectionIndex(listName, index1.getName()) };
        	else
        		return new ListItemSpec[] { new SelectionIndex(listName, (Operand)index1.getExpression()) };
        }
        ListItemSpec listItemSpec1 = null;
        listItemSpec1 = new ArrayIndex(listName, null, index1.toString()); 
        if (index1.hasName())  
            listItemSpec1 = new SelectionIndex(listName, index1.getName()); 
        else
            listItemSpec1 = new ArrayIndex(listName, (Operand)index1.getExpression()); 
        DualIndex index2 = listReference.getListItemSpec2();
        ListItemSpec listItemSpec2 = null;
        if (index2.hasName())
            listItemSpec2 = new SelectionIndex(listName, index2.getName()); 
        else
            listItemSpec2 = new SelectionIndex(listName, (Operand)index2.getExpression()); 
        return new ListItemSpec[] { listItemSpec1, listItemSpec2 };
    }
    
    /**
     * Process ContextListDeclaration production
     * @param listName List name
     * @param varSpec Variable specification or null if axiom list
     */
    @Override
    public void contextListDeclaration(String listName, IVariableSpec varSpec) {
    	parserContext.putContextList(listName, (VariableSpec)varSpec);
    }
 
	@Override
	public void listItemAssign(QualifiedName listName, ListReference listReference, IOperand expression) {
    	ListItemSpec[] indexExpression = indexExpression(listName, listReference);
    	ListItemSpec indexData = indexExpression[listReference.dimension() == 1 ? 0 : 1];
    	ParserAssembler parserAssembler = getParserAssembler();
	    ItemList<?> itemList = parserAssembler.getListAssembler().findItemList(indexData.getQualifiedListName());
	    if (itemList == null)
	        throw new SyntaxException("List \"" + indexData.getListName() + "\" must be declared before being initialized");
	    listItemAssign(itemList, indexData, (Operand)expression);
	}
	
    /**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

    private QualifiedName findListName(String currentName) {
		return getParserAssembler().findListName(currentName);
	}
    
	/**
	 * Returns list name for given qualified name
	 * @param currentName Name in parts format
	 * @return QualifiedName object
	 */
	private QualifiedName getListName(String currentName) {
        QualifiedName listName = findListName(currentName);
        if (listName != null)
        	return listName;
		return new NameParser(currentName).getQualifiedName();
	}

    private CursorItemVariable cursorItemVariable(CursorList cursorList, 
            QualifiedName listName, 
            ListItemSpec[] indexData) {
    	if (parserCursor == null)
    		parserCursor = new ParserCursor(parserContext);
		QualifiedName cursorQname = cursorList.getCursor().getCursorQname();
		QualifiedName qname = new QualifiedName(cursorQname.getName() + "_var" + cursorQname.incrementReferenceCount(), cursorQname);
		
		CursorItemVariable itemVariable = new CursorItemVariable(qname, indexData, cursorList, parserCursor.getCursorOperand(cursorList.getCursor()));
       	parserContext.getParserAssembler().registerLocaleListener(itemVariable);
		return itemVariable;
	}
    
    /**
     * Process ListItemAssign production
     * @param itemList List to set
     * @param indexData Index data for target dimension
     * @param assignExpression Assignment expression
     * @throws SyntaxException
     */
    private void listItemAssign(ItemList<?> itemList, ListItemSpec indexData, Operand assignExpression) throws SyntaxException {
		if (assignExpression == null)
		    throw new CompilerException("Statement to initialize List \"" + indexData.getListName() + "\"  must be assigned a value");
		if (!(itemList instanceof ArrayItemList))
			throw new CompilerException("Cannot assign a value to a list of type " + itemList.getClass().getSimpleName());
		indexData.setOffset(-itemList.getOffset());
		indexData.assemble(itemList);
		// Evaluate dynamic scope item expressions on demand
        if (indexData.getItemExpression() != null) {
	    	QualifiedName qname = parserContext.getTemplateName();
	    	if (qname.getTemplate().equals(Scope.SCOPE)) {
	    		int id = getParserAssembler().getTemplateAssembler().getTemplate(qname).getId();
	    		indexData.evaluate(itemList, id);
	    	}
        }
		int index = getListIndex(indexData);
		((ArrayItemList<?>)itemList).assignItem(index, assignExpression);
    }

	private int getListIndex(ListItemSpec indexData) {
	    int index = indexData.getListIndex().getIndex();
	    if (index == -1)
	        throw new SyntaxException("Invalid index '" + index + "' for list \"" + indexData.getListName() + "\" ");
	    return index;
	}
    
}
