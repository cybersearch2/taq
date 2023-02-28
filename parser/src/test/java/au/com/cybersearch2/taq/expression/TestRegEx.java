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
package au.com.cybersearch2.taq.expression;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import au.com.cybersearch2.taq.LexiconResourceProvider;
import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.ResourceHelper;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Group;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.ArchiveIndexHelper;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;
import au.com.cybersearch2.taq.query.LogicQueryExecuter;
import au.com.cybersearch2.taq.query.QueryExecuterAdapter;

/**
 * RegExTest
 * @author Andrew Bowley
 * 21 Dec 2014
 */
public class TestRegEx 
{
    private LexiconResourceProvider lexiconProvider;
    private AxiomArchetype archetype;
    private AxiomSource lexiconSource;
 
    @Before
	public void setUp()
	{
        lexiconProvider = new LexiconResourceProvider();
        archetype = new AxiomArchetype(QualifiedName.parseGlobalName("lexicon"));
        archetype.addTermName("word");
        archetype.addTermName("definition");
        archetype.clearMutable();
        lexiconSource = new AxiomSource(){

            @Override
            public Iterator<Axiom> iterator(ExecutionContext context)
            {
                return lexiconProvider.iterator(archetype);
            }

            @Override
            public Archetype<Axiom, Term> getArchetype()
            {
                return archetype;
            }};
        //Archetype.CASE_INSENSITIVE_NAME_MATCH = true;
	}
	

	@Test
	public void test_RegEx_query() throws IOException 
	{
        TemplateArchetype wordArchetype = new TemplateArchetype(new QualifiedTemplateName(QualifiedName.EMPTY, "in_words"));
		Template inWordsTemplate = new Template(wordArchetype);
		inWordsTemplate.setKey("Lexicon");
		Operand regexOp = new StringOperand(new QualifiedName(Term.ANONYMOUS), "^in[^ ]+");
		RegExOperand regExOperand = new RegExOperand(QualifiedName.parseName("word"), regexOp, null,  null);
		Variable var = new Variable(regExOperand.getQualifiedName());
		inWordsTemplate.addTerm(new Evaluator(regExOperand.getQualifiedName(), regExOperand, "?", var));
		inWordsTemplate.addTerm(new TestStringOperand("definition"));
		assertThat(inWordsTemplate.toString()).isEqualTo("in_words(word \\^in[^ ]+\\?word, definition)");
        QueryExecuterAdapter adapter = new QueryExecuterAdapter(lexiconSource, Collections.singletonList(inWordsTemplate));
        QueryParams queryParams = new QueryParams(adapter.getScope(), adapter.getQuerySpec());
        queryParams.initialize();
        LogicQueryExecuter inWordsQuery = new LogicQueryExecuter(queryParams);
    	File inWordList = ResourceHelper.getTestResourceFile("in_words.lst");
     	LineNumberReader reader = new LineNumberReader(new FileReader(inWordList));
		while (inWordsQuery.execute())
		{
 	 	    String line = reader.readLine();
			assertThat(inWordsQuery.toString()).isEqualTo(line);
		}
		reader.close();
	}

	@Test
	public void test_groups()
	{
        TemplateArchetype wordArchetype = new TemplateArchetype(new QualifiedTemplateName(QualifiedName.EMPTY, "dictionary"));
		Template dictionaryTemplate = new Template(wordArchetype);
		dictionaryTemplate.setKey("Lexicon");
		Group group = new Group();
		Operand g1 = mock(Operand.class);
		Operand g2 = mock(Operand.class);
		group.addGroup(g1);
		group.addGroup(g2);
		Operand regexOp = new StringOperand(new QualifiedName(Term.ANONYMOUS), "^(.)\\. (.*+)");
		RegExOperand regExOperand = new RegExOperand(QualifiedName.parseName("definition"), regexOp, null, group);
		dictionaryTemplate.addTerm(new TestStringOperand("word"));
		dictionaryTemplate.addTerm(regExOperand);
		getParserTask(dictionaryTemplate).run();
        QueryExecuterAdapter adapter = new QueryExecuterAdapter(lexiconSource, Collections.singletonList(dictionaryTemplate));
        QueryParams queryParams = new QueryParams(adapter.getScope(), adapter.getQuerySpec());
        queryParams.initialize();
        LogicQueryExecuter dictionaryQuery = new LogicQueryExecuter(queryParams);
		//while(dictionaryQuery.execute())
        //    System.out.println(dictionaryQuery.toString());
		if (dictionaryQuery.execute())
		{
		    // TODO - capture to verify
		    ArgumentCaptor<Parameter> paramCaptor = ArgumentCaptor.forClass(Parameter.class);
			verify(g1).assign(paramCaptor.capture());
			assertThat(paramCaptor.getValue().toString()).isEqualTo( "n");
			verify(g2).assign(paramCaptor.capture());
	        assertThat(paramCaptor.getValue().toString()).isEqualTo( "a monastery ruled by an abbot");
			assertThat(regExOperand.toString()).isEqualTo("definition=true"); 
		}
		else
			fail("Query execute returned false");
	}
	
	private Runnable getParserTask(Template template)
    {
        return new Runnable(){

            @Override
            public void run()
            {   // Use helper to set archive index and id in all operands
                ArchiveIndexHelper archiveIndexHelper = new ArchiveIndexHelper(template);
                archiveIndexHelper.setOperandTree(1);
                archiveIndexHelper.setOperandTree(2);
                // Lock archetype
                template.getTemplateArchetype().clearMutable();
            }};
    }

}
