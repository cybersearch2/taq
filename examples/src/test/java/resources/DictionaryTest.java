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
package resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.helper.Taq;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import utils.ResourceHelper;

/**
 * DictionaryTest
 * @author Andrew Bowley
 */
public class DictionaryTest
{
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testInWords() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("dictionary");
		List<Axiom> wordsList = new ArrayList<>();
		ProviderManager providerManager = new ProviderManager();
		Taq.initPropertyManager(providerManager);
		providerManager.chainAxiomListener("lexicon", new LocaleAxiomListener() {

			@Override
			public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) {
				wordsList.add(axiom);
				return false;
			}});
        Taq taq = new Taq(args, providerManager);
		taq.findFile();
		taq.compile();
		taq.execute();
        File testFile = ResourceHelper.getResourceFile("resources/dictionary.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        Iterator<Axiom> wordIterator = wordsList.iterator();
        while (wordIterator.hasNext()) 
              checkSolution(reader, wordIterator.next().toString());
        reader.close();
    }
    
    private void checkSolution(BufferedReader reader, String word)
    {
        try
        {
            String line = reader.readLine();
            assertThat(word).isEqualTo(line);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }

    }
}
