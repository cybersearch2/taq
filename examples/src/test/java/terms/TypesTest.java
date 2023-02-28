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
package terms;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;

/**
 * TypesTest
 * @author Andrew Bowley
 * 5Feb,2017
 */
public class TypesTest
{
	private static final String[] TYPES = {
        "types(",
        " Boolean=true,",
        " String=penguins,",
        " Integer=12345,",
        " Double=123400.0,",
        " Decimal=1234.56,",
        " Currency=12345.67",
        ")",
	};
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testTypes() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("types");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
		taq.execute();
        Iterator<String> iterator = captureList.iterator();
        assertThat(iterator.hasNext()).isTrue();
        int index = 0;
        while (iterator.hasNext()) {
	        checkSolution(TYPES[index++], iterator.next().toString());
        }
    }
    
    protected void checkSolution(String solution, String axiom)
    {
        assertThat(axiom).isEqualTo(solution);
    }
}
