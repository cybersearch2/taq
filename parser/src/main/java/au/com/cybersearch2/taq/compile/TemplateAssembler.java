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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.axiom.TemplateAxiomSource;
import au.com.cybersearch2.taq.compiler.CompilerException;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.ArchiveIndexHelper;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;

/**
 * TemplateAssembler
 * @author Andrew Bowley
 * 12May,2017
 */
public class TemplateAssembler
{
    /** The templates mapped by qualified name */
    private final Map<QualifiedName, Template> templateMap;
    /**  Qualified names of incidental nested receiver templates during compilation */
    private final Deque<QualifiedName> receiverStack;
    /** The templates enclosed by receivers mapped by qualified name */
    private Map<QualifiedName, List<Template>> receiverMap;

    /**
     * Construct TemplateAssembler object
     */
    public TemplateAssembler()
    {
        templateMap = new HashMap<>();
        receiverStack = new ArrayDeque<>();
    }
    /**
     * Add contents of another TemplateAssembler to this object
     * @param templateAssembler Other TemplateAssembler object
     */
    public void addAll(TemplateAssembler templateAssembler) 
    {
        templateMap.putAll(templateAssembler.templateMap);
    }

    /**
     * Returns set of qualified name template keys
     * @return QualifiedName set
     */
    public Set<QualifiedName> getTemplateNames()
    {
        return templateMap.keySet();
    }

    /**
     * Returns a newly created template which can then be referenced by qualified name
     * @param qualifiedName Qualified template name, or axiom name if for choice
     * @param templateType - template/calculator/choice
     * @return Template object
     */
    public Template createTemplate(QualifiedName qualifiedName, TemplateType templateType)
    {
        boolean isCalculator = templateType == TemplateType.calculator;
        boolean isChoice = templateType == TemplateType.choice;
        TemplateArchetype archetype = new TemplateArchetype(qualifiedName);
        Template template = new Template(archetype);
        template.setCalculator(isCalculator | isChoice);
        if (isChoice)
            template.setChoice(true);
        templateMap.put(qualifiedName, template);
        return template;
    }

    /**
     * Add a new choice template to this ParserAssembler
     * @param qualifiedName Qualified template name or axiom name if for Choice
     * @param key Template key
     * @return Template object
     */
    public ITemplate createChoiceTemplate(QualifiedName qualifiedName, String key)
    {
    	if (templateMap.containsKey(qualifiedName))
    		throw new CompilerException(String.format("Template '%s' is duplicated", qualifiedName.toString()));
        TemplateArchetype archetype = new TemplateArchetype(qualifiedName);
        Template template = new Template(key, archetype);
        template.setCalculator(true);
        template.setChoice(true);
        templateMap.put(qualifiedName, template);
        return template;
    }

    /**
     * Returns choice template given an outer tempate and a qualified name
     * @param outerTemplate Outer template to which this choice is attached
     * @param choiceQualifiedName Qualified name
     * @return Template object
     */
    public Template createChoiceTemplate(Template outerTemplate, QualifiedName choiceQualifiedName)
    {
    	Template template = getTemplate(choiceQualifiedName);
    	if (template == null)
    		throw new CompilerException(String.format("Template '%s' not found", choiceQualifiedName.toString()));
        Template choiceTemplate = outerTemplate.choiceInstance(template);
        templateMap.put(choiceTemplate.getQualifiedName(), choiceTemplate);
        if (!receiverStack.isEmpty())
        	addReceiverTemplate(choiceTemplate);
        return choiceTemplate;
    }

    /**
     * Returns replicate of given template
     * @param template Template to replicate
     * @param templateName Qualified name of replicate
     * @return Template object
     */
	public Template createTemplate(Template template, QualifiedName templateName) {
    	template = new Template(template, templateName);
        templateMap.put(templateName, template);
		return template;
	}

    /**
     * Returns list of templates enclosed by a receiver, identified by qualified name
     * @param qname Qualified name
     * @return Template list, may be empty
     */
    public List<Template> getTemplateList(QualifiedName qname) {
    	List<Template> templateList = receiverMap.get(qname);
    	return templateList != null ? templateList : Collections.emptyList();
    }
    
	/**
     * Add a term to a template
     * @param qualifiedTemplateName Qualified template name
     * @param term Operand object
     */
    public void addTemplate(QualifiedName qualifiedTemplateName, Operand term)
    {
        ITemplate template = templateMap.get(qualifiedTemplateName);
        template.addTerm(term);
    }

    /**
     * Set template properties - applies only to Calculator
     * @param qualifiedTemplateName Qualified template name
     * @param properties Properties
     */
    public void addTemplate(QualifiedName qualifiedTemplateName, List<Term> properties)
    {
        Template template = templateMap.get(qualifiedTemplateName);
        template.getProperties().setInitData(properties);
    }

    /**
     * Returns template with specified qualified name
     * @param qualifiedTemplateName Qualified template name
     * @return Template object or null if template not found
     */
    public Template getTemplate(QualifiedName qualifiedTemplateName)
    {
        return (Template) templateMap.get(qualifiedTemplateName);
    }

    /**
     * Returns template with specified name
     * @param textName Text name
     * @return Template object or null if template not found
     */
    public Template getTemplate(String textName)
    {
        return templateMap.get(QualifiedName.parseTemplateName(textName));
    }

    /**
     * Returns inner template with specified key
     * @param key Qualified name with parts to find template 
     * @return Template object or null if template not found
     */
	public ITemplate getInnerTemplateByKey(QualifiedName key) {
		String templateKey = key.getTemplate() + "." + key.getName();
		for (ITemplate template: templateMap.values()) {
			if (template.isInnerTemplate() && 
				templateKey.equals(template.getKey()) &&
				key.getTemplateScope().equals(template.getQualifiedName().getScope()))
				return template;
		}
		return null;
	}

