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
package select;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Shows selection default strategy of simply skipping over items that 
fail to match any of the available choices. This only works in a template as it relies 
on the fact a template solution is discarded if any terms are blank.
 */
public class PerfectMatch
{
    private QueryProgramParser queryProgramParser;

    public PerfectMatch()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles perfect-match.taq and runs the "star_people" query which gives each person 
     * over the age of 20 an age rating and those not rated are omitted.<br/>
     * @return Axiom iterator
     */
    public Iterator<Axiom> getAgeRating()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("select/perfect-match.taq");
        Result result = queryProgram.executeQuery("star_people");
        return result.axiomIterator("star_people");
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            PerfectMatch perfectMatch = new PerfectMatch();
            Iterator<Axiom> iterator = perfectMatch.getAgeRating();
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
