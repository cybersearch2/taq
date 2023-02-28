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

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.operator.StringOperator;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
 Demonstrates a select receiver template with a map nested inside. 
The "charge_plus_tax" flow produces an invoice total which is local-specific. The 
"district" select sets locale variables "language"and "region". A receiver attached
to the select maps "region" to "percent" sales tax.
 */
public class ChargePlusTax 
{
    private static final String[] REGION_CODES  = {
	    "de", "fr", "be_fr", "be_nl"
    };
   
    private QueryProgramParser queryProgramParser;
 
    public ChargePlusTax()
    {
        queryProgramParser = 
           new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles charge-plus-tax.taq and runs an "item_query" query for a specific locale and returns a "euro_amount" axiom<br/>
	 * @return AxiomTermList iterator containing the final Calculator solution
	 */
    public Axiom getFormatedTotalAmount(String regionCode)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("receiver/charge-plus-tax.taq");
		// Create QueryParams object for  query "item_query"
		QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, regionCode + "_item_query");
        Solution initialSolution = queryParams.getInitialSolution();
        Axiom parameter2 = new Axiom("amount", new OperatorTerm("amount", "12.345,67 €", new StringOperator()));
        initialSolution.put("amount", parameter2);
        Result result = queryProgram.executeQuery(queryParams);
        return result.getAxiom("charge_plus_tax.euro_total");
	}
	
    /**
     * Run tutorial
     * The expected result:<br/>
euro_total(Total=Gesamtkosten 14.567,89 EUR, Tax=18% Steuer, Locale=de_DE)
euro_total(Total=le total 14 197,52 EUR, Tax=15% impôt, Locale=fr_FR)
euro_total(Total=le total 13.703,69 EUR, Tax=11% impôt, Locale=fr_BE)
euro_total(Total=totale kosten 13.703,69 EUR, Tax=11% belasting, Locale=nl_BE)
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        ChargePlusTax foreignScope = new ChargePlusTax();
	        List<Axiom> solutionList = new ArrayList<>();
	        for (String regionCode: REGION_CODES) 
	           solutionList.add(foreignScope.getFormatedTotalAmount(regionCode));
	        for (Axiom formatedTotal: solutionList)
	        	System.out.println(formatedTotal.toString());
		} 
		catch (Throwable e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
