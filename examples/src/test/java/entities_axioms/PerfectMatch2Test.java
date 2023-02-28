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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.h2.H2;
import au.com.cybersearch2.taq.helper.Taq;
import au.com.cybersearch2.taq.provider.GlobalFunctions;
import star_person.ReadStarPersons;
import star_person.ReadStarPersons.RecordCallback;

/**
 * PerfectMatchTest
 * @author Andrew Bowley
 */
public class PerfectMatch2Test {

	private String[] STAR_PEOPLE = {
		"1	John,Gemini,23,1.0",
		"2	Sue,Cancer,19,NaN",
		"3	Sam,Scorpio,34,0.3",
		"4	Jenny,Gemini,28,0.6",
		"5	Andrew,Virgo,26,0.6",
		"6	Alice,Pisces,20,1.0",
		"7	Ingrid,Cancer,23,1.0",
		"8	Jack,Pisces,32,0.3",
		"9	Sonia,Gemini,33,0.3",
		"10	Alex,Aquarius,22,1.0",
		"11	Jill,Cancer,33,0.3",
		"12	Fiona,Gemini,29,0.6",
		"13	Melissa,Virgo,30,0.3",
		"14	Tom,Cancer,22,1.0",
		"15	Bill,Virgo,19,NaN"
	};

	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
        // star_people.log_to_console() displays the list which is written to the database.
        // So suppress console output.
        GlobalFunctions.printCapture();
	}
	
    @Test
    public void testPerfectMatch2() throws IOException {
    	List<String> args = new ArrayList<>();
    	args.add("perfect-match2");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
    	RecordCallback callback = new RecordCallback() {

    		int index = 0;
           	NumberFormat formatter = new DecimalFormat("#0.0");  
    		
			@Override
			public void onNextRecord(StarPerson starPerson) {
				String actual = starPerson.getId() +  "\t" + 
                		starPerson.getName() + "," +
                		starPerson.getStarsign().name() + "," +
                		starPerson.getAge() + "," +
                        formatter.format(starPerson.getRating());
		        assertThat(STAR_PEOPLE[index++]).isEqualTo(actual);
			}};
		String starPeoplePath = Paths.get(Taq.WORKSPACE, "db", "star-people").toString();
		taq.execute();
		ConnectionProfile profile =  new ConnectionProfile("star_people", new H2(), starPeoplePath);
    	profile.setUser("sa");
    	profile.setPassword("secret?");
		Map<String,Object> propertiesMap = new HashMap<>();
		ReadStarPersons readStarPersons = new ReadStarPersons();
		readStarPersons.selectAll(profile, propertiesMap, callback);
	}

}
