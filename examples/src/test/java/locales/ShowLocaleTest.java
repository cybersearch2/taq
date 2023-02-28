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
import java.util.Locale;

public class ShowLocaleTest {

	private static final String[] LOCALES = {
			"lux_locale(name=luxenberg, language=fr, region=LU, locale=fr_LU)",
			String.format("global_locale(name=global, language=%s, region=%s, locale=%s)",
					Locale.getDefault().getLanguage(), 
					Locale.getDefault().getCountry(),
					Locale.getDefault().toString())
		};

	private static final String LOCALE_QUERY = "_locale";
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
    public void testShowLocale() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("show-locale");
         Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		doTest(taq, "lux", LOCALES[0]);
		doTest(taq, "global", LOCALES[1]);
    }

	private void doTest(Taq taq, String queryPrefix, String expected) {
		List<String> captureList = taq.getCaptureList();
		String query = queryPrefix + LOCALE_QUERY;
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
