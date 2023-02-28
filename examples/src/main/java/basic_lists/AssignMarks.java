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
package basic_lists;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
In this example, a report of student marks is generated with the
assistance of a list with index range 1 to 18. */
public class AssignMarks 
{
    private QueryProgramParser queryProgramParser;

    public AssignMarks()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }
    
	/**
	 * Compiles the assign-marks.taq script and runs the "marks" query, displaying the solution on the console.<br/>
	 * Demonstrates a values list. See AxiomMarks for perhaps a better alternative to using a values list.
	 * The expected result:<br/>
        score(student=George, mark_english=b+, mark_maths=b-, mark_history=a-)<br/>
        score(student=Sarah, mark_english=c+, mark_maths=a, mark_history=b+)<br/>
        score(student=Amy, mark_english=b, mark_maths=a-, mark_history = e+)<br/>
	 */
	public Iterator<Axiom> displayLists()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("basic_lists/assign-marks.taq");
        Result result = queryProgram.executeQuery("marks");
        return result.axiomIterator("marks");
	}

	public static void main(String[] args)
	{
		try 
		{
	        AssignMarks listsDemo = new AssignMarks();
			Iterator<Axiom> iterator = listsDemo.displayLists();
			while (iterator.hasNext())
	            System.out.println(iterator.next().toString());
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
