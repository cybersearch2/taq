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
 * Production of an artifact which performs select or map operation
 */
public interface ChoiceFactoryArtifact {

    /**
     * Adds a term to a selection axiom
     * @param qualifiedAxiomName Qualified name of axiom
     * @param listName Qualified name of list
     * @param currentName Name as it appears in the script
     */
    void selectionList(QualifiedName qualifiedAxiomName, QualifiedName listName, String currentName);
    
    /**
     * Process Selection production
     * @param parserChoice Production content
     * @param selectionOperand Selection operand
     */
    void selection(ChoiceArtifact parserChoice, IOperand selectionOperand);
    
}
