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
Demonstrates a function returning an axiom list that is 
passed to an attached receiver termplate . The name given to the list is the same as 
that of the function. The axiom list content is declared as an archetype declaration.
 */
public class ReceiveStudentScores
{
    private QueryProgramParser queryProgramParser;

    public ReceiveStudentScores()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles receive-student-scores.taq and runs the "marks" query.<br/>
     * The expected results:<br/>
        student_marks(student = Amy, total = 36)<br/>
        student_marks(student = George, total = 44)<br/>
        student_marks(student = Sarah, total = 43)<br/>
     * @return Axiom iterator
     */
    public Iterator<Axiom>  generateReport()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("receiver/receive-student-scores.taq");
        Result result = queryProgram.executeQuery("marks");
        return result.axiomIterator("score.student_marks");
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            ReceiveStudentScores schoolMarks = new ReceiveStudentScores();
            Iterator<Axiom> iterator = schoolMarks.generateReport();
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
