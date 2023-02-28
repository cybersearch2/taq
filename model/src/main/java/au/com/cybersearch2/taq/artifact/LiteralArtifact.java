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
import java.util.function.IntPredicate;

import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Unknown;

public interface LiteralArtifact {

    /**
     * Process long Literal production
     * @param value Value
     * @return Operand
     */
    IOperand literal(Long value);

    /**
     * Process double Literal production
     * @param value Value
     * @return Operand
     */
    IOperand literal(Double value);
    
    /**
     * Process string Literal production
     * @param value Value
     * @return Operand
     */
    IOperand literal(String value);

    /**
     * Process boolean Literal production
     * @param flag Value
     * @return Operand
     */
    IOperand literal(boolean flag);

    /**
     * Process unknown Literal production
     * @param unknown Value
     * @return Operand
     */
    IOperand literal(Unknown unknown);

    /**
     * Returns operand containing a primitive array
     * @param literalList Literal parameters, expected to be of same type 
     * @return Operand object
     */
    IOperand literalSet(List<Parameter> literalList);
    
    /**
     * Process TypedLiteralTerm production
     * @param varSpec Variable specification
     * @param literal Parameter containing literal value
     * @return IOperand object
     */
    IOperand typedLiteralTerm(IVariableSpec varSpec, Parameter literal);

    static boolean isMatch(Boolean bool, String string) {
    	if ((bool == null) || (string == null))
    		return false;
		if (bool.booleanValue()) 
			return string.equals("true") || string.equals("yes");
		else
			return string.equals("false") || string.equals("no");
	}

	static boolean isNumber(String string) {
		// Numeric comparison to String is not easy, so just assume convergence if the string contains a digit character
	   	IntPredicate digitChar = new IntPredicate() {

			@Override
			public boolean test(int x) {
				return Character.isDigit(x);
			}};
	    return string.chars().anyMatch(digitChar);
    }

}
