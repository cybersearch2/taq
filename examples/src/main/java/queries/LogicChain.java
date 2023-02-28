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
package queries;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.QueryExecutionException;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates a chained query used to aggregate data from 3 different 
tables organized as axiom lists.
 */
public class LogicChain 
{
    private QueryProgramParser queryProgramParser;

    public LogicChain()
    {
        queryProgramParser = 
            	new QueryProgramParser(ResourceHelper.getResourcePath());
    }
    
    /**
	 * Compiles the logic-chain.taq script and runs the "customer_charge" query, displaying the solution on the console.
	 * The expected result:<br/>
        delivery(name=Marathon Marble, city=Sparta, freight=16)
        delivery(name=Acropolis Construction, city=Athens, freight=5)
        delivery(name=Agora Imports, city=Sparta, freight=16)
        delivery(name=Spiros Theodolites, city=Milos, freight=22)
	 */
	public Iterator<Axiom> displayCustomerCharges()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("queries/logic-chain.taq");
 		Result result = queryProgram.executeQuery("greek_business");
 		return result.axiomIterator("greek_business");
	}

	public static void main(String[] args)
	{
		try 
		{
	        LogicChain greekConstruction = new LogicChain();
	        Iterator<Axiom> iterator = greekConstruction.displayCustomerCharges();
	        while (iterator.hasNext())
	            System.out.println(iterator.next().toString());
		} 
		catch (ExpressionException e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        catch (QueryExecutionException e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
		System.exit(0);
	}
}
