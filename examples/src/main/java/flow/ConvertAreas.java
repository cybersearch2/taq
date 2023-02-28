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
package flow;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Uses a flow to convert surface area in square miles to square kilometers if unit is "mi2"
*/
public class ConvertAreas {
    private QueryProgramParser queryProgramParser;

    public ConvertAreas()
	{	
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
	}
	
    /**
     * Compiles the convert_mi2.taq script and runs the "surface_area" query, displaying the solution on the console.<br/>
     * The countries Australia and New Zealand will have their areas reported in squer kilometers.<br/>
     * The expected results include:<br/>
     * <code>
		convert_area(country=Antigua and Barbuda, area=440)
		convert_area(country=Australia, area=7741212)
		convert_area(country=Bahamas, area=13880)
		...
		convert_area(country=Namibia, area=824290)
		convert_area(country=New Zealand, area=267710)
		convert_area(country=Pakistan, area=796100)
		...
		</code>
      */
	public Iterator<Axiom> getSurfaceAreas()
	{
		String query = "convert_areas";
        QueryProgram queryProgram = queryProgramParser.loadScript("flow/convert_mi2.taq");
        Result result = queryProgram.executeQuery(query);
        return result.axiomIterator(query);
	}

	public static void main(String[] args)
	{
		try 
		{
			ConvertAreas convertAreas = new ConvertAreas();
			Iterator<Axiom> countryIterator = convertAreas.getSurfaceAreas();
	        while(countryIterator.hasNext())
	            System.out.println(countryIterator.next().toString());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
