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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.StructureType;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.terms.TermMetaData;

/**
 * Archetype
 * Foundation for constructing Axioms and Templates
 * @author Andrew Bowley
 * 2May,2017
 */
public abstract class Archetype <T extends TermList<P>, P extends Term> implements TermListManager, Comparable<Archetype<T,P>>, Serializable
{
    private static final long serialVersionUID = 7567670630018077130L;
    
    public static List<TermMetaData> EMPTY_LIST;
    
    static
    {
        EMPTY_LIST = Collections.emptyList();
    }

    /** Unique name of structure */
    protected QualifiedName structureName;
    /** Structure type defines usage */
    transient protected StructureType structureType;
    /** List of term meta data in same order as terms in structure */
    transient protected List<TermMetaData> termMetaList;
    /** Flag set true if this Structure allows updates. Set true on creation and cleared when all term data complete */
    transient protected boolean isMutable;
    /** Flag set true if all terms anonymous */
    transient protected boolean isAnonymousTerms;
    /** Flag set true if duplicate term names allowed */
    transient protected boolean isDuplicateTermNames;
    
    /**
     * Construct Archetype object
     * @param structureName Qualified name of archetype
     * @param structureType Structure type - axiom, template, choice, archetype
     */
    public Archetype(QualifiedName structureName, StructureType structureType)
    {
        this.structureName = structureName;
        this.structureType = structureType;
        termMetaList = EMPTY_LIST;
        isMutable = true;
        isAnonymousTerms = true;
    }

    /**
     * @return isMutable flag
     */
    public boolean isMutable()
    {
        return isMutable;
    }

    /**
     * Set isMutable flag
     */
    public void setMutable()
    {
        isMutable = true;
    }
    
    /**
     * Clear isMutable flag
     */
    public void clearMutable()
    {
        isMutable = false;
    }

    /**
     * @return isAnonymousTerms flag
     */
    public boolean isAnonymousTerms()
    {
        return isAnonymousTerms;
    }

    /**
     * Create item containing given terms
     * @param terms List of terms
     * @return T
     */
    public T itemInstance(List<P> terms)
    {
        if (terms != null)
        {
            if (termMetaList.isEmpty())
            {
                createTermMetaList(terms);
                clearMutable();
            }
            else
                checkTerms(terms);
        }
        return newInstance(terms);
    }

    @Override
    public QualifiedName getQualifiedName()
    {
        return structureName;
    }

    @Override
    public String getName()
    {
        return structureName.getName();
    }

    @Override
    public int getTermCount()
    {
        return termMetaList.size();
    }

    @Override
    public int getNamedTermCount()
    {
        int count = 0;
        for (TermMetaData termMetaData: termMetaList)
            if (!termMetaData.isAnonymous())
                ++count;
        return count;
    }

    @Override
    public int addTerm(TermMetaData termMetaData)
    {
        checkMutable(termMetaData);
        if (termMetaData.getIndex() == -1)
            termMetaData.setIndex(getTermCount());
        if (termMetaList.isEmpty())
            termMetaList = new ArrayList<TermMetaData>();
        else if (termMetaList.contains(termMetaData) && 
                (termMetaData.getLiteralType() != LiteralType.unspecified) &&
                !setLiteralType(termMetaData))
            throw new ExpressionException("Term " + termMetaData.getName() + " already exists in " + toString());
        if (termMetaData.getIndex() >= termMetaList.size())
            termMetaList.add(termMetaData);
        if (!termMetaData.isAnonymous())
            isAnonymousTerms = false;
        return termMetaData.getIndex();
    }

    @Override
    public void checkTerm(TermMetaData termMetaData)
    {
        if (isValidMetaData(termMetaData))
            return;
        // Check if type conversion will handle this term
        LiteralType altLiteralType;
        switch(termMetaData.getLiteralType())
        {
        case integer:
            altLiteralType = LiteralType.taq_double; break;
        case taq_double:
            altLiteralType = LiteralType.integer; break;
        case decimal:
            altLiteralType = LiteralType.taq_double; break;
        default:
            altLiteralType = null;
        }
        if (altLiteralType != null)
        {
            TermMetaData altMetaData = new TermMetaData(altLiteralType, termMetaData.getName(), termMetaData.getIndex());
            if (isValidMetaData(altMetaData))
                return;
            if (altLiteralType == LiteralType.integer)
            {
                altMetaData = new TermMetaData(LiteralType.decimal, termMetaData.getName(), termMetaData.getIndex());
                if (isValidMetaData(altMetaData))
                    return;
            }
        }
        throw new ExpressionException("Term " + termMetaData.getName() + " incompatible with definition for " + toString());
    }

