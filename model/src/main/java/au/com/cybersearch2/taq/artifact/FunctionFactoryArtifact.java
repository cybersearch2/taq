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

import java.util.List;

import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;

/**
 * Singleton companion to function artifact
 */
public interface FunctionFactoryArtifact {

    /**
     * Process Function production
     * @param library Library containing the function
     * @param name Name of function
     * @param termList Function arguments
     * @return Parameter object
     */
    Parameter function(String library, String name, List<Term> termList);

    /**
     * Process function production
     * @param functionName Function name
     * @param operandType Function return type or null if none specified
     * @param returnsList Flag set true if list returned
     * @param archetype Archetype name if operand type is AXIOM, otherwise null
     */
	void function(String functionName, OperandType operandType, boolean returnsList, String archetype);
	
    /**
     * Returns operand which invokes an external function call. 
     * The function name must consist of 2 parts. The first is the name of a library.
     * The type of object returned from the call depends on the library.
     * 
     * @param functionArtifact Helper to collect function details
     * @return Operand object
     */
	IOperand createCallOperand(FunctionArtifact functionArtifact);

	/**
	 * Returns ObjectOperand for scope operand identified by name 
	 * @param name Qualified name of operand
	 * @param function Method signature
	 * @return
	 */
	IOperand createScopeFunction(QualifiedName name, String function);

    /**
     * Returns receiver template which is created given it's name and outer template name
     * @param receiverName Qualified name of receiver
     * @return Template object
     */
    ITemplate createReceiverTemplate(QualifiedName receiverName, FunctionArtifact functionArtifact);

	/**
	 * Set optional list reference to extract an item from a returned list
	 * @param callOperand Function operand
	 * @param listReference List reference 
	 * @return List reference operand
	 */
	IOperand setListReference(IOperand callOperand, ListReference listReference);

	/**
	 * Pop receiver off receiver stack
	 * @param functionArtifact Helper to collect function details
	 */
	void popReceiver(FunctionArtifact functionArtifact);
}
