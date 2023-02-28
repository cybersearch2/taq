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
package scopes;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates that the result of a selection operation can be accessed 
in an attached receiver. This is useful for appending the result to an
axiom list.
 */
public class GermanColors 
{
    private QueryProgramParser queryProgramParser;
 
    public GermanColors()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles foreign-colors.taq and runs the "color_query" query, displaying the solution on the console.
 	 * @return AxiomTermList iterator containing the final Calculator solution
	 */
    public Axiom getColorSwatch()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("scopes/german-colors.taq");
        Result result = queryProgram.executeQuery("colors");
        return result.getAxiom("colors");
	}
	
    /**
     * Run tutorial
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        GermanColors germanColors = new GermanColors();
	        Axiom axiom = germanColors.getColorSwatch();
	        axiom.forEach(term -> System.out.println(term.toString()));
 		} 
		catch (Throwable e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
