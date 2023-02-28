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

import au.com.cybersearch2.taq.language.Group;
import au.com.cybersearch2.taq.language.SyntaxException;

/**
 * Text pattern matching with support for grouping
 */
public interface RegularExpressionArtifact {

	/**
     * Process Group production
     * @param group Group container
     * @param name Name of group item
     */
    void group(Group group, String name);

	/**
	 * Returns Java constant mapped from text representation
	 * @param flag
	 * @return int 
	 * @throws SyntaxException if text value is invalid
	 */
	int mapRegexFlag(String flag);

    /**
     * Process PatternDeclaration production
     * @param name Name
     * @param literal Literal regular expression or null if variable specified
     * @param variable Reference to a variable containing regular expression or null if literal specified
     * @param flags Optional flags provided by Java library regular expressions
     */
    void patternDeclaration(String name, String literal, String variable, int flags);
 
}
