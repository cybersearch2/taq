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
Demonstrates how to return an axiom list from a function 
call to a template which only has fixed return type of term list. Function "school.report()" 
converts 3 numeric scores to an axiom list containing subject + alpha mark records. 
A "subjects" term declares an axiom list which is exported by the "report" flow.
 */
public class ReceiveStudentScores2
{
    private QueryProgramParser queryProgramParser;

    public ReceiveStudentScores2()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles the query-student-scores.taq script and runs the "marks" query.<br/>
     * The expected results:<br/>
        student_marks(report=George: English:b+, Math:b-, History:a- )<br/>
        student_marks(report=Sarah: English:c+, Math:a, History:b+ )<br/>
        student_marks(report=Amy: English:b, Math:a-, History:e+ )<br/>
     * @return Axiom iterator
     */
    public Iterator<Axiom>  generateReport()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("receiver/receive-student-scores2.taq");
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
            ReceiveStudentScores2 schoolMarks = new ReceiveStudentScores2();
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
