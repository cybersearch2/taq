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
package au.com.cybersearch2.taq.terms;

import java.util.Arrays;

import au.com.cybersearch2.taq.language.GenericParameter;
import au.com.cybersearch2.taq.language.Literal;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.Term;

/**
 * DoubleArrayTerm
 * @author Andrew Bowley
 */
public class DoubleArrayTerm extends GenericParameter<double[]> implements Literal 
{


	/**
	 * Construct an anonymous DoubleArrayTerm object
	 * @param value double
	 */
	public DoubleArrayTerm(double[] value) 
	{
		super(Term.ANONYMOUS, value);

	}

	/**
	 * Construct an anonymous DoubleArrayTerm object
	 * @param value String array representation of a double array value
	 */
	public DoubleArrayTerm(String... value) 
	{
		super(Term.ANONYMOUS, parseArray(value));

	}

    private static double[] parseArray(String... value) {
    	double[] array = new double[value.length];
    	int[] index = new int[] {0};
		Arrays.asList(value).forEach(item -> array[index[0]++] = Double.parseDouble(item));
		return array;
	}

	@Override
    public LiteralType getLiteralType()
    {
        return LiteralType.double_array;
    }
}
