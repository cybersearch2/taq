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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.h2.H2;
import au.com.cybersearch2.taq.helper.Taq;
import city.ReadCities;
import city.ReadCities.Record;

/**
 * HighCitiesSorted3Test
 * @author Andrew Bowley
 */
public class HighCitiesSorted3Test
{
	private static Record[] IMPORTED = new Record[]
	{
		new Record(1,"bilene",1718),
		new Record(2,"addis ababa",8000),
		new Record(3,"denver",5280),
		new Record(4,"flagstaff",6970),
		new Record(5,"jacksonville",8),
		new Record(6,"leadville",10200),
		new Record(7,"madrid",1305),
		new Record(8,"richmond",19),
		new Record(9,"spokane",1909),
		new Record(10,"wichita",1305)
	};
	
	private static Record[] EXPORTED = new Record[]
	{
		new Record(1,"denver",5280),
		new Record(2,"flagstaff",6970),
		new Record(3,"addis ababa",8000),
		new Record(4,"leadville",10200)
	};

	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void test_HighCitiesSorted3() throws SQLException, InterruptedException, IOException
    {
    	List<String> args = new ArrayList<>();
    	args.add("cities");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		// Suppress console output
		taq.getCaptureList();
		taq.execute();
		args = new ArrayList<>();
    	args.add("high-cities-sorted3");
        taq = new Taq(args);
		taq.findFile();
		taq.compile();
		taq.execute();
		ReadCities.RecordCallback callback = new ReadCities.RecordCallback() {
	
			int index = 0;
			
			@Override
			public void onNextRecord(Record record) {
				checkRecord(IMPORTED[index++], record);
 			}
		};
		ReadCities readCities = new ReadCities();
		Path citiesPath = Paths.get(Taq.WORKSPACE, "db", "cities");
		ConnectionProfile profile =  new ConnectionProfile("cities", new H2(), citiesPath.toString());
		profile.setUser("sa");
		profile.setPassword("secret?");
		Map<String,Object> propertiesMap = new HashMap<>();
		readCities.selectAll(profile, propertiesMap, callback);
		callback = new ReadCities.RecordCallback() {
			
			int index = 0;
			
			@Override
			public void onNextRecord(Record record) {
				checkRecord(EXPORTED[index++], record);
 			}
		};
		Path sortedPath = Paths.get(Taq.WORKSPACE, "db", "sorted-cities");
		profile =  new ConnectionProfile("sort_cities", new H2(), sortedPath.toString());
		propertiesMap = new HashMap<>();
		readCities.selectAll(profile, propertiesMap, callback);
    }

	private void checkRecord(Record expected, Record actual) {
        assertThat(expected.id).isEqualTo(actual.id);
        assertThat(expected.name).isEqualTo(actual.name);
        assertThat(expected.altitude).isEqualTo(actual.altitude);
	}
}
