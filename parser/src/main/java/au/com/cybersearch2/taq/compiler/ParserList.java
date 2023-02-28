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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.cybersearch2.taq.artifact.ListArtifact;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.compile.TemplateType;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.ArrayItemList;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.list.DynamicList;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;

/**
 * Helper to collect list details and create list artifacts 
 *
 */
public class ParserList implements ListArtifact {

	/** Applies compiler logic to the token stream */
	private final Compiler compiler;
	/** List name (1-part only) */
	private final String listName;
	/** Qualified list name */
	private QualifiedName qualifiedListName;
	/** Flag set true if this is an axiom list */
	private final boolean isAxiom;
	/** Flag set true if list is to be exported to the solution */
	private final boolean isExport;
	/** Offset of first index in a bounded list */
	private int begin;
	/** Last index in a bounded list */
	private int end;
	/** Flag set true if list name is an alias for an actual list */
	private boolean hasTarget;
	/** List type, may initially be set to null in the constructor */
	private VariableSpec varSpec;
	/** Qualified name of axiom, axiom list or axiom source */
	private QualifiedName qualifiedAxiomName;
	/** Qualified context name to be restored upon completion */
	private QualifiedName contextName;
	/** Template name to bee restored upon completion */ 
	private QualifiedName templateContextName;
	/** Actual qualified name of list being aliased */
	private QualifiedName qualifiedTargetName;
	/** Template to initialize a basic list */
	private Template template;
	/** Template to initialize a dynamic axiom list */
	private Template initializeTemplate;
	/** Templates for creating a dynamic axiom list */
	private List<ITemplate> axiomList;

	/**
	 * Construct ParserList object
	 * @param compiler Applies compiler logic to the parser token stream
	 * @param listName 1-part name for list
	 * @param varSpec Variable type specification
	 * @param isExport Flag set true if list is to be exported to the solution
	 */
	public ParserList(Compiler compiler,
			           String listName, 
                       IVariableSpec varSpec,
                       boolean isExport) {
		this.compiler = compiler;
		this.listName = listName;
		this.varSpec = (VariableSpec)varSpec;
		this.isExport = isExport;
		ListType listType = ListType.basic;
		if ((varSpec != null) && (varSpec.getOperandType() == OperandType.AXIOM))
			listType = ListType.axiom_dynamic; 
		isAxiom = listType == ListType.axiom_dynamic;
		ParserAssembler parserAssembler = compiler.getParserAssembler();
		IVariableSpec contextListSpec = compiler.getContextListSpec(listName);
		if (contextListSpec != null) {
			OperandType operandType = contextListSpec.getOperandType();
			if (((varSpec != null) && (varSpec.getOperandType() != operandType)) ||
				(isAxiom && (operandType != OperandType.AXIOM)))
				throw new CompilerException(String.format("Context list %s declared with wrong type", listName));
			qualifiedListName = new QualifiedName(parserAssembler.getScope().getName(), QualifiedName.EMPTY, listName);
			if (isAxiom)
				qualifiedAxiomName = qualifiedListName;
		} else {
			qualifiedListName = parserAssembler.getContextName(listName);
		}
		begin = -1;
		axiomList = Collections.emptyList();
        if (isAxiom)
        {
        	ParserContext parserContext = compiler.getParserContext();
	        contextName = parserContext.getContextName();
	        templateContextName = parserContext.getTemplateName();
	        if (listType == ListType.axiom_dynamic)
	            template = createDynamicAxiomTemplate(listName);
	        if (qualifiedAxiomName == null)
	            qualifiedAxiomName = parserAssembler.getContextName(listName);
        }
	}

	/**
	 * Set template to initialize a dynamic axiom list
	 * @param initializeTemplate Template
	 */
	public void setInitializeTemplate(Template initializeTemplate) {
		this.initializeTemplate = initializeTemplate;
	}

