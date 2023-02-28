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
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
In this example, data on birds is segmented into categories "order", "family" and "species".
This data is incomplete as not all attributes apply to any particular bird. 
 */
public class Birds
{
    private QueryProgramParser queryProgramParser;

    public Birds()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles the birds.taq script and runs the "birds" query, displaying the solution on the console.<br/>
     * @return Axiom iterator
  	 */
	public Iterator<Axiom> getBirds()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("archetypes/birds.taq");
		Result result = queryProgram.executeQuery("waterfowl"); 
		return result.axiomIterator("waterfowl");
	}

    public static void main(String[] args)
	{
		try 
		{
		    Birds birds = new Birds();
	        Iterator<Axiom> iterator = birds.getBirds();
	        while(iterator.hasNext()) {
	        	Axiom axiom = iterator.next();
	        	StringBuilder builder = new StringBuilder();
	        	for (int i = 0; i < axiom.getTermCount(); ++i) {
	        		Term term = axiom.getTermByIndex(i);
	        		if (!term.getValue().toString().isEmpty()) {
		        		if (i > 0)
		        			builder.append(", ");
		        		builder.append(term.toString());
	        		}
	        	}
	            System.out.println(builder.toString());
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
