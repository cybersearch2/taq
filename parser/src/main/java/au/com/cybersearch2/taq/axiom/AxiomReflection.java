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

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * Translates between the term names of an Axiom Archetype and Java field names
 *
 */
public class AxiomReflection {

	public static final String EMPTY_ARCHETYPE_ERROR = "Axiom archetype %s has no term names set";
	
	/** Axiom meta-data used to create, and test for, congruent axioms */
	private final AxiomArchetype archetype;
	/** List of axiom term names. If not supplied, the term names come from data object field names */
	private final List<NameMap> termNameList;
    /** Flag set true if archetype does not define any terms */
    private boolean isArchetypeEmpty;
	
	/**
	 * Construct AxiomReflection object
	 * @param archetype Meta-data used to create, and test for, congruent axioms
	 */
	public AxiomReflection(AxiomArchetype archetype) {
		this.archetype = new AxiomArchetype(archetype);
		termNameList = new ArrayList<>();
        if (archetype.getTermCount() > 0)
            for (int index = 0; index < archetype.getTermCount(); ++index)
            {
                String termName = archetype.getMetaData(index).getName();
                // Allow for convention snake-case used for term names
                NameMap nameMap = new NameMap(termName, snakeToCamel(termName));
                nameMap.setPosition(index);
                termNameList.add(nameMap);
            }
 	}

	/**
	 * Construct AxiomReflection object using given term names
	 * @param archetype Meta-data used to create, and test for, congruent axioms
     * @param termNames List mapping term names to Java field names
	 */
	public AxiomReflection(AxiomArchetype archetype, List<NameMap> termNames) {
        this(archetype);
        if ((termNames != null) && !termNames.isEmpty())
            alignTermNamesToArchetype(termNames);
        isArchetypeEmpty = archetype.getTermCount() == 0;
        if (isArchetypeEmpty && termNameList.isEmpty())
        	throw new IllegalStateException(String.format(EMPTY_ARCHETYPE_ERROR, archetype.getName()));
	}
	
    public List<NameMap> getTermNameList() {
		return termNameList;
	}

	public AxiomArchetype getArchetype() {
        return archetype;
    }

	public Axiom axiomInstance() {
		return archetype.newInstance();
	}
	
	public void clearMutable() {
		archetype.clearMutable();
	}
	
    public boolean isArchetypeEmpty() {
		return isArchetypeEmpty;
	}

	/**
     * Add term to list in specified position
     * @param termList Term list
     * @param index Position
     * @param term Parameter specifying term name and value
     */
	public void assignItem(List<Term> termList, int index, Parameter term) 
	{
		if (index < termList.size())
			termList.set(index, term);
		else
		{
			for (int i = termList.size(); i < index; i++)
				termList.add(null);
			termList.add(index, term);
		}
	}

    /**
     * Lock archetype - override if archetype needs modification before being locked
     */
    protected void lockArchetype()
    {
        archetype.clearMutable();
    }

    private void alignTermNamesToArchetype(List<NameMap> termNames) {
        if (termNameList.isEmpty())  // archetype has no terms 
            termNames.forEach(termName -> termNameList.add(new NameMap(termName)));
        else
        {
        	termNameList.forEach(nameMap -> {
        		for (NameMap termName: termNames) 
                    if (termName.getTermName().equalsIgnoreCase(nameMap.getTermName())) {
                        nameMap.setFieldName(termName.getFieldName());
                        break;
                    }
         	}); 
        }
        int index = 0;
        for (NameMap nameMap: termNameList)
            nameMap.setPosition(index++);
	}

    private String snakeToCamel(String str) {
        while (str.contains("_")) {
        	  
            // Replace the first occurrence
            // of letter that present after
            // the underscore, to capitalize
            // form of next letter of underscore
            str = str.replaceFirst(
                          "_[^_]",
                          String.valueOf(
                              Character.toUpperCase(
                                  str.charAt(
                                      str.indexOf("_") + 1))));
        }
        return str;    
    }

}
