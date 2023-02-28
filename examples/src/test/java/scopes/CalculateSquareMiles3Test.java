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
 * CalculateSquareMiles3Test
 * @author Andrew Bowley
 * 25Apr.,2017
 */
public class CalculateSquareMiles3Test
{
	private static final String[] KMS = {
		"country_area(country=Australia, surface_area=7741220.0, units=km2)",
		"country_area(country=United States, surface_area=9831510.0, units=km2)"
	};
	
	private static final String[] MILES = {
		"country_area(country=Australia, surface_area=2988885.042, units=mi2)",
		"country_area(country=United States, surface_area=3795946.011, units=mi2)"
	};

	private static final String SURFACE_AREA_QUERY = "surface_area_query";
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
    public void testCalculateSquareMiles3() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("calc-square-miles3");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		doTest(taq, "au_", KMS);
		doTest(taq, "us_", MILES);
		doTest(taq, "xx_", KMS);
    }

	private void doTest(Taq taq, String queryPrefix, String[] expected) {
		List<String> captureList = taq.getCaptureList();
		String query = queryPrefix + SURFACE_AREA_QUERY;
		boolean success = false;
		if (!taq.execute(query)) {
			System.err.println(String.format("Query %s not found", query));
		} else {
			Iterator<String> iterator = captureList.iterator();
	        if (iterator.hasNext()) {
	        	checkSolution(expected[0], iterator.next().toString());
	            if (iterator.hasNext()) {
	            	checkSolution(expected[1], iterator.next().toString());
	            	success = true;
	            }
	        }
		}
        assertThat(success).isTrue();
	}
	
    protected void checkSolution(String solution, String axiom)
    {
        assertThat(axiom).isEqualTo(solution);
    }
}
