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

public interface IOperand {

	/**
	 * Returns Parameter value 
	 * @return Object
	 */
	Object getValue();

	/**
	 * Returns Parameter value class or Null.class if value is null
	 * @return Class object
	 */
    Class<?> getValueClass();
    
	/**
     * Set this operand private - not visible in solution
     * @param isPrivate Flag set true if operand not visible in solution
     */
    void setPrivate(boolean isPrivate);
    
    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
	void assign(Parameter parameter);
	
    Parameter toParameter();

}
