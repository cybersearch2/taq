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
package au.com.cybersearch2.taq.compile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.axiom.AxiomListSource;
import au.com.cybersearch2.taq.axiom.SingleAxiomSource;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * AxiomAssembler
 * @author Andrew Bowley
 * 13May,2017
 */
public class AxiomAssembler
{
    private static final List<String> EMPTY_NAME_LIST;

    static
    {
        EMPTY_NAME_LIST = Collections.emptyList();
    }
    
    /** Container for axioms under construction */
    private final Map<QualifiedName, Axiom> axiomMap;
    /** Archetypes for axioms */
    private final Map<QualifiedName, AxiomArchetype> axiomArchetypeMap;
    /** Scope */
    private Scope scope;

    /**
     * Construct an AxiomAssembler object
     * @param scope Scope of ParserAssembler owner
     */
    public AxiomAssembler(Scope scope)
    {
        this.scope = scope;
        axiomMap = new HashMap<QualifiedName, Axiom>();
        axiomArchetypeMap = new HashMap<QualifiedName, AxiomArchetype>();
    }

    /**
     * Add a term to axiom under construction
     * @param qualifiedAxiomName Qualified axiom name
     * @param term Term object
     */
    public void addAxiom(QualifiedName qualifiedAxiomName, Term term)
    {
        Axiom axiom = axiomMap.get(qualifiedAxiomName);
        if (axiom == null)
            // No axiom currently under construction, so create one.
        	axiom = createAxiom(qualifiedAxiomName);
        axiom.addTerm(term);
    }

    /**
     * Create am axiom with given qualified name as the archetype name
     * @param qualifiedAxiomName Qualified name of axiom
     * @return Axiom object
     */
    public Axiom createAxiom(QualifiedName qualifiedAxiomName) {
        AxiomArchetype axiomArchetype = axiomArchetypeMap.get(qualifiedAxiomName);
        if (axiomArchetype == null)
            axiomArchetype = createAxiomArchetype(qualifiedAxiomName);
        Axiom axiom = axiomArchetype.itemInstance();
        axiomMap.put(qualifiedAxiomName, axiom);
        return axiom;
    }
    /**
     * Add name to list of axiom term names
     * @param qualifiedAxiomName Qualified axiom name
     * @param termName Term name
     */
    public void addAxiomTermName(QualifiedName qualifiedAxiomName, String termName)
    {
        AxiomArchetype axiomArchetype = axiomArchetypeMap.get(qualifiedAxiomName);
        if (axiomArchetype == null)
            axiomArchetype = createAxiomArchetype(qualifiedAxiomName);
        axiomArchetype.addTermName(termName);
    }

    /**
     * Returns axiom archetype created with given qualified name
     * @param qualifiedAxiomName Qualified axiom name
     * @return AxiomArchetype object
     */
    public AxiomArchetype createAxiomArchetype(QualifiedName qualifiedAxiomName)
    {
        AxiomArchetype axiomArchetype = new AxiomArchetype(qualifiedAxiomName);
        axiomArchetypeMap.put(qualifiedAxiomName, axiomArchetype);
        return axiomArchetype;
    }
    
   /**
     * Get axiom term name by position
     * @param qualifiedAxiomName Qualified axiom name
     * @param position Position 
     * @return axiom term name
     */
    public String getAxiomTermName(QualifiedName qualifiedAxiomName, int position)
    {
        AxiomArchetype axiomArchetype = axiomArchetypeMap.get(qualifiedAxiomName);
        if (axiomArchetype == null)
            return null;
        return axiomArchetype.getMetaDataByIndex(position).getName();
    }
    
    /**
     * Get axiom term name for current axiom under construction
     * @param qualifiedAxiomName Qualified axiom name
     * @return axiom term name
     */
    public String getAxiomTermName(QualifiedName qualifiedAxiomName)
    {
    	AxiomArchetype axiomArchetype;
        Axiom axiom = axiomMap.get(qualifiedAxiomName);
        if (axiom == null) {
	        axiomArchetype = axiomArchetypeMap.get(qualifiedAxiomName);
	        if ((axiomArchetype == null) || (axiomArchetype.getTermNameList().size() == 0))
	            return "";
	        return axiomArchetype.getTermNameList().get(0);
        }
        int position = axiom.getTermCount();
        axiomArchetype = (AxiomArchetype)axiom.getArchetype();
        if (axiomArchetype.getTermNameList().size() <= position)
	        return "";
        return axiomArchetype.getMetaDataByIndex(position).getName();
    }
    
    /**
     * Returns list of axiom term names
     * @param qualifiedAxiomName Qualified axiom name
     * @return String list
     */
    public List<String> getTermNameList(QualifiedName qualifiedAxiomName)
    {
        AxiomArchetype axiomArchetype = scope.getGlobalAxiomAssembler().axiomArchetypeMap.get(qualifiedAxiomName);
        if ((axiomArchetype == null) && !QueryProgram.GLOBAL_SCOPE.equals(scope.getName()))
            axiomArchetype = axiomArchetypeMap.get(qualifiedAxiomName);
        return axiomArchetype == null ? EMPTY_NAME_LIST : axiomArchetype.getTermNameList();
    }
    
    /**
     * Transfer axiom under construction to the list of axioms with same name
     * @param qualifiedAxiomName Qualified axiom name
     * @return Axiom object
     */
    public Axiom saveAxiom(QualifiedName qualifiedAxiomName)
    {
        Axiom axiom = axiomMap.get(qualifiedAxiomName);
        scope.getParserAssembler().getListAssembler().add(qualifiedAxiomName, axiom);
        axiomMap.remove(qualifiedAxiomName);
        return axiom;
    }

    /**
     * Finds axiom archetype by qualified name
     * @param qualifiedAxiomName Qualified axiom name
     * @return AxiomArchetype object aor null if not found
     */
    public AxiomArchetype getAxiomArchetype(QualifiedName qualifiedAxiomName)
    {
        return axiomArchetypeMap.get(qualifiedAxiomName);
    }
    
    protected List<String> findTermNameList(QualifiedName qualifiedAxiomName)
    {
        AxiomArchetype axiomArchetype = axiomArchetypeMap.get(qualifiedAxiomName);
        List<String> axiomTermNameList;
        if (axiomArchetype == null)
            axiomTermNameList = Collections.emptyList();
        else
            axiomTermNameList = axiomArchetype.getTermNameList();
        return axiomTermNameList;
    }
    
    protected AxiomSource createAxiomSource(QualifiedName qualifiedAxiomName, List<Axiom> axiomList)
    {
        AxiomArchetype axiomArchetype = axiomArchetypeMap.get(qualifiedAxiomName);
        if (axiomArchetype == null)
            axiomArchetype = createAxiomArchetype(qualifiedAxiomName);
        AxiomListSource axiomListSource = new AxiomListSource(axiomList, axiomArchetype);
        return axiomListSource;
    }
    
    protected AxiomSource createAxiomSource(QualifiedName qualifiedAxiomName, Axiom axiom)
    {
        AxiomArchetype axiomArchetype = axiomArchetypeMap.get(qualifiedAxiomName);
        if (axiomArchetype == null)
            axiomArchetype = createAxiomArchetype(qualifiedAxiomName);
        return  new SingleAxiomSource(axiom);
    }
}