	/**
	 * Returns flag set true if an exported axiom list needs a creation template 
	 * @return boolean
	 */
	public boolean missingAxiomList() {
		return isAxiom &&  !hasTarget && axiomList.isEmpty();
	}

	/**
	 * Returns flag set true if this is a basic list and does not have an alias or is context scoped
	 * @return boolean
	 */
	public boolean missingItemList() {
		if (!isAxiom && !hasTarget) {
	        if ((varSpec == null) && (qualifiedAxiomName == null))
	            throw new CompilerException("Invalid declaration for list \"" + listName + "\". Missing type or axiom name.");
	        return true;
		}
		return isAxiom;
	}

	/**'
	 * Set variable type
	 * @param varSpec Variable type specification
	 * @return Operand object if list type, otherwise null
	 */
	public void setVarSpec(IVariableSpec varSpec) {
		this.varSpec = (VariableSpec)varSpec;
	}

	/**
	 * Restore context name after possible change as side effect of a previous operation
	 */
	public void restoreContextName() {
    	ParserContext parserContext = compiler.getParserContext();
        if (contextName != null)
        	parserContext.setContextName(contextName);
        if (templateContextName != null)
        	parserContext.setTemplateName(templateContextName);
	}

	/**
	 * Returns list type
	 * @return VariableType object
	 */
	public IVariableSpec getVarSpec() {
        if (varSpec == null) // List declared using 'list<axiom>'
        	varSpec = new VariableSpec(OperandType.AXIOM);
		return varSpec;
	}

	/**
	 * Create basic list
	 * @return ItemList object
	 */
	public ItemList<?> createItemList() {
        if (varSpec == null)
        	getVarSpec();
        if (qualifiedAxiomName != null)
        	varSpec.setAxiomKey(qualifiedAxiomName);
        else if (varSpec.getOperandType() == OperandType.TERM)
        	varSpec.setAxiomKey(qualifiedListName);
        ItemList<?> itemList;
        VariableFactory variableFactory = new VariableFactory(varSpec);
        if ((template != null) && !isAxiom && (varSpec.getOperandType() != OperandType.TERM))
        	itemList = variableFactory.getDynamicListInstance(qualifiedListName, template);
        else
            itemList = variableFactory.getItemListInstance(qualifiedListName, false);
        if (isExport) {
            itemList.setPublic(true);
            if (itemList.getOperandType() == OperandType.TERM) {
        		ParserAssembler parserAssembler = compiler.getParserAssembler();
        		ListAssembler listAssembler = parserAssembler.getListAssembler();
        		listAssembler.axiomInstance(qualifiedListName, true);
            }
        }
        if (begin != -1)
            sizeList(itemList,  begin, end);
        return itemList;
	}

	public void registerSetList() {
		if (varSpec.getOperandType() == OperandType.SET_LIST) {
			OperandMap operandMap =  compiler.getParserAssembler().getOperandMap();
			Operand operand = null;
			if (qualifiedTargetName != null) {
				// If target operand already exists, just map it with an alternate key
				operand = operandMap.getOperand(qualifiedTargetName);
				if (operand != null) {
					operandMap.addOperand(qualifiedAxiomName, operand);
					return;
				}
			}
			// The list variable is an AxiomOperand instance and
			// the list is created on evaluation by an AxiomListEvaluator instance
			varSpec.setTemplateList(axiomList);
		    if (initializeTemplate != null)
		    	varSpec.setTemplate(initializeTemplate);
		    if (isExport)
		    	varSpec.setExport(true);
	        VariableFactory variableFactory = new VariableFactory(varSpec);
            operand = variableFactory.getExpressionInstance(getQualifiedAxiomName(), compiler.getParserAssembler());
            operandMap.addOperand(operand);
            //if ((template != null) && !isAxiom)
            if (template != null)
            	// This is a dynamic list
                template.addTerm(operand);
		}
	}
	
