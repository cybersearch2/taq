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
Demonstrates the default flow strategy. The "swatch" select maps
a 32-bit color value to a color name and does not have a final choice for an unmatched 
color value. The default case must therefore be dealt with post select and the clue 
given by the select operation is the color name is set to blank.
 */
public class ColorSwatch2 
{
    private QueryProgramParser queryProgramParser;

    public ColorSwatch2()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles the CHOICE_COLORS script and runs the "color_query" query, displaying the solution on the console.<br/>
	 */
	public String getColorSwatch(int hexColor)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("select/color-swatch2.taq");
        // Create QueryParams object for Global scope and query "query_color"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "query_color");
        // Add a shade Axiom with a single "aqua" term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("RGB", new Parameter("color32", hexColor)); 
        Result result = queryProgram.executeQuery(queryParams);
        return result.axiomIterator("query_color").next().toString();
	}
	
    /**
     * Run tutorial
     * The expected result:<br/>
	shade(color=aqua, 0)<br/>
	shade(color=blue, 2)<br/>
	shade(color=unknown, -1)<br/>
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        ColorSwatch2 colorSwatch = new ColorSwatch2();
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
