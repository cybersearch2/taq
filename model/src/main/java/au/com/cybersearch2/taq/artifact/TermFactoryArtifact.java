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
package au.com.cybersearch2.taq.artifact;

import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Singleton companion to term artifact
 */
public interface TermFactoryArtifact {

	/**
	 * Returns fact operand
	 * @param parserTerm Parser term to analyze
	 * @return IOperand object
	 */
	IOperand createFact(TermArtifact parserTerm);
	
	/**
	 * Returns a regular expression short circuit evaluator
	 * @param regexOp regular expression operand
	 * @return IOperand object
	 */
    IOperand createRegexTerm(IOperand regexOp);
	
	/**
	 * Returns fact operand
	 * @param parserTerm Parser term to analyse
	 * @param postFix Post fix operator
	 * @return IOperand object
	 */
	IOperand createFact(TermArtifact parserTerm, String postFix);

	/**
	 * Returns fact operand
	 * @param qualifiedName Qualified name
	 * @return IOperand object
	 */
	IOperand createFact(QualifiedName qualifiedName);

    /**
     * Process Expression production
     * @param param Expression packaged in an operand
     * @param assignOp Optional assignment symbol, possibly reflexive
     * @param assignOperand Assignment expression associated with assigOp
     * @return IOperand object
     */
    IOperand expression(IOperand param, String assignOp, IOperand assignOperand);


    /**
     * Process TemplateTerm production
     * @param termArtifact Term production
     * @return IOperand object
     */
    IOperand templateTerm(TermArtifact termArtifact);

    /**
     * Returns anonymous operand containing given expression
     * @param expression Expression operand
     * @return IOperand object
     */
	IOperand templateTerm(IOperand expression);
  
}
