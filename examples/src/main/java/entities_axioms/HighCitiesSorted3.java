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

import java.util.HashMap;
import java.util.Map;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.h2.H2;
import au.com.cybersearch2.taq.provider.generic.EntityPersistence;
import city.ReadCities;
import city.ReadCities.Record;
import utils.ResourceHelper;

/**
Creates a list of cities which are at 5,000 feet or higher, sorted by elevation..
The query reads the cities from an SQL database table where each row contains a 
name column and an elevation column.
 */
public class HighCitiesSorted3 
{
	static final String CITES_PATH = ResourceHelper.getTestResourceFile("db/cities.db").getAbsolutePath();
	static final String SORTED_CITES_PATH = ResourceHelper.getTestResourceFile("db/sorted-cities.db").getAbsolutePath();
	
    private QueryProgramParser queryProgramParser1;
    private QueryProgramParser queryProgramParser2;
    private EntityPersistence sortedCitiesResourceProvider;

	public HighCitiesSorted3() throws InterruptedException
	{
		ConnectionProfile profile = new ConnectionProfile("cities", new H2(), CITES_PATH);
		profile.setUser("sa");
		profile.setPassword("secret?");
        EntityPersistence citiesResourceProvider = 
			new EntityPersistence(profile);
        citiesResourceProvider.addCollectorEntity("city", City.class);
        sortedCitiesResourceProvider = 
			new EntityPersistence(new ConnectionProfile("sort_cities", new H2(), SORTED_CITES_PATH));
        sortedCitiesResourceProvider.addEmitterEntity("insert_sort.high_cities", City.class);
        queryProgramParser1 = 
                new QueryProgramParser(ResourceHelper.getResourcePath(), citiesResourceProvider);
        queryProgramParser2 = 
                new QueryProgramParser(ResourceHelper.getResourcePath(), citiesResourceProvider, sortedCitiesResourceProvider);
	}
	
	/**
	 * Compiles high-cities-sorted.taq and runs the "cities_query" query, displaying the solution on the console.<br/>
	 * The expected result:<br/>
<code>
1	denver,5280
2	flagstaff,6970
3	addis ababa,8000
4	leadville,10200 
</code> */
    public void runHighCities()
	{
        QueryProgram queryProgram1 = queryProgramParser1.loadScript("entities_axioms/cities.taq");
		queryProgram1.executeQuery("cities");
        QueryProgram queryProgram2 = queryProgramParser2.loadScript("entities_axioms/high-cities-sorted3.taq");
		queryProgram2.executeQuery("sort_cities");
	}

	public static void main(String[] args)
	{
		ReadCities.RecordCallback callback = new ReadCities.RecordCallback() {
			
			@Override
			public void onNextRecord(Record record) {
                System.out.println(record.id + "\t" + record.name + "," + record.altitude);
			}
		};
		try 
		{
	        HighCitiesSorted3 highCities = new HighCitiesSorted3();
	        highCities.runHighCities();
			ReadCities readCities = new ReadCities();
			System.out.println("Exported cities:\n");
			ConnectionProfile profile =  new ConnectionProfile("sort_cities", new H2(), SORTED_CITES_PATH);
			Map<String,Object> propertiesMap = new HashMap<>();
			readCities.selectAll(profile, propertiesMap, callback);
		} 
        catch (Throwable e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
		finally
		{
		    System.exit(0);
		}
	}
}
