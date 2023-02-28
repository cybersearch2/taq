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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.db.ColumnSetter;
import au.com.cybersearch2.taq.db.DataEmitter;
import au.com.cybersearch2.taq.db.StatementRunner;
import au.com.cybersearch2.taq.db.MethodAnalyser.MethodData;
import au.com.cybersearch2.taq.provider.generic.GenericEntityEmitter;
import au.com.cybersearch2.taq.query.QueryExecutionException;

public class AgriYearsEmitter extends GenericEntityEmitter<YearPercent> {

    /**  Static empty Object array to represent no parameters in reflection method call */
    public static final Object[] NO_ARGS = new Object[] {};
	private static final String INVOKE_ERROR_MESSAGE = "Bean method call failed for class %s";

	private final class AgriPercentDataEmitter extends DataEmitter<AgriAreaPercent, YearPercent> {
		
		private final List<YearPercent> yearPercentList;
			
		public AgriPercentDataEmitter() {
			super(AgriAreaPercent.class);
			yearPercentList = new ArrayList<>();
		}

   			
		@Override
		protected List<YearPercent> emit(AgriAreaPercent agriAreaPercent) {
			yearPercentList.clear();
			String countryField = agriAreaPercent.getCountry();
			long surfaceArea = Double.doubleToLongBits(agriAreaPercent.getSurfaceAreaKm2());
			Country country = new Country();
			country.setCountry(countryField);
			country.setSurfaceAreaKm2(surfaceArea);
			int countryId;
			try {
				countryId = countryEmitter.insertEntity(country);
			} catch (InterruptedException | ExecutionException e) {
				throw new QueryExecutionException("Insert country in database failed", e);
			}
    		for (Map.Entry<String, MethodData> entry: columnGetters.entrySet())
    		{
    			if (!entry.getKey().startsWith("y"))
    				continue;
    	        Object value = null;
    	        try {
    	        	value = entry.getValue().method.invoke(agriAreaPercent, NO_ARGS);
    			} catch (Throwable e) {
    	            throw new QueryExecutionException(String.format(INVOKE_ERROR_MESSAGE, AgriAreaPercent.class.getName()), e);
    			} 
    			double percent = (Double)value;
    			YearPercent yearPercent = new YearPercent();
    			yearPercentList.add(yearPercent);
    			yearPercent.setDecade(entry.getKey());
    			yearPercent.setPercent(Double.doubleToLongBits(percent));
    			yearPercent.setCountryId(countryId);
    		}
			return yearPercentList;
		}		
	}
	
	private final StatementRunner<Country> countryEmitter;
	private final Map<String, MethodData> columnGetters;
	
	public AgriYearsEmitter(StatementRunner<Country> countryEmitter) {
		super(YearPercent.class);
		this.countryEmitter = countryEmitter;
		try {
			ColumnSetter<AgriAreaPercent> agriPercentYearsSetter = new ColumnSetter<>(AgriAreaPercent.class);
			columnGetters = agriPercentYearsSetter.getFieldMap();
		} catch (ExecutionException e) {
			throw new QueryExecutionException(String.format("%s reflection error", AgriAreaPercent.class.getName()), e);
		}
		setDataEmitter(new AgriPercentDataEmitter());
	}
}
