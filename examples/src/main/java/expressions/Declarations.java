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
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Reveals important details about variable declarations made in what is called the 
"template scope". This is optionally placed before the template body. enclosed 
in `{}` braces and contains declarations that are private to the template.
 */
public class Declarations 
{
    private QueryProgramParser queryProgramParser;
    
    public Declarations()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles declarations.taq and runs the "query_sample" query
     */
    public Axiom checkDeclarations() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("expressions/declarations.taq");
        Result result = queryProgram.executeQuery("query_sample");
        return result.axiomIterator("query_sample").next();
    }

    /**
     * Displays query result on the console.
     * <br/>
     * The expected result:<br/>
        sample(d=1234.56)<br/>
	 */
    public static void main(String[] args)
    {
        try 
        {
            Declarations expressions = new Declarations();
            Axiom axiom = expressions.checkDeclarations();
            //Term evaluateTerm = axiom.getTermByName("can_evaluate");
            System.out.println(axiom.toString());
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
