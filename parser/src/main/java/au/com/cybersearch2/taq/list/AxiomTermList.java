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

import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.result.ResultList;

/**
 * AxiomTermList
 * List wrapper over Axiom object implementation. Axiom terms are referenced by index using array notation.
 * An AxiomListVariable is created to participate in item expressions. 
 * This object resides in an OperandMap never directly interacts with other operands.
 * @author Andrew Bowley
 * 19 Jan 2015
 */
public class AxiomTermList implements ItemList<Term>, AxiomContainer 
{
	/** Qualified name of list */
	protected QualifiedName qname;
    /** Axiom key */
    protected QualifiedName key;
    /** The Axiom being wrapped */
    protected Axiom axiom;
    /** Axiom listener is notified of axiom received each iteration */
	protected LocaleAxiomListener axiomListener;
	/** Axiom term names */
	protected List<String> axiomTermNameList;
    /** Source item to be updated in parser task */
    protected SourceItem sourceItem;
    /** Archetype identity used to detect axiom specification changes */
    protected String archetypeName;
    /** Flag set true if list is exported */
    protected boolean isPublic;

	/** Empty Axiom constant */
	static Axiom EMPTY_AXIOM;

	static
	{
		EMPTY_AXIOM = new Axiom("*");
	}
	
	/**
	 * Construct an AxiomTermList object. The initial axiom is empty until axiomListener is notified.
	 * @param qname Qualified name of list
	 * @param key Qualified name of Axiom
	 */
	public AxiomTermList(QualifiedName qname, QualifiedName key) 
 	{
		this.qname = qname;
		this.key = key;
		axiom = EMPTY_AXIOM;
		axiomListener = new LocaleAxiomListener(){

			@Override
			public boolean onNextAxiom(QualifiedName qname, Axiom nextAxiom, Locale locale) 
			{
				axiom = nextAxiom;
                TermListManager axiomArchetype = axiom.getArchetype();
                if ((archetypeName == null) || 
                    !axiomArchetype.toString().equals(archetypeName) ||
                    ((axiomTermNameList != null) && 
                     (axiom.getTermCount() > axiomTermNameList.size())))
                {
                    axiomTermNameList = axiom.getArchetype().getTermNameList();
                    archetypeName = axiomArchetype.toString();
                }
		        if (sourceItem != null)
		            sourceItem.setInformation(AxiomTermList.this.toString());
				return true;
		    }
			
			@Override
		    public QualifiedName getName() {
				 return qname;
		    }
		};
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

	public void setKey(QualifiedName key) {
		this.key = key;
	}

	/**
	 * Returns backing axiom
	 * @return Axiom object
	 */
	public Axiom getAxiom()
	{
		if (!qname.getName().equals(axiom.getName())) {
			return new Axiom(qname.getName(), axiom);
		}
	    return axiom;
	}
	
	/**
	 * @return the axiomTermNameList
	 */
    @Override
	public List<String> getAxiomTermNameList() 
	{
        if (axiomTermNameList == null)
        {
            if (!isEmpty())
                axiomTermNameList = axiom.getArchetype().getTermNameList();
            else
                return Collections.emptyList();
        }
		return axiomTermNameList;
	}

	/**
	 * @param axiomTermNameList the axiomTermNameList to set
	 */
    @Override
	public void setAxiomTermNameList(List<String> axiomTermNameList) 
	{
		this.axiomTermNameList = axiomTermNameList;
	}

	/**
	 * Returns axiom listener
	 * @return AxiomListener object
	 */
    @Override
	public LocaleAxiomListener getAxiomListener()
	{
		return axiomListener;
	}

	/**
	 * Sets axiom. An alternative to receiving it by axiom listener.
	 * @param axiom Axiom object
	 */
	public void setAxiom(Axiom axiom)
	{
		this.axiom = axiom;
        TermListManager archetype = axiom.getArchetype();
        List<String> archetypeNames = archetype.getTermNameList();
        if ((axiomTermNameList == null) || axiomTermNameList.isEmpty())
            axiomTermNameList = archetypeNames;
        archetypeName = archetype.toString();
		if (sourceItem != null)
		    sourceItem.setInformation(toString());
	}

    /**
     * clear
     * @see au.com.cybersearch2.taq.interfaces.ItemList#clear()
     */
    @Override
    public void clear() 
    {
        axiom = EMPTY_AXIOM;
        if (sourceItem != null)
            sourceItem.setInformation(toString());
    }

	/**
	 * getLength
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#getLength()
	 */
	@Override
	public int getLength() 
	{
		return axiom.getTermCount();
	}

	/**
	 * getQualifiedName
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#getQualifiedName()
	 */
    @Override
    public QualifiedName getQualifiedName()
    {
        return qname;
    }


	/**
	 * getName
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#getName()
	 */
	@Override
	public String getName() 
	{
		String name = qname.getName();
		return name.isEmpty()? qname.getTemplate() : name;
	}

	/**
	 * isEmpty
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#isEmpty()
	 */
	@Override
	public boolean isEmpty() 
	{
		return (axiom.getTermCount() == 0);
	}

	/**
	 * assignItem
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#assignItem(int, java.lang.Object)
	 */
	@Override
	public void assignItem(ListIndex listIndex, Term term) { 
		assignItem(listIndex.getIndex(), term);
	}
	
	
	@Override
	public void append(Term term) {
        assignItem(axiom.getTermCount(), term);
	}

	/**
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#getItem(int)
	 */
	@Override
	public Term getItem(ListIndex listIndex) 
	{
		int position = listIndex.getPosition();
		if (position == -1)
		    return axiom.getTermByIndex(listIndex.getIndex());
		else
		    return axiom.getTermByIndex(position);
	}

	/**
	 * hasItem
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#hasItem(int)
	 */
	@Override
	public boolean hasItem(ListIndex listIndex) 
	{
		int index = listIndex.getIndex();
		return (index >= 0) && (index < axiom.getTermCount());
	}

	/**
	 * Verify axiom has been assigned to this list and index is in bounds
	 * @param index index
	 */
	public void verify(int index) 
	{
		if (index >= axiom.getTermCount() || (index < 0))
			throw new IllegalStateException("AxiomTermList \"" + qname.toString() +"\" index " + index + " out of bounds");
	}

	/**
	 * iterator
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Term> iterator() 
	{
		return getIterable().iterator();
	}

	/**
	 * getIterable
	 * @see au.com.cybersearch2.taq.interfaces.ItemList#getIterable()
	 */
	@Override
	public Iterable<Term> getIterable() 
	{
		// Use copy of axiom in case clear() is called
        final Axiom axiom2 = new Axiom(axiom.getName());
        for (int i = 0; i < axiom.getTermCount(); i++)
        	axiom2.addTerm(axiom.getTermByIndex(i));
		return new Iterable<Term>()
		{
			@Override
			public Iterator<Term> iterator() 
			{
				return new Iterator<Term>()
				{
		            int index = 0;
		            
					@Override
					public boolean hasNext() 
					{
						return index < axiom2.getTermCount();
					}
		
					@Override
					public Term next() 
					{
						return axiom2.getTermByIndex(index++);
					}
		
					@Override
					public void remove() 
					{
					}
				};
			}
		};
	}

    /**
     * @return public flag
     */
    @Override
    public boolean isPublic()
    {
        return isPublic;
    }

    /**
     * @param isPublic Public flag
     */
    @Override
    public void setPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }

