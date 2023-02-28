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
package expressions;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates how variables declared in a scope behave from one query 
to the next. There are 3 declared scopes named, in order, "one", "two" and "three". 
Each of these scopes plus the global scope have an integer variable named "index" with
a chain of dependency starting with scope "one" depending on the global scope.
 */
public class ScopeCycles 
{
    private QueryProgramParser queryProgramParser;
    
    public ScopeCycles()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles scope-cycles.taq and runs the "first_time" and "second_time" queries
     */
    public List<Axiom> checkCycles() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("expressions/scope-cycles.taq");
        Result result = queryProgram.executeQuery("first_time");
        List<Axiom> resultList = new ArrayList<>();
        result.axiomIterator("first_time").forEachRemaining(item ->  resultList.add(item));
        result = queryProgram.executeQuery("second_time");
        result.axiomIterator("second_time").forEachRemaining(item ->  resultList.add(item));
        return resultList;
    }

    /**
     * Displays expressions success summary flag on the console.
     * <br/>
     * The expected result:<br/>
indexes(phase=1, one 1, two 2, three 3, four 4, random=nnn):<br/>
indexes(phase=2, one 1, two 2, three 3, four 7, random=nnn):<br/>
indexes(phase=1, one 1, two 2, three 3, four 4, random=mmm):<br/>
indexes(phase=2, one 1, two 2, three 3, four 7, random=mmm):<br/>
	 */
    public static void main(String[] args)
    {
        try 
        {
            ScopeCycles scopeCycles = new ScopeCycles();
            List<Axiom> resultList = scopeCycles.checkCycles();
            resultList.forEach(axiom -> System.out.println(axiom.toString()));
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
