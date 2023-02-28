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
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;

public class EuroTotalTest {

	private static final String[] TOTALS = {
		"totals(Total=Gesamtkosten 14.567,89 EUR, Tax=18% Steuer, Locale=de_DE)",
		"totals(Total=le total 14 197,52 EUR, Tax=15% impôt, Locale=fr_FR)",
		"totals(Total=le total 13 703,69 EUR, Tax=11% impôt, Locale=fr_BE)"
	};

	private static final String ITEM_QUERY = "_item_query";
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
    public void testEuroTotal() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("euro-total");
    	args.add("amount=\"12.345,67 €\"");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		doTest(taq, "de", TOTALS[0]);
		doTest(taq, "fr", TOTALS[1]);
		doTest(taq, "be_fr", TOTALS[2]);
    }

	private void doTest(Taq taq, String queryPrefix, String expected) {
		List<String> captureList = taq.getCaptureList();
		String query = queryPrefix + ITEM_QUERY;
		boolean success = false;
		if (!taq.execute(query)) {
			System.err.println(String.format("Query %s not found", query));
		} else {
			Iterator<String> iterator = captureList.iterator();
	        if (iterator.hasNext()) {
	        	checkSolution(expected, iterator.next().toString());
	        	success = true;
	        }
		}
        assertThat(success).isTrue();
	}
	
    protected void checkSolution(String solution, String axiom)
    {
        assertThat(axiom).isEqualTo(solution);
    }
}
