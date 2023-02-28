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

import java.security.ProviderException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.axiom.AxiomReflection;
import au.com.cybersearch2.taq.axiom.NameMap;
import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.CustomDatabaseProvider;
import au.com.cybersearch2.taq.db.MethodAnalyser;
import au.com.cybersearch2.taq.db.ObjectSelector;
import au.com.cybersearch2.taq.db.StatementRunner;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.provider.generic.GenericEntityEmitter;
import au.com.cybersearch2.taq.provider.generic.EntityPersistence;
import au.com.cybersearch2.taq.terms.TermMetaData;

public class AgriPercentProvider extends EntityPersistence implements CustomDatabaseProvider {

	public static final AxiomArchetype countryArchetype;
	
	static {
		QualifiedName countryQname = QualifiedName.parseGlobalName(Country.COUNTRY);
		countryArchetype = 
    			new AxiomArchetype(countryQname);
    		countryArchetype.addTerm(new TermMetaData(LiteralType.string, Country.COUNTRY, 0));
    		countryArchetype.addTerm(new TermMetaData(LiteralType.integer, AgriAreaPercent.SURFACE_AREA_TERM, 1));
    		countryArchetype.clearMutable();
	}

	/** AgriYearsCollector by default maps YearPercent, so this must be overridden to AgriAreaPercent */
	private final List<NameMap> termNameList;

	public AgriPercentProvider(ConnectionProfile profile) {
		super(profile);
		termNameList = MethodAnalyser.getNameMap(AgriAreaPercent.class);
		addCollectorEntity(Country.COUNTRY, Country.class);
	}

    @Override
    protected AxiomReflection getAxiomReflection(AxiomArchetype archetype) {
    	if (archetype.getQualifiedName().getName().equals(getName()))
            return new AxiomReflection(archetype, termNameList);
    	else
    		return new AxiomReflection(archetype);
    }

	@SuppressWarnings("unchecked")
	@Override
	public void addCArtifact(ArtifactType artifactType, String axiomName) {
		if (artifactType == ArtifactType.collector) {
			addCollector(axiomName, new AgriYearsCollector((ObjectSelector<Country>) getObjectSelector(Country.COUNTRY)));
		} else {
			GenericEntityEmitter<Country> countryEmitter = addEmitterEntity(Country.COUNTRY, Country.class);
			StatementRunner<Country> countryRunner;
			try {
				countryRunner = countryEmitter.getStatementRunner();
			} catch (ExecutionException e) {
				throw new ProviderException("Error creating country statement runner", e);
			}
			addEmitter(axiomName, new AgriYearsEmitter(countryRunner));
		}
	}

	@Override
	public EnumSet<ArtifactType> requiredArtifactTypes() {
		return EnumSet.of(ArtifactType.collector, ArtifactType.emitter);
	}
}
