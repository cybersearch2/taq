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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.TemplateType;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomCollection;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.ArchiveIndexHelper;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * QueryExecuterAdapter
 * @author Andrew Bowley
 * 17 Feb 2015
 */
public class QueryExecuterAdapter 
{
	protected static final String QUERY_NAME = "Test";
	protected QuerySpec querySpec;
	protected Scope scope;
	protected QueryProgram queryProgram;
	
	public QueryExecuterAdapter(AxiomCollection axiomCollection, List<Template> templateList) 
	{
		queryProgram = new QueryProgram();
		scope = queryProgram.getGlobalScope();
		querySpec = new QuerySpec(QUERY_NAME, true);
		ParserAssembler parserAssembler = scope.getParserAssembler();
		List<String> keyList = new ArrayList<String>();
		for (ITemplate template: templateList)
		{
			String key = template.getKey();
			keyList.add(key);
			KeyName keyName = new KeyName(key, template.getName());
			querySpec.addKeyName(keyName);
		}
		addAxiomCollection(parserAssembler, keyList, axiomCollection);
		addTemplateList(parserAssembler, templateList);
	}

	public QueryExecuterAdapter(AxiomSource axiomSource, List<Template> templateList) 
	{
		this(ensembleFromSource(axiomSource), templateList);
	}
	
	public QueryParams getQueryParams()
	{
		QueryParams queryParams = new QueryParams(scope, querySpec);
		queryParams.initialize();
		return queryParams;
	}
	
	public QueryProgram getQueryProgram()
	{
		return queryProgram;
	}
	
	public QuerySpec getQuerySpec() 
	{
		return querySpec;
	}

	public Scope getScope() 
	{
		return scope;
	}

	/**
	 * Adapt an axiom source to an ensemble
	 * @param axiomSource The AxiomSource object
	 * @return AxiomCollection
	 */
	public static AxiomCollection ensembleFromSource(final AxiomSource axiomSource)
	{
		return new AxiomCollection(){

			@Override
			public AxiomSource getAxiomSource(String name) {
				return axiomSource;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}};
	}

    /**
     * Add all axioms in a collection to a ParserAssembler object.
     * @param parserAssembler The destination
     * @param keyList List of axiom names
     * @param axiomCollection The axiom collection
     */
    public void addAxiomCollection(ParserAssembler parserAssembler, List<String> keyList, AxiomCollection axiomCollection)
    {
        for (String key: keyList)
        {
            AxiomSource axiomSource = axiomCollection.getAxiomSource(key);
            Iterator<Axiom> iterator = axiomSource.iterator(null);
            boolean firstTime = true;
            while (iterator.hasNext())
            {
                Axiom axiom = iterator.next();
                QualifiedName qname = QualifiedName.parseGlobalName(key);
                if (firstTime)
                {
                    firstTime = false;
                    parserAssembler.getListAssembler().createAxiomItemList(qname, false);
                    for (int i = 0; i < axiom.getTermCount(); i++)
                        parserAssembler.getAxiomAssembler().addAxiom(qname, axiom.getTermByIndex(i));
                    parserAssembler.getAxiomAssembler().saveAxiom(qname);
                }
                else
                    parserAssembler.getListAssembler().add(qname, axiom);
            }
        }
    }
    
    /**
     * Add templates to  a ParserAssembler object.
     * @param parserAssembler The destination
     * @param templateList List of templates
     */
    public void addTemplateList(ParserAssembler parserAssembler, List<Template> templateList)
    {
        for (Template template: templateList)
        {
            String templateName = template.getName();
            QualifiedName qname = new QualifiedTemplateName(QualifiedName.EMPTY, templateName);
            String templateKey = template.getKey();
            List<Term> props = template.getProperties().getProperties();
            parserAssembler.getTemplateAssembler().createTemplate(qname, TemplateType.template);
            parserAssembler.getTemplateAssembler().getTemplate(qname).setKey(templateKey);
            if (props != null)
                parserAssembler.getTemplateAssembler().addTemplate(qname, props);
            Template newTemplate = parserAssembler.getTemplateAssembler().getTemplate(qname);
            for (int i = 0; i < template.getTermCount(); i++)
                newTemplate.addTerm((Operand)template.getTermByIndex(i));
            ArchiveIndexHelper archiveIndexHelper = new ArchiveIndexHelper(newTemplate);
            archiveIndexHelper.setOperandTree(1);
        }
    }
    

}
