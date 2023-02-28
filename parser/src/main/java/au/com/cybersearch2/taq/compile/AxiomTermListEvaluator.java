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
import java.util.List;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.provider.CallHandler;

/**
 * AxiomTermListEvaluator
 * Evaluates to populate a given AxiomTermList from an initialization template
 * @author Andrew Bowley
 * 7Jun.,2017
 */
public class AxiomTermListEvaluator
{
    protected static List<Term> EMPTY_TERM_LIST = Collections.emptyList();
    
    /** Qualified name of list to be populated */
    private final AxiomTermList axiomTermList;
    /** Template to evaluate axiom terms */
    private Template template;
    /** Function call evaluator which returns result as axiom term list */
    private CallHandler callHandler;
    
    /**
     * Evaluates AxiomTermList
     * @param axiomTermList AxiomTermList to populate
     * @param template Template to evaluate axiom terms
     */
    public AxiomTermListEvaluator(AxiomTermList axiomTermList, Template template)
    {
        this.axiomTermList = axiomTermList;
        this.template = template;
    }
    
    /**
     * Implements call evaluator which returning the result in an AxiomTermList
     * @param axiomTermList AxiomTermList to populate
     * @param callHandler Function call evaluator which returns result as axiom term list
     * @param template Template to evaluate axiom terms
    */
    public AxiomTermListEvaluator(AxiomTermList axiomTermList, CallHandler callHandler, Template template)
    {
        this.axiomTermList = axiomTermList;
        this.callHandler = callHandler;
        this.template = template;
    }
    
    /**
     * Returns list qualified name
     * @return QualifiedName object
     */
    public QualifiedName getQualifiedName()
    {
        return axiomTermList.getQualifiedName();
    }

    /**
     * @return the axiomKey
     */
    public QualifiedName getAxiomKey()
    {
        return axiomTermList.getKey();
    }

    /**
     * Returns list name
     * @return name
     */
   public String getName()
    {
        return axiomTermList.getQualifiedName().getName();
    }

   /**
    * Evaluate to create axiom list
    * @param id Modification id
    * @return AxiomTerm List object
    */
    public AxiomTermList evaluate(int id, ExecutionContext context)
    {
        List<Term> termList;
        if (template != null)
        {
            template.evaluate(context);
            termList = template.toArray();
        }
        else
            termList = EMPTY_TERM_LIST;
        setAxiom(termList);
        if (callHandler != null) {
            callHandler.setAxiomListener(axiomTermList.getAxiomListener());
            callHandler.evaluate(termList);
        }
        return axiomTermList;
    }

    /**
     * Backup initialization template
     * @param id Modification id
     */
    public void backup(int id)
    {
        if (template != null)
            template.backup(id != 0);
        if (callHandler != null)
            callHandler.backup(id);
    }

    /**
     * Returns list size
     * @return int
     */
    public int size()
    {
        return template != null ? template.getTermCount() : 0;
    }
    
    /**
     * toString
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(axiomTermList.getQualifiedName().toString());
        builder.append('(');
        if (template != null)
        {
            Term op1 = template.getTermByIndex(0);
            builder.append(op1.toString());
            int count = template.getTermCount();
            if (count > 1)
            {
                Term op2 = template.getTermByIndex(count - 1);
                builder.append(" ... ").append(op2.toString());
            }
        }
        builder.append(')');
        return builder.toString();
    }
    
    private AxiomTermList setAxiom(List<Term> termList) {
        AxiomArchetype archetype = new AxiomArchetype(axiomTermList.getKey());
        axiomTermList.setAxiom(new Axiom(archetype, termList));
        archetype.clearMutable();
        return axiomTermList;
     }
}
