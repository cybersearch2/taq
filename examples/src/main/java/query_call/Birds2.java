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

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates a query made by a function call operation. 
The "list_waterfowl" query belongs to the global scope and returns an axiom list containing distinguishing 
attributes of birds living in watery habitats. The "waterfowl" query calls the "list_waterfowl" query 
and converts each returned axiom into a list of strings skipping over blank terms representing attributes 
which are not relevant.
 */
public class Birds2
{
    private QueryProgramParser queryProgramParser;
 
    public Birds2()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles the birds.taq script and runs the "birds" query, displaying the solution on the console.<br/>
	 * The expected result:<br/>
waterfowl=whistling swan, family=swan, color=white, flight=ponderous, voice=muffled musical whistle<br/>
waterfowl=trumpeter swan, family=swan, color=white, flight=ponderous, voice=loud trumpeting<br/>
waterfowl=snow goose, family=goose, color=white, size=plump, flight=powerful, voice=honks<br/>
waterfowl=pintail, family=duck, flight=agile, voice=short whistle<br/>
        @return Axiom iterator
  	 */
	public Iterator<String> getBirds()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("query_call/birds2.taq");
        Result result = queryProgram.executeQuery("list_waterfowl"); 
		return result.stringIterator("list_waterfowl.species");
	}

    public static void main(String[] args)
	{
		try 
		{
	        Iterator<String> iterator = new Birds2().getBirds();
	        while(iterator.hasNext()) 
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
