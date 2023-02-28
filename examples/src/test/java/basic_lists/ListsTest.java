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
package basic_lists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.Taq;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;
import utils.ResourceHelper;

/**
 * ListsTest
 * Previews all seven list types
 * @author Andrew Bowley
 * 5Feb,2017
 */
public class ListsTest
{
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testLists() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("lists");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
        File testFile = ResourceHelper.getResourceFile("basic_lists/lists.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        List<Axiom> axiomList = getLists(taq.getQueryProgram());
        for (Axiom axiom: axiomList)
        {
            checkSolution(reader, axiom.getName());
            for (int i = 0; i < axiom.getTermCount(); ++i)
            {
                Term term = axiom.getTermByIndex(i);
                checkSolution(reader, term.toString());
            }
        }
        reader.close();
    }
 
    /**
     * Reveals template contents of lists.taq
     */
    public List<Axiom> getLists(QueryProgram queryProgram) 
    {
        TemplateAssembler templateAssembler = 
        	queryProgram.getGlobalScope().getParserAssembler().getTemplateAssembler();
        ExecutionContext context = new ExecutionContext();
        Template template = templateAssembler.getTemplate("greatest");
        template.evaluate(context);
        template = templateAssembler.getTemplate("fruit");
        template.evaluate(context);
        List<Axiom> result = new ArrayList<Axiom>();
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("dice");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("dimensions");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("huges");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("flags");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("stars");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("movies");
        template.evaluate(context);
        result.add(template.toAxiom());
        return result;
    }

    private void checkSolution(BufferedReader reader, String list)
    {
        try
        {
            String line = reader.readLine();
            assertThat(list).isEqualTo(line);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
}
