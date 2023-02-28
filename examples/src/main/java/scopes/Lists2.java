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
package scopes;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Displays the contents and size of 7 context lists implemented in 2 different 
scopes  named "london" and "new york". The console out shown above is aggregated from 
each of 14 queries to show the entire list collection.
 */
public class Lists2 
{
	private static String[] LISTS = {
		"fruit",
		"dice",
		"dimensions",
		"roaches",
		"movies",
		"flags",
		"stars"
	};
	
    protected QueryProgramParser queryProgramParser;
    
    public Lists2()
    {
        queryProgramParser = 
           new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles lists2.taq and runs the query for each scope
     */
    public List<Axiom> checkLists(String city) 
    {
        List<Axiom> lists = new ArrayList<Axiom>();
        QueryProgram queryProgram = queryProgramParser.loadScript("scopes/lists2.taq");
        for (String list: LISTS) {
	        Result result = queryProgram.executeQuery(city + "_" + list);
	        lists.add(result.getAxiom(city + "_" + list));
        }
        return lists;
    }

    /**
     * Displays all the lists on the console.
     */
    public static void main(String[] args)
    {
        try 
        {
            Lists2 lists = new Lists2();
            System.out.println("London lists\n");
            List<Axiom> axiomList = lists.checkLists("london");
            displayLists(axiomList);
            System.out.println("\nNew York lists\n");
            axiomList = lists.checkLists("new_york");
            displayLists(axiomList);
        } 
        catch (Throwable e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

	private static void displayLists(List<Axiom> axiomList) {
        for (Axiom axiom: axiomList)
        {
        	String name = axiom.getName();
        	int pos = name.lastIndexOf('_');
            System.out.println("List: " + name.substring(pos + 1));
            for (int i = 0; i < axiom.getTermCount(); ++i)
            {
                Term term = axiom.getTermByIndex(i);
                System.out.println(term.toString());
            }
        }
	}
}
