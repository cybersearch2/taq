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

import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.CursorArtifact;
import au.com.cybersearch2.taq.axiom.ResourceAxiomSource;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.CursorSentinelOperand;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.ResourceSentinel;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.Cursor;
import au.com.cybersearch2.taq.list.ResourceCursor;
import au.com.cybersearch2.taq.list.ResourceList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * Parser helper for creating cursor artifacts
 */
public class ParserCursor implements CursorArtifact {

	/** Parser context */
	private final ParserContext parserContext;

	/**
	 * Construct ParserCursor object
	 * @param parserContext Parser context
	 */
	public ParserCursor(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	/**
	 * Returns, if term or axiom list cursor type specified, variable to receive list item
	 * @param cursor Cursor
	 * @return Variable or null if cursor term or axiom list type not specified
	 */
    public Operand getCursorOperand(Cursor cursor) {
    	Operand operand = null;
        VariableSpec varSpec = cursor.getVariableSpec();
        // Create normal variable to receive list item and possibly perform type conversion
        if ((varSpec != null) && 
        	!((varSpec.getOperandType() == OperandType.AXIOM) || 
        	  (varSpec.getOperandType() == OperandType.TERM))) {
        	QualifiedName cursorQname = cursor.getCursorQname();
        	VariableFactory variableFactory = new VariableFactory(varSpec);
        	operand = 
        		variableFactory.getExpressionInstance(
        			new QualifiedName(cursorQname.getName() + cursorQname.incrementReferenceCount(), cursorQname), getParserAssembler());
        }   
    	return operand;
    }
    
    /**
     * Process Cursor production
     * @param varSpec Variable Specification
     * @param cursorName Name of cursor
     * @param listName Name of list referenced by cursor
     * @param isReverse Flag set true if direction is reverse
     * @return CursorSentinelOperand object
     */
    public CursorSentinelOperand createCursorSentinal(VariableSpec varSpec, String cursorName, String listName, boolean isReverse) {
    	CursorSentinelOperand cursorSentinelOperand;
    	QualifiedName listQname;
    	ParserAssembler parserAssembler = getParserAssembler();
    	if (listName.equals(cursorName)) {
    		// Cursor to bound to List with same name implemented as a ListOperand object
        	VariableFactory variableFactory = new VariableFactory(varSpec);
        	listQname = QualifiedName.parseName(listName, parserAssembler.getQualifiedContextname());
     		ItemList<?> itemList = variableFactory.getItemListInstance(listQname, true);
     		cursorSentinelOperand = createCursorSentinal(varSpec, cursorName, itemList, isReverse);
    	} else {
	    	if (parserContext.getVariableSpec(listName) != null)
	        	listQname = new QualifiedName(Scope.SCOPE, QualifiedName.EMPTY, listName);
	        else
	        	listQname = name(listName, true, false);
	    	cursorSentinelOperand = createCursorSentinal(varSpec, cursorName, listQname, isReverse);
   	    }
        return cursorSentinelOperand;
    }
 
    /**
     * Returns cursor sentinel operand
     * @param varSpec Cursor type specification or null
     * @param cursorQname Qualified name of cursor
     * @param listQname Qualified name of list linked to cursor
     * @param isReverse Flag set true if reverse cursor required
     * @return CursorSentinelOperand object
     */
    public CursorSentinelOperand createCursorSentinalOperand(VariableSpec  varSpec, QualifiedName cursorQname, QualifiedName listQname, boolean isReverse) {
        Cursor cursor = new Cursor(cursorQname, varSpec, isReverse);
        ParserAssembler parserAssembler = getParserAssembler();
        Operand operand = parserAssembler.getOperandMap().getOperand(listQname);
        CursorSentinelOperand cursorSentinelOperand = null;
        if (operand instanceof ListOperand)
        	cursorSentinelOperand = new CursorSentinelOperand(cursor, (ListOperand<?>)operand);
        else {
        	if (listQname.isGlobalName()) {
        		ItemList<?> itemList = parserAssembler.getListAssembler().findItemListByName(listQname);
        		if (itemList != null)
            		cursorSentinelOperand = new CursorSentinelOperand(cursor, itemList);
        		else {
	        		// Load list if it is linked to a data source
	        		ParserAssembler globalAssembler = parserContext.getScope().getGlobalParserAssembler();
	        		String resourceName = globalAssembler.getResourceName(listQname);
	        		if (resourceName != null) {
	        			ResourceProvider resourceProvider = globalAssembler.getResourceProvider(resourceName);
	                    if (resourceProvider != null) {
	                    	final AxiomArchetype archetype = globalAssembler.getAxiomAssembler().getAxiomArchetype(listQname);
	                    	if (archetype != null)
	                    	{
	                			List<Axiom> axiomList = globalAssembler.getListAssembler().getAxiomItems(listQname);
	                			Iterator<Axiom> iterator = new ResourceAxiomSource(resourceProvider, archetype).iterator(null);
	                			iterator.forEachRemaining(axiom -> axiomList.add(axiom));
	                			AxiomList axiomItemList = new AxiomList(listQname, axiomList, archetype.getName());
	                			cursorSentinelOperand = new CursorSentinelOperand(cursor, axiomItemList);
	                    	}
	                    }
	        		}
        		}
        	}
        	if (cursorSentinelOperand == null)
        		cursorSentinelOperand = new CursorSentinelOperand(cursor, listQname);
        }
        parserAssembler.getListAssembler().registerCursor(cursorQname, cursorSentinelOperand);
        return cursorSentinelOperand;
    }

	/**
	 * Returns cursor sentinel operand which manages cursor initialization and backup
     * @param varSpec Variable Specification
     * @param cursorName Name of cursor
     * @param listName Name of list referenced by cursor
     * @param isReverse Flag set true if direction is reverse
	 * @return CursorSentinalOperand object
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Operand cursorDeclaration(IVariableSpec varSpec, String cursorName, String listName, boolean isReverse) {
		ParserAssembler parserAssembler = getParserAssembler();
		CursorSentinelOperand operand = createCursorSentinal((VariableSpec)varSpec, cursorName, listName, isReverse);
		Operand left = operand.assembleList(parserAssembler);
		if ((left == null) && (operand.getItemList() instanceof ListOperand))
			left = (ListOperand)operand.getItemList();
		operand.setLeftOperand(left);
		parserAssembler.getOperandMap().addOperand(operand.getCursorQname(), operand);
		return operand;
	}

    /**
     * Returns resource cursor sentinel operand
     * @param cursorQname Qualified name of cursor
     * @param resourceName Unique name
     * @param isReverse Flag set true if reverse cursor required
     * @return ResourceSentinel object
     */
	@Override
    public ResourceSentinel createResourceSentinel(String cursorName, String resourceName, boolean isReverse) {
        ResourceCursor resourceCursor = createResourceCursor(cursorName, resourceName, isReverse);
        return new ResourceSentinel(resourceCursor);
    }
    
    /**
     * Process Cursor production for given item list
     * @param varSpec Variable specification or null
     * @param cursorName Name of cursor
     * @param itemList Item list referenced by cursor
     * @param isReverse Flag set true if direction is reverse
     * @return CursorSentinalOperand object
     */
    private CursorSentinelOperand createCursorSentinal(VariableSpec varSpec, String cursorName, ItemList<?> itemList, boolean isReverse) {
        ParserAssembler parserAssembler = getParserAssembler();
     	QualifiedName cursorQname = parserAssembler.getContextName(cursorName);
        String[] parts = cursorQname.getScope().split("@");
        if (parts.length > 1)
        	cursorQname = new QualifiedName(parts[0],parts[1], cursorQname.getName());
        Cursor cursor = new Cursor(cursorQname, varSpec, isReverse);
        CursorSentinelOperand cursorSentinelOperand =  new CursorSentinelOperand(cursor, itemList);
        parserAssembler.getListAssembler().registerCursor(cursorQname, cursorSentinelOperand);
        return cursorSentinelOperand;
    }
    /**
     * Process Cursor production for list identified by qualified name
     * @param varSpec Variable specification or null
     * @param cursorName Name of cursor
     * @param listQname Qualified name of list referenced by cursor
     * @param isReverse Flag set true if direction is reverse
     * @return CursorSentinalOperand object
     */
    private CursorSentinelOperand createCursorSentinal(VariableSpec varSpec, String cursorName, QualifiedName listQname, boolean isReverse) {
    	QualifiedName cursorQname = getParserAssembler().getContextName(cursorName);
        String[] parts = cursorQname.getScope().split("@");
        if (parts.length > 1)
        	cursorQname = new QualifiedName(parts[0],parts[1], cursorQname.getName());
        return createCursorSentinalOperand(varSpec, cursorQname, listQname, isReverse);
    }

    /**
     * Returns resource cursor
     * @param cursorName Cursor name
     * @param resourceName Resource provider name
     * @param isReverse Flag set true if reverse cursor required
     * @return ResourceCursor object
     */
    private ResourceCursor createResourceCursor(String cursorName, String resourceName, boolean isReverse) {
       	ParserAssembler parserAssembler = getParserAssembler();
    	ResourceProvider resourceProvider = parserAssembler.getResourceProvider(resourceName);
     	QualifiedName cursorQname = parserAssembler.getContextName(cursorName);
        String[] parts = cursorQname.getScope().split("@");
        if (parts.length > 1)
        	cursorQname = new QualifiedName(parts[0],parts[1], cursorQname.getName());
        // All resource mappings are available in global scope
        ParserAssembler globalAssembler = parserAssembler.getScope().getGlobalParserAssembler();
        ResourceList resourceList = globalAssembler.getResourceList(resourceProvider);
    	ResourceCursor resourceCursor = new ResourceCursor(cursorQname, resourceList, isReverse);
        parserAssembler.getListAssembler().registerCursor(cursorQname, resourceCursor);
        return resourceCursor;
    }
    
   
	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

	/**
	 * Process Name production
	 * @param name Identifier
	 * @param isContextName Flag set true to incorporate context details
	 * @param isDelaration Is part of a declaration
	 * @return QualifiedName object
	 */
	private QualifiedName name(String name, boolean isContextName, boolean isDeclaration) {
	    return getParserAssembler().name(name, isContextName, isDeclaration);
	}
}
