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
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.Term;

/*
 * A template performs logical operations and is organized as a structure containing a sequence of operands
 */
public interface TemplateArtifact {

    /**
     * Append an operand to the template under construction
     * @param operand IOperand object
     */
	void addTerm(IOperand operand);
	
	/**
     * Create a variable and append iot to the template under construction
	 * @param varSpec Variable specification
	 * @param name Variable name
	 */
	void addVariable(IVariableSpec varSpec, String name);
	
	/**
	 * Add term properties 
	 * @param termList Term list containing the properties to add
	 */
	void addProperties(List<Term> termList);
	
	/**
	 * Reset parser qualified context name after possible change as side effect of a previous operation
	 */
	void adjustContextName();

	/**
	 * Set parser context template name to that of the template under construction
	 */
	void setOuterTemplateName();
	
	/**
	 * Returns the actual template constructed by this artifact
	 * @return ITemplate object
	 */
	ITemplate getTemplate();
	
	/**
	 * Returns flag set true if the template is declared as returning an axiom term list
	 * @return boolean
	 */
	boolean isReturnsTerm();
	
    /**
     * Create term list to return template solution
     */
    void createReturnTermList();

    /**
     * Process FlowDeclaration production
     */
    void flowDeclaration();
}
