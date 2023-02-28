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
import au.com.cybersearch2.taq.artifact.ChoiceArtifact;
import au.com.cybersearch2.taq.artifact.ChoiceFactoryArtifact;
import au.com.cybersearch2.taq.artifact.NameIndex;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.expression.Evaluator;
import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.DualIndex;
import au.com.cybersearch2.taq.language.ExpressionIndex;
import au.com.cybersearch2.taq.language.GenericParameter;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;

/**
 * Parser helper for creating select and map artifacts
 */
public class ParserChoiceFactory implements ChoiceFactoryArtifact {

	/** Parser context */
	private final ParserContext parserContext;
	/** Parser helper for creating list artifacts */
	private final ParserListFactory parserListFactory;

	/**
	 * Construct ParserChoiceFactory object
	 * @param parserContext Parser context
	 */
	public ParserChoiceFactory(ParserContext parserContext) {
		this.parserContext = parserContext;
		parserListFactory = new ParserListFactory(parserContext);
	}

    /**
     * Adds a term to a selection axiom
     * @param qualifiedAxiomName Qualified name of axiom
     * @param listName Qualified name of list
     * @param currentName Name as it appears in the script
     */
	@Override
    public void selectionList(QualifiedName qualifiedAxiomName, QualifiedName listName, String currentName) {
    	ParserAssembler parserAssembler = getParserAssembler();
        ListAssembler listAssembler = parserAssembler.getListAssembler();
    	if (listAssembler.getListType(listName) != ListType.none) {
    	    Parameter param = new GenericParameter<QualifiedName>(Term.ANONYMOUS, listName);
            getParserAssembler().getAxiomAssembler().addAxiom(qualifiedAxiomName, param);
    	} else
    		throw new CompilerException(String.format("List %s not found", currentName));
    }
    
    /**
     * Process Selection production
     * @param parserChoice Production content
     * @param selectionOerand Selection operand
     */
	@Override
    public void selection(ChoiceArtifact parserChoice, IOperand selectionOperand) {
    	Operand operand = (Operand)selectionOperand;
    	QualifiedName qualifiedAxiomName = parserChoice.getQualifiedAxiomName();
    	QualifiedName qualifiedTemplateName = parserChoice.getQualifiedTemplateName();
    	ParserAssembler parserAssembler =  getParserAssembler();
    	parserAssembler.getAxiomAssembler().saveAxiom(qualifiedAxiomName).getArchetype().clearMutable();
        QualifiedName listQname = parserChoice.getListQname();
        if ((listQname != null) && (operand.getLeftOperand() instanceof Evaluator)) {
        	Variable rightOperand = (Variable)operand.getLeftOperand().getRightOperand();
        	String termName = rightOperand.getName();
        	DualIndex listItemSpec = new DualIndex(new NameIndex("->", termName));
           	ListReference listReference;
        	if (!listQname.getScope().equals(Scope.SCOPE)) {
    			ExpressionIndex expressionIndex = 
    				new ExpressionIndex(new IntegerOperand(QualifiedName.ANONYMOUS, 0L));
     			DualIndex arrayIndex = new DualIndex(expressionIndex);
        	    listReference = new ListReference(arrayIndex, listItemSpec);
        	} else
        	    listReference = new ListReference(listItemSpec);
        	rightOperand.setExpression(parserListFactory.listReference(listQname, listReference));
        }
        getParserAssembler().getTemplateAssembler().addTemplate(qualifiedTemplateName, operand);
    }
 
	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

}