    /**
     * setSourceItem
     * @see au.com.cybersearch2.taq.interfaces.SourceInfo#setSourceItem(au.com.cybersearch2.taq.engine.SourceItem)
     */
    @Override
    public void setSourceItem(SourceItem sourceItem)
    {
        this.sourceItem = sourceItem;
    }

    /**
     * toString
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() 
    {
        StringBuilder builder = new StringBuilder("list<term> ");
        builder.append(qname.toString()).append('(');
        if (!qname.equals(key))
            builder.append(key.toString());
        builder.append(')');
        if (!axiom.equals(EMPTY_AXIOM))
            builder.append('{').append(axiom.getTermCount()).append('}');
        return builder.toString();
    }

    @Override
    public OperandType getOperandType()
    {
        return OperandType.TERM;
    }

	@Override
	public ItemList<Term> newInstance() {
		AxiomTermList newList = new AxiomTermList(qname, key);
		newList.setAxiom(axiom);
		return newList;
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public ResultList<Term> getSolution() {
		ResultList<Term> resultList = new ResultList<Term>() {

			private ArrayList<Term> items;

			@Override
			public QualifiedName getQualifiedName() {
				return qname;
			}

			@Override
			public OperandType getOperandType() {
				return OperandType.TERM;
			}

			@Override
			public ArrayList<Term> getList() {
				if (items == null) {
					items = new ArrayList<Term>();
					axiom.forEach(term -> items.add(term));
				}
				return items;
			}
			
		};
		resultList.getList();
		axiom = EMPTY_AXIOM;
		return resultList;
	}

	private void assignItem(int index, Term term) { 
	    if (axiom.getTermCount() == 0) {
	        if (index == 0) {
	            AxiomArchetype archetype = null;
	            if (key.getName().isEmpty())
                    archetype = new AxiomArchetype(new QualifiedName(key.getScope(), key.getTemplate()));
	            else
	                archetype = new AxiomArchetype(key);
	            Locale locale;
	            if (term instanceof OperatorTerm)
	            	locale = ((OperatorTerm)term).getOperator().getTrait().getLocale();
	            else
	            	locale = Locale.getDefault();
	            axiomListener.onNextAxiom(archetype.getQualifiedName(), new Axiom(archetype), locale);
	            archetype.setMutable();
	            axiom.addTerm(term);
	            axiomTermNameList.add(term.getName());
	        }
	        else
	            verify(index);
	    }
	    else if (index == axiom.getTermCount()) {
	    	((AxiomArchetype)axiom.getArchetype()).setMutable();
            axiom.addTerm(term);
            axiomTermNameList.add(term.getName());
	    } else {
	        verify(index);
	        axiom.getTermByIndex(index).setValue(term.getValue());
	    }
	}

}
