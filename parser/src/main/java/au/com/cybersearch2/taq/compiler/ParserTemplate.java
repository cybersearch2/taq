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

import java.util.List;

import au.com.cybersearch2.taq.artifact.TemplateArtifact;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.TemplateType;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * Parser helper to collect Template declaration details 
 *
 */
public class ParserTemplate implements TemplateArtifact {

	/** Creates artifacts from parser productions */
	private final Compiler compiler;
	/** Template under construction */
	private final Template template;
	/** Flag set true if artifact allows branching during executiion */
	private final boolean isCalculator;
	/** Flag set true if the artifact is declared as returning a term list */
	private final boolean returnsTerm;
	/** Parser context name */
    private final QualifiedName contextName;
    /** Template qualified name */
    private final QualifiedName qualifiedTemplateName;
   
    /**
     * Construct ParserTemplate object
     * @param compiler Applies compiler logic to the javacc token stream
     * @param name Template name
     * @param isCalculator flag set true if this is a calculator
     * @param returnsTerm Produces a term solution
     * @param returnsAxiom Produces an axiom solution
     */
	public ParserTemplate(Compiler compiler, String name, boolean isCalculator, boolean returnsTerm) {
		this.compiler = compiler;
		this.isCalculator = isCalculator;
		this.returnsTerm = returnsTerm;
		ParserContext parserContext = compiler.getParserContext();
		contextName = parserContext.getContextName();
	    qualifiedTemplateName = new QualifiedTemplateName(parserContext.getScope().getAlias(), name);
	    parserContext.setContextName(qualifiedTemplateName);
	    parserContext.setTemplateName(qualifiedTemplateName);
	    template = parserContext.getParserAssembler().getTemplateAssembler()
	    	.createTemplate(qualifiedTemplateName, isCalculator ?
	    			                               TemplateType.calculator : 
	    			                               TemplateType.template); 
	}

	/**
     * Construct ParserTemplate object from an existing template
     * @param compiler Applies compiler logic to the javacc token stream
	 * @param template Template
	 */
	public ParserTemplate(Compiler compiler, ITemplate template) {
		this.compiler = compiler;
		this.template = (Template) template;
		isCalculator = template.isCalculator();
		returnsTerm = false;
		ParserContext parserContext = compiler.getParserContext();
		contextName = parserContext.getContextName();
	    qualifiedTemplateName = template.getQualifiedName();
	    if (qualifiedTemplateName.isNameEmpty())
	    	parserContext.setContextName(qualifiedTemplateName);
	   	else
	        parserContext.setContextName(new QualifiedName(qualifiedTemplateName.getTemplateScope(), qualifiedTemplateName.getName(), ""));
	}
	
	public Template getTemplate() {
		return template;
	}

	public boolean isCalculator() {
		return isCalculator;
	}

	public QualifiedName getQualifiedName() {
		return qualifiedTemplateName;
	}

	public void addTerm(Operand operand) {
	    template.addTerm(operand);
	}

	public ITemplate createInnerTemplate() {
        return compiler.getParserAssembler().getTemplateAssembler().chainTemplate(template.getQualifiedName());
	}

	@Override
	public boolean isReturnsTerm() {
		return returnsTerm;
	}

	@Override
	public void addTerm(IOperand operand) {
		addTerm((Operand)operand);
	}
	
	@Override
	public void setOuterTemplateName() {
		 compiler.getParserContext().setTemplateName(qualifiedTemplateName);
	}
	
	/**
	 * Reset context name after possible change as side effect of a previous operation
	 */
	@Override
	public void adjustContextName() {
        if (contextName != null)
            compiler.getParserContext().setContextName(contextName);
	}

    /**
     * Add a template-scope variable. Note there is no option for it to be uninitialized.
     * @param varType Variable type
     * @param name Variable name
     */
	@Override
	public void addVariable(IVariableSpec varSpec, String name) {
		VariableFactory variableFactory = new VariableFactory((VariableSpec)varSpec);
    	Operand var = variableFactory.getContextInstance(name, compiler.getParserAssembler());
    	var.setPrivate(true);
    	compiler.getOperandMap().addOperand(var);
    	template.addTerm(var);
    }

	@Override
	public void addProperties(List<Term> termList) {
		template.getProperties().addProperties(termList);
	}
 
    /**
     * Create term list to return template solution
     */
	@Override
    public void createReturnTermList() {
		OperandType operandType = OperandType.TERM;
        ParserAssembler parserAssembler = compiler.getParserAssembler();
        VariableSpec varSpec = new VariableSpec(operandType);
        varSpec.setAxiomKey(getQualifiedName());
        VariableFactory variableFactory = new VariableFactory(varSpec);
        ItemList<?> itemList = variableFactory.getItemListInstance(getTemplate().getName(), parserAssembler.getQualifiedContextname());
        parserAssembler.registerAxiomTermList((AxiomTermList) itemList);	        
        itemList.setPublic(true);
       	parserAssembler.getListAssembler().addItemList(itemList.getQualifiedName(), itemList);
    }

    /**
     * Process FlowDeclaration production
     */
	@Override
    public void flowDeclaration() {
        if (isReturnsTerm()) {
        	OperandType operandType = OperandType.TERM;
	        ParserAssembler parserAssembler = compiler.getParserAssembler();
	        VariableSpec varSpec = new VariableSpec(operandType);
	        varSpec.setAxiomKey(getQualifiedName());
	        VariableFactory variableFactory = new VariableFactory(varSpec);
	        ItemList<?> itemList = variableFactory.getItemListInstance(getTemplate().getName(), parserAssembler.getQualifiedContextname());
	        parserAssembler.registerAxiomTermList((AxiomTermList) itemList);
	        itemList.setPublic(true);
	        parserAssembler.getListAssembler().addItemList(itemList.getQualifiedName(), itemList);
        }
    }
    
}
