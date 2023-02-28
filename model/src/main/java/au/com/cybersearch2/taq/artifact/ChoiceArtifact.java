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
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Declaration of an artifact which performs select or map operation
 */
public interface ChoiceArtifact {

	static final String CHOICE_PREFIX = "CHOICE_";
	
	/**
	 * Returns name of the term to receive the selection value
	 * @return name
	 */
	String getName();

	/**
	 * Returns qualified name of list used for matching to context-scoped axiom terms
	 * @return QualifiedName name object
	 */
	QualifiedName getListQname();

	/**
	 * Returns flag set true if choice is map (just key/value pairing)
	 * @return boolean
	 */
	boolean isMap();

	/** Returns flag set true if choice declared in a scope and false if declared in a template 
	 * @return boolean
	 */
	boolean isScopeContext();
	
	/**
	 * Check the name of the first axiom term name to see if it is a context term list
	 * @param name Term name
	 */
	void analyseFirstTermName(String name);
	
	/**
	 * Returns qualified name of axiom list containing available choices
	 * @return QualifiedName object
	 */
	QualifiedName getQualifiedAxiomName();

	/**
	 * Returns qualified name of evaluation template 
	 * @return QualifiedName object
	 */
	QualifiedName getQualifiedTemplateName();

	/**
	 * Returns evaluation template
	 * @return Template object
	 */
	ITemplate getTemplate();

	/**
	 * Returns choice name
	 * @return name
	 */
	String getChoiceName();

	/**
	 * Returns qualified name of this Choice
	 * @return QualifiedName object
	 */
	QualifiedName getQualifiedName();
	
	/**
	 * Returns operand to select from key/value mappings
	 * @param qname Qualified name
	 * @param owner Template containing this Map
	 * @param isList Flag set true if list map
	 * @return IOperand object
	 */
	IOperand getMap(QualifiedName qname, ITemplate owner, boolean isList);

	/**
	 * Sets operand to evaluate value to be mapped
	 * @param operand Operand to provide value
	 */
	void setValueExpression(IOperand operand);

}
