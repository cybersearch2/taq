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
import java.util.Collections;
import java.util.List;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.operator.StringOperator;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.QueryExecutionException;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates using an axiom list in a scope to apply locale-specific 
data to a query. In addition, each scope has a query for just itself. 
*/
public class EuroTotal2 
{
    protected QueryProgramParser queryProgramParser;

    public EuroTotal2()
    {
        queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getResourcePath());
    }
    
	/**
	 * Compiles euro-total2.taq and runs the "item_query" query for the given scope, 
	 * displaying the solution on the console>
	 */
	public List<String> getFormatedTotalAmount(String scope)
	{
    Result result;
        result = getResult(scope);
        if ("global".equals(scope)) {
        	List<String> list = new ArrayList<>();
        	list.add(getAxiom("french", result));
        	list.add(getAxiom("german", result));
        	list.add(getAxiom("belgium_fr", result));
        	return list;
        } else
	        return Collections.singletonList(getAxiom(scope, result));
	}
	
    private String getAxiom(String scope, Result result) {
    	return result.getAxiom(scope, "item_query").getTermByName("total_text").getValue().toString();
	}

	/**
     * Run tutorial
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        EuroTotal2 euroTotal2 = new EuroTotal2();
	        euroTotal2.getFormatedTotalAmount("french").forEach(total -> System.out.println(total));
	        euroTotal2.getFormatedTotalAmount("german").forEach(total -> System.out.println(total));
	        euroTotal2.getFormatedTotalAmount("belgium_fr").forEach(total -> System.out.println(total));
	        System.out.println("\nglobal query\n");
	        euroTotal2.getFormatedTotalAmount("global").forEach(total -> System.out.println(total));

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

	/**
	 * Executes the "item_query" query for all scopes
	 * @return Result object
	 */
	Result getResult()
	{
		return getResult("global");
	}

	/**
	 * Executes the "item_query" query 
	 * @return Result object
	 */
	Result getResult(String scope)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("locales/euro-total2.taq");
		QueryParams queryParams = queryProgram.getQueryParams(scope, "item_query");
        Solution initialSolution = queryParams.getInitialSolution();
        Axiom parameter2 = new Axiom("amount", new OperatorTerm("amount", "12.345,67 €", new StringOperator()));
        initialSolution.put("amount", parameter2);
        return queryProgram.executeQuery(queryParams);
	}
}
