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
package star_person;

import java.io.IOException;
import java.security.ProviderException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.db.DatabaseSupport;
import au.com.cybersearch2.taq.db.EntityCollector;
import au.com.cybersearch2.taq.db.EntityEmitter;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.CallHandler;
import entities_axioms.StarPerson;

public class StarPersonProvider  implements FunctionProvider {

	public static final class StarPersionCollback implements ReadStarPersons.RecordCallback {

		@Override
		public void onNextRecord(StarPerson starPerson) {
           	NumberFormat formatter = new DecimalFormat("#0.0");  

            System.out.println(starPerson.getId() +  "\t" + 
            		starPerson.getName() + "," +
            		starPerson.getStarsign().name() + "," +
            		starPerson.getAge() + "," +
                    formatter.format(starPerson.getRating()) + " \t" +
                    starPerson.getTimestamp());
		}
		
	}

	public static final class StarPersionCallHandler extends CallHandler implements DatabaseSupport {

		private final Map<String,DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>>
		providerMap;

		protected StarPersionCallHandler(String name) {
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
					ReadStarPersons readStarPersons = new ReadStarPersons();
					try {
						ConnectionProfile connectionProfile = provider.getConnectionProfile();
						readStarPersons.selectAll(connectionProfile,  provider.getConnectionProperties(), new StarPersionCollback());
					} catch (IOException e) {
						throw new ProviderException("Error decoding Zodiac value");
					}
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public String getName() {
		return "star_person_sql";
	}

	@Override
	public CallHandler getCallEvaluator(String identifier) {
		return new StarPersionCallHandler(getName());
	}

}
