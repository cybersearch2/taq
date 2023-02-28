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
package scopes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates usage of a scope property and how the absence of 
a property is handled. There are 3 queries which all display the surface area of both 
the USA and Australia. A "location" scope property is read to determine if the normal 
unit of area, square kilometers, should be changed to imperial square miles. The latter 
case applies if location = "United States", Comparing the "au_surface_area_query" for 
Australia to "us_surface_area_query" for USA in the console, the results are as expected.
 */
public class CalculateSquareMiles3 
{
    private QueryProgramParser queryProgramParser;

	public CalculateSquareMiles3()
	{
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
	}
	
	/**
	 * Display country surface areas
	 * The expected results:
	<code>
	Locale = australia
	country_area(country=Australia, surface_area=7741220.0, units=km2)
	country_area(country=United States, surface_area=9831510.0, units=km2)
	Locale = usa
	us_surface_area_query
	country_area(country=Australia, surface_area=2988885.042, units=mi2)
	country_area(country=United States, surface_area=3795946.011, units=mi2)
	Locale = global
	country_area(country=Australia, surface_area=7741220.0, units=km2)
	country_area(country=United States, surface_area=9831510.0, units=km2) 
	</code>
    */
	public List<String> displaySurfaceArea()
	{
	    List<String> resultList = new ArrayList<>(9);
        QueryProgram queryProgram = queryProgramParser.loadScript("scopes/calc-square-miles3.taq");
        resultList.add("Locale = australia");
		Result result = queryProgram.executeQuery("au_surface_area_query");
		Iterator<Axiom> iterator = result.axiomIterator("au_surface_area_query");
        while(iterator.hasNext())
            resultList.add(iterator.next().toString());
        resultList.add("Locale = usa");
        resultList.add("us_surface_area_query");
        result = queryProgram.executeQuery("us_surface_area_query");
		iterator = result.axiomIterator("us_surface_area_query");
        while(iterator.hasNext())
            resultList.add(iterator.next().toString());
        resultList.add("Locale = global");
        result = queryProgram.executeQuery("xx_surface_area_query");
        iterator = result.axiomIterator("xx_surface_area_query");
        while(iterator.hasNext())
            resultList.add(iterator.next().toString());
        return resultList;
	}

	public static void main(String[] args)
	{
		CalculateSquareMiles3 calculateSquareMiles = new CalculateSquareMiles3();
		try 
		{
		    Iterator<String> iterator = calculateSquareMiles.displaySurfaceArea().iterator();
	        while(iterator.hasNext())
	            System.out.println(iterator.next());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
