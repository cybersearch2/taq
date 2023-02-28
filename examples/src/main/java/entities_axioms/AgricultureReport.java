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

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import agriculture.AgriAreaPercent;
import agriculture.provider.Agri20Year1Provider;
import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.DatabaseSupport;
import au.com.cybersearch2.taq.db.h2.H2;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.CallHandler;
import au.com.cybersearch2.taq.provider.generic.EntityPersistence;
import utils.ResourceHelper;

/**
Demonstrates two highly customized database resource providers. 
The 'more_agriculture' query produces a list of countries which have increased the area
under agriculture by more than 1% over the twenty years between 1990 and 2010. 
The database records are revealed by a separate Agri20Year1Provider object
which employs SQL to query the agri_20_year1 database.
 */
public class AgricultureReport
{
	/** Custom provider to demonstrate application override of program resource declaration */ 
	private static final class CustomAgriPercentProvider extends EntityPersistence {
		
		private static final String AGRI_AREA_PERCENT = "agri_area_percent";
		private static final String AGRI_AREA_PERCENT_DB = 
				ResourceHelper.getWorkspaceFile("db/agri-area-percent1").getAbsolutePath();
		public static final String AGRI_DECADES = "agri_decades";

		public CustomAgriPercentProvider() {
			super(new ConnectionProfile(AGRI_AREA_PERCENT, new H2(), AGRI_AREA_PERCENT_DB));
			addEmitterEntity(AGRI_DECADES, AgriAreaPercent.class);
			addCollectorEntity(AGRI_AREA_PERCENT, AgriAreaPercent.class);
		}
		
		@Override
		public void dropAllTables() {
			super.dropAllTables();
			// Evidence on the console that this is the provider employed
			System.out.println("Dropped all tables in datatbase agri-area-percent1.dn\n");
		}
	}
	

	public static final String AGRI_SOLUTION_DB = 
			ResourceHelper.getWorkspaceFile("db/agri_20_year1").getAbsolutePath();

    private final QueryProgramParser queryProgramParser;
    private final EntityPersistence agriAreaPercentProvider;
    private final EntityPersistence agriAxiomProvider;
    
    protected QueryProgram queryProgram;

	/**
	 * Construct AgricultureReport1 object
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	public AgricultureReport() throws InterruptedException, ExecutionException
	{
		agriAreaPercentProvider = new CustomAgriPercentProvider();
        agriAxiomProvider = new EntityPersistence(new ConnectionProfile("agri_20_year", new H2(), AGRI_SOLUTION_DB));
        queryProgramParser = new QueryProgramParser(ResourceHelper.getResourcePath(), agriAreaPercentProvider, agriAxiomProvider);
	}
	
	/**
	 * Compiles the agriculture-report.taq script and runs the "more_agriculture" query, 
	 * displaying the solution on the console.<br/>
	 * The expected result first 3 lines:<br/>
        Albania 986<br/>
        Algeria 25722<br/>
        American Samoa 10<br/><br/>
     * The full result can be viewed in file src/main/resources/agriculture-report.txt.
     * First time this is run, agricultural data is loaded into the source database, with
     * each country displayed as it's data is persisted.
	 */
	public void execute() 
	{
		if (queryProgram == null) {
	        queryProgram = queryProgramParser.loadScript("entities_axioms/agriculture-report.taq");
	        QueryParams queryParams = queryProgram.getQueryParams("initialize", "do_export");
	        queryProgram.executeQuery(queryParams);
		}
		queryProgram.setResourceBase(ResourceHelper.getResourcePath());
		queryProgram.executeQuery("more_agriculture");
	}
	
	/**
	 * Run tutorial
	 * @param args
	 */
	public static void main(String[] args)
	{
		int exitCode = 0;
		AgricultureReport agricultureReport = null;
		try 
		{
			agricultureReport = new AgricultureReport();
			agricultureReport.execute();
			Agri20Year1Provider agri20Year1Provider = new Agri20Year1Provider();
			CallHandler callHandler = agri20Year1Provider.getCallEvaluator("print_solution");
			((DatabaseSupport)callHandler).setDatabaseProviders(agricultureReport.queryProgram.getDatabaseProviders());
			callHandler.evaluate(Collections.singletonList(new Parameter(Term.ANONYMOUS, "agri_20_year")));
		} catch (Throwable e) {
			e.printStackTrace();
			exitCode = 1;
		}
		finally
		{
		    System.exit(exitCode);
		}
	}
}
