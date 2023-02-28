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

/**
 * OperandVisitor
 * @author Andrew Bowley
 * 16 Dec 2014
 */
public interface OperandVisitor 
{
	/**
	 * Visit next term
	 * @param operand Operand object
	 * @param depth Depth in Operand tree
	 * @return Flag set true to continue
	 */
	boolean next(Operand operand, int depth);
}
