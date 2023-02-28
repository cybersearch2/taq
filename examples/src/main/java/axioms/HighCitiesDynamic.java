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
high-cities-dynamic.taq demonstrates exporting an axiom list as a means to returning 
a query result
 */
public class HighCitiesDynamic
{
    private QueryProgramParser queryProgramParser;

    public HighCitiesDynamic()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles the high-cities-dynamic.taq script and runs the "high_city" query, displaying the solution on the console.<br/>
	 * The expected result:<br/>
     * high_city(name = addis ababa, altitude = 8000)<br/>
     * high_city(name = denver, altitude = 5280)<br/>
	 * high_city(name = flagstaff, altitude = 6970)<br/>
	 * high_city(name = leadville, altitude = 10200)<br/>
	 */
	public Iterator<Axiom> getHighCities()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("axioms/high-cities-dynamic.taq");
		Result result = queryProgram.executeQuery("cities"); 
		return result.axiomIterator("high_city.high_cities");
	}

    public static void main(String[] args)
	{
		try 
		{
		    HighCitiesDynamic highCities = new HighCitiesDynamic();
	        Iterator<Axiom> iterator = highCities.getHighCities();
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
