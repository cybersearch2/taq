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
import au.com.cybersearch2.taq.artifact.AxiomArtifact;
import au.com.cybersearch2.taq.compile.AxiomAssembler;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.language.DoubleTerm;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.LiteralParameter;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;

/**
 * Parser helper for creating axiom artifacts
 */
public class ParserAxiom implements AxiomArtifact {

	/** Parser context */
	private final ParserContext parserContext;

	/**
	 * Construct ParserAxiom object
	 * @param parserContext Parser context
	 */
	public ParserAxiom(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	/**
     * Add name to list of axiom term names
     * @param qualifiedAxiomName Qualified axiom name
     * @param name Term name
     */
	@Override
	public void addAxiomTermName(QualifiedName qualifiedAxiomName, String name) {
	    getParserAssembler().getAxiomAssembler().addAxiomTermName(qualifiedAxiomName, name);
	}
	
    /**
     * Process AxiomItem production
     * @param qualifiedAxiomName Axiom qualified name
     */
	@Override
    public void axiomItem(QualifiedName qualifiedAxiomName) {
    	getParserAssembler().getAxiomAssembler().saveAxiom(qualifiedAxiomName).getArchetype().clearMutable();
    }

    /**
     * Process Fact production
     * @param qualifiedAxiomName Axiom qualified name
     * @param param Function parameter
     * @return Parameter object
     */
	@Override
    public Parameter fact(QualifiedName qualifiedAxiomName, Parameter param) {
    	AxiomAssembler axiomAssembler = getParserAssembler().getAxiomAssembler();
    	if (param instanceof ListOperand) {
    	    String termName = axiomAssembler.getAxiomTermName(qualifiedAxiomName);
    	    param = new Parameter(termName, param);
    	}
    	axiomAssembler.addAxiom(qualifiedAxiomName, param);
        return param;
    }

    /**
     * Returns parameter containing double "NaN" value (not a number)
     * @param qualifiedAxiomName Axiom qualified name
     * @return Parameter object
     */
	@Override
    public Parameter nan(QualifiedName qualifiedAxiomName) {
    	Parameter doubleTerm = new DoubleTerm("NaN");
        getParserAssembler().getAxiomAssembler().addAxiom(qualifiedAxiomName, doubleTerm);
        return doubleTerm;
    }
 
    /**
     * Returns parameter containing "blank" literal
     * @param qualifiedAxiomName Axiom qualified name
     * @return Parameter object
     */
	@Override
    public Parameter blank(QualifiedName qualifiedAxiomName) {
    	Parameter blankTerm = new LiteralParameter(Term.ANONYMOUS, new Blank(), LiteralType.unspecified);
        getParserAssembler().getAxiomAssembler().addAxiom(qualifiedAxiomName, blankTerm);
        return blankTerm;
    }
 
    /**
     * Create new axiom item list. Do not report a duplicate error if list already exists.
     * @param qualifiedAxiomName List name
     * @return flag set true if list created
     * @see AxiomAssembler#saveAxiom(QualifiedName)
     */
	@Override
	public boolean createAxiomItemList(QualifiedName qualifiedAxiomName, boolean isExport) {
		return getParserAssembler().getListAssembler().createAxiomItemList(qualifiedAxiomName, isExport);
	}

    /**
     * Create new axiom. 
     * @param qualifiedAxiomName List name
     * @param isExprt Flag set true if axiom is to be exported
     * @see AxiomAssembler#saveAxiom(QualifiedName)
     */
	@Override
	public void createAxiom(QualifiedName qualifiedAxiomName, boolean isExport) {
		ListAssembler listAssembler = getParserAssembler().getListAssembler();
		QualifiedName contextListName = (new QualifiedName(qualifiedAxiomName.getScope(), QualifiedName.EMPTY, qualifiedAxiomName.getName()));
		boolean createTermList = false;
		if (parserContext.getVariableSpec(qualifiedAxiomName.getName()) != null)
			listAssembler.createTermList(contextListName);
		else
			createTermList = true;
		listAssembler.createAxiom(qualifiedAxiomName, isExport, createTermList);
		if (!createTermList) {
			Scope globalScope = parserContext.getScope().getGlobalScope();
			listAssembler = globalScope.getParserAssembler().getListAssembler();
			AxiomTermList termList =  listAssembler.getAxiomTerms(contextListName);
			Axiom axiom = getParserAssembler().getAxiomAssembler().createAxiom(qualifiedAxiomName);
			termList.setAxiom(axiom);
		}
	}

	/**
     * Process AxiomInitializer production
     * @param qualifiedAxiomName Axiom qualified name
     * @param initializeTemplate Template to initialize axiom
     * @return Template
     */
	@Override
    public ITemplate axiomInitializer(QualifiedName qualifiedAxiomName, ITemplate initializeTemplate) {
        if (initializeTemplate != null)
            return initializeTemplate;
        TemplateArchetype architype = new TemplateArchetype(new QualifiedTemplateName(parserContext.getScope().getAlias(), qualifiedAxiomName.getName()));
        return new Template(architype);
    }

	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

}
