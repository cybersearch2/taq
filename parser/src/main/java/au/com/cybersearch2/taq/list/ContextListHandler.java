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

import java.util.HashMap;
import java.util.Map;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Manages scope transitions of a context list
 */
public class ContextListHandler {

	/** Name part of qualified list name */
	private final String listName;
    /** Maps list name for particular scope to list */
    private Map<QualifiedName, ItemList<?>> contextMap;
    /** List Operand type */
    private OperandType operandType;
    /** Maps item list to scope */
    private Map<String,Operand> operandMap;
    /** Operand to evaluate dynamic axiom list, if required, otherwise null */
    private Operand dynamicOperand;
    /** Name of scope in most recent context change */
    private String currentScope;

    /**
     * Construct ContextListHandler object
     * @param listName Name part of qualified list name
     * @param localeListener Listens for change of scape notifications 
     */
	public ContextListHandler(String listName) {
		this.listName = listName;
		operandType = OperandType.UNKNOWN;
		currentScope = QueryProgram.GLOBAL_SCOPE;
	}

	/**
	 * Returns OperandType of items in this container
	 * @return OperandType enum
	 */
	public OperandType getOperandType() {
		return operandType;
	}

	/**
	 * Returns operand to evaluate dynamic axiom list
	 * @return Operand object or null if none
	 */
    public Operand getDynamicOperand() {
		return dynamicOperand;
	}

	public String getCurrentScope() {
		return currentScope;
	}

	/**
     * Assemble context lists
     * @param parserAssembler ParserAssembler for dynamic scope
     */
    public ItemList<?> assembleContextVariable(ParserAssembler parserAssembler)
    {
        // Find all context lists and map by name in contextMap
        boolean[] globalListExists = new boolean[] {false};
        Scope globalScope = parserAssembler.getScope().getGlobalScope();
        globalScope.forEach(scope -> {
            QualifiedName key = new QualifiedName(scope.getName(), listName);
            if (findContextList(scope, key) && key.isScopeEmpty())
                globalListExists[0] = true;
        });
        if (contextMap == null)
            throw new ExpressionException("Context list \"" + listName + "\"  is not found in any scope");
        // Default to global scope if global list defined
        if (globalListExists[0])
            return onScopeChange(globalScope);
    	VariableSpec varSpec = new VariableSpec(operandType);
    	VariableFactory variableFactory = new VariableFactory(varSpec);
    	QualifiedName qualifiedListName = new QualifiedName("scope", listName);
    	if ((operandType == OperandType.AXIOM) || (operandType == OperandType.TERM))
    		varSpec.setAxiomKey(qualifiedListName);
    	return variableFactory.getItemListInstance(qualifiedListName, true);
    }

    /**
     * Returns context list for given scope
     * @param scope Scope
     * @return ItemList object
     */
    public ItemList<?> onScopeChange(Scope scope)
    {
        QualifiedName listQName = new QualifiedName(scope.getName(), listName);
        ItemList<?> itemList = contextMap.get(listQName);
        if (itemList != null) {
        	if (operandMap != null)
        	    dynamicOperand = operandMap.get(scope.getName());
        	if (itemList.isDynamic() && itemList.isEmpty())
        		itemList.evaluate(scope.getExecutionContext());
        	currentScope = scope.getName();
        }
        return itemList;
    }

    private boolean findContextList(Scope scope, QualifiedName key) {
        ListAssembler listAssembler = scope.getParserAssembler().getListAssembler();
        ItemList<?> itemList = listAssembler.findItemListByName(key);
        if (itemList == null) {
        	itemList = listAssembler.findAxiomTerms(key);
            if (itemList == null)  {
            	itemList = scope.getGlobalScope().getParserAssembler().getListAssembler().findAxiomTerms(key);
                if (itemList!= null)  {
            	    // Axiom was created before scope declared. Create list in correct scope.
            	    AxiomTermList termList = listAssembler.getAxiomTerms(key);
            	    listAssembler.axiomInstance(key, false);
            	    listAssembler.add(key, ((AxiomTermList)itemList).getAxiom());
           	        itemList = termList;
                }
            }
        }
        if (itemList == null)
        	itemList = listAssembler.findAxiomItemList(key);
        if (itemList != null) {
        	if ((itemList.getOperandType() == OperandType.AXIOM) &&
        		listAssembler.existsKey(ListType.axiom_dynamic, key)) {
        		Operand listOperand = scope.getParserAssembler().findOperandByName(listName);
        		if (listOperand != null) {
        		    if (operandMap == null)
        			    operandMap = new HashMap<>();
        		    operandMap.put(scope.getName(), listOperand);
        		}
        	}
        }
    	if (itemList == null)  
    		itemList = listAssembler.getAxiomSource(key);
    	boolean listFound = itemList != null;
    	if (listFound) {
            // List found in this scope
	        // Create map to access context list
	        if (contextMap == null) {
	            contextMap = new HashMap<QualifiedName, ItemList<?>>();
	            operandType = itemList.getOperandType();
	        }
	        contextMap.put(key, itemList);
    	}
    	return listFound;
    }
}
