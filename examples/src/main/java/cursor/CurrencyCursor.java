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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates cursor used to iterate through a string list containing amounts in Euros, 
and currency list used to receive the amounts as decimal values. The cursor is
created by a function invoked as a list attribute. The cursor points initially to
the first item in the list and advances when the increment ++ postfix operator
is applied. When the end of the list is reached, the cursor becomes empty, which
is detected using the "fact" attribute of the cursor. The currency list grows
using concatenation and the list variable retains the last item appended to 
the list, which in this case, allows a total amount to be calculated using
 a simple expression.
 */
public class CurrencyCursor
{
    static class Amounts
    {
        public Iterator<BigDecimal> items;
        public String total;
    }
    
    private QueryProgramParser queryProgramParser;

    public CurrencyCursor()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles the currency-cursor.taq script and runs the "parse_amounts" query.<br/>
     * The expected results:<br/>
        14567.89<br/>
        14197.52<br/>
        590<br/>
        total=29355.41<br/>
     * @return Amounts object
     */
    @SuppressWarnings("unchecked")
	public Amounts  amounts()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("cursor/currency-cursor.taq");
        Result result = queryProgram.executeQuery("parse_amounts");
        Axiom axiom = result.getAxiom("all_amounts.amounts");
        Amounts amounts = new Amounts();
        amounts.items = ((ArrayList<BigDecimal>) axiom.getTermByIndex(0).getValue()).iterator();
        amounts.total = axiom.getTermByIndex(1).toString();
        return amounts;
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            CurrencyCursor currencyCursor = new CurrencyCursor();
            Amounts amounts = currencyCursor.amounts();
            amounts.items.forEachRemaining(amount -> System.out.println(amount.toString()));
            System.out.println(amounts.total);
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
