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
 * XplLiteral
 * Word literals enumerated (excludes "NaN")
 * @author Andrew Bowley
 */
public enum TaqLiteral
{
    axiom, 
    taq_boolean, // boolean 
    complex,
    currency, 
    cursor,
    decimal, 
    taq_double, // double
    export,
    fact, 
    flow, 
    function,
    include, 
    integer, 
    list, 
    map,
    pattern,
    query, 
    resource, 
    reverse,
    scope, 
    select,
    string, 
    template, 
    term, 
    unknown,
    variable,
    post_release ;// Defined in more recent release

	/**
	 * Returns actual literal text by removing "taq_" prefix, if used in name
	 * @return word
	 */
	public String word() {
        String keyword = name();
        if (keyword.startsWith("taq_"))
        	keyword = keyword.substring(4);
        return keyword;
	}
}
