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
package agriculture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.db.BatchCollector;
import au.com.cybersearch2.taq.db.ColumnGetter;
import au.com.cybersearch2.taq.db.ObjectSelector;
import au.com.cybersearch2.taq.db.MethodAnalyser.MethodData;
import au.com.cybersearch2.taq.provider.generic.GenericEntityCollector;
import au.com.cybersearch2.taq.query.QueryExecutionException;

public class AgriYearsCollector extends GenericEntityCollector<YearPercent> {

	static int PAGE_SIZE = AgriAreaPercent.DECADE_COUNT * 5;

	private class AgriYearsBatchCollector implements BatchCollector<YearPercent,AgriAreaPercent>{

		private final ColumnGetter<AgriAreaPercent> agriYearsGetter;
		private final ObjectSelector<Country> countrySelector;
		
		public AgriYearsBatchCollector(ColumnGetter<AgriAreaPercent> agriYearsGetter, ObjectSelector<Country> countrySelector) {
			this.agriYearsGetter = agriYearsGetter;
			this.countrySelector = countrySelector;
		}
		
		@Override
		public List<AgriAreaPercent> processBatch(List<YearPercent> yearPercentList) {
			agriYearsList.clear();
	        Iterator<YearPercent> iterator = yearPercentList.iterator();
	        while (iterator.hasNext())
	        {
	        	YearPercent yearPercent = iterator.next();
	        	String decade = yearPercent.getDecade();
	        	Country country;
				try {
					country = countrySelector.getObjectById(yearPercent.getCountryId());
				} catch (InterruptedException | ExecutionException e) {
    				throw new QueryExecutionException("Query for countries failed", e);
				}
	        	if (!currentCountry.equals(country.getCountry()))
	        	{
	        		currentCountry = country.getCountry();
	        		if (agriYears != null)
	        			agriYearsList.add(agriYears);
	        		agriYears = new AgriAreaPercent();
	        		agriYears.setCountry(country.getCountry());
	        		double surfaceArea = Double.longBitsToDouble(country.getSurfaceAreaKm2());
	        		agriYears.setSurfaceAreaKm2(surfaceArea);
	        	}
	    		try {
    				Map<String, MethodData> columnSetters = agriYearsGetter.getFieldMap();
    				MethodData methodData = columnSetters.get(decade);
    				if (methodData != null)
    					methodData.method.invoke(agriYears, Double.longBitsToDouble(yearPercent.getPercent()));
    			} catch (Throwable e) {
    				throw new QueryExecutionException(String.format("Error Setting field %s", decade), e);
    			}
	        }
    		if (agriYears != null) {
    			agriYearsList.add(agriYears);
    			agriYears = null;
    		}
	        return agriYearsList;
		}
	}
	
	
    private AgriAreaPercent agriYears;
    private String currentCountry = "";
    private List<AgriAreaPercent> agriYearsList;

	public AgriYearsCollector(ObjectSelector<Country> countrySelector) {
		super(YearPercent.class);
		setMaxResults(PAGE_SIZE);
		agriYearsList = new ArrayList<>(AgriAreaPercent.DECADE_COUNT);
		ColumnGetter<AgriAreaPercent> agriYearsGetter;
		try {
			agriYearsGetter = new ColumnGetter<>(AgriAreaPercent.class);
		} catch (ExecutionException e) {
			throw new QueryExecutionException(String.format("%s reflection error", AgriAreaPercent.class.getName()), e);
		}
    	setBatchCollector(new AgriYearsBatchCollector(agriYearsGetter, countrySelector));
	}

	@Override
	public String getDescription() {
		return "Collects agriculture area percent data sampled every 10 years";
	}
}
