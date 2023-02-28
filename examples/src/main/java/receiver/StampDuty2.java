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
package receiver;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates a select receiver template used with a skip on default 
strategy. The "stamp_duty_query" query calculates stamp duty, a form of tax applying 
to real estate, according to sale amount and the bracket it falls into. There is a 
threshold below which a flat rate applies and no calculation is required. 
 */
public class StampDuty2 
{
    private QueryProgramParser queryProgramParser;

    public StampDuty2()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles stamp-duty2.taq and runs the "stamp_duty_query" query. 
	 * Choice named "bracket" here is a term of the "stamp_duty_payable"a calculator.
	 * Note how selection term "amount" is declared preceding the Choice so as to give it a specific type.<br/>
	 * The expected results <br/>
	stamp_duty_payable(100077, USD3,789.00, bracket=0, payable=USD20.00) <br/>
	stamp_duty_payable(100078, USD123,458.00, bracket=5, payable=USD3,768.32) <br/>
	stamp_duty_payable(100079, USD55,876.33, bracket=6, payable=USD1,285.67) <br/>
	stamp_duty_payable(100080, USD1,245,890.00, bracket=1, payable=USD62,353.95) <br/>
     * @return Axiom iterator
	 */
	public Iterator<Axiom> getStampDuty()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("receiver/stamp-duty2.taq");
        Result result = queryProgram.executeQuery("stamp_duty_query");
        return result.axiomIterator("stamp_duty_query");
	}
	
    /**
     * Run tutorial
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        StampDuty2 stampDuty = new StampDuty2();
            Iterator<Axiom> iterator = stampDuty.getStampDuty();
            while(iterator.hasNext())
            {
                System.out.println(iterator.next().toString());
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
