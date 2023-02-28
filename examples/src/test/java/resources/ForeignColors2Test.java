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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

/**
 * ForeignColors2Test
 * @author Andrew Bowley
 * 25Apr.,2017
 */
public class ForeignColors2Test
{
	private static final String[] LEXICON = {
		"colors(aqua=Wasser, black=schwarz, blue=blau, white=weiß)",
		"colors(aqua=bleu vert, black=noir, blue=bleu, white=blanc)"
	};
	
	private static final String[] COLORS = {
		"color_query(name=Wasser, red=0, green=255, blue=255)",
		"color_query(name=schwarz, red=0, green=0, blue=0)",
		"color_query(name=weiß, red=255, green=255, blue=255)",
		"color_query(name=blau, red=0, green=0, blue=255)",
		"color_query(name=bleu vert, red=0, green=255, blue=255)",
		"color_query(name=noir, red=0, green=0, blue=0)",
		"color_query(name=blanc, red=255, green=255, blue=255)",
		"color_query(name=bleu, red=0, green=0, blue=255)"
	};

	private static final String COLOR_QUERY = "color_query";
	

	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
    public void testForeignColors2() throws Exception
    {
		testForeignLexicon2();
		int index = 0;
        doTest("german", "Wasser", index++);
        doTest("german", "schwarz", index++);
        doTest("german", "weiß", index++);
        doTest("german", "blau", index++);
        doTest("french", "bleu vert", index++);
        doTest("french", "noir", index++);
        doTest("french", "blanc", index++);
        doTest("french", "bleu", index);
    }

	private void doTest(String scopeName, String color, int index)  throws Exception {
    	List<String> args = new ArrayList<>();
    	args.add("foreign-colors2");
    	args.add("name=" + color);
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		List<String> captureList = taq.getCaptureList();
		boolean success = false;
		String query = COLOR_QUERY;
		if (!taq.execute(scopeName + "." + query)) {
			System.err.println(String.format("Query %s not found", query));
		} else {
			Iterator<String> iterator = captureList.iterator();
	        if (iterator.hasNext()) {
	        	checkSolution(COLORS[index], iterator.next().toString());
	        	success = true;
	        }
		}
        assertThat(success).isTrue();
	}
	
    private void checkSolution(String solution, String axiom)
    {
        assertThat(axiom).isEqualTo(solution);
    }

    private void testForeignLexicon2() throws Exception
    { 
    	deleteFile("DE");
    	deleteFile("DE.xml");
    	deleteFile("FR");
    	deleteFile("FR.xml");
    	List<String> args = new ArrayList<>();
    	args.add("foreign-lexicon2");
		List<Axiom> captureList = new ArrayList<>();
		ProviderManager providerManager = new ProviderManager();
		Taq.initPropertyManager(providerManager);
		LocaleAxiomListener colorAxiomListener = new LocaleAxiomListener() {

			@Override
			public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) {
				captureList.add(axiom);
				//System.out.println(axiom.toString());
				return true;
			}};
		providerManager.chainAxiomListener("xstream", colorAxiomListener);
        Taq taq = new Taq(args, providerManager);
		taq.findFile();
		taq.compile();
		taq.execute();
        Iterator<Axiom> iterator = captureList.iterator();
        assertThat(iterator.hasNext()).isTrue();
        checkSolution(LEXICON[0], iterator.next().toString());
        assertThat(iterator.hasNext()).isTrue();
        checkSolution(LEXICON[1], iterator.next().toString());
    }

	private void deleteFile(String fileName) {
    	Path path = Paths.get(Taq.WORKSPACE, "xstream", fileName);
        File testPath = path.toFile();
        if (testPath.exists())
            testPath.delete();
	}
    
}
