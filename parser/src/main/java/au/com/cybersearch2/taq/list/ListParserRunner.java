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

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.expression.AxiomOperand;
import au.com.cybersearch2.taq.expression.DynamicListOperand;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * Compiles list on second pass
 *
 */
public class ListParserRunner implements ParserRunner {

    /** List operand to supply list object on evaluation */
	private Operand listOperand;
	/** Qualified name of list */
	private QualifiedName listName;
	/** Dynamic axiom list mapped name */
	private QualifiedName targetName;
	/** Assembled item list  */
	private ItemList<?> itemList;
	/** Flag set true if list is context list. Field 'itemList' will be null if true */
	private boolean isContextList;

	/**
	 * Construct ListParserRunner object
	 * @param listName  Qualified name of list
	 * @param listItemVariable Variable operand
	 */
	public ListParserRunner(QualifiedName listName) {
		this.listName = listName;
		isContextList = false;
	}

	public QualifiedName getListName() {
		return listName;
	}

	public ItemList<?> getItemList() {
		return itemList;
	}

	public Operand getListOperand() {
		return listOperand;
	}

	public boolean isContextList() {
		return isContextList;
	}

	public QualifiedName getTargetName() {
		return targetName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run(ParserAssembler parserAssembler) {
        String listScopeName = listName.getScope();
        if (listScopeName.isEmpty())
        	listScopeName = QueryProgram.GLOBAL_SCOPE;
        Scope listScope = parserAssembler.getScope().findScope(listScopeName);
        ListAssembler listAssembler;
        if (listScope != null)
            listAssembler = listScope.getParserAssembler().getListAssembler();
        else {
        	listScope = parserAssembler.getScope();
            listAssembler = parserAssembler.getListAssembler();
        }
        boolean isDynamicAxiomList = listAssembler.existsKey(ListType.axiom_dynamic, listName);
        if (isDynamicAxiomList) {
           targetName = listAssembler.getAxiomListMapping(listName);
           if (targetName.getScope().equals(Scope.SCOPE))
        	   listName = targetName;
        }
        if (listName.getScope().equals(Scope.SCOPE) || listName.getName().equals(Scope.SCOPE))
        {   // Context list only supported in global scope
            String scopeName = parserAssembler.getScope().getName();
            if (scopeName.equals(QueryProgram.GLOBAL_SCOPE))
            {
            	isContextList = true;
                return;
            }
            if (listName.getScope().equals(Scope.SCOPE))
            	// Assign scope permanently when not in global scope
                listName = new QualifiedName(scopeName, QualifiedName.EMPTY, listName.getName());
        }
        itemList = listAssembler.findItemListByName(listName);
        if ((itemList == null) || isDynamicAxiomList)
        {   
            if (isDynamicAxiomList) {
	            // Search for an operand in the current scope with same name as the list name
	            // If found, the operand will be an AxiomList operand, which creates a list upon evaluation.
            	Operand operand = listScope.getParserAssembler().getOperandMap().getOperand(targetName);
	            listOperand = getAxiomListOperand(operand);
	            if (listOperand == null) {
	            	operand = listScope.getParserAssembler().findOperandByName(targetName.getName());
		            listOperand = getAxiomListOperand(operand);
	            }
	            if (listOperand != null)
	            {   // Use a helper to evaluate the list operand and resolve list parameters
	            	handleDynamicList((ListOperand<Axiom>)listOperand);
	                return;
	            } else if ((itemList != null) && (operand instanceof AxiomOperand)) {
	            	ItemList<Axiom> axiomList = (ItemList<Axiom>)itemList;
	            	ListOperand<Axiom> axiomListOperand = new DynamicListOperand<Axiom>(axiomList, (AxiomOperand)operand);
	            	handleDynamicList(axiomListOperand);
	            	listOperand = axiomListOperand;
	                return;
	            }
            } else {
	            listOperand = parserAssembler.getOperandMap().getOperand(listName);
	            if (listOperand instanceof ListOperand)
	            	itemList = (ItemList<?>)listOperand;
            }
            if (itemList == null) 
            	// If all else fails, look for the list using global scope version of name
                itemList = findGlobalItemList(listName, parserAssembler);
        }
        if (itemList == null)
        {
        	if (listName.isTemplateEmpty())
        	{
        		QualifiedName contextListName = parserAssembler.getContextName(listName.getName());
        		itemList = listAssembler.findAxiomTerms(contextListName);  
                if (itemList != null)
                	listName = contextListName;
        	}
            if (itemList == null)
        	    itemList = listAssembler.findAxiomTerms(listName);     
        }
	}

	@SuppressWarnings("unchecked")
	private ListOperand<Axiom> getAxiomListOperand(Operand operand) {
		ListOperand<Axiom> returnListOperand = null;
    	if (operand instanceof ListOperand<?>) {
    		ListOperand<?> genericListOperand = (ListOperand<?>)operand;
    		if (genericListOperand.getOperandType() == OperandType.AXIOM)
    			returnListOperand = (ListOperand<Axiom>) genericListOperand;
    	}
    	return returnListOperand;
	}
	/**
	 * Complete dynamic list creation
	 * @param listOperand Operand to supply AxiomList object on evaluation
	 */
	protected void handleDynamicList(ListOperand<Axiom> listOperand) {
	}
	
    /**
     * Searches for and returns item list using global scope version of name
     * @param listName Name of list with non-global scope part
     * @param parserAssembler Parser assembler in context scope
     * @return ItemList object or null if list not found
     */
    private ItemList<?> findGlobalItemList(QualifiedName listName, ParserAssembler parserAssembler)
    {
        ListAssembler listAssembler = parserAssembler.getListAssembler();
        QualifiedName qualifiedListName = listName;
        if (!listName.getScope().isEmpty()) {
        	qualifiedListName = new QualifiedName(listName);
            qualifiedListName.clearScope();
        }
        return listAssembler.findItemList(qualifiedListName);
    }

}
