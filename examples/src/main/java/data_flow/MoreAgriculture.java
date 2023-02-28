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
Produces a list of countries which have increased the area
under agriculture by more than 1% over the twenty years between 1990 and 2010.  
*/
public class MoreAgriculture 
{
    private QueryProgramParser queryProgramParser;
   
    public MoreAgriculture()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
     }

    /**
     * Compiles the more_agriculture.taq script and runs the "more_agriculture" query
     * @return Axiom iterator
     */
    public Iterator<Axiom> findIncreasedAgriculture() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("data_flow/more_agriculture.taq");
        Result result = queryProgram.executeQuery("more_agriculture");
        return result.axiomIterator("more_agriculture");
    }

	/**
     * Displays the solution on the console.<br/>
	 * The expected result first 3 lines:<br/>
	surface_area_increase(country=Albania, surface_area=986)<br/>
	surface_area_increase(country=Algeria, surface_area=25722)<br/>
	surface_area_increase(country=American Samoa, surface_area=10)<br/>
 	 */
    public static void main(String[] args)
    {
        try 
        {
            MoreAgriculture moreAgriculture = new MoreAgriculture();
            Iterator<Axiom> iterator = moreAgriculture.findIncreasedAgriculture();
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