    @Override
    public TermMetaData analyseTerm(Term term, int index)
    {
        if (index < termMetaList.size())
        {
            TermMetaData termMetaData = termMetaList.get(index);
            if (term.getName().isEmpty() && !termMetaData.isAnonymous())
            {
                term.setName(termMetaData.getName());
                isAnonymousTerms = false;
            }
        }
        return new TermMetaData(term, index);
    }

    @Override
    public int getIndexForName(String termName)  
    {
    	return getIndexForName(termName, false);
    }

    @Override
    public int getIndexForName(String termName, boolean caseInsensitiveNameMatch)
    {
        if (termName == null)
            return -1;
        int i = 0;
        for (TermMetaData termMetaData: termMetaList)
        {
            String name = caseInsensitiveNameMatch ? termMetaData.getName().toUpperCase() : termMetaData.getName();
            String toMatch = caseInsensitiveNameMatch ? termName.toUpperCase() : termName;
            if (/*!termMetaData.isAnonymous() &&*/ toMatch.equals(name))
                return i;
            ++i;
        }
        return -1;
    }

    @Override
    public List<String> getTermNameList()
    {
        List<String> termNameList = new ArrayList<String>(termMetaList.size());
        for (TermMetaData termMetaData: termMetaList)
            termNameList.add(termMetaData.getName());
        return termNameList;
    }

    @Override
    public boolean changeName(int index, String name)
    {
        if ((index < 0) || (index >= getTermCount()))
            return false;
        TermMetaData termMetaData = termMetaList.get(index);
        if (termMetaData.setName(name))
        {
            isAnonymousTerms = false;
            return true;
        }
        return false;
    }

    @Override
    public TermMetaData getMetaData(int index)
    {
        if ((index < 0) || (index >= getTermCount()))
            return null;
        return termMetaList.get(index);
        
    }
    
    @Override
    public void setDuplicateTermNames(boolean isDuplicateTermNames)
    {
        this.isDuplicateTermNames = isDuplicateTermNames;
    }

    @Override
    public int compareTo(Archetype<T,P> other)
    {
        return structureName.compareTo(other.structureName);
    }

    /**
     * Check validity of all terms
     * @param terms
     */
    private void checkTerms(List<P> terms)
    {
        if (termMetaList.isEmpty()) // Paranoid check
            throw new IllegalStateException("checkTerms() called before createTermMetaList()");
        int index = 0;
        for (Term term: terms)
        {
            checkTerm(new TermMetaData(term, index++));
        }
    }

    /**
     * Create term metadata list for given terms
     * @param terms List of terms
     */
    private void createTermMetaList(List<P> terms)
    {
        int index = 0;
        for (Term term: terms)
        {
            addTerm(new TermMetaData(term, index++));
        }
    }

    /**
     * Create new T instance given list of terms
     * @param terms List of terms
     * @return T
     */
    abstract protected T newInstance(List<P> terms);
    

    /**
     * Returns flag set true if termMetaList contains given term metadata.
     * Updates Literal type of list item too if unspecified
     * @param termMetaData TermMetaData object
     * @return boolean
     * @see #checkTerm(TermMetaData)
     */
    private boolean isValidMetaData(TermMetaData termMetaData)
    {
        for (TermMetaData item: termMetaList)
            if (item.compareTo(termMetaData) == 0)
            {
                if (item.getLiteralType() == LiteralType.unspecified)
                    item.setLiteralType(termMetaData.getLiteralType());
                return true;
            }
        return false;
    }
 
    protected void checkMutable(TermMetaData termMetaData)
    {
        if (!isMutable)
            throw new ExpressionException("Term " + termMetaData.toString() + " cannot be added to locked " + toString());
    }
    
    /**
     * Sets list item literal type if currently unspecified. Returns flag set true if 
     * operation succeeds.
     * @param termMetaData TermMetaData object
     * @return boolean
     * @see #addTerm(TermMetaData)
     */
    protected boolean setLiteralType(TermMetaData termMetaData)
    {
        for (TermMetaData item: termMetaList)
            if (item.compareTo(termMetaData) == 0)
            {
                if (item.getLiteralType() == LiteralType.unspecified)
                {
                	 LiteralType dataLiteralType = termMetaData.getLiteralType();
                	 if (dataLiteralType != LiteralType.unspecified)
                         item.setLiteralType(dataLiteralType);
                     return true;
                }
                if (!isDuplicateTermNames)
                    return false;
                return termMetaData.getIndex() != item.getIndex();
            }
        return false;
    }
}
