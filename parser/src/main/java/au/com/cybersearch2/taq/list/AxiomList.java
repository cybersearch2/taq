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
package au.com.cybersearch2.taq.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.TermListIterable;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * AxiomList
 * List of AxiomTermList-wrapped Axions 
 * @author Andrew Bowley
 * 28 Jan 2015
 */
public class AxiomList extends ArrayItemList<Axiom> implements AxiomContainer, TermListIterable 
{
	private class AxiomListListener implements LocaleAxiomListener {

		@Override
		public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) 
		{
			assignItem(getLength(), axiom);
			TermListManager axiomArchetype = axiom.getArchetype();
			// Set term name list if out of sync 
			if ((archetypeName == null) || 
			     !axiomArchetype.toString().equals(archetypeName) ||
                 ((axiomTermNameList != null) && 
                  (axiom.getTermCount() > axiomTermNameList.size())))
			{
			    axiomTermNameList = axiom.getArchetype().getTermNameList();
			    archetypeName = axiomArchetype.toString();
			}
			return true;
		}
	}

	private static final String CONCATENATE_ERROR = "Cannot concatenate %s to %s";
	
	/** Axiom term names */
	private List<String> axiomTermNameList;
	/** Qualified list name */
	private QualifiedName qname;
    /** Axiom key */
	private QualifiedName key;
    /** Archetype identity used to detect axiom specification changes */
	private String archetypeName;
    /** Axiom listener is notified of axiom to add to list */
	private LocaleAxiomListener axiomListener;

	/**
	 * Construct an AxiomList object
	 * @param qname Name of axiom list
	 * @param key Axiom key, may be same as name of list
	 */
	public AxiomList(QualifiedName qname, QualifiedName key) 
	{
		super(OperandType.AXIOM, qname);
		this.qname = qname;
		this.key = key;
	}

	/**
	 * Construct an AxiomList object
	 * @param qname Name of axiom list
	 * @param key Axiom key, may be same as name of list
	 */
	public AxiomList(QualifiedName qname, List<Axiom> axiomList, String archetypeName) 
	{
		super(OperandType.AXIOM, qname, axiomList);
		this.qname = qname;
		this.archetypeName = archetypeName;
		key = qname;
	}
	
	/**
	 * Concatenate given axiom list to this list
	 * @param rightList The list to add, which is on the right in a concatenation expression
	 * @return this list post concatenation
	 */
    public AxiomList concatenate(AxiomList rightList)
    {
        if (rightList.isEmpty())
            // No items to add
            return this;
        if (isEmpty()) {
            // Adding to empty list, so set axiom key to same as on right 
            setKey(rightList.getKey());
            Axiom axiom = rightList.getItem(0);
			TermListManager axiomArchetype = axiom.getArchetype();
		    axiomTermNameList = axiom.getArchetype().getTermNameList();
		    archetypeName = axiomArchetype.toString();
        }  else {  
        	// Check that left and right lists are compatible, which is true if tboth share them same archetype 
            getArchetypeName();
            if ((archetypeName == null) || !archetypeName.equals(rightList.getArchetypeName()))
                // When archetypes cannot be compared or are different, fall back to ensuring term names align
                checkTermNameCongruence(rightList);
        }
        // Update this list and return 
        Iterator<Axiom> iterator = rightList.getIterable().iterator();
        int index = getLength();
        while (iterator.hasNext())
            assignItem(index++, iterator.next());
        return this;
    }
 
    AxiomTermList getItemAsList(int index) {
    	AxiomTermList axiomTermList = new AxiomTermList(qname, key);
    	axiomTermList.setAxiom(getItem(index));
    	return axiomTermList;
    }
    /**
     * Returns listener to add Axiom objects to this container
     * @return AxiomListener
     */
    @Override
	public LocaleAxiomListener getAxiomListener()
	{
    	if (axiomListener == null)
    		axiomListener = new AxiomListListener();
		return axiomListener;
	}

	/**
	 * Returns Axiom key
	 * @return String
	 */
	@Override
	public QualifiedName getKey()
	{
		return key;
	}

	/**
	 * Allow key to be updated when set to list qname
	 * @param key Qualified name of axioms in list
	 * @return flag set true if name changed
	 */
	public boolean setKey(QualifiedName key)
	{
	    if (this.key.equals(getQualifiedName()))
	    {
	        this.key = key;
	        return true;
	    }
	    return false;
	}
	
	/**
	 * Returns axiom term name list
	 * @return List of axiom term names
	 */
    @Override
	public List<String> getAxiomTermNameList() 
	{
        if (axiomTermNameList == null)
        {
            if (getLength() > 0) {
                TermListManager archetype = getItem(0).getArchetype();
                axiomTermNameList = archetype.getTermNameList();
            } else
                return Collections.emptyList();
        }
		return axiomTermNameList;
	}

	/**
	 * Set  axiom term name list
	 * @param axiomTermNameList The axiomTermNameList to set
	 */
    @Override
	public void setAxiomTermNameList(List<String> axiomTermNameList) 
	{
		this.axiomTermNameList = axiomTermNameList;
	}

 	public void assignItem(int index, Term term) {
		if (!(term.getValue() instanceof Axiom))
			throw new ExpressionException(String.format("%s passed when expecting axiom", term.getValue().getClass().getName()));
         assignItem(index, (Axiom) term.getValue());
	}

	/**
     * Concatenates copy of given axiom term list to this list
     * @param axiomTermList The axiom term list to add, which is on the right in a concatenation expression
     * @return this list post concatenation
     */
    @Override
    public AxiomList concatenate(Axiom axiom) {
        if (isEmpty()) {
            // Adding to empty list, so set axiom key to same as on right 
            //setKey(axiomTermList.getKey());
			TermListManager axiomArchetype = axiom.getArchetype();
		    axiomTermNameList = axiom.getArchetype().getTermNameList();
		    archetypeName = axiomArchetype.toString();
        } else {
            // Check that left and right lists are compatible, which is true if tboth share them same archetype 
            getArchetypeName();
            if ((archetypeName == null) || !archetypeName.equals(axiom.getArchetype().getName()))
                // When archetypes cannot be compared or are different, fall back to ensuring term names align
                checkTermNameCongruence(axiom);
        }
        // Update this list and return 
        int index = getLength(); 
        assignItem(index, axiom);
        return this;
    }
 
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("list<axiom> ");
		builder.append(getQualifiedName().toString());
		if (!getQualifiedName().equals(key))
		    builder.append('(').append(key.toString()).append(')');
		if (getLength() > 0)
		{
		    if (getLength() == 1)
		        builder.append(": ").append(getItem(0).toString());
		    else
		        builder.append('[').append(Integer.toString(getLength())).append(']');
		}
		return builder.toString();
	}

	/**
	 * getOperandType
	 * @see au.com.cybersearch2.taq.list.ArrayItemList#getOperandType()
	 */
    @Override
    public OperandType getOperandType()
    {
        return OperandType.AXIOM;
    }

	@Override
	public AxiomList newInstance() {
		return new AxiomList(qname, key);
	}
	
    protected List<Axiom> getItems() {
    	List<Axiom> valueList = new ArrayList<>();
    	iterator().forEachRemaining(item -> valueList.add(item));
    	return Collections.unmodifiableList(valueList);
    }

    private void checkTermNameCongruence(AxiomList rightList) {
    	if (!checkTermNameCongruence(axiomTermNameList))
		throw new ExpressionException(String.format(CONCATENATE_ERROR, qname.getName(), rightList.qname.getName()));
	}

    /**
     * Ensure term names align
     * @param axiom Axiom with which to compare term names
     * @throws ExpressionException if term names do not match 
     */
    private void checkTermNameCongruence(Axiom axiom)
    {
    	if (!checkTermNameCongruence(axiom.getArchetype().getTermNameList()))
    		throw new ExpressionException(String.format(CONCATENATE_ERROR, qname.getName(), axiom.toString()));
    }
    
    /**
     * Ensure term names align
     * @param rightNames Term names list on right hand side
     * @throws ExpressionException if term names do not match 
     */
    private boolean checkTermNameCongruence(List<String> rightNames)
    {
        int size = getAxiomTermNameList().size();
        boolean isCongruent = size == rightNames.size();
        if (isCongruent)
            for (int i = 0; i < axiomTermNameList.size(); i++)
            {
                if (!axiomTermNameList.get(i).equals(rightNames.get(i)))
                {
                    isCongruent = false;
                    break;
                }
            }
        return isCongruent;
    }

    /**
     * Returns current archetype name
     * @return name of the first item's archetype or empty string if list is empty
     */
    protected String getArchetypeName()
    {
        if (archetypeName == null)
        {
            if (getLength() > 0)
                archetypeName = getItem(0).getArchetype().getName();
            else
                return "";
        }
        return archetypeName;
    }
}
