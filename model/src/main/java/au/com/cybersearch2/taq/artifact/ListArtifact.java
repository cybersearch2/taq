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
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * A list contains a sequence of items that can be referenced by index, cursor, or if it contains terms, by name.
 * Lists have various manifestations governed by how they are created and populated.
 */
public interface ListArtifact {

	/**
	 * Returns template for ListParameters production
	 * @return Template object
	 */
	ITemplate chainTemplate();
	
	/**
	 * Returns list parameters term
	 * @param expression Term content 
	 * @return IOperand object
	 */
	IOperand listParameters(IOperand expression);
	
	/**
	 * Returns axiom qualified name
	 */
	QualifiedName getQualifiedAxiomName();
	
	/**
	 * Set templates for creating a dynamic axiom list
	 * @param axiomList Template list
	 */
	void setAxiomList(List<ITemplate> axiomList, List<String> termNames);
	
	
	/**
	 * Set template to initialize the list
	 * @param template List parameters template
	 */
	void setTemplate(ITemplate template);
	
	
	/**
	 * Map a local list name to an actual list in another context
	 * @param target Qualified name of actual list
	 */
	void setTarget(QualifiedName target);
	
	
	/**
	 * Set range for sub list
	 * @param begin Start index
	 * @param end End index
	 */
	void setRange(int begin, int end);
	
	
	/**
	 * Set axiom qualified name
	 * @param qualifiedAxiomName Axiom qualified name
	 */
	void setQualifiedAxiomName(QualifiedName qualifiedAxiomName);

    /**
     * Process ListDeclaration production
     */
    void listDeclaration();

    /**
     * Returns number of names in axiom header
     * @param axiomHeader String list or null if none
     * @return int
     */
    int getTermCount(List<String> axiomHeader);
}
