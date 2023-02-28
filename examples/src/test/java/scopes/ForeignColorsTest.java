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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;

/**
 * ForeignColorsTest
 * @author Andrew Bowley
 * 17Apr.,2017
 */
public class ForeignColorsTest
{
	private static final String[] COLORS = {
		"color(shade=Wasser, red=0, green=255, blue=255)",
		"color(shade=schwarz, red=0, green=0, blue=0)",
		"color(shade=weiß, red=255, green=255, blue=255)",
		"color(shade=blau, red=0, green=0, blue=255)",
		"color(shade=bleu vert, red=0, green=255, blue=255)",
		"color(shade=noir, red=0, green=0, blue=0)",
		"color(shade=blanc, red=255, green=255, blue=255)",
		"color(shade=bleu, red=0, green=0, blue=255)"
	};

	private static final String COLOR_QUERY = "color_query";
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
    public void testForeignColors() throws Exception
    {
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
    	args.add("foreign-colors");
    	args.add("shade=" + color);
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
	
    protected void checkSolution(String solution, String axiom)
    {
        assertThat(axiom).isEqualTo(solution);
    }
}
