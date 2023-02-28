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
package query_call;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.operator.BigDecimalOperator;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates making a query to a template using a function call operation. 
The "x_by_factor" template belongs to the "math" scope and is designed to take 2 decimal 
numbers, multiply one by the other and return a decimal result in a term named "product".
 */
public class Circumference
{
    private QueryProgramParser queryProgramParser;
 
    public Circumference()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
     }

    /**
     * Compiles circum.taq and runs the "circumference" query
     */
    public Axiom findCircum() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("query_call/circum.taq");
		QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "circumference");
        Solution initialSolution = queryParams.getInitialSolution();
        Axiom parameter = new Axiom("radius", new OperatorTerm("radius", "1.425", new BigDecimalOperator()));
        initialSolution.put("radius", parameter);
        Result result = queryProgram.executeQuery(queryParams);
        return result.getAxiom("circumference");
    }

	/**
     * Displays the solution to the circumference query on the console.<br/>
     * The expected result:<br/>
     * radius_by_2pi(circumference=8.94900)
     */
	public static void main(String[] args)
	{
		try 
		{
	        Circumference circumference = new Circumference();
	        System.out.println(circumference.findCircum().toString());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
