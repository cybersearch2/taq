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
package flow;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Calculates the factorial of numbers 4 and 5
*/
public class Factorial 
{
    private QueryProgramParser queryProgramParser;

    public Factorial()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }
    
    /**
	 * Compiles the factorial.taq script and runs the "factorial" query, displaying the solution on the console.<br/>
	 * Also shows use of query parameter to set a variable.<br/>
	 * The expected result:<br/>
	 * factorial(n = 4, i = 5, factorial = 24)<br/>
	 * factorial(n = 5, i = 6, factorial = 120)<br/>
	 */
	public Axiom[] displayFactorial4and5()
	{
	    Axiom[] factorials = new Axiom[2];
        QueryProgram queryProgram = queryProgramParser.loadScript("flow/factorial.taq");
		Result result = queryProgram.executeQuery("factorial4");
		factorials[0] = result.getAxiom("factorial4");
		result = queryProgram.executeQuery("factorial5");
        factorials[1] = result.getAxiom("factorial5");
        return factorials;
	}

	public static void main(String[] args)
	{
		Factorial factorial = new Factorial();
		try 
		{
		    Axiom[] factorials = factorial.displayFactorial4and5();
			System.out.println(factorials[0].toString());
            System.out.println(factorials[1].toString());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
