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
package select;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.AxiomAssembler;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.helper.Taq;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import utils.ResourceHelper;

/**
 * DefaultAccountTest
 * @author Andrew Bowley
 */
public class DefaultAccountTest
{
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testDefaultAccount() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("bank-accounts");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
		addBogusAccount(taq.getQueryProgram());
		taq.execute("bank_details");
        Iterator<String> iterator = captureList.iterator();
        File testFile = ResourceHelper.getTestResourceFile("select/bank-accounts.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        assertThat(iterator.hasNext()).isTrue();
        while (iterator.hasNext())
            checkSolution(reader, iterator.next());
        reader.close();
        captureList = taq.getCaptureList();
		taq.execute("account");
		iterator = captureList.iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().equals("account_type(account_type=cre)"));
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().equals("account_type(account_type=sav)"));
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next().equals("account_type(account_type=chq)"));
    }
 
    private void addBogusAccount(QueryProgram queryProgram) {
		Scope scope = queryProgram.getScope(QueryProgram.GLOBAL_SCOPE);
		ListAssembler listAssembler = scope.getParserAssembler().getListAssembler();
		AxiomAssembler axiomAssembler = scope.getParserAssembler().getAxiomAssembler();
		QualifiedName qualifiedAxiomName = new QualifiedName("prefix_account");
       	List<Axiom> axiomList = listAssembler.getAxiomItems(qualifiedAxiomName);
        AxiomArchetype axiomArchetype = axiomAssembler.getAxiomArchetype(qualifiedAxiomName);
        List<Term> terms = new ArrayList<>();
        terms.add(new Parameter("prefix", "999999"));
        terms.add(new Parameter("account", 9L));
        Axiom bogusAccount = axiomArchetype.itemInstance(terms);
        axiomList.add(bogusAccount);	
	}

    private void checkSolution(BufferedReader reader, String account)
    {
        try
        {
            String line = reader.readLine();
            assertThat(account).isEqualTo(line);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }

    }
}
