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
 * IntegerTerm
 * @author Andrew Bowley
 * 8 Dec 2014
 */
public class IntegerTerm extends GenericParameter<Long> 
{
	/**
	 * Construct an anonymous IntegerTerm object 
	 * @param value int
	 */
	public IntegerTerm(long value) 
	{
		super(Term.ANONYMOUS, value);

	}

	/**
	 * Construct an anonymous IntegerTerm object
	 * @param value String representaion of integer value
	 */
	public IntegerTerm(String value) 
	{
		super(Term.ANONYMOUS, (value.indexOf('x', 0) == 1 || value.indexOf('X', 0) == 1) ? Long.parseLong(value.substring(2), 16) : Long.parseLong(value));

	}

}
