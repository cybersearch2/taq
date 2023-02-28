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
package operations;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Shows regular expression selecting from a list, words starting with "in"
*/
public class QueryInWords 
{
    private QueryProgramParser queryProgramParser;
	
	public QueryInWords()
	{
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
	}

    /**
     * Compiles query_in_words.taq and runs the "query_in_words" query.
     * @return Axiom iterator (for testing only)
     */
    public Iterator<Axiom> findInWords() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("operations/query_in_words.taq");
        Result result = queryProgram.executeQuery("query_in_words");
        return result.axiomIterator("query_in_words");
    }

	/*
     * Expected 54 results. 
     * Here is the first result: </br>
     * in_words(word=inadequate, definition=j. not sufficient to meet a need)</br>
	 */
    public static void main(String[] args)
    {
        try 
        {
            QueryInWords queryInWords = new QueryInWords();
            Iterator<Axiom> iterator = queryInWords.findInWords();
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
