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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.operator.StringOperator;

/**
 * TemplateProperties
 * Collection of properties which can be declared in both query and template statements
 * @author Andrew Bowley
 * 9Sep.,2017
 */
public class TemplateProperties
{
    public static List<Term> EMPTY_TERM_LIST;
    static
    {
        EMPTY_TERM_LIST = Collections.emptyList();
    }
    
    /** Query properties */
    protected List<Term> queryTerms;
    /** Template properties */
    protected List<Term> templateTerms;

    /**
     * Construct TemplateProperties object
     */
    public TemplateProperties()
    {
        queryTerms = EMPTY_TERM_LIST;
        templateTerms = EMPTY_TERM_LIST;
    }

    /**
     * Construct TemplateProperties object as a copy of another 
     * @param otherProperties Properties of other template
     */
    public TemplateProperties(TemplateProperties otherProperties)
    {
        queryTerms = copy(otherProperties.queryTerms);
        templateTerms = copy(otherProperties.templateTerms);
    }

    /**
     * Set provided axiom with properties
     * @param axiom Empty axiom to populate
     * @param templateTerms Operand list
     * @return axiom to initialize template - maybe empty or null
     * @see #putInitData(String, Object)
     * @see #setInitData(List) 
     */
    public Axiom initialize(Axiom axiom, TermList<Operand> templateTerms)
    {
        List<Term> properties = getProperties();
        if (templateTerms.getTermCount() == 0)
            return axiom;
        int index = 0; // Map unnamed arguments to template term names
        for (Term argument: properties)
        {
            Operand term = null;
            // All parameters are Parameters with names set by caller and possibly empty for match by position.
            String argName = argument.getName();
            if (!argName.isEmpty())
                term = templateTerms.getTermByName(argName);
            if (term == null)
            {   // Place by position if argument name not available or not matching any term name
                if (index == templateTerms.getTermCount())
                    throw new ExpressionException("Argument at position " + (index + 1) + " out of bounds");
                term = templateTerms.getTermByIndex(index);
            }
            Operator operator = argument instanceof OperatorTerm ? ((OperatorTerm)argument).getOperator() : new StringOperator();
            axiom.addTerm(new OperatorTerm(term.getName(), argument.getValue(), operator));
            ++index;
        }
        return axiom;
    }

    /**
     * Set initial query value for one term
     * @param name Term name - can be empty for selection by position
     * @param value Term value
     */
    public void putInitData(String name, Object value)
    {
        if (queryTerms.isEmpty())
            // Assume empty list is EMPTY_TERM_LIST
           queryTerms = new ArrayList<Term> ();
        queryTerms.add(new Parameter(name, value));
    }

    /**
     * Set initial query values
     * @param termList List of terms. A term may be anonymous, but must not be empty.
     */
    public void setInitData(List<Term> termList)
    {
        if (queryTerms.isEmpty())
            // Assume empty list is EMPTY_TERM_LIST
            queryTerms = new ArrayList<Term>();
        else
            queryTerms.clear();
        queryTerms.addAll(termList);
    }
    
    /**
     * Set initialization data, used for seeding calculations
     * @param properties Initialization properties
     */
    public void setProperties(Map<String, Object> properties) 
    {
        if (!properties.isEmpty())
        {
            if (queryTerms.isEmpty())
                // Assume empty list is EMPTY_TERM_LIST
                queryTerms = new ArrayList<Term>();
            else
                queryTerms.clear();
            for (Map.Entry<String,Object> entry: properties.entrySet())
                queryTerms.add(new Parameter(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Add initialization data, used for seeding calculations
     * @param termList Initialization term list
     */
    public void addProperties(List<Term> termList) 
    {
        if (!termList.isEmpty())
        {
            if (templateTerms.isEmpty())
                // Assume empty list is EMPTY_TERM_LIST
                templateTerms = new ArrayList<Term>();
            else
                templateTerms.clear();
            templateTerms.addAll(termList);
            
        }
    }

    /**
     * Returns properties
     * @return List of terms, possibly empty
     */
    public List<Term> getProperties()
    {
        if (templateTerms.isEmpty())
            return queryTerms;
        if (queryTerms.isEmpty())
            return templateTerms;
        // Combine query and template terms
        List<Term> allProperties = new ArrayList<Term>();
        allProperties.addAll(queryTerms);
        allProperties.addAll(templateTerms);
        return allProperties;
    }

    /**
     * Returns array containing both query and template terms and
     * clears properties in preparation for query calll
     * @return Term list array
     */
    public List<Term>[] getInitData()
    {
        @SuppressWarnings("unchecked")
        List<Term>[] initDataCopy =  (ArrayList<Term>[]) Array.newInstance(ArrayList.class, 2);
        initDataCopy[0] = new ArrayList<Term>();
        initDataCopy[1] = new ArrayList<Term>();
        if (!queryTerms.isEmpty())
        {
            initDataCopy[0].addAll(queryTerms);
            queryTerms.clear();
        }
        if (!templateTerms.isEmpty())
        {
            initDataCopy[1].addAll(templateTerms);
            templateTerms.clear();
        }
        return initDataCopy;
    }

    /**
     * Restores properties saved in prior call to getInitData()
     * @param initData Term list array
     */
    public void setInitData(List<Term>[] initData)
    {
        if (!queryTerms.isEmpty())
            queryTerms.clear();
        if (!templateTerms.isEmpty())
            templateTerms.clear();
        if (!initData[0].isEmpty())
            queryTerms.addAll(initData[0]);
        if (!initData[1].isEmpty())
            templateTerms.addAll(initData[1]);
    }

    /**
     * Rreturns copy of properties
     * @param terms Properties to copy
     * @return Term list
     */
    private List<Term> copy(List<Term> terms)
    {
        if (terms.isEmpty())
            return EMPTY_TERM_LIST;
        List<Term> termsCopy = new ArrayList<Term>();
        termsCopy.addAll(terms);
        return termsCopy;
    }
}