    public void cleanUp() {
    	if (isAxiom && (template != null) && (template.getTermCount() == 0) && (template.getNext() == null)) {
    		compiler.getParserAssembler().getTemplateAssembler().removeTemplate(template);
    	}
    		
    }

	/**
	 * Returns Axiom qualified name
	 * @return QualifiedName object
	 */
	@Override
	public QualifiedName getQualifiedAxiomName() {
		return qualifiedAxiomName;
	}

	/**
	 * Set axiom qualified name
	 * @param qualifiedAxiomName Axiom qualified name
	 */
	@Override
	public void setQualifiedAxiomName(QualifiedName qualifiedAxiomName) {
		this.qualifiedAxiomName = qualifiedAxiomName;
	}

	/**
	 * Set range for sub list
	 * @param begin Start index
	 * @param end End index
	 */
	@Override
	public void setRange(int begin, int end) {
	    this.begin = begin;
	    this.end = end;
	}

	/**
	 * Set template to initialize the list
	 * @param template List parameters template
	 */
	@Override
	public void setTemplate(ITemplate template) {
		this.template = (Template) template;
	}

	/**
	 * Map a local list name to an actual list in another context
	 * @param target Qualified name of actual list
	 */
	@Override
	public void setTarget(QualifiedName target) {
		hasTarget = true;
		ParserContext parserContext = compiler.getParserContext();
		ParserAssembler parserAssembler = compiler.getParserAssembler();
		ListAssembler listAssembler = parserAssembler.getListAssembler();
		if (listAssembler.existsKey(ListType.axiom_dynamic, target)) {
		       listAssembler.mapAxiomList(qualifiedAxiomName, target);
			return;
		}
        if (qualifiedAxiomName == null)
        	// Use list name as local name
            qualifiedAxiomName = parserAssembler.getContextName(listName);
        if (target.isGlobalName())
        	// A global target needs no modification
            qualifiedTargetName = target;
        else if (!target.getTemplate().isEmpty() && 
        		   target.getTemplate().equals(parserContext.getContextName().getTemplate())) 
        	// Convert a template name in current context to axiom format
            qualifiedTargetName = target.toScopeName();
        if (qualifiedTargetName == null)
        	// Use context to fill in parts missing from the given target
            qualifiedTargetName = parserAssembler.getContextName(target.toString());
        listAssembler.mapAxiomList(qualifiedAxiomName, qualifiedTargetName);
	}

	/**
	 * Returns template for ListParameters production
	 * @return Template object
	 */
	@Override
	public ITemplate chainTemplate() {
		// Creating a parameter list template requires explicit list type
		if (varSpec == null)
		      throw new CompilerException("Invalid declaration for list \"" + listName + "\". Type omitted from declaration.");
		return compiler.chainTemplate();
	}

	/**
	 * Set templates for creating a dynamic axiom list
	 * @param axiomList Template list
	 * @param termNames Optional term names
	 */
	@Override
	public void setAxiomList(List<ITemplate> axiomList, List<String> termNames) {
		this.axiomList = axiomList;
		if (termNames != null)
		    axiomList.forEach(template -> {
		    	int index[] = new int[] {0};
		    	Template toUpdate = (Template)template;
		    	toUpdate.forEach(term -> {
		    		if (term.getName().isEmpty() && (index[0] < termNames.size()))
		    			term.setName(termNames.get(index[0]));
		    		++index[0];
		    	});
		    });
		ParserAssembler parserAssembler = compiler.getParserAssembler();
        ListAssembler listAssembler = parserAssembler.getListAssembler();
        // Map the axiom list to itself as an indication it is a dynamic axiom list
        listAssembler.mapAxiomList(qualifiedAxiomName, qualifiedAxiomName);
	}

