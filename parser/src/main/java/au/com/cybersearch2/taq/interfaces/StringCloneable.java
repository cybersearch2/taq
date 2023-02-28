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
 * StringCloneable
 * Support for conversion of text in format to suit locale to a number
 * @author Andrew Bowley
 * 27Apr.,2017
 */
public interface StringCloneable
{
    /**
     * Create an instance of same type as self and 
     * set it to the parsed value contained in given StringOperand
     * @param stringOperand The operand containing the text value
     * @return Operand of same type as self
     */
    Operand cloneFromOperand(Operand stringOperand);
}