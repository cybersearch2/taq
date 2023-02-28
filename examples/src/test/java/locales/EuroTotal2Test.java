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
package locales;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;

public class EuroTotal2Test {

	private static final String[] FOREIGN_TOTALS = {
		"item_query(",
		" de_DE,",
		" total=14567.89,",
		" total_text=Gesamtkosten + Steuer: 14.567,89 EUR",
		")",
		"item_query(fr_BE, total=13703.69, total_text=le total + impôt: 13 703,69 EUR)",
		"item_query(fr_FR, total=14197.52, total_text=le total + impôt: 14 197,52 EUR)"	};

   	private static final String[] GERMAN_TOTOLS = {
   			"item_query(",
   			" de_DE,",
   			" total=14567.89,",
   			" total_text=Gesamtkosten + Steuer: 14.567,89 EUR",
   			")"
    };
   	
	private static final String ITEM_QUERY = "item_query";
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
    public void testEuroTotal() throws Exception
    {
		doTest("global", Arrays.asList(FOREIGN_TOTALS));
		doTest("french", Collections.singletonList(FOREIGN_TOTALS[6]));
		doTest( "german", Arrays.asList(GERMAN_TOTOLS));
		doTest("belgium_fr", Collections.singletonList(FOREIGN_TOTALS[5]));
    }

	private void doTest(String scopeName, List<String> expected) throws IOException {
    	List<String> args = new ArrayList<>();
    	args.add("euro-total2");
    	args.add("amount=\"12.345,67 €\"");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		List<String> captureList = taq.getCaptureList();
		String query = scopeName + "." +  ITEM_QUERY;
		boolean success = false;
		if (!taq.execute(query)) {
			System.err.println(String.format("Query %s not found", query));
		} else {
			int index = 0;
			Iterator<String> iterator = captureList.iterator();
	        while (iterator.hasNext()) {
	        	checkSolution(expected.get(index++), iterator.next());
	        	success = true;
	        }
		}
        assertThat(success).isTrue();
	}
	
	private void checkSolution(String solution, String axiom)
    {
        assertThat(axiom).isEqualTo(solution);
    }

}
