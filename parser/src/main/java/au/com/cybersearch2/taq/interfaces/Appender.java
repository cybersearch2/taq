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

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.language.Term;

/**
 * Appender
 * Operand operation to append to a list
 * @author Andrew Bowley
 */
public interface Appender extends Term, ListContainer {

	void append(Object value);

    /**
     * Returns current item value
     * @return Object
     */
    Object getItemValue();

	/**
	 * Evaluate value using data gathered during unification.
	 * @param id Identity of caller, which must be provided for backup()
	 * @return EvaluationStatus
	 */
	default EvaluationStatus evaluate(int id) 
	{
	    return EvaluationStatus.COMPLETE;
	}
}
