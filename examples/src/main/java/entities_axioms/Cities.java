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
Creates a list of cities
 */
public class Cities 
{
	static final String CITES_PATH = ResourceHelper.getTestResourceFile("db/cities.db").getAbsolutePath();
	
    private QueryProgramParser queryProgramParser;

	public Cities() throws InterruptedException
	{
        EntityPersistence citiesResourceProvider = 
			new EntityPersistence(new ConnectionProfile("cities", new H2(), CITES_PATH));
		//citiesResourceProvider.addEmitterEntity("city", City.class); 
        queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getResourcePath(), citiesResourceProvider);
	}
	
	/**
	 * Compiles cities.taq and runs the "cities_query" query, displaying the solution on the console.
     */
    public void runCities()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("entities_axioms/cities.taq");
		queryProgram.executeQuery("cities");
	}

	public static void main(String[] args) throws InterruptedException
	{
		Cities cities = new Cities();
		ReadCities.RecordCallback callback = new ReadCities.RecordCallback() {
			
			@Override
			public void onNextRecord(Record record) {
                System.out.println(record.id + "\t" + record.name + "," + record.altitude);
			}
		};
		try 
		{
			cities.runCities();
			ReadCities readCities = new ReadCities();
			System.out.println("Imported cities:\n");
			ConnectionProfile profile =  new ConnectionProfile("cities", new H2(), CITES_PATH);
			profile.setUser("sa");
			profile.setPassword("secret?");
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
