/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.debug;

public interface ExecutionConsole {

	/**
	 * Set universal case-insensitive names flag
	 * @param flag boolean
	 */
	void setCaseInsensitiveNames(boolean flag);
	
	/**
	 * Set scope-specific case-insensitive names flag
	 * @param scopeName Name of scope to flag
	 * @param flag boolean
	 * @throws IllegalArgumentException if scope not found
	 */
	void setCaseInsensitiveNames(String scopeName, boolean flag);

	/**
	 * Sets loop timeout value in seconds.
	 * @param timeout int
	 */
	void setLoopTimeout(int timeout);

	/**
	 * Sets the maximum number of loop iterations threshold
	 * @param scopeName Name of scope to set
	 * @param threshold int
	 */
	void setLoopThreshold(int threshold);
	
	/**
	 * Sets scope-specific loop timeout value in seconds.
	 * @param scopeName Name of scope to set
	 * @param timeout long
	 */
	void setLoopTimeout(String scopeName, int timeout);

	/**
	 * Sets scope-specific  maximum number of loop iterations threshold
	 * @param threshold int
	 */
	void setLoopThreshold(String scopeName, int threshold);
}