	/**
	 * Returns list parameters term
	 * @param expression Term content 
	 * @return Operand object
	 */
	@Override
    public Operand listParameters(IOperand expression) {
    	Operand operand = (Operand)expression;
    	Operand var;
        if (varSpec.getOperandType() == operand.getOperator().getTrait().getOperandType())
            var = operand;
        else {
	        VariableFactory variableFactory = new VariableFactory(varSpec);
            var = variableFactory.getInstance(operand.getQualifiedName(), operand, compiler.getParserAssembler());
        }
        return var;
    }

    /**
     * Process ListDeclaration production
     * @param parserList Helper to collect declaration content
     * @return ItemList object
     */
    @Override
    public void listDeclaration() {
        ParserAssembler parserAssembler = compiler.getParserAssembler();
        if (missingAxiomList()) 
        {
        	List<ITemplate> axiomList = new ArrayList<>();
            TemplateArchetype archetype = 
            	new TemplateArchetype(
            		new QualifiedTemplateName(parserAssembler.getScope().getAlias(), 
            				                  getQualifiedAxiomName().getName()));
            axiomList.add(new Template(archetype));
            setAxiomList(axiomList, null);
        }
        if (!axiomList.isEmpty())
        {
        	IVariableSpec varSpec = new VariableSpec(OperandType.SET_LIST);
            setVarSpec(varSpec);
        }
        restoreContextName();
        ItemList<?> itemList =  null;
        if (missingItemList()) {
             itemList =  createItemList();  
             registerItemList(itemList);
             OperandType operandType = getVarSpec().getOperandType();
             if (operandType == OperandType.AXIOM)
             	parserAssembler.registerAxiomList((AxiomList) itemList);
             else if (operandType == OperandType.TERM)
             	parserAssembler.registerAxiomTermList((AxiomTermList) itemList);
             else if (operandType == OperandType.SET_LIST) 
            	 registerSetList();

        }
        cleanUp();
    }

    /**
     * Returns number of names in axiom header
     * @param axiomHeader String list or null if none
     * @return int
     */
	@Override
	public int getTermCount(List<String> axiomHeader) {
		int count = 0;
		if (axiomHeader != null)
			count = axiomHeader.size();
		return count;
	}

    /**
     * Add ItemList object to it's assembly container
     * @param itemList ItemList object to add
     */
	private void registerItemList(ItemList<?> itemList) {
		ListAssembler listAssembler = compiler.getParserAssembler().getListAssembler();
		listAssembler.addItemList(itemList.getQualifiedName(), itemList);
    }

	/**
     * Set list range for given item list. Referenced by ParserList class.
     * @param itemList Item list to set
     * @param begin
     * @param end
     * @throws CompilerException
     */
    private void sizeList(ItemList<?> itemList, int begin, int end) throws CompilerException {
	    if (end <= begin)
	        throw new CompilerException("List \"" + itemList.getName() + "\" begin parameter must less then end");
	    if (itemList instanceof ArrayItemList) {
	        ArrayItemList<?> arrayItemList = (ArrayItemList<?>)itemList;
	        arrayItemList.setOffset(begin);
	        arrayItemList.setSize(end - begin + 1);
	    } else if (itemList instanceof DynamicList) {
	    	DynamicList<?> dynamicList = (DynamicList<?>)itemList;
	    	dynamicList.setOffset(begin);
	    } else
            throw new CompilerException("List \"" + itemList.getName() + "\" is not sizeable");
    }

    /**
     * Returns template to be used to implement a dynamic axiom list. Referenced by ParserList class.
     * @param listName Name of list
     * @return Template object
     */
    private Template createDynamicAxiomTemplate(String listName) {
    	ParserContext parserContext = compiler.getParserContext();
	    QualifiedName qualifiedTemplateName = new QualifiedTemplateName(parserContext.getScope().getAlias(), listName);
	    parserContext.setTemplateName(qualifiedTemplateName);
	    TemplateAssembler templateAssembler = parserContext.getParserAssembler().getTemplateAssembler();
	    Template  newTemplate =  templateAssembler.createTemplate(qualifiedTemplateName, TemplateType.calculator); 
	    return newTemplate;
    }

}
