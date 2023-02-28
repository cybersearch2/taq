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
package expressions;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Performs a sequence of arithmetic operations to demonstrate that TAQ 
follows Java notation and precedence in regard to those operations.
 */
public class Expressions 
{
    private QueryProgramParser queryProgramParser;
    
    public Expressions()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles expressions.taq and runs the "expressions" query
     */
    public Axiom checkExpressions() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("expressions/expressions.taq");
        Result result = queryProgram.executeQuery("expressions");
        return result.getAxiom("expressions");
    }

    /**
     * Displays expressions success summary flag on the console.
     * <br/>
     * The expected result:<br/>
        can_evaluate = true<br/>
	 */
    public static void main(String[] args)
    {
        try 
        {
            Expressions expressions = new Expressions();
            Axiom axiom = expressions.checkExpressions();
            Term evaluateTerm = axiom.getTermByName("can_evaluate");
            System.out.println(evaluateTerm.toString());
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
