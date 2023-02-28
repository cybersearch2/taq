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
package au.com.cybersearch2.taq.axiom;

import java.util.ArrayList;
import java.util.Iterator;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TermList;

/**
 * TemplateAxiomSource
 * @author Andrew Bowley
 * 3Sep.,2017
 */
public class TemplateAxiomSource implements AxiomSource
{
    protected Archetype<Axiom,Term> archetype;
    protected Template template;
    protected AxiomList axiomList;
    
    /**
     * Construct TemplateAxiomSource object
     * @param template Template
     */
    public TemplateAxiomSource(Template template)
    {
        this.template = template;
    }

	@Override
    public Iterator<Axiom> iterator(ExecutionContext context)
    {
        template.backup(true);
        Axiom axiom = new Axiom(template.getKey());
        template.getProperties().initialize(axiom, (TermList<Operand>)template);
        if ((axiom != null) && (axiom.getTermCount() > 0)) {
        	if ((context != null) && context.isCaseInsensitiveNameMatch())
        		template.unifyCaseInsensitive(axiom, null);
        	else
        		template.unify(axiom, null);
        }
        if (template.evaluate(context) == EvaluationStatus.COMPLETE)
            axiomList = (AxiomList) template.getTermByIndex(0).getValue();
        else
        	axiomList = null;
        if (axiomList == null)
        	return new ArrayList<Axiom>().iterator();
        return axiomList.getIterable().iterator();
    }

    @Override
    public Archetype<Axiom, Term> getArchetype()
    {
        if (axiomList != null)
            return getAxiomListArchetype();
        if (archetype == null)
            archetype = getEmptyArchetype();
        return archetype;
    }

    private Archetype<Axiom, Term> getEmptyArchetype()
    {
        AxiomArchetype archetype = new AxiomArchetype(QualifiedName.ANONYMOUS);
        archetype.clearMutable();
        return archetype;
    }

    @SuppressWarnings("unchecked")
    private Archetype<Axiom, Term> getAxiomListArchetype()
    {
        if (!axiomList.isEmpty())
            return (Archetype<Axiom, Term>) axiomList.getItem(0).getArchetype();
        AxiomArchetype archetype = new AxiomArchetype(axiomList.getKey());
        for (String termName: axiomList.getAxiomTermNameList())
            archetype.addTermName(termName);
        archetype.clearMutable();
        return archetype;
    }
}
