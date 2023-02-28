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

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates a sequence of records being processed by a template 
in several scopes, This is achieved using a query chain where the final link
aggregates the output from the preceding links.
 */
public class ScopeChain {

    private QueryProgramParser queryProgramParser;

    public ScopeChain()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

	/**
	 * Compiles the foreign-scope3.xpl script and runs the "item_query" query, displaying the solution on the console.<br/>
	 * @return AxiomTermList iterator containing the final Calculator solution
	 */
    public List<Axiom> getFormatedTotalAmount()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("locales/scope-chain.taq");
        List<Axiom> solutionList = new ArrayList<Axiom>();
        Result result = queryProgram.executeQuery("item_query");
        result.axiomIterator("item_query").forEachRemaining(axiom -> solutionList.add(axiom));
        return solutionList;
	}
	
    /**
     * Run example
    * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        ScopeChain scopeChain = new ScopeChain();
	        List<Axiom> solutionList = scopeChain.getFormatedTotalAmount();
	        for (Axiom axiom: solutionList)
	        {
	        	for (int i = 0; i < axiom.getTermCount(); ++i) {
	                Term term  = axiom.getTermByIndex(i);
	                if (i == 0) {
	                	System.out.println(axiom.getName() + "(");
	                	System.out.println((" " + term.getValue().toString()) + ",");
	                } else
	                	System.out.println((" " + term.getValue().toString()));
	        	}
	        	System.out.println(")");
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
