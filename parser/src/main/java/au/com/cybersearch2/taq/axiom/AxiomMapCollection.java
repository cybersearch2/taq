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
package au.com.cybersearch2.taq.axiom;

import java.util.HashMap;
import java.util.Map;

import au.com.cybersearch2.taq.interfaces.AxiomCollection;
import au.com.cybersearch2.taq.interfaces.AxiomSource;

/**
 * AxiomMapCollection
 * A set of AxiomSource objects referenced by name
 * @author Andrew Bowley
 * 9 Jan 2015
 */
public class AxiomMapCollection implements AxiomCollection
{
	/** The AxiomSource map */
	Map<String, AxiomSource> axiomSourceMap;

	/**
	 * Construct an empty AxiomMapCollection object
	 */
	public AxiomMapCollection()
	{
		axiomSourceMap = new HashMap<String, AxiomSource>();
	}

	/**
	 * Construct an AxiomMapCollection object with specified AxiomSource map
	 * @param axiomSourceMap The AxiomSource map
	 */
	public AxiomMapCollection(Map<String, AxiomSource> axiomSourceMap)
	{
		this.axiomSourceMap = axiomSourceMap;
	}

	/**
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.AxiomCollection#getAxiomSource(java.lang.String)
	 */
	@Override
	public AxiomSource getAxiomSource(final String name) 
	{
		return axiomSourceMap.get(name);
	}

	/**
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.AxiomCollection#isEmpty()
	 */
	@Override
	public boolean isEmpty() 
	{
		return axiomSourceMap.isEmpty();
	}

	/**
	 * Add AxiomSource object
	 * @param axiomKey The AxiomSource name
	 * @param axiomSource The AxiomSource object
	 */
	public void put(String axiomKey, AxiomSource axiomSource) 
	{
		axiomSourceMap.put(axiomKey, axiomSource);
	}

}
