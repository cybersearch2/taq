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
package au.com.cybersearch2.taq.pattern;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.cybersearch2.taq.interfaces.StructureType;
import au.com.cybersearch2.taq.language.LiteralParameter;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.terms.TermMetaData;

/**
 * AxiomArchetype
 * Axiom factory which type checks data passed to construct axioms
 * @author Andrew Bowley
 * 3May,2017
 */
public class AxiomArchetype extends Archetype<Axiom, Term> implements Serializable
{
    private static final long serialVersionUID = -8403891863022754448L;
    
    private static final Unknown UNKNOWN ;
    public static TermList<Term> EMPTY_AXIOM;
    
    static
    {
        UNKNOWN =  new Unknown();
        EMPTY_AXIOM = new TermList<Term>(new AxiomArchetype(QualifiedName.parseGlobalName("*"))){

            private static final long serialVersionUID = 2071000367077189439L;
            
        };
    }
    

    /**
     * Construct AxiomArchetype
     * @param structureName Qualified name which uniquely identifies the axioms being produced - must have a name part
     */
    public AxiomArchetype(QualifiedName structureName)
    {
        super(structureName, StructureType.axiom);
        if ((structureName.getName().isEmpty()) && (structureName.getTemplate().isEmpty()))
            throw new IllegalArgumentException("Axiom qualified name must have a name part");
    }

    public AxiomArchetype(AxiomArchetype axiomArchetype) {
        super(axiomArchetype.structureName, StructureType.axiom);
        if (axiomArchetype.termMetaList.isEmpty())
            termMetaList = EMPTY_LIST;
        else {
        	termMetaList = new ArrayList<>(axiomArchetype.termMetaList.size());
        	axiomArchetype.termMetaList.forEach(termMeta -> {
        		termMetaList.add(new TermMetaData(termMeta));
        	});
        }
        isMutable = true;
        isAnonymousTerms = axiomArchetype.isAnonymousTerms;
    }
    
    /**
     * Create default Axiom instance
     * @return Axiom object
     */
    public Axiom newInstance()
    {
        Axiom axiom = new Axiom(new AxiomArchetype(this), Collections.emptyList());
        return axiom;
    }

    /**
     * Create Axiom instance
     * @return Axiom object
     * @see au.com.cybersearch2.taq.pattern.Archetype#newInstance(java.util.List)
     */
    @Override
    protected Axiom newInstance(List<Term> terms)
    {
        Axiom axiom = new Axiom(this, terms);
        return axiom;
    }

    /**
     * Create Axiom instance
     * @param values Objects to populate axiom - may be Terms
     * @return Axiom object
     */
    public Axiom itemInstance(Object... values)
    {
        List<Term> terms = new ArrayList<Term>();
        if ((values != null)&& (values.length > 0))
        {
            for (Object datum: values)
            {
                if (datum instanceof Term)
                    terms.add((Term) datum);
                else
                    terms.add(new Parameter(Term.ANONYMOUS, datum));
            }
        }
        return newInstance(terms);
    }

    /**
     * Add term to archetype, specifying only name. Archetype must be in mutable state to succeed.
     * @param termName Name of term
     * @return Term index
     */
    public int addTermName(String termName)
    {
        return addTerm(new TermMetaData(new LiteralParameter(termName, UNKNOWN, LiteralType.unspecified)));
    }

    /**
     * Returns meta-data for term specified by index
     * @param index Term index
     * @return TermMetaData object
     */
    public TermMetaData getMetaDataByIndex(int index)
    {
        if ((index >= 0) && (index < getTermCount()))
            return termMetaList.get(index);
        return null;
    }
 
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Archetype " + structureName.toString();
    }

    private void writeObject(ObjectOutputStream oos)
            throws IOException 
    {
        oos.writeObject(structureName);
    }
    
    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException  
    {
        structureName = (QualifiedName)ois.readObject();
        structureType = StructureType.axiom;
        termMetaList = EMPTY_LIST;
        isMutable = true;
        isAnonymousTerms = true;
    }
}
