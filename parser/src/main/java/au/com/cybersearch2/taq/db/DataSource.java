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
package au.com.cybersearch2.taq.db;

import java.util.Iterator;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.DataCollector;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * DataSource
 * AxiomSource data implementation which translates database rows to axioms
 * @author Andrew Bowley
 * 8 Feb 2015
 */
public class DataSource implements AxiomSource
{
    
	/** Translates between axioms of a particular archetype and corresponding Java beans */
	protected AxiomConverter axiomConverter;
	/** Queries a database to obtain all all rows in an entity table */
	private final DataCollector dataCollector;

	/**
	 * Construct DataSource object
	 * @param dataCollector Queries a database to obtain all all rows in an entity table
	 * @param axiomConverter Translates between axioms of a particular archetype and corresponding Java beans
	 */
	public DataSource(DataCollector dataCollector, AxiomConverter axiomConverter) 
	{
		this.dataCollector = dataCollector;
		this.axiomConverter = axiomConverter;
    }

	@Override
	public Iterator<Axiom> iterator(ExecutionContext context) 
	{
		return new DataSourceIterator(dataCollector, axiomConverter);
	}

    @Override
    public Archetype<Axiom, Term> getArchetype()
    {
        return axiomConverter.getArchetype();
    }

}
