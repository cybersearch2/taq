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
package select;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates using a familiar default selection strategy.
The "swatch" select maps a 32-bit color value to both a color name and the red-green-blue 
hex components. As it is not possible to map all possible 32-bit color values, a default 
strategy is required to handle unsupported colors. This is implemented using a final 
"always true" choice.
*/
public class ColorSwatch 
{
    private QueryProgramParser queryProgramParser;

    public ColorSwatch()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles color-swatch.taq and runs the "color_query" query, 
	 * displaying the solution on the console.
	 */
	public String getColorSwatch(int hexColor)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("select/color-swatch.taq");
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "query_color");
        // Add a shade Axiom with a single "aqua" term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("RGB", new Parameter("rgb", hexColor)); 
        Result result = queryProgram.executeQuery(queryParams);
        return result.axiomIterator("query_color").next().toString();
	}
	
    /**
     * Run tutorial
     * The expected result:<br/>
shade(rgb=65535, color=aqua, red=0, green=255, blue=255, index=0)<br/>
shade(rgb=255, color=blue, red=0, green=0, blue=255, index=2)<br/>
shade(rgb=7864319, color=unknown, red=0, green=0, blue=0, index=4)<br/>
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        ColorSwatch colorSwatch = new ColorSwatch();
            System.out.println(colorSwatch.getColorSwatch(0x00ffff).toString());
            System.out.println(colorSwatch.getColorSwatch(0x0000ff).toString());
            System.out.println(colorSwatch.getColorSwatch(0x77ffff).toString());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
