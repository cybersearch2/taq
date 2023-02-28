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

import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Factual information packaged as a structure containing a sequence of terms 
 */
public interface AxiomArtifact {

	/**
     * Add name to list of axiom term names
     * @param qualifiedAxiomName Qualified axiom name
     * @param name Term name
     */
	void addAxiomTermName(QualifiedName qualifiedAxiomName, String name);
	
	
    /**
     * Process AxiomItem production
     * @param qualifiedAxiomName Axiom qualified name
     */
    void axiomItem(QualifiedName qualifiedAxiomName);

    /**
     * Process Fact production
     * @param qualifiedAxiomName Axiom qualified name
     * @param param Function parameter
     * @return Parameter object
     */
    Parameter fact(QualifiedName qualifiedAxiomName, Parameter param);
    
    /**
     * Returns parameter containing double "NaN" value (not a number)
     * @param qualifiedAxiomName Axiom qualified name
     * @return Parameter object
     */
    Parameter nan(QualifiedName qualifiedAxiomName);

    /**
     * Returns parameter containing "blank" literal
     * @param qualifiedAxiomName Axiom qualified name
     * @return Parameter object
     */
    Parameter blank(QualifiedName qualifiedAxiomName);
  
    /**
     * Create new axiom item list. Do not report a duplicate error if list already exists.
     * @param qualifiedAxiomName List name
     * @return flag set true if list created
     */
	boolean createAxiomItemList(QualifiedName qualifiedAxiomName, boolean isExport);
	
    /**
     * Create new axiom. 
     * @param qualifiedAxiomName List name
     * @param isExport Flag set true if axiom is to be exported
     */
	void createAxiom(QualifiedName qualifiedAxiomName, boolean isExport);

	/**
     * Process AxiomInitializer production
     * @param qualifiedAxiomName Axiom qualified name
     * @param initializeTemplate Template to initialize axiom
     * @return Template
     */
    ITemplate axiomInitializer(QualifiedName qualifiedAxiomName, ITemplate initializeTemplate);

}
