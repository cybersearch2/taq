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
package data_flow;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Groups large cities of the world by continent
*/
public class Grouping 
{
    private QueryProgramParser queryProgramParser;
    
    public Grouping()
    {
        queryProgramParser = 
            	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles the grouping.taq script and runs the "mega_cities_by_continent" query
     */
    public Iterator<Axiom> findMegaCities() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("data_flow/grouping.taq");
        Result result = queryProgram.executeQuery("mega_cities_by_continent");
        return result.axiomIterator("mega_cities_by_continent");
    }

	/**
	 * Displays the asia_top_ten solution on the console.<br/>
	 * The first 3 expected result:<br/>
        group_by_continent(continent=Asia, city=Tokyo, country=Japan, rank=1, population=37,900,000)<br/>
        group_by_continent(continent=Asia, city=Delhi, country=India, rank=2, population=26,580,000)<br/>
        group_by_continent(continent=Asia, city=Seoul, country=South,Korea, rank=3, population=26,100,000)<br/>	 
     */
    public static void main(String[] args)
    {
        try 
        {
            Grouping megaCities = new Grouping();
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
