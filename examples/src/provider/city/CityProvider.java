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
package city;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.db.DatabaseSupport;
import au.com.cybersearch2.taq.db.EntityCollector;
import au.com.cybersearch2.taq.db.EntityEmitter;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.CallHandler;

public class CityProvider implements FunctionProvider {

	private static final class CitiesRecordCallback implements ReadCities.RecordCallback {
		
		@Override
		public void onNextRecord(ReadCities.Record record) {
            System.out.println(record.id + "\t" + record.name + "," + record.altitude);
		}
	};

	private static final class CitiesCallHandler extends CallHandler implements DatabaseSupport {

		private final Map<String,DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>>
		providerMap;

		protected CitiesCallHandler(String name) {
			super(name);
			this.providerMap = new HashMap<>();
		}

		@Override
		public void setDatabaseProviders(
				List<DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>> providers) {
			providers.forEach(provider -> providerMap.put(provider.getName(), provider));
		}

		@Override
		public boolean evaluate(List<Term> argumentList) {
			if (!argumentList.isEmpty()) {
				String reaourceName = argumentList.get(0).getValue().toString();
				DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>> provider = providerMap.get(reaourceName);
				if (provider != null) {
					ReadCities readCities = new ReadCities();
					readCities.selectAll(provider.getConnectionProfile(), provider.getConnectionProperties(),  new CitiesRecordCallback());
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public String getName() {
		return "cities_sql";
	}

	@Override
	public CallHandler getCallEvaluator(String identifier) {
		return new CitiesCallHandler(getName());
	}

}
