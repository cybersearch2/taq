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

import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * SingleAxiomIterator
 * AxiomSource adapter for a single axiom
 * @author Andrew Bowley
 * 4 Dec 2014
 */
public class SingleAxiomSource implements AxiomSource, Iterator<Axiom>, Iterable<Axiom> 
{
    /** Count up to 1 */
    int count;
    /** The axiom */
    Axiom axiom;

    /**
     * Construct SingleAxiomSource object
     * @param axiom The axiom object
     */
    public SingleAxiomSource(Axiom axiom)
    {
    	this.axiom = axiom;
    }

    /**
     * Returns self 
     * @return this
     */
    public Iterable<Axiom> getIterable() 
    {
        return this;
    }

    public List<String> getAxiomTermNameList()
    {
        return axiom.getArchetype().getTermNameList();
    }
	@Override
    public boolean hasNext()
    {
        return count == 0;
    }

	@Override
    public Axiom next()
    {
        ++count;
        return axiom;
    }

	@Override
	public void remove() 
	{
	}

	@Override
	public Iterator<Axiom> iterator(ExecutionContext context) 
	{
		return new SingleAxiomSource(axiom);
	}

	@Override
	public Iterator<Axiom> iterator() 
	{
		return new SingleAxiomSource(axiom);
	}

    @SuppressWarnings("unchecked")
    @Override
    public Archetype<Axiom, Term> getArchetype()
    {
        return (Archetype<Axiom, Term>) axiom.getArchetype();
    }

}
