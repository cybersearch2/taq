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
package archetypes;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
demonstrates unification involving pairing of terms with literal 
values. There are 3 queries to find who has completed a course of vacination jabs for a 
the latest pandemic. Each query selects a different axiom list containing vacination 
records. The axiom terms are named "name" and "is_vaxxed". The twist is that the format 
of the is_vaxxed term is different in each list.
 */
public class WhoIsVaxxed
{
    private QueryProgramParser queryProgramParser;

    public WhoIsVaxxed()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles the birds.taq script and runs the "birds" query, displaying the solution on the console.<br/>
     * @return Axiom iterator
  	 */
	public Iterator<Axiom> getWhoIsVAxxed(String query)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("archetypes/who-is-vaxxed.taq");
		Result result = queryProgram.executeQuery(query); 
		return result.axiomIterator(query);
	}

    public static void main(String[] args)
	{
		try 
		{
		    WhoIsVaxxed whoIsVaxxed = new WhoIsVaxxed();
	        Iterator<Axiom> iterator = whoIsVaxxed.getWhoIsVAxxed("vaxxed_contacts");
	        iterator.forEachRemaining(item -> System.out.println(item.toString()));
	        iterator = whoIsVaxxed.getWhoIsVAxxed("vaxxed_patients");
	        iterator.forEachRemaining(item -> System.out.println(item.toString()));
	        iterator = whoIsVaxxed.getWhoIsVAxxed("vaxxed_staff");
	        iterator.forEachRemaining(item -> System.out.println(item.toString()));
        }
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
