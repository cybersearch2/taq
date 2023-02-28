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
package select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates grouping using a **map** operation to route each item 
to a specific group list. This allows the grouping to be completed in a single pass 
through the incoming collection. The query result shows there are 5 different export 
lists, one for each continent - africa, asia, europe, north_america, south_america.
The **map** is qualified with the keyword **list** to indicate each mapping target 
is a list.
 */
public class DynamicGrouping 
{
    private QueryProgramParser queryProgramParser;
    
    public DynamicGrouping()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
     }

    /**
     * Compiles dynamic-grouping.taq  and runs the "mega_cities_by_continent" query
     */
    public Iterator<Axiom> findMegaCities() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("select/dynamic-grouping.taq");
        Result result = queryProgram.executeQuery("mega_cities_by_continent");
        List<Axiom> axiomList = new ArrayList<Axiom>();
        Iterator<Axiom> iterator = result.axiomIterator("group_cities.asia");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
        iterator = result.axiomIterator("group_cities.africa");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
        iterator = result.axiomIterator("group_cities.europe");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
        iterator = result.axiomIterator("group_cities.south_america");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
        iterator = result.axiomIterator("group_cities.north_america");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
       return axiomList.iterator();
    }

	/**
	 * Displays the asia_top_ten solution on the console.<br/>
	 */
    public static void main(String[] args)
    {
        try 
        {
            DynamicGrouping megaCities = new DynamicGrouping();
            Iterator<Axiom> iterator = megaCities.findMegaCities();
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
