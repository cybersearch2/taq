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
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Singleton companion to ListArtifact
 */
public interface ListFactoryArtifact {

	/**
	 * Returns list name for given qualified name
	 * @param qname Qualified name
	 * @return QualifiedName object
	 */
	QualifiedName getListName(QualifiedName qname);
	
	/**
	 * Returns list name given both qualified name and name in original format
	 * @param qname Qualified name
	 * @return QualifiedName object
	 */
	QualifiedName getListName(QualifiedName qname, String originalName);

    /**
     * Process List reference production
     * @param listName List name
     * @param listReference List item reference
     * @return IOperand object
     */
    IOperand listReference(QualifiedName listName, ListReference listReference);
    
    /**
     * Process ContextListDeclaration production
     * @param listName List name
     * @param varSpec Variable specification or null if axiom list
     */
    void contextListDeclaration(String listName, IVariableSpec varSpec);

    /**
     * Process ListItemAssign production
     * @param listName Name of list
     * @param listReference Reference to list item
     * @param expression Assignment expression
     */
    void listItemAssign(QualifiedName listName, ListReference listReference, IOperand expression);

}
