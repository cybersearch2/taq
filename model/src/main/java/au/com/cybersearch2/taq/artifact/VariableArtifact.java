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
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.TaqLiteral;

/**
 * A variable is an operand that evaluates a value
  */
public interface VariableArtifact {

    /**
     * Process Type production
     * @param literal Literal type
     * @param qualifier Optional parameter
     * @param qualifierName Qualifier resolved to qualified name or null
     * @return VariableSpec object
     */
    IVariableSpec variableSpec(TaqLiteral literal, String qualifier, QualifiedName qualifierName, String source);

    /**
     * Returns a variable specification for a term list to substitute "axiom" type
     * @return
     */
    IVariableSpec termList();
    
	/**
	 * Returns list variable or cursor for same depending on "isCursor" flag
	 * @param listName Name of variable
	 * @param varSpec List type specification
	 * @param isCursor Flag set true if list is referenced by a cursor
	 * @param isReverse Flag set true if cursor operates in reverse
	 * @param function Call operand, if assigned, else null
	 * @return Operand object -  either ListOperand or CursorSentinalOperand
	 */
	IOperand createListVariable(String listName, IVariableSpec varSpec, boolean isCursor, boolean isReverse, IOperand function);

	/**
	 * Returns cursor sentinel bound to list of given name
	 * @param listName Name of variable
	 * @param varSpec List type specification
	 * @param target Qualified name of bound list
	 * @param isReverse Flag set true if cursor operates in reverse
	 * @return CursorSentinalOperand
	 */
	IOperand createCursorSentinel(String listName, IVariableSpec varSpec, QualifiedName target, boolean isReverse);

    /**
     * Returns list variable to access target
	 * @param target Qualified name of actual list
	 * @param isExport Flag set true if list to be exported
     * @return Operand object
     */
	IOperand getListVariable(QualifiedName target, boolean isExport);
	
    /**
     * Process VariableDeclaration production
     * @param name Name
     * @param expression Optional assignment expression
     * @param isUntyped Flag set true if type is not specified
     * @param varSpec Variable type specification or null if untyped
     */
    void variableDeclaration(String name, IOperand expression, boolean isUntyped, IVariableSpec varSpec);

}
