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
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * A term artifact occupies a template while a term object is placed in an axiom.
 * The artifact is always implemented as an operand and is potentially complex. 
 */
public interface TermArtifact {

	/**
	 * Returns qualified name of term
	 * @return QualifiedName object
	 */
	QualifiedName getQname();
	
	/** 
	 * Returns Variable type or null if not specified 
	 * @return IVariableSpec object
	 */
	IVariableSpec getVarSpec();
	
	/**
	 * Set operator applied to the term
	 * @param operator Operator as it appears in the source
	 */
	void setOperator(String operator);
	
	/**
	 * Set literal on left hand side of a binary operation such as assignment
	 * @param literal Non-empty operand
	 */
	void setLiteral(IOperand literal);
	
	/**
	 * Set expression on left hand side of a binary operation such as assignment
	 * @param expression IOperand object
	 */
	void setExpression(IOperand expression);
	
	/**
	 * Set function parameters
	 * @param parameterTemplate Template containing parameter terms
	 */
	void setParameterTemplate(ITemplate parameterTemplate);
	
	/**
	 * Assign given axiom list to this term
	 * @param axiomName Axiom key
	 * @param initializeTemplate Template to evaluate the axiom list
	 */
	void assignAxiomList(QualifiedName axiomName, ITemplate initializeTemplate);
	
	/**
	 * Assign to self the evaluation of the given combination of operator and expression on self
	 * @param operator Operator as it appears in the source
	 * @param expression Operand containing expression
	 */
	void reflexiveAssign(String operator, IOperand expression);
}
