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
package functions;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates a function which returns an axiom list declared as an 
"alpha_grades" template archetype. Each axiom in this list has a "subject" term and 
a "mark" term. Both values are derived from an integer term with a subject name and 
in this example there are 3 subjects passed as function parameters - english, maths 
and history.
*/
public class FunctionStudentScores
{
    private QueryProgramParser queryProgramParser;

    public FunctionStudentScores()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles the function-student-score script and runs the "marks" query.<br/>
     * The expected results:<br/>
    student_marks(report=George english:b+ maths:b- history:a-):<br/>
    student_marks(report=Sarah english:c+ maths:a history:b+):<br/>
    student_marks(report=Amy english:b maths:a- history:e+):<br/>
     * @return Axiom iterator
     */
    public Iterator<Axiom>  generateReport()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("functions/function-student-scores.taq");
        Result result = queryProgram.executeQuery("marks");
        return result.axiomIterator("score.student_marks");
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args) {
        try {
            FunctionStudentScores schoolMarks = new FunctionStudentScores();
            Iterator<Axiom> iterator = schoolMarks.generateReport();
            while(iterator.hasNext()) {
                System.out.println(iterator.next().toString());
            }
        } 
		catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
