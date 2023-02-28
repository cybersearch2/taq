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
package au.com.cybersearch2.taq.interfaces;

import java.util.List;

import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * AxiomContainer
 * Holds one or more axioms
 * @author Andrew Bowley
 * 14Apr.,2017
 */
public interface AxiomContainer
{
    QualifiedName getKey();
    LocaleAxiomListener getAxiomListener();
    void setAxiomTermNameList(List<String> axiomTermNameList);
    List<String> getAxiomTermNameList();
    OperandType getOperandType();
    
	/**
	 * Clear item list
	 */
	void clear();
}
