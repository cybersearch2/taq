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
package au.com.cybersearch2.taq.trait;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Scanner;

import au.com.cybersearch2.taq.language.OperandType;

/**
 * ComplexTrait
 * Behaviours for localization and specialization of Comp;ex operands
 * @author Andrew Bowley
 */
public class ComplexTrait extends DefaultTrait {

	public ComplexTrait() {
		super(OperandType.COMPLEX);
	}

    /**
     * Parse text value to return a double[] object
     * @param string Value to parse
     * @return Number=type object
     */
    public double[] parseValue(String string)
    {
        double[] value = new double[] {Double.NaN, Double.NaN};
        if (string.startsWith("{"))
        	string = string.substring(1, string.length() -1);
        try (Scanner scanner = new Scanner(string).useDelimiter("\\s*,\\s*")) {
	        scanner.useLocale(getLocale());
	        if (scanner.hasNextDouble())
	            value[0] = scanner.nextDouble();
	        if (scanner.hasNextDouble())
	             value[1] = scanner.nextDouble();
        }    
        return value;
    }

    /**
     * formatValue
     * @see au.com.cybersearch2.taq.interfaces.TextFormat#formatValue(java.lang.Object)
     */
    @Override
    public String formatValue(Object value)
    {
		StringBuilder builder = new StringBuilder();
		NumberFormat formatter = new DecimalFormat("#0.0####");
		double[] doubleArray = (double[])value;
		builder.append('{')
		    .append(formatter.format(doubleArray[0]))
		    .append(',')
		    .append(formatter.format(doubleArray[1]))
		    .append('}');
		return builder.toString();
    }

}
