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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.OperandVisitor;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.terms.TermMetaData;

/**
 * ArchiveIndexHelper
 * Completes parser task of adding metadata for template operands contained in operand tree to the archetype.
 * Also generates fix ups for operands referenced by other templates so they have correct archive indexes at unification.
 * @author Andrew Bowley
 * 11May,2017
 */
public class ArchiveIndexHelper implements OperandVisitor
{
    /** Current archive index used when visiting operands */
    private int index;
    /** Map of term names and archive indexes used to detect multiple occurrences of the same 
     *  unification pairing eg. same operand appearing in different places */
    private Map<String, Integer> indexMap;
    /** Template archetype to be updated with operand metadata */
    private TermListManager archetype;
    /** Template to assemble */
    private Template template;
 
    /**
     * Construct ArchiveIndexHelper object
     * @param template Template to assemble
     */
    public ArchiveIndexHelper(Template template)
    {
        this.archetype = template.getTemplateArchetype();
        this.template = template;
    }
    
    /**
     * Set term meta data in archetype for template operands in evaluation tree. 
     * Has 2 passes. The first if for operands in template namespace, where operands are bound to the archetype.
     * The second pass only updates archtypes and covers operands outside the template namespace.
     * @param pass Which one of two passes - 1 or 2.
     */
    public void setOperandTree(int pass) 
    {
        getIndexMap();
        List<Operand> operandList = new ArrayList<Operand>();
        for (int i = 0; i < template.getTermCount(); i++)
        {
            Operand operand = template.getTermByIndex(i);
            boolean isTemplateOperand = operand.getQualifiedName().getTemplate().equals(template.getQualifiedName().getTemplate());
            if (((pass == 1) && isTemplateOperand) || 
                 ((pass == 2) && !isTemplateOperand))
            {
                TermMetaData metaData = archetype.getMetaData(i);
                if (!metaData.isAnonymous())
                    indexMap.put(metaData.getName(), i);
                operandList.add(operand);
                if (operand.getArchetypeId() == 0)
                {
                    operand.setArchetypeId(template.getId());
                    setArchiveIndex(operand, i);
                }
            }
        }
        index = pass == 1 ? template.getTermCount() : archetype.getTermCount();
        for (Operand operand: operandList)
        {
            OperandWalker operandWalker = new OperandWalker(operand);
            operandWalker.visitAllNodes(this);
        }
        // Remove indexMap reference as it is only needed while visiting operands
        indexMap = null;
        if (pass == 2)
            archetype.clearMutable();
    }

    /**
     * next operand
     * @see au.com.cybersearch2.taq.interfaces.OperandVisitor#next(au.com.cybersearch2.taq.interfaces.Operand, int)
     */
    @Override
    public boolean next(Operand operand, int depth)
    {
        String name = operand.getName();
        // Skip anonymous Evaluator operands
        if ((depth > 1) && !name.isEmpty() && template.isInSameSpace(operand)) 
        {    
            // Set operand archive index or create fix up if a different value is already assigned
            getIndexMap();
            boolean containsKey = indexMap.containsKey(name);
            // Sometimes names appear more than once due to list variable naming
            // Just use a single index value as these variables do not perform unification
            int indexForName = template.getTemplateArchetype().getIndexForName(name);
            if (containsKey || (indexForName != -1))
            {
                if (operand.getArchetypeId() == 0)
                    // Index for name already assigned. Assumes meta data is identical to that of original operand.
                    setArchiveIndex(operand, containsKey ? indexMap.get(name) : indexForName);
            }
            else
            {   // Add term meta data to archetype and index map
                int archiveIndex = index++;
                indexMap.put(name, archiveIndex);
                if (archiveIndex >= archetype.getTermCount())
                    archetype.addTerm(new TermMetaData(operand, archiveIndex));
                if (operand.getArchetypeId() == 0)
                    setArchiveIndex(operand, archiveIndex);
            }
            if (operand.getArchetypeId() == 0)
                operand.setArchetypeId(template.getId());
        }
        // Keep going
        return true;
    }

    /**
     * Set archive index in operand or add fix up if required
     * @param operand The operand to set
     * @param archiveIndex The index value to set
     */
    private void setArchiveIndex(Operand operand, int archiveIndex)
    {
        if (operand.getArchetypeIndex() == -1)
            operand.setArchetypeIndex(archiveIndex);
    }

    /**
     * Returns index map, creating it if required 
     * @return index map
     */
    private Map<String, Integer> getIndexMap()
    {
        if (indexMap == null)
            indexMap = new HashMap<String, Integer>();
        return indexMap;
    }

}
