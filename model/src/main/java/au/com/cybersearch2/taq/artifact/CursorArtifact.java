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

/**
 * List index that increments, decrements and can set be set to an arbitrary value. 
 * The index may not necessarily always be in range for the list to which it is bound.
 */
public interface CursorArtifact {

	/**
	 * Returns cursor sentinel operand which manages cursor initialization and backup
     * @param varSpec Variable Specification
     * @param cursorName Name of cursor
     * @param listName Name of list referenced by cursor
     * @param isReverse Flag set true if direction is reverse
	 * @return IOperand object
	 */
	IOperand cursorDeclaration(IVariableSpec varSpec, String cursorName, String listName, boolean isReverse);


    /**
     * Returns resource cursor sentinel operand
     * @param cursorName Name of cursor
     * @param resourceName Unique name
     * @param isReverse Flag set true if reverse cursor required
     * @return IOperand object
     */
    public IOperand createResourceSentinel(String cursorName, 
    		                               String resourceName, 
    		                               boolean isReverse);

}
