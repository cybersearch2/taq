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

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates using a context list to perform language translation. 
This is a simple case of translating the word "total" into French 
"le total" or German "Gesamtkosten".
 */
public class ForeignTotal 
{
    private QueryProgramParser queryProgramParser;

    public ForeignTotal()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }


	/**
	 * Returns total for given country
	 * @return Axiom object
	 */
    public Axiom getTotalAmount(String country)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("scopes/foreign-total.taq");
		// Create QueryParams object for scope "german" and query "item_query"
		QueryParams queryParams = queryProgram.getQueryParams(country, "item_query");
		// Add an item Axiom with a single "12.345,67 EUR" term
		// This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("item", new Axiom("item", new Parameter("amount", "12.345,67 €")));
        Result result = queryProgram.executeQuery(queryParams);
        return result.getAxiom(country, "item_query");
	}
	
    /**
	 * Compiles foreign-total.taq and runs the "item_query" query  for 
	 * each country, displaying the solution on the console.<br/>
     * The expected result:<br/>
	format_total(total_text=Gesamtkosten + tax: 13.580,24 EUR):<br/>
	:<br/>
	format_total(total_text=le total + tax: 13 580,24 EUR):<br/>
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
            ForeignTotal foreignTotal = new ForeignTotal();
            System.out.println(foreignTotal.getTotalAmount("german").toString() + "\n");
            System.out.println(foreignTotal.getTotalAmount("french").toString());
		} 
		catch (Throwable e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
