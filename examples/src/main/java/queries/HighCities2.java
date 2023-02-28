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
package queries;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Displays cities are at 5,000 feet or higher from a list of cities with their elevations,
The elevation threshold is set as a query parameter.
*/
public class HighCities2 implements SolutionHandler
{
    private QueryProgramParser queryProgramParser;
 
    public HighCities2()
    {
        queryProgramParser = 
        	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles high_cities.taq and runs the "high_city" query
     */
    public Iterator<Axiom> findHighCities() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("queries/high_cities2.taq");
        Result result = queryProgram.executeQuery("high_cities");
        return result.axiomIterator("high_cities");
    }

	/**
	 * Compiles high_cities.taq and runs the "high_cities" query
	 */
	public void findHighCities(SolutionHandler solutionHandler) 
	{
		QueryProgram queryProgram = queryProgramParser.loadScript("queries/high_cities2.taq");
		queryProgram.executeQuery("high_cities", solutionHandler);
	}

	/**
	 * onSolution - Handler for alternative query solution collection
	 * @see au.com.cybersearch2.taq.interfaces.SolutionHandler#onSolution(au.com.cybersearch2.taq.query.Solution)
	 */
    @Override
    public boolean onSolution(Solution solution) 
    {
        System.out.println(solution.getAxiom("high_city").toString());
        // Return false if you want to terminate query when a particular solution has been found
        return true;
    }
    
	/**
     * Displays the solution to the high_cities query on the console.<br/>
     * The expected result:<br/>
     * high_city(name = addis ababa, altitude = 8000)<br/>
     * high_city(name = denver, altitude = 5280)<br/>
     * high_city(name = flagstaff, altitude = 6970)<br/>
     * high_city(name = leadville, altitude = 10200)<br/>	
     */
	public static void main(String[] args)
	{
		try 
		{
	        HighCities2 highCities2 = new HighCities2();
	        Iterator<Axiom> iterator = highCities2.findHighCities();
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
