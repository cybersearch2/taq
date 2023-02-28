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
package cursor;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates cursor state when pointing to an empty list
 */
public class EmptyList 
{
    private QueryProgramParser queryProgramParser;

    public EmptyList()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }
    
	/**
	 * Compiles the CITY_EVELATIONS script and runs the "high_city" query, displaying the solution on the console.<br/>
	 * The expected result:<br/>
       empty_list_demo(value=NaN, backward=NaN, inc=NaN, dec=NaN, index=-1, isFact=false)
  	 */
	public Axiom displayEmptyList()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("cursor/empty-list.taq");
		Result result = queryProgram.executeQuery("empty_list");
		return result.getAxiom("empty_list");
	}

	public static void main(String[] args)
	{
		EmptyList highCities = new EmptyList();
		try 
		{
	        Axiom axiom = highCities.displayEmptyList();
	        System.out.println(axiom.toString());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
