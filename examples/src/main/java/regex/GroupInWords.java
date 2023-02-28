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
package regex;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Shows regular expression with grouping. Selects from a list, words starting with "in"
using grouping to extract the description from the definition
*/

public class GroupInWords {
    private QueryProgramParser queryProgramParser;
	
	public GroupInWords()
	{
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
	}

    /**
     * Compiles the query_in_words.taq script and runs the "query_in_words" query.
     * The Lexicon resource provides both axiom source and export to console.
     * @return Axiom iterator (for testing only)
     */
    public Iterator<Axiom> findInWords() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("regex/group_in_words.taq");
        Result result = queryProgram.executeQuery("query_in_words");
        return result.axiomIterator("query_in_words");
    }

	/*
     * Expected 54 results. 
     * Here is the first result: </br>
     * in_words(word=inadequate, is=not sufficient to meet a need)
	 */
    public static void main(String[] args)
    {
        try 
        {
        	GroupInWords inWords = new GroupInWords();
            Iterator<Axiom> iterator = inWords.findInWords();
            while (iterator.hasNext())
            	System.out.println(iterator.next());
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
	}
}
