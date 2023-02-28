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
package au.com.cybersearch2.taq.query;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * Solution
 * Container to hold axioms produced processing a query chain
 * @author Andrew Bowley
 * 8 Jan 2015
 */
public class Solution 
{
    protected static String EMPTY_KEY = "";
    
	/** Axioms referenced by key */
	private final Map<String, Axiom> axiomMap;
    /** Keys used for put() in sequence order */
	private final Deque<String> keyStack;
	private final Locale globalLocale;
	
	/** Optional axiom listeners referenced by key */
	private Map<QualifiedName, List<LocaleAxiomListener>> axiomListenerMap;
	/** Solution handler for self-evaluation */
	private SolutionHandler solutionHandler;

	/**
	 * Construct a Solution object
	 */
	public Solution(Locale globalLocale) 
	{
		this.globalLocale = globalLocale;
		axiomMap = new HashMap<String, Axiom>();
		keyStack = new ArrayDeque<>();
	}

	/**
	 * Set solution handler for self-evaluation
	 * @param solutionHandler Solution handler
	 */
	public void setSolutionHandler(SolutionHandler solutionHandler)
    {
        this.solutionHandler = solutionHandler;
    }

	/**
	 * Returns key used for last put()
	 * @return key or null if not available
	 */
    public String getCurrentKey()
    {
        return keyStack.peekFirst();
    }

    public int getStackSize() 
    {
    	return keyStack.size();
    }
 
    public Locale getGlobalLocale() {
		return globalLocale;
	}

	public String[] getStack() {
    	String[] array = new String[keyStack.size()];
    	int[] index = new int[] {0};
    	keyStack.forEach(item -> array[index[0]++] = item);
    	return array;
    }
    
    /**
	 * Returns count of axioms
	 * @return int
	 */
	public int size() 
	{
		return axiomMap.size();
	}

    /**
     * Add axiom to this object and notify listener if present
     * @param key Name of axiom
     * @param axiom Axiom
     */
    public void put(String key, Axiom axiom) 
    {
    	put(key, axiom, globalLocale);
    }
    
    /**
     * Add axiom to this object and notify listener if present
     * @param key Name of axiom
     * @param axiom Axiom
     */
    public void put(String key, Axiom axiom, Locale locale) 
    {
        axiomMap.put(key, axiom);
        QualifiedName qname = QualifiedName.parseTemplateName(key);
        if ((axiomListenerMap != null) && axiomListenerMap.containsKey(qname) && (axiom.getTermCount() > 0)) {
        	Axiom deepCopy = copy(axiom);
            for (LocaleAxiomListener axiomListener: axiomListenerMap.get(qname))
                axiomListener.onNextAxiom(axiom.getArchetype().getQualifiedName(), deepCopy, locale);
        }
        keyStack.push(key);
    }

    private Axiom copy(Axiom axiomToCopy) {
    	AxiomArchetype axiomArchetype = (AxiomArchetype) axiomToCopy.getArchetype();
        Axiom axiom = new Axiom(axiomArchetype);
        boolean mutable = axiomArchetype.isMutable();
        axiomArchetype.clearMutable();
        for (int i = 0; i < axiomToCopy.getTermCount(); ++i) {
        	Term term = axiomToCopy.getTermByIndex(i);
        	Object value = term.getValue();
        	if (value instanceof Axiom) {
        		Axiom axiomValue = (Axiom)value;
                axiom.addTerm(new Parameter(term.getName(), copy(axiomValue)));
        	} else if (value instanceof AxiomTermList)  {
        		AxiomTermList termListValue = (AxiomTermList)value;
        		AxiomTermList termListCopy = new AxiomTermList(termListValue.getQualifiedName(), termListValue.getKey());
        		termListCopy.setAxiom(copy(termListValue.getAxiom()));
                axiom.addTerm(new Parameter(term.getName(), termListCopy));
        	} else if (value instanceof AxiomList)  {
        		AxiomList listValue = (AxiomList)value;
        		AxiomList listCopy = new AxiomList(listValue.getQualifiedName(), listValue.getKey());
                Iterator<Axiom> iterator = listValue.getIterable().iterator();
                int index = 0;
                while (iterator.hasNext())
                	listCopy.assignItem(index++, copy(iterator.next()));
                axiom.addTerm(new Parameter(term.getName(), listCopy));
       	} else
                axiom.addTerm(term);
        }
        if (mutable)
            axiomArchetype.setMutable();
		return axiom;
	}

	/**
     * Remove axiom referenced by key
     * @param key Key
     */
    public void remove(String key) 
    {
        if (axiomMap.remove(key) != null) {
        	if (keyStack.contains(key)) {
		        int size = keyStack.size();
		        for (int i = 0; i < size; ++i) {
		        	String popped = keyStack.pop();
		        	if (key.equals(popped))
		        		break;
		        }
        	}
        }
    }

	/**
	 * Returns set of axiom keys
	 * @return Set of generic type String
	 */
	public Set<String> keySet() 
	{
		return axiomMap.keySet();
	}

	/**
	 * Returns axiom referenced by key
	 * @param key Key
	 * @return Axiom
	 */
	public Axiom getAxiom(String key)
	{
	    Axiom axiom = axiomMap.get(key);
		return axiom == null ? new Axiom(key) : axiom;
	}

	public void put(String key, Parameter... parameters) {
		Axiom axiom = new Axiom(key, parameters);
		put(key, axiom, globalLocale);
	}

	/**
	 * Clear axiom container. Has no impact on axiom listeners.
	 */
	public void reset() 
	{
		axiomMap.clear();
        keyStack.clear();
	}

	/**
	 * Returns term value as Object referenced by axiom key and term name
	 * @param key Axiom key
	 * @param name Term name
	 * @return Object or null if axiom or term not found
	 */
	public Object getValue(String key, String name)
	{
		Axiom axiom = axiomMap.get(key);
		if (axiom != null)
		{
			Term term = axiom.getTermByName(name);
			if (term != null)
				return term.getValue();
		}
		return null;
	}

	/**
	 * Self evaluate using external solution handler
	 * @return EvaluationStatus enum COMPLETE or SHORT_CIRCUIT
	 */
	public EvaluationStatus evaluate()
	{
        if ((solutionHandler != null) &&
             !solutionHandler.onSolution(this))
            return EvaluationStatus.SHORT_CIRCUIT;
        return EvaluationStatus.COMPLETE;
	}
	
	/**
	 * Returns term value as String referenced by axiom key and term name
	 * @param key Axiom key
	 * @param name Term name
	 * @return String or null if axiom or term not found
	 */
	public String getString(String key, String name)
	{
		Object object = getValue(key, name);
		return object == null ? null : object.toString();
	}

	/**
	 * Set axiom listener for specified axiom key
	 * @param key
	 * @param axiomListener LocaleAxiomListener object
	 */
	void setAxiomListener(QualifiedName key, LocaleAxiomListener axiomListener) 
	{
		List<LocaleAxiomListener> axiomListenerList = null;
		if (axiomListenerMap == null)
			axiomListenerMap = new HashMap<>();
		else
			axiomListenerList = axiomListenerMap.get(key);
		if (axiomListenerList == null)
		{
			axiomListenerList = new ArrayList<>();
			axiomListenerMap.put(key, axiomListenerList);
		}
		axiomListenerList.add(axiomListener);
	}
	
	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		return axiomMap.toString();
	}


}
