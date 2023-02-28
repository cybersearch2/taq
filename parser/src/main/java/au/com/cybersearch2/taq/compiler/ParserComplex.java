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

import au.com.cybersearch2.taq.artifact.ComplexArtifact;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.expression.ComplexOperand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Parser helper for creating complex artifacts
 */
public class ParserComplex implements ComplexArtifact {

	/** Parser context */
	private final ParserContext parserContext;

	/**
	 * Construct ParserComplex object
	 * @param parserContext Parser context
	 */
	public ParserComplex(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

    /**
     * Process ComplexDeclaration production
     * @param name Name
     * @param expression Optional assignment expression
     * @param varSpec Variable type or null if untyped
     */
	@Override
    public void complexDeclaration(String name, IOperand expression, IVariableSpec varSpec) {
    	if ((varSpec == null) || (varSpec.getOperandType() != OperandType.COMPLEX))
    		throw new CompilerException("Only complex type allowed for set asignment");
    	ParserAssembler parserAssembler = getParserAssembler();
    	QualifiedName qname = parserAssembler.getContextName(name);
    	ComplexOperand complexOperand = new ComplexOperand(qname, (double[]) expression.getValue());
        getOperandMap().addOperand(complexOperand);
    }
    
	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

	/**
	 * Returns operand Map
	 * @return OPerandMap object
	 */
	private OperandMap getOperandMap() {
		return parserContext.getParserAssembler().getOperandMap();
	}

}
