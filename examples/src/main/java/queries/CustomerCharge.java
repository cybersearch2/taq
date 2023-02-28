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

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.query.Solution;
import utils.ResourceHelper;

/**
Demonstrates a cascading query used to aggregate data from 2 different 
tables organized as axiom lists. The first, named "shipping", contains freight charged 
for delivery to a particular city and the second, named "customer", contains customer 
details. The "customer_delivery" query returns for each customer, name, city, and freight 
charge.
 */
public class CustomerCharge implements SolutionHandler
{
    private QueryProgramParser queryProgramParser;


    public CustomerCharge()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
     }

	/**
	 * Compiles customer-charge.taq and runs the "customer_delivery" query, displaying the solution on the console.<br/>
	 * The expected "customer_freight" result:<br/>
	freight(charge=23.99, city=Athens)<br/>
	customer_freight(name=Acropolis Construction, city=Athens, charge=23.99)<br/>
	freight(charge=13.99, city=Sparta)<br/>
	customer_freight(name=Marathon Marble, city=Sparta, charge=13.99)<br/>
	freight(charge=13.99, city=Sparta)<br/>
	customer_freight(name=Agora Imports, city=Sparta, charge=13.99)<br/>
	freight(charge=17.99, city=Milos)<br/>
	customer_freight(name=Spiros Theodolites, city=Milos, charge=17.99)<br/>
	 */
	public void displayCustomerCharges(SolutionHandler solutionHandler)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("queries/customer-charge.taq");
		queryProgram.executeQuery("customer_delivery", solutionHandler);
	}

    /**
     * onSolution - Print solution of both templates
     * @see au.com.cybersearch2.taq.interfaces.SolutionHandler#onSolution(au.com.cybersearch2.taq.query.Solution)
     */
    @Override
    public boolean onSolution(Solution solution) 
    {
        System.out.println(solution.getAxiom("freight").toString());
        System.out.println(solution.getAxiom("customer_freight").toString());
        return true;
    }

	public static void main(String[] args)
	{
		try 
		{
		    CustomerCharge greekConstruction = new CustomerCharge();
			greekConstruction.displayCustomerCharges(greekConstruction);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
