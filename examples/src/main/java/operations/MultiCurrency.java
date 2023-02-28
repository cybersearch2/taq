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
package operations;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Note: The query solution may not match the one recorded here because locales change over time.
Demonstrates use of currency type with world currencies.
The country is dynamically qualified each round of unification.
Like the SingleCurrency example, this performs a Goods and Services Tax calculation
and formats the resulting amount with currency code. The item price is represented
this time as a double literal with correct number of decimal places for the indicated for the currency.
The currency type applies the rounding recommended for financial transactions.
 */
public class MultiCurrency 
{
    private QueryProgramParser queryProgramParser;
    
    /**
	 * Construct MultiCurrency object
	 */
	public MultiCurrency() 
	{
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
	}

	/**
	 * Compiles the WORLD_CURRENCY script and runs the "price_query" query.<br/>
	 * The first 3 of 104 expected results:<br/>
	 MY Total + gst: MYR10,682.12<br/>
     QA Total + gst: QAR 545.81<br/>
     IS Total + gst: 510, ISK<br/>
	 * To view full expected result, see multi-currency.txt
	 * @return Axiom iterator
	 */
	public Iterator<Axiom> getFormatedAmounts()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("operations/multi-currency.taq");
		// Use this query to see the total amount before it is formatted
		// Note adjustment of decimal places to suite currency.
		Result result = queryProgram.executeQuery("price_query");
		return result.axiomIterator("price_query");
	}
	
    /**
     * Run tutorial
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        MultiCurrency multiCurrency = new MultiCurrency();
	        Iterator<Axiom> iterator = multiCurrency.getFormatedAmounts();
	        while(iterator.hasNext())
	        {
	            System.out.println(iterator.next().getTermByName("total_text").getValue().toString());
	        }
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