    /**
     * Returns receiver template with given name
     * @param outerTemplateName Qualified name of head template
     * @param name Receiver name
     * @return Template object
     */
    public ITemplate createReceiverTemplate(QualifiedName outerTemplateName, String name) 
    {
        Template template = getTemplate(outerTemplateName);
        Template receiverTemplate = template.innerTemplateInstance(name, TemplateType.template, null);
        templateMap.put(receiverTemplate.getQualifiedName(), receiverTemplate);
        if (receiverMap == null)
        	receiverMap = new HashMap<>();
        receiverMap.put(receiverTemplate.getQualifiedName(), Collections.emptyList());
        receiverStack.push(receiverTemplate.getQualifiedName());
        return receiverTemplate;
    }
 
    /**
     * Pop receiver template stack. Then, if the stack is not empty,  add all templates enclosed 
     * by this receiver to the head of the stack.
     */
    public void popReceiver() {
    	QualifiedName qname = receiverStack.pop();
    	if (!receiverStack.isEmpty()) {
			List<Template> templateList = receiverMap.get(receiverStack.peek());
			if ((templateList != null) && !templateList.isEmpty()) {   
				List<Template> currentList = receiverMap.get(qname);
				if (currentList.isEmpty()) {
					currentList = new ArrayList<>();
					receiverMap.put(qname, currentList);
				}
				currentList.addAll(templateList);
			}
    	}
    }

    /**
     * Create new template and add to head template chain
     * @param outerTemplateName Qualified name of head template
     * @return Template object
     */
    public Template chainTemplate(QualifiedName outerTemplateName) 
    {
        Template template = getTemplate(outerTemplateName);
        return chainTemplate(template);
    }
    
    /**
     * Create new template and add to head template chain
     * @param template Outer template
     * @return Template object
     */
    public Template chainTemplate(Template template) 
    {
        Template chainTemplate = template.innerTemplateInstance(TemplateType.template);
        templateMap.put(chainTemplate.getQualifiedName(), chainTemplate);
        if (!receiverStack.isEmpty())
        	addReceiverTemplate(chainTemplate);
        return chainTemplate;
    }

    /**
     * Create new template and add to head template chain
     * @param template Outer template
     * @return Template object
     */
    public Template chainTemplate(ITemplate template) 
    {
    	return chainTemplate((Template)template);
    }
    
    /**
     * Run parser task for every non=empty template. For each operand in the template, 
     * the task walks the operand tree and adds all terms to the archetype which belong
     * to the template name space.
     */
    public void doParserTask()
    {
        for (Template template: templateMap.values())
            if ((template.getTermCount() > 0) && !(template.isChoice() && template.isInnerTemplate()) && !template.isReplicate())
            {
                // Complete archetype initialization. This cannot be performed earlier due to fact parser tasks, 
                // which can modify operand terms, run after template construction.
                // Note replicate templates share the master fixUpList as this operation can only be performed once.
                ArchiveIndexHelper archiveIndexHelper = new ArchiveIndexHelper(template);
                archiveIndexHelper.setOperandTree(1);
            }
        for (Template template: templateMap.values())
            if ((template.getTermCount() > 0) && !(template.isChoice() && template.isInnerTemplate()) && !template.isReplicate())
            {
                ArchiveIndexHelper archiveIndexHelper = new ArchiveIndexHelper(template);
                archiveIndexHelper.setOperandTree(2);
                // The archetype meta data needs to include all operands found by walking the operand trees 
                // The archetype is mutable until this is completed
                template.getTemplateArchetype().clearMutable();
            }
    }
    
    /**
     * Create dynamic axiom list source
     * @param operand AxiomOperand object
     * @return AxiomSource object
     */
    public AxiomSource createAxiomSource(Operand operand)
    {
        QualifiedName qname = operand.getQualifiedName();
        QualifiedName templateName = new QualifiedTemplateName(qname.getScope(), qname.getName());
        final Template template = templateMap.get(templateName);
        if (template != null)
            return new TemplateAxiomSource(template);
        return null;
    }

    /**
     * Remove given template from this object. Expected to be called only
     * during compilation as a clean up step. 
     * @param template Template to remove
     */
	public void removeTemplate(ITemplate template) {
		templateMap.remove(template.getQualifiedName());
	}
    
    /**
     * Returns scope of axiom source specified by qname
     * @param scope Scope of caller
     * @param qname Qualified name
     * @return Scope object or null if axiom source not found
     */
    Scope findTemplateScope(Scope scope, QualifiedName qname)
    {
        String scopeName = qname.getScope();
        if (!scopeName.isEmpty() && !scopeName.equals(scope.getName()))
            return scope.findScope(scopeName);
        
        if (qname.getTemplate().isEmpty())
            // qname must be in axiom form
            return null;
        ITemplate template = templateMap.get(qname);
        if (template != null)
            return scope;
        if (!qname.getScope().isEmpty())
            qname = new QualifiedTemplateName(QueryProgram.GLOBAL_SCOPE, qname.getTemplate());
        template = scope.getGlobalTemplateAssembler().getTemplate(qname);
        if (template != null)
            return scope.getGlobalScope();
        return null; 
    }

    private void addReceiverTemplate(Template template) {
		List<Template> templateList = receiverMap.get(receiverStack.peek());
		if (templateList.isEmpty()) {
			templateList = new ArrayList<>();
			receiverMap.put(receiverStack.peek(), templateList);
		}
		templateList.add(template);
	}
    
}
