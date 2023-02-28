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
package terms;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Shows the result of unifying a template containing 6 untyped variables with 
an axiom containing literal terms of 6 different types. The "types" query returns a 
single axiom with terms that reflect the original literal values.
 */
public class Types 
{
    private QueryProgramParser queryProgramParser;
     
    public Types()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles  types.taq and runs the "types" query
     */
    public Axiom checkTypes() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("terms/types.taq");
        Result result = queryProgram.executeQuery("types");
        return result.getAxiom("types");
    }

    /**
     * Displays types solution on the console.
     * The expected result:<br/>
        Boolean=true(Boolean)<br/>
        String=penguins(String)<br/>
        Integer=12345(Long)<br/>
        Double=123400.0(Double)<br/>
        Decimal=1234.56(BigDecimal)<br/>
        Currency=12345.67(BigDecimal)
      */
    public static void main(String[] args)
    {
        try 
        {
            Types types = new Types();
            Axiom axiom = types.checkTypes();
            for (int i = 0; i < axiom.getTermCount(); ++i)
            {
                Term term = axiom.getTermByIndex(i);
                System.out.println(term.toString() + "(" + term.getValue().getClass().getSimpleName() + ")");
            }
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
