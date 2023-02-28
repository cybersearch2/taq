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

import au.com.cybersearch2.taq.artifact.LiteralArtifact;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.BooleanOperand;
import au.com.cybersearch2.taq.expression.DoubleOperand;
import au.com.cybersearch2.taq.expression.ExpressionOperand;
import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.expression.NullOperand;
import au.com.cybersearch2.taq.expression.StringOperand;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.operator.DoubleOperator;

/**
 * Parser helper for creating literal artifacts
 */
public class ParserLiteral implements LiteralArtifact {

	/** Parser context */
	private final ParserContext parserContext;

	/**
	 * Construct ParserLiteral object
	 * @param parserContext Parser context
	 */
	public ParserLiteral(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

    /**
     * Process long Literal production
     * @param value Value
     * @return Operand
     */
	@Override
    public Operand literal(Long value) {
        Operand operand = new IntegerOperand(QualifiedName.ANONYMOUS, value);
    	getParserAssembler().registerLocaleListener((LocaleListener) operand);
        return operand;
    }
    
    /**
     * Process double Literal production
     * @param value Value
     * @return Operand
     */
	@Override
    public Operand literal(Double value) {
        Operand operand = new DoubleOperand(QualifiedName.ANONYMOUS, value);
    	getParserAssembler().registerLocaleListener((LocaleListener) operand);
        return operand;
    }
    
    /**
     * Process string Literal production
     * @param value Value
     * @return Operand
     */
	@Override
    public Operand literal(String value) {
        Operand operand = new StringOperand(QualifiedName.ANONYMOUS, value);
    	getParserAssembler().registerLocaleListener((LocaleListener) operand);
        return operand;
    }

    /**
     * Process boolean Literal production
     * @param flag Value
     * @return Operand
     */
	@Override
    public Operand literal(boolean flag) {
    	return new BooleanOperand(QualifiedName.ANONYMOUS, flag);   
    }
    
    /**
     * Process unknown Literal production
     * @param unknown Value
     * @return Operand
     */
	@Override
    public Operand literal(Unknown unknown) {
    	return new NullOperand(QualifiedName.ANONYMOUS, unknown);
    }

    /**
     * Returns operand containing a primitive array
     * @param literalList Literal parameters, expected to be of same type 
     * @return Operand object
     */
	@Override
    public Operand literalSet(List<Parameter> literalList) {
    	if (literalList.isEmpty())
    		throw new CompilerException("Literal set is empty");
    	for (Parameter param: literalList)
    	    if (param.getValueClass() != Double.class)
    		    throw new CompilerException("Literal set can only contain double values");
    	double[] doubleArray = new double[literalList.size()];
    	int index = 0;
    	for (Parameter param: literalList)
    		doubleArray[index++] = (double)param.getValue();
    	return new ExpressionOperand<double[]>(QualifiedName.ANONYMOUS, doubleArray) {
            final private Operator operator = new DoubleOperator();
			@Override
			public Operator getOperator() {
				return operator;
			}};
    }

    /**
     * Process TypedLiteralTerm production
     * @param varType Variable type
     * @param literal Parameter containg literal value
     * @return Operand object
     */
    @Override
    public Operand typedLiteralTerm(IVariableSpec varSpec, Parameter literal) {
    	VariableFactory variableFactory = new VariableFactory((VariableSpec)varSpec);
        return variableFactory.getParameter(literal, parserContext.getScope());
    }


	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}
}
