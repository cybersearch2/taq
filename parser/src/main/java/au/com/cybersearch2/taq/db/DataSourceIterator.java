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
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.DataCollector;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * DataSourceIterator
 * Iterator feed by a DataSource object 
 * @author Andrew Bowley
 * 13 Feb 2015
 */
public class DataSourceIterator implements Iterator<Axiom> 
{
	/** Name to use when creating axioms. Defaults to data object simple class name. */
	protected String axiomName;
	/** Translates between axioms of a particular archetype and corresponding Java beans */
	protected AxiomConverter axiomConverter;
	/** Queries a database to obtain all all rows in an entity table */
	protected DataCollector dataCollector;
	/** Data collection iterator */
	protected Iterator<?> entityIterator;

	/**
	 * Construct DataSourceIterator object
	 * @param dataCollector Queries a database to obtain all all rows in an entity table
	 * @param axiomConverter Translates between axioms of a particular archetype and corresponding Java beans
	 */
	public DataSourceIterator(DataCollector dataCollector, AxiomConverter axiomConverter)
	{
		this.dataCollector = dataCollector;
		this.axiomConverter = axiomConverter;
    }

    /**
     * hasNext
     * @see java.util.Iterator#hasNext()
     */
	@Override
	public boolean hasNext() 
	{   // Ensure entity iterator is ready to navigate database table
		if ((entityIterator== null) || (!entityIterator.hasNext() && dataCollector.isMoreExpected()))
			entityIterator = dataCollector.getData().iterator();
		return entityIterator.hasNext();
	}

	/**
	 * next
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Axiom next() 
	{   // Don't assume hasNext() has been called prior
		if ((entityIterator== null) && !hasNext())
			return null;
		try {
			return axiomConverter.getAxiomFromEntity(entityIterator.next());
		} catch (ExecutionException e) {
			throw new ExpressionException(String.format("Error retrieving data from %s axiom source",  axiomName), e);
		}
	}

    /**
	 * Not implemented
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() 
	{
	}

}
