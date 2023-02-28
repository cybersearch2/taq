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
package flow;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates  one loop nested inside another. he inner loop does an insert sort 
while the outer loop advances to the next axiom term to be sorted. 
*/
public class NestedLoops 
{
    private QueryProgramParser queryProgramParser;
 
    public NestedLoops()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

/**
	 * Compiles the nested-loops.taq script and runs the "sort_axiom" query, displaying the solution on the console.<br/>
	 * Demonstrates one loop nested inside another. The inner loop does an insert sort 
	 * while the outer loop advances to the next axiom term to be sorted.
	 * The expected result:<br/>
	 * 1,3,5,8,12
	 */
	public Axiom displayAxiomSort()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("flow/nested-loops.taq");
 		Result result = queryProgram.executeQuery("sort_axiom");
		return result.getAxiom("insert_sort.sorted");
	}

	public static void main(String[] args)
	{
		NestedLoops nestedLoops = new NestedLoops();
		try 
		{
			Axiom axiom = nestedLoops.displayAxiomSort();
			System.out.println(axiom.toString());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

}
