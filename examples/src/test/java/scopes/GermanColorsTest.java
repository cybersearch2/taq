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
public class GermanColorsTest
{
	private static final String[] COLORS = {
		"colors(",
		" color(color=weiß, r=255, g=255, b=255, tag=background)",
		" color(color=blau, r=0, g=0, b=255, tag=foreground)",
		")"
	};

	private static final String COLOR_QUERY = "color_query";
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
    public void testFGermanColors() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("german-colors");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		List<String> captureList = taq.getCaptureList();
		boolean success = false;
		String query = COLOR_QUERY;
		if (!taq.execute()) {
			System.err.println(String.format("Query %s not found", query));
		} else {
			Iterator<String> iterator = captureList.iterator();
			assertThat(iterator.hasNext()).isTrue();
			int index = 0;
	        while (iterator.hasNext()) {
	        	checkSolution(COLORS[index++], iterator.next());
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
