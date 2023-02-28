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
package entities_axioms;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.h2.H2;
import au.com.cybersearch2.taq.provider.generic.EntityPersistence;
import star_person.ReadStarPersons;
import utils.ResourceHelper;

/**
Demonstrates a database resource data collector with an entity 
class used to define the database records. The "star_people" query creates a dating profile 
for each person in a database that is 20 years old and over.
 */
public class PerfectMatch2
{
    static final String STAR_PEOPLE_PATH;

    static {
    	STAR_PEOPLE_PATH = ResourceHelper.getTestResourceFile("db/star-people").getAbsolutePath();
    }
    
    private QueryProgramParser queryProgramParser;
    private EntityPersistence starPeopleResourceProvider;

    public PerfectMatch2()
    {
    	ConnectionProfile profile = new ConnectionProfile("star_people", new H2(), STAR_PEOPLE_PATH);
    	profile.setUser("sa");
    	profile.setPassword("secret?");
    	starPeopleResourceProvider = new EntityPersistence(profile);
    	starPeopleResourceProvider.addEmitterEntity("apply_age_rating", StarPerson.class);
        queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getResourcePath(), starPeopleResourceProvider );
    }

    /**
     * Compiles /perfect-match2.taq and runs the "star_people" query which gives each person 
     * over the age of 20 an age rating and those not rated have an unknown value.<br/>
     * The expected result is shown in 7 columns. The last column will be different each time.<br/>
<code>
1	John,Gemini,23,1.0 		2021-09-14T05:12:03.788453Z
2	Sue,Cancer,19,NaN 		2021-09-14T05:12:03.943631Z
3	Sam,Scorpio,34,0.3 		2021-09-14T05:12:04.087620Z
4	Jenny,Gemini,28,0.6 	2021-09-14T05:12:04.233016Z
5	Andrew,Virgo,26,0.6 	2021-09-14T05:12:04.378164Z
6	Alice,Pisces,20,1.0 	2021-09-14T05:12:04.512484Z
7	Ingrid,Cancer,23,1.0 	2021-09-14T05:12:04.648593Z
8	Jack,Pisces,32,0.3 		2021-09-14T05:12:04.771937Z
9	Sonia,Gemini,33,0.3 	2021-09-14T05:12:04.894595Z
10	Alex,Aquarius,22,1.0 	2021-09-14T05:12:05.017299Z
11	Jill,Cancer,33,0.3 		2021-09-14T05:12:05.140186Z
12	Fiona,Gemini,29,0.6 	2021-09-14T05:12:05.274477Z
13	Melissa,Virgo,30,0.3 	2021-09-14T05:12:05.408954Z
14	Tom,Cancer,22,1.0 		2021-09-14T05:12:05.542798Z
15	Bill,Virgo,19,NaN 		2021-09-14T05:12:05.677665Z</code>
     */
    public void runAgeRating()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("entities_axioms/perfect-match2.taq");
        starPeopleResourceProvider.dropAllTables();
        queryProgram.executeQuery("star_people");
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
    	ReadStarPersons.RecordCallback callback = new ReadStarPersons.RecordCallback() {

           	NumberFormat formatter = new DecimalFormat("#0.0");  

			@Override
			public void onNextRecord(StarPerson starPerson) {
                System.out.println(starPerson.getId() +  "\t" + 
                		starPerson.getName() + "," +
                		starPerson.getStarsign().name() + "," +
                		starPerson.getAge() + "," +
                        formatter.format(starPerson.getRating()) + " \t" +
                        starPerson.getTimestamp());
			}
		};
        try 
        {
            PerfectMatch2 perfectMatch2 = new PerfectMatch2();
            perfectMatch2.runAgeRating();
            System.out.println("\nid\tName,Starsign,Rating\tTimestamp");
    		ConnectionProfile profile =  new ConnectionProfile("star_people", new H2(), STAR_PEOPLE_PATH);
        	profile.setUser("sa");
        	profile.setPassword("secret?");
    		Map<String,Object> propertiesMap = new HashMap<>();
    		ReadStarPersons readStarPersons = new ReadStarPersons();
    		readStarPersons.selectAll(profile, propertiesMap, callback);
        } 
        catch (Throwable e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
