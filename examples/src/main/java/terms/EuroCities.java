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

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Displays European cities from a list of maga cites spanning 5 continents,
The "Continent" term is not exported by placing a ',' at the start of the line.
*/
public class EuroCities
{
    private QueryProgramParser queryProgramParser;
 
    public EuroCities()
    {
        queryProgramParser = 
        	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles high_cities.taq and runs the "high_city" query
     */
    public Iterator<Axiom> findHighCities() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("terms/euro_cities.taq");
        Result result = queryProgram.executeQuery("euro_megacities");
        return result.axiomIterator("euro_megacities");
    }

	/**
     * Displays the solution to the high_cities query on the console.<br/>
     * The expected result:<br/>
megacities(Megacity=Moscow, Country=Russia)<br/>
megacities(Megacity=London, Country=United Kingdom)<br/>
megacities(Megacity=Istanbul, Country=Turkey)<br/>
megacities(Megacity=Rhine-Ruhr, Country=Germany)<br/>
megacities(Megacity=Paris, Country=France)<br/>
     */
	public static void main(String[] args)
	{
		try 
		{
	        EuroCities euroCities = new EuroCities();
	        Iterator<Axiom> iterator = euroCities.findHighCities();
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
