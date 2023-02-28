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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.operator.StringOperator;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Displays set of personality traits associated with a person's zodiac sign
Has an example of a cursor declaration with the same name as the list to which 
it is bound */
public class Personalities2
{
	public static class Profile {
		
		public String name;
		public long age;
		public String starSign;
		public String traits;
	}
	
    private QueryProgramParser queryProgramParser;

    public Personalities2()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Returns query solution
     * @return Axiom iterator
     */
    public Iterator<Axiom> findZodiacPerson(String name)
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("basic_lists/personalities2.taq");
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        QueryParams queryParams = queryProgram.getQueryParams("person_search");
        // Add a shade Axiom with a specified color term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("person", new Axiom("person", new OperatorTerm("search_name", name, new StringOperator())));
        Result result = queryProgram.executeQuery(queryParams);
        return result.axiomIterator("person_search");
    }

    static public List<Profile> doSearch(String name) {
    	List<Profile> profiles = new ArrayList<>();
        Personalities2 personalities = new Personalities2();
        Iterator<Axiom> iterator = personalities.findZodiacPerson(name);
        while(iterator.hasNext())
        {
        	Profile profile = new Profile();
        	Axiom axiom = iterator.next();
        	// name, sex, age, starsign, traits
        	profile.name = (String) axiom.getValueByName("name");
        	profile.age = (Long) axiom.getValueByName("age");
        	profile.starSign = (String) axiom.getValueByName("starsign");
         	profile.traits = axiom.getValueByName("personality").toString();
        	profiles.add(profile);
        }
        return profiles;
    }
    
    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
    	String person = "John";
    	if (args.length == 1)
    		person = args[0];
        try 
        {
        	List<Profile> profiles = doSearch(person);
        	profiles.forEach(profile -> {
                System.out.println("Name: " + profile.name + ", Age: " + profile.age + ", Zodiac: " + profile.starSign);
                System.out.println("Traits: " + profile.traits);
                System.out.println();
            });
        } 
        catch (Throwable e) 
        { 
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
