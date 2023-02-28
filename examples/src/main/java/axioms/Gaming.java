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
package axioms;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
In this example, fruit permutations are displayed unpredictably from
initialization parameters containing random numbers. 
 */
public class Gaming 
{
    private QueryProgramParser queryProgramParser;

    public Gaming()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
	 * Compiles the gaming.taq script and runs the "spin" query, displaying the solution on the console.<br/>
	 * The result will be an unpredictable three rows and four columns containing lemon, banana, apple and orange:
	 */
	public Iterator<Axiom> displayFruit()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("axioms/gaming.taq");
        Result result = queryProgram.executeQuery("gamble");
        return result.axiomIterator("gamble");
	}

	public static void main(String[] args)
	{
		try 
		{
	        Gaming gaming = new Gaming();
	        Iterator<Axiom> iterator = gaming.displayFruit();
	        while (iterator.hasNext())
	        {
	            Axiom axiom = iterator.next();
	            System.out.print(axiom.getTermByIndex(0).getValue().toString() + ", ");
                System.out.print(axiom.getTermByIndex(1).getValue().toString() + ", ");
                System.out.print(axiom.getTermByIndex(2).getValue().toString() + ", ");
                System.out.println(axiom.getTermByIndex(3).getValue().toString());
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
