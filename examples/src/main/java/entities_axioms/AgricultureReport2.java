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

import agriculture.AgriPercentProvider;
import agriculture.provider.Agri20Year2Provider;
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

public class AgricultureReport2 {

	private static final String AGRI_AREA_PERCENT_DB = 
			ResourceHelper.getWorkspaceFile("db/agri-area-percent2").getAbsolutePath();
	public static final String AGRI_SOLUTION_DB = 
			ResourceHelper.getWorkspaceFile("db/agri_20_year2").getAbsolutePath();

    private final QueryProgramParser queryProgramParser;
    private QueryProgram queryProgram;
 
    public AgricultureReport2() {
    	ConnectionProfile profile1 = new ConnectionProfile("agri_area_percent", new H2(), AGRI_AREA_PERCENT_DB);
    	AgriPercentProvider agriPercentProvider = new AgriPercentProvider(profile1);
    	ConnectionProfile profile2 = new ConnectionProfile("agri_20_year", new H2(), AGRI_SOLUTION_DB);
        EntityPersistence agriAxiomProvider = new EntityPersistence(profile2);
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath(), agriPercentProvider, agriAxiomProvider);
    }

	/**
	 * Execute 
	 */
	public void execute() 
	{
		if (queryProgram == null) {
	        queryProgram = queryProgramParser.loadScript("entities_axioms/agriculture-report2.taq");
	        QueryParams queryParams = queryProgram.getQueryParams("initialize", "do_export");
	        queryProgram.executeQuery(queryParams);
		}
		queryProgram.setResourceBase(ResourceHelper.getResourcePath());
		queryProgram.executeQuery("more_agriculture");
	}
	
	public static void main(String[] args)
	{
		try 
		{
	        AgricultureReport2 agricultureReport2 = new AgricultureReport2();
	        agricultureReport2.execute();
	        Agri20Year2Provider agri20YearProvider = new Agri20Year2Provider();
			CallHandler callHandler = agri20YearProvider.getCallEvaluator("print_solution");
			((DatabaseSupport)callHandler).setDatabaseProviders(agricultureReport2.queryProgram.getDatabaseProviders());
			callHandler.evaluate(Collections.singletonList(new Parameter(Term.ANONYMOUS, "agri_20_year")));
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
