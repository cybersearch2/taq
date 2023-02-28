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
package operations;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates a logic selection set. which contains one or more 
comma-delimited items to match on and enclosed in `{}` braces
 */
public class AmericanMegaCities 
{
    private QueryProgramParser queryProgramParser;
	 
    public AmericanMegaCities()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles american_megacities.taq  and runs the "american_megacities" query
     */
    public Iterator<Axiom> findAmericanMegaCities() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("operations/american_megacities.taq");
        Result result = queryProgram.executeQuery("american_megacities");
        return result.axiomIterator("american_megacities");
    }

	/**
	 * Displays the american_megacities solution on the console.<br/>
	 */
    public static void main(String[] args)
    {
        try 
        {
            AmericanMegaCities americanMegaCities = new AmericanMegaCities();
            Iterator<Axiom> iterator = americanMegaCities.findAmericanMegaCities();
            while (iterator.hasNext())
                System.out.println(iterator.next().toString());
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
