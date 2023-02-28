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
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * AxiomListSource
 * @author Andrew Bowley
 * 5 Dec 2014
 */
public class AxiomListSource  implements AxiomSource, Iterable<Axiom>
{
	/** The axiom list */
    protected List<Axiom> axiomList;
    /** The axiom archetype */
    protected Archetype<Axiom,Term> archetype;

    /**
     * Construct an AxiomListSource object
     * @param axiomList The axiom list or null to create a empty AxiomSource
     */
	public AxiomListSource(List<Axiom> axiomList) 
	{
		this.axiomList = axiomList;
	}

    /**
     * Construct an AxiomListSource object with supplied archetype
     * @param axiomList The axiom list or null to create a empty AxiomSource
     * @param archetype The axiom archetype 
     */
    public AxiomListSource(List<Axiom> axiomList, Archetype<Axiom,Term> archetype) 
    {
        this.axiomList = axiomList;
        this.archetype = archetype;
    }

	/**
	 * Returns the axiom list Iterable
	 * @return Iterable of generic type Axiom
	 */
	public Iterable<Axiom> getIterable() 
	{
		return axiomList;
	}

	/**
	 * Returns term name list
	 * @return String list
	 */
    public List<String> getAxiomTermNameList()
    {
        return getArchetype().getTermNameList();
    }

    @Override
    public Iterator<Axiom> iterator(ExecutionContext context) 
    {
        return axiomList.iterator();
    }

    @Override
    public Iterator<Axiom> iterator() 
    {
        return axiomList.iterator();
    }

    @Override
    public Archetype<Axiom, Term> getArchetype()
    {
        if (archetype == null)
            archetype = getAxiomListArchetype();
        return archetype;
    }

    /**
     * Returns axiom archetype form axiom list contents, if available,
     * otherwise returns archetype for an empty axiom
     * @return Archetype object
     */
    @SuppressWarnings("unchecked")
    private Archetype<Axiom, Term> getAxiomListArchetype()
    {
        if (!axiomList.isEmpty())
            return (Archetype<Axiom, Term>) axiomList.get(0).getArchetype();
        AxiomArchetype archetype = new AxiomArchetype(QualifiedName.ANONYMOUS);
        archetype.clearMutable();
        return archetype;
    }
}
