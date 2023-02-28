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
Selects the ten top Asian cities from a list of 30 highly populated 
cities from 5 continents 
*/
public class AsiaTopTen 
{
    private QueryProgramParser queryProgramParser;
    
    public AsiaTopTen()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
     }

    /**
     * Compiles the asia_top_ten.taq script and runs the "asia_top_ten" query
     */
    public Iterator<Axiom> findMegaCities() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("data_flow/asia_top_ten.taq");
        Result result = queryProgram.executeQuery("asia_top_ten");
        return result.axiomIterator("asia_top_ten");
    }

	/**
	 * Displays the asia_top_ten solution on the console.<br/>
	 * The expected result:<br/>
        asia_top_ten(city=Tokyo, country=Japan, population=37,900,000)<br/>
        asia_top_ten(city=Delhi, country=India, population=26,580,000)<br/>
        asia_top_ten(city=Seoul, country=South,Korea, population=26,100,000)<br/>
        asia_top_ten(city=Shanghai, country=China, population=25,400,000)<br/>
        asia_top_ten(city=Mumbai, country=India, population=23,920,000)<br/>
        asia_top_ten(city=Beijing, country=China, population=21,650,000)<br/>
        asia_top_ten(city=Jakarta, country=Indonesia, population=20,500,000)<br/>
        asia_top_ten(city=Karachi, country=Pakistan, population=20,290,000)<br/>
        asia_top_ten(city=Osaka, country=Japan, population=20,260,000)<br/>
        asia_top_ten(city=Manila, country=Philippines, population=20,040,000)<br/>
	 */
    public static void main(String[] args)
    {
        try 
        {
            AsiaTopTen asiaTopTen = new AsiaTopTen();
            Iterator<Axiom> iterator = asiaTopTen.findMegaCities();
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
