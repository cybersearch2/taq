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
import au.com.cybersearch2.taq.interfaces.ExecutionTracker;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * AxiomListEvaluator
 * Evaluates to populate a given AxiomList from an initialization template list
 * @author Andrew Bowley
 * 7Jun.,2017
 */
public class AxiomListEvaluator implements ExecutionTracker
{
    private static final List<Template> EMPTY_TEMPLATE_LIST = Collections.emptyList();
  
    /** Axiom list to populate */
    private final AxiomList axiomList;
    /** List of templates, each defining the terms of an axiom to add to the list */
    private final  List<Template> initializeList;
    /** Template to initialize list of templates */
    private final  Template initializeTemplate;
    /** Flag set true to export list */
    protected boolean isPublic;
    /** ExecutionContext */
    private ExecutionContext context;
    
    /**
     * Construct AxiomListEvaluator object
     * @param axiomList Empty list to be populated
     * @param initializeList List of templates, each defining the terms of an axiom to add to the list
     * @param initializeTemplate Template to initialize list of templates
      */
    public AxiomListEvaluator(AxiomList axiomList, List<Template> initializeList, Template initializeTemplate)
    {
    	this.axiomList = axiomList;
        this.initializeList = initializeList == null ? EMPTY_TEMPLATE_LIST : initializeList;
        this.initializeTemplate = initializeTemplate;
    }
    
    /**
     * Returns list qualified name
     * @return QualifiedName object
     */
    public QualifiedName getQualifiedName()
    {
        return axiomList.getQualifiedName();
    }

    /**
     * @return the axiomKey
     */
    public QualifiedName getAxiomKey()
    {
        return axiomList.getKey();
    }

    /**
     * Returns list name
     * @return name
     */
    public String getName()
    {
        return axiomList.getQualifiedName().getName();
    }

    /**
     * Evaluate to create axiom list
     * @param id Modification id
     * @return AxiomList object
     */
    public AxiomList evaluate(int id)
    {
         if (isPublic)
            axiomList.setPublic(true);
        // Prepare to create axioms
        AxiomArchetype archetype = new AxiomArchetype(axiomList.getKey());
        int index = 0;
        Axiom axiom = null;
        if (initializeTemplate != null)
        {
            initializeTemplate.backup(true);
            initializeTemplate.evaluate(context);
            axiom = initializeTemplate.toAxiom();
        }
        for (Template template: initializeList)
        {
            if (axiom != null)
                template.unify(axiom, null);
            template.evaluate(context);
            List<Term> termList =  template.toArray();
            // Do not add empty axioms to list
            if (termList.size() > 0)
               axiomList.assignItem(index++, new Axiom(archetype, termList));
        }
        return axiomList;
    }

    /**
     * Backup initialization template(s)
     * @param id Modification id
     */
    public void backup(int id)
    {
        for (Template template: initializeList)
        {
            template.backup(id);
            if (template.getId() != id)
                template.backup(id != 0);
        }
        axiomList.clear();
    }

    /**
     * Returns flag set true if evaluator is in empty state
     * @return boolean
     */
    public boolean isEmpty()
    {
        return axiomList.isEmpty();
    }
    
    /**
     * Returns list size
     * @return int
     */
    public int size()
    {
       return axiomList.getLength();
    }

    /**
     * Set flag for export list
     * @param isPublic Flage to set
     */
    public void setPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }

	public AxiomList getAxiomList() {
		return axiomList;
	}

	@Override
	public void setExecutionContext(ExecutionContext context) {
		this.context = context;
	}
	
}
