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

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates using a context list to translate a selection value. 
The context list is named "colors"
 */
public class ForeignColors 
{
    private QueryProgramParser queryProgramParser;
 
    public ForeignColors()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles foreign-colors.taq and runs the "color_query" query, displaying the solution on the console.
     * The expected result:<code>
		color(shade=Wasser, Red=0, Green=255, Blue=255)
		color(shade=schwarz, Red=0, Green=0, Blue=0)
		color(shade=weiß, Red=255, Green=255, Blue=255)
		color(shade=blau, Red=0, Green=0, Blue=255)
		color(shade=bleu vert, Red=0, Green=255, Blue=255)
		color(shade=noir, Red=0, Green=0, Blue=0)
		color(shade=blanc, Red=255, Green=255, Blue=255)
		color(shade=bleu, Red=0, Green=0, Blue=255)</code>
	 * @return AxiomTermList iterator containing the final Calculator solution
	 */
    public String getColorSwatch(String language, String name)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("scopes/foreign-colors.taq");
          
        // Create QueryParams object for specified scope and query "color_query"
        QueryParams queryParams = queryProgram.getQueryParams(language, "color_query");
        // Add a shade Axiom with a single "aqua" term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("color", new Parameter("shade", name));
        Result result = queryProgram.executeQuery(queryParams);
        return result.getAxiom("color").toString();
	}
	
    /**
     * Run tutorial
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        ForeignColors foreignColors = new ForeignColors();
            System.out.println(foreignColors.getColorSwatch("german", "Wasser"));
            System.out.println(foreignColors.getColorSwatch("german", "schwarz"));
            System.out.println(foreignColors.getColorSwatch("german", "weiß"));
            System.out.println(foreignColors.getColorSwatch("german", "blau"));
            System.out.println(foreignColors.getColorSwatch("french", "bleu vert"));
            System.out.println(foreignColors.getColorSwatch("french", "noir"));
            System.out.println(foreignColors.getColorSwatch("french", "blanc"));
            System.out.println(foreignColors.getColorSwatch("french", "bleu"));
		} 
		catch (Throwable e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
