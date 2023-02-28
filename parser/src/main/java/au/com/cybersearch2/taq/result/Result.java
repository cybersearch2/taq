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
package au.com.cybersearch2.taq.result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.language.NameParser;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * Result
 * Contains result lists and axioms generated by a query
 * @author Andrew Bowley
 * 3 Mar 2015
 */
public class Result 
{
    private static final Iterator<Axiom> EMPTY_ITERATOR = 
        new Iterator<Axiom>(){

            @Override
            public boolean hasNext()
            {
                return false;
            }

            @Override
            public Axiom next()
            {
                return null;
            }};
            
    /** Empty collection */
    static Map<QualifiedName, ResultList<?>> EMPTY_LIST_MAP;
    /** Container of result lists accessible by Iterable interface */
	protected Map<QualifiedName, ResultList<?>> listMap;
    /** Container of result axioms */
    protected Map<QualifiedName, AxiomTermList> axiomMap;

	static
	{
		EMPTY_LIST_MAP = Collections.emptyMap();
	}
	
	/**
	 * Create Result object
	 * @param listMap Container of result axiom lists accessible by Iterable interface or null if none available
	 * @param axiomMap Container of result axioms, which may include exported basic lists accessible by an iterator
	 */
	public Result(Map<QualifiedName, ResultList<?>> listMap, Map<QualifiedName, AxiomTermList> axiomMap) 
	{
		this.listMap = listMap == null ? EMPTY_LIST_MAP : listMap;
		this.axiomMap = axiomMap;
	}

    /**
     * Returns iterator for result list specified by global namespace key
     * @param name Name of list
     * @return Axiom Iterator
     */
    public Iterator<Axiom> axiomIterator(String name)
    {
        NameParser nameParser = new NameParser(name);
        return getList(nameParser.getQualifiedName());
    }

    /**
     * Returns iterator for result list specified by global namespace key
     * @param scope Scope
     * @param name Name of list
     * @return Axiom Iterator
     */
	public Iterator<Axiom> axiomIterator(String scope, String name)
    {
    	return getList(new QualifiedName(scope, name));
    }

	/**
	 * Returns iterator for result list specified by key
	 * @param qname Qualified name of list
	 * @return Iterator of generic type Axiom
	 */
    public Iterator<Axiom> axiomIterator(QualifiedName qname)
	{
	    return getList(qname);
	}

    /**
     * Returns result axiom specified by name
     * @param name Name of axiom
     * @return Axiom object
     */
    public Axiom getAxiom(String name)
    {
    	AxiomTermList atl  = axiomMap.get(QualifiedName.parseGlobalName(name));
        return atl != null ? atl.getAxiom() : new Axiom(name);
    }

    /**
     * Returns result axiom specified by scope and name
     * @param scope Scope
     * @param name Name of axiom
     * @return Axiom object
     */
    public Axiom getAxiom(String scope, String name)
    {
    	QualifiedName qname = new QualifiedName(scope, name);
        Axiom axiom  = getAxiom(qname);
        if (axiom == null)
        	qname = new QualifiedTemplateName(scope, name);
        return getAxiom(qname);
    }

    /**
     * Returns result axiom specified by qualified name
     * @param qname Qualified name of axiom
     * @return Axiom object
     */
    public Axiom getAxiom(QualifiedName qname)
    {
    	Axiom axiom;
    	AxiomTermList atl  = axiomMap.get(qname);
        if (atl == null) {
    		if (qname.isNameEmpty())
            	axiom = new Axiom(qname.getTemplate());
    		else
        	    axiom = new Axiom(qname.getName());
        } else
        	axiom = atl.getAxiom();
        return axiom;
    }

    /**
     * Returns result axioms specified by key
     * @param qname Axiom key qualified name
     * @return Axiom list
     */
    public List<Axiom> getAxiomsByKey(QualifiedName qname)
    {
    	List<Axiom> axioms = new ArrayList<>();
        for (AxiomTermList atl: axiomMap.values()) {
        	if (atl.getKey().equals(qname))
        		axioms.add(atl.getAxiom());
        }
        return axioms;
    }

    /**
     * Returns result axioms specified by key
     * @param scope Scope
     * @param name Name of axiom
     * @return Axiom list
     */
    public List<Axiom> getAxiomcByKey(String scope, String name)
    {
    	QualifiedName qname = new QualifiedName(scope, name);
    	return getAxiomsByKey(qname);
    }
    
    /**
     * Returns axiom result specified by axiom key
     * @param qname Axiom key part name
     * @return xiom list
     */
    public List<Axiom> getAxiomsByKey(String name) {
    	return getAxiomsByKey(QualifiedName.parseGlobalName(name));
    }
    
	public Iterator<Long> integerIterator(String name)
    {
        return basicIterator(name);
    }
    
    public Iterator<Double> doubleIterator(String name)
    {
        return basicIterator(name);
    }
    
    public Iterator<BigDecimal> decimalIterator(String name)
    {
        return basicIterator(name);
    }
    
    public Iterator<BigDecimal> currencyIterator(String name)
    {
        return basicIterator(name);
    }
    
    public Iterator<String> stringIterator(String name)
    {
        return basicIterator(name);
    }
    
    public Iterator<Boolean> booleanIterator(String name)
    {
        return basicIterator(name);
    }
 
    public OperandType getListType(QualifiedName listName) {
    	ResultList<?> resultList = listMap.get(listName);
    	return resultList != null ? resultList.getOperandType() : null;
    }
    
    /**
     * Returns the result list for specified key
     * @param qname Qualified name of list
     * @return Iterable object
     */
    @SuppressWarnings("unchecked")
	protected Iterator<Axiom> getList(QualifiedName qname) 
    {
    	ResultList<?> axiomListIterable = listMap.get(qname);
        if (axiomListIterable == null)
            axiomListIterable = listMap.get(new QualifiedTemplateName(qname.getScope(), qname.getName()));
        if (axiomListIterable != null) {
        	if (axiomListIterable.getOperandType() != OperandType.AXIOM)
        		throw new ExpressionException("List does not contain axioms");
        	return ((ResultList<Axiom>)axiomListIterable).getList().iterator();
        } else
        	return  EMPTY_ITERATOR;
    }
  
    @SuppressWarnings("unchecked")
	private <T> Iterator<T> basicIterator(String name)
    {
        NameParser nameParser = new NameParser(name);
        QualifiedName qname = nameParser.getQualifiedName();
 		ResultList<T> basicListIterable = (ResultList<T>) listMap.get(qname);
        if (basicListIterable == null)
        	basicListIterable = (ResultList<T>) listMap.get(new QualifiedTemplateName(qname.getScope(), qname.getName()));
        if (basicListIterable != null) {
        	return ((ResultList<T>)basicListIterable).getList().iterator();
        } else
        	return  Collections.emptyIterator();
    }

}