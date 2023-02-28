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

import au.com.cybersearch2.taq.provider.CallHandler;

/**
 * FunctionProvider
 * Sources functions from external libraries
 * @author Andrew Bowley
 * 30 Jul 2015
 */
public interface FunctionProvider
{
    /**
     * Name of function provider - must be unique
     * @return String
     */
    String getName();
    
    /**
     * Returns function object specified by name
     * @param identifier Name of function
     * @return Function object implementing CallEvaluator interface
     */
    CallHandler getCallEvaluator(String identifier);

	default boolean setProperty(String key, Object value) {
		return false;
	}
}
