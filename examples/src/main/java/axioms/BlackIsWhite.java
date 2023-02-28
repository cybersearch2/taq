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
package axioms;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
In this example color "black" is turned to "white" The 
color is specified by both name and red-green-blue components. 
 */
public class BlackIsWhite 
{
    private QueryProgramParser queryProgramParser;

    public BlackIsWhite()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }
    
	/**
	 * Compiles the black-is-white.taq script and runs the "color_query" query, displaying the solution on the console.
	 * @return Axiom iterator
     */
	public Axiom displayShade(String query)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("axioms/black-is-white.taq");
        Result result = queryProgram.executeQuery(query);
        Axiom axiom = result.getAxiom(query);
        if (axiom.getTermCount() == 1) {
        	Object object = axiom.getTermByIndex(0).getValue();
        	if (object instanceof AxiomTermList) {
        	    AxiomTermList axiomTermList = (AxiomTermList)object; 
        	    axiom = axiomTermList.getAxiom();
        	    axiom = new Axiom(query, axiom);
        	}
        }
        return axiom;
	}

	public static void main(String[] args)
	{
		try 
		{
	        BlackIsWhite blackIsWhite = new BlackIsWhite();
	        System.out.println(blackIsWhite.displayShade("color_query").toString());
	        System.out.println(blackIsWhite.displayShade("dyna_query").toString());
	        System.out.println(blackIsWhite.displayShade("list_query").toString());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
