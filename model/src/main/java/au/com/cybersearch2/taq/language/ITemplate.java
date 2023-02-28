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
package au.com.cybersearch2.taq.language;

public interface ITemplate {

	/**
	 * Returns the name of the Structure    
	 * @return String
	 */
	String getName();

	/**
	 * Returns key to match with Axiom name for unification
	 * @return String
	 */
	String getKey();

	/**
	 * Sets axiom key
	 * @param value Value
	 */
	void setKey(String value);

	/**
	 * Returns flag set true if this object is used as a Calculator
	 * @return boolean
	 */
	boolean isCalculator();

	/**
	 * Returns flag set true if this object is used as a Choice
	 * @return boolean
	 */
	boolean isChoice();

	/**
	 * Returns flag set true if this is an inner template
	 * @return boolean
	 */
	boolean isInnerTemplate();

	/**
	 * Returns flag set true if this si a replicate template
	 * @return boolean
	 */
	boolean isReplicate();

	boolean isEmpty();
	
	/**
	 * Returns template qualified name
	 * @return QualifiedName object
	 */
	QualifiedName getQualifiedName();

	/**
	 * Add Operand term. 
	 * @param operand Operand object
	 */
	void addTerm(IOperand operand);

	int getTermCount();

	void reverse();

}
