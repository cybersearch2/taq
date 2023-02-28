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

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.artifact.RegularExpressionArtifact;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.expression.PatternFactory;
import au.com.cybersearch2.taq.expression.StringOperand;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Group;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.RegexFlag;

/**
 * Parser helper for creating regular expression artifacts
 */
public class ParserRegex implements RegularExpressionArtifact {

	/** Parser context */
	private final ParserContext parserContext;

	/**
	 * Construct ParserRegex object
	 * @param parserContext Parser context
	 */
	public ParserRegex(ParserContext parserContext) {
		this.parserContext = parserContext;
	}
	
	/**
     * Process Group production
     * @param group Group container
     * @param name Name of group item
     */
    public void group(Group group, String name) {
    	ParserAssembler parserAssembler = getParserAssembler();
    	OperandMap operandMap = parserAssembler.getOperandMap();
    	QualifiedName groupQname = name(name, true, false);
    	Operand var = operandMap.getOperand(groupQname);
        if (var == null) // Operand does not exists
            var = getOperandMap().addOperand(name, parserAssembler.getQualifiedContextname());   
        group.addGroup(var);
    }

	/**
	 * Returns Java constant mapped from text representation
	 * @param flag
	 * @return int 
	 * @throws CompilerException if text value is invalid
	 */
	public int mapRegexFlag(String flag) {
		RegexFlag[] flags = RegexFlag.values();
		for (int i = 0; i < flags.length; ++i)
		    if (flags[i].name().equalsIgnoreCase(flag))
		    	return flags[i].flag;
		throw new CompilerException(String.format("Regular expresssion flage %s invalid", flag));
	}

    /**
     * Process PatternDeclaration production
     * @param name Name
     * @param literal Literal regular expression or null if variable specified
     * @param variable Reference to a variable containing regular expression or null if literal specified
     * @param flags Optional flags provided by Java library regular expressions
     */
    public void patternDeclaration(String name, String literal, String variable, int flags) {
        Operand patternOp = null;
        if (literal != null)
            patternOp = new StringOperand(QualifiedName.ANONYMOUS, literal);
        else 
            patternOp = getOperandMap().addOperand(variable, getParserAssembler().getQualifiedContextname());
       getQueryProgram().addPatternFactory(new PatternFactory(name, patternOp, flags));  
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

    /**
     * Returns Query Program
     * @return QueryProgram object
     */
	private QueryProgram getQueryProgram() {
	    return parserContext.getQueryProgram();
    }

}
