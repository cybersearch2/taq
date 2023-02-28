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

/**
 * OperandType
 * TAQ types used for variables and lists.
 * @author Andrew Bowley
 * 10 Mar 2015
 */
public enum OperandType 
{
    INTEGER,
    BOOLEAN,
    DOUBLE,
    STRING,
    DECIMAL,
    TERM,   // AxiomParameterOperand or AxiomTermList created and passed to registerAxiomList()
    AXIOM,  // AxiomOperand or  AxiomList
    CURRENCY,
    COMPLEX,
    SET_LIST,   // AxiomOperand with ParameterList<AxiomList> to populate it
    TERM_LIST,  // Operand contains a list which can be passed to the solution  
    UNKNOWN,
    CURSOR
}
