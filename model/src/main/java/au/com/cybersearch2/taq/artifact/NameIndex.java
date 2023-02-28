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

/**
 * List item reference by name
 * @author Andrew Bowley
 *
 */
public class NameIndex {

	/** The name */
	private final String name;
	/** Index symbol, dot or right arrow */
	private final String pointer;
	
	/**
	 * Construct NameIndex object 
	 * @param pointer Index symbol, dot or right arrow
	 * @param name Name
	 */
	public NameIndex(String pointer, String name) {
		this.pointer = pointer;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return pointer + name;
	}

}
