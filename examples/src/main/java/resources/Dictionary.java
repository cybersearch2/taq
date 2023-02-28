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
package resources;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import utils.ResourceHelper;

/**
Demonstrates a bi-directional resource. The data source reads a file 
containing words starting with "i" and the data consumer sends the query result to 
the console. The "query_in_words" query uses a regular expression to select words 
starting with "in". 
 */
public class Dictionary 
{
    private QueryProgramParser queryProgramParser;
	
	public Dictionary()
	{
		ProviderManager providerManager = new ProviderManager();
		providerManager.setProperty("libraries", ResourceHelper.getLibrariesePath());
		providerManager.setProperty("resource_base", ResourceHelper.getResourcePath());
        queryProgramParser = 
            new QueryProgramParser(providerManager);
	}

    /**
     * Compiles query_in_words.taq and runs the "query_in_words" query.
     * The Lexicon resource provides both axiom source and export to console.
     */
    public void findInWords() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("resources/dictionary.taq");
        queryProgram.executeQuery("query_in_words");
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
            Dictionary dictionary = new Dictionary();
            dictionary.findInWords();
        } 
        catch (Throwable e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
