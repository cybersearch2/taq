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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.StructureType;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.terms.TermMetaData;

/**
 * TemplateArchetype
 * Template factory containing term metat-data including term names
 * @author Andrew Bowley
 * 5May,2017
 */
public class TemplateArchetype extends Archetype<Template, Operand> implements Serializable
{
    private static final long serialVersionUID = 5322860830312952352L;
    
    transient protected Map<QualifiedName,int[]> termMappingMap;
	private OperandType operandType;
   
    /**
     * Construct TemplateArchetype object
     * @param structureName Qualified name which uniquely identifies the templates being produced -must have a template part
     */
    public TemplateArchetype(QualifiedName structureName)
    {
        super(structureName, StructureType.template);
        setDuplicateTermNames(true);
        if (structureName.getTemplate().isEmpty())
            throw new IllegalArgumentException("Template qualified name must have a template part");
        termMappingMap = new HashMap<QualifiedName,int[]>();
        
    }

    /**
     * Returns array of indexes mapping operands registered in this archetype to terms of an axiom archetype
     * @param pairArchetype Axiom archetype narrowed to TermListManager
     * @return int[]
     */
    public int[] getTermMapping(TermListManager pairArchetype)
    {
    	return getTermMapping(pairArchetype, false);
    }
    
    /**
     * Returns array of indexes mapping operands registered in this archetype to terms of an axiom archetype
     * @param pairArchetype Axiom archetype narrowed to TermListManager
     * @param caseInsensitive Flag set true if matching to be case-insensitive
     * @return int[]
     */
    public int[] getTermMapping(TermListManager pairArchetype, boolean caseInsensitive)
    {
        if (pairArchetype.getTermCount() == 0)
            // Return empty mapping if pair archetype is empty - not expected to happen
            return new int[]{};
        QualifiedName pairQName = pairArchetype.getQualifiedName();
        int[] termMapping = termMappingMap.get(pairQName);
        if (termMapping == null)
        {
            termMapping = createTermMapping(pairArchetype, caseInsensitive);
            termMappingMap.put(pairQName, termMapping);
        }
        return termMapping;
    }
 
    public OperandType getOperandType() {
		return operandType;
	}

	public void setOperandType(OperandType operandType) {
		this.operandType = operandType;
	}

	/**
     * Creates and returns array of indexes mapping operands registered in this archetype to terms of an axiom archetype
     * @param pairArchetype Axiom archetype narrowed to TermListManager
     * @param caseInsensitive Flag set true if matching to be case-insensitive
     * @return int[]
     */
    protected int[] createTermMapping(TermListManager pairArchetype, boolean caseInsensitive)
    {
        int[] termMapping = new int[getTermCount()];
        for (int i = 0; i < termMapping.length; i++)
            termMapping[i] = -1;
        int index = 0;
        boolean isAnonymousTerms = pairArchetype.isAnonymousTerms();
        for (TermMetaData termMetaData: termMetaList)
        {
            int pairIndex = -1;
            TermMetaData pairMetaData = null;
            if (isAnonymousTerms)
            {
                if (index == pairArchetype.getTermCount())
                    break;
                pairIndex = index;
                pairMetaData = pairArchetype.getMetaData(pairIndex);
                if ((termMetaData.getLiteralType() == pairMetaData.getLiteralType()) ||
                        (termMetaData.getLiteralType() == LiteralType.object) ||    
                        areConvertibleTypes(termMetaData.getLiteralType(), pairMetaData.getLiteralType() )) 
                {
                    pairArchetype.changeName(index, termMetaData.getName());
                    termMapping[index] = index; 
                }
                else
                    // Remaining items left set to -1 to indicate "no mapping"
                    break;
            }
            else
            {
                pairIndex = pairArchetype.getIndexForName(termMetaData.getName(), caseInsensitive);
                if (pairIndex != -1)
                    termMapping[index] = pairIndex; 
            }
            ++index;
        }
        return termMapping;
    }

    /**
     * Returns flag set true if one literal type is convertable to the second literal type
     * @param literalType First literal type is for an operand
     * @param literalType2 Secound literal type is for an axiom term
     * @return boolean
     */
    protected boolean areConvertibleTypes(LiteralType literalType,  LiteralType literalType2)
    {
        switch (literalType)
        {
        case integer:
        case taq_double:
        case decimal:
            switch(literalType2)
            {
            case integer:
            case taq_double:
            case decimal:
            case string:
                return true;
            default:
            }
        default:
       }
        return false;
    }

    /**
     * Create new Template instance
     * @see au.com.cybersearch2.taq.pattern.Archetype#newInstance(java.util.List)
     */
    @Override
    protected Template newInstance(List<Operand> terms)
    {
        return new Template(this, terms);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Archetype " + structureName.toString();
    }

}
