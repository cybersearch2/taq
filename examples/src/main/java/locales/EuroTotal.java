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
package locales;

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
Demonstrates the role scopes can play in providing information about 
locale. Three queries are each assigned a specific locale to create a total 
amount invoice with language and regional adaptions.
 */
public class EuroTotal 
{
    private static final String[] REGION_CODES  = {
	    "de", "fr", "be_fr"
    };
   
    private QueryProgramParser queryProgramParser;
 
    public EuroTotal()
    {
        queryProgramParser = 
           new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles euro-totaltaq and runs the "item_query" query for given region code,<br/> 
	 * displaying the solution on the console.<br/>
	 * @return AxiomTermList iterator containing the final Calculator solution
	 */
    public List<Axiom> getFormatedTotalAmount(String regionCode)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("locales/euro-total.taq");
		// Create QueryParams object for  query "item_query"
		QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, regionCode + "_item_query");
        Solution initialSolution = queryParams.getInitialSolution();
        Axiom parameter2 = new Axiom("amount", new OperatorTerm("amount", "12.345,67 €", new StringOperator()));
        initialSolution.put("amount", parameter2);
        List<Axiom> solutionList = new ArrayList<Axiom>();
        Result result = queryProgram.executeQuery(queryParams);
        result.axiomIterator("totals").forEachRemaining(axiom -> solutionList.add(axiom));
        return solutionList;
	}
	
    /**
     * Run tutorial
     * The expected result:<br/>
totals(Total=Gesamtkosten 14.567,89 EUR, Tax=18% Steuer, Locale=de_DE)<br/>
totals(Total=le total 14 197,52 EUR, Tax=15% impôt, Locale=fr_FR)<br/>
totals(Total=le total 13.703,69 EUR, Tax=11% impôt, Locale=fr_BE)<br/>
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        EuroTotal foreignScope = new EuroTotal();
	        List<Axiom> solutionList = new ArrayList<>();
	        for (String regionCode: REGION_CODES) 
	           solutionList.addAll(foreignScope.getFormatedTotalAmount(regionCode));
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
