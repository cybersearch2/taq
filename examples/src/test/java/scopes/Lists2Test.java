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
package scopes;

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
import au.com.cybersearch2.taq.helper.Taq;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
 * Lists2Test
 * Previews all seven list types
 * @author Andrew Bowley
 * 5Feb,2017
 */
public class Lists2Test
{
	private static final String[] LISTS = {
			"fruit",
			"dice",
			"dimensions",
			"roaches",
			"movies",
			"flags",
			"stars"
		};

	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
    public void testLists2() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("lists2");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		doTest(taq);
    }

    private void doTest(Taq taq) throws Exception
    {
        File testFile = ResourceHelper.getResourceFile("scopes/lists2.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        List<Axiom> axiomList = queryLists(taq, "london");
        axiomList.addAll(queryLists(taq, "new_york"));
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
     * Compiles the lists2.taq script and runs the "lists" query
     */
    private List<Axiom> queryLists(Taq taq, String city) 
    {
        List<Axiom> lists = new ArrayList<Axiom>();
        QueryProgram queryProgram = taq.getQueryProgram();
        for (String list: LISTS) {
            String query = city + "_" + list;
	        Result result = queryProgram.executeQuery(query);
	        lists.add(result.getAxiom(city + "_" + list));
        }
        return lists;
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
