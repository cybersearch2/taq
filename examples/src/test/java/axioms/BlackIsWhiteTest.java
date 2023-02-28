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
package axioms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;

/**
 * BlackIsWhiteTest
 * @author Andrew Bowley
 * 8Apr.,2017
 */
public class BlackIsWhiteTest
{

	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testBlackIsWhite() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("black-is-white");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
		taq.execute();
        Iterator<String> iterator = captureList.iterator();
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo("color_query((name=\"white\", red=255, green=255, blue=255))");
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo("dyna_query(dyna_list(name=white, red=255, green=255, blue=255))");
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo("list_query(axiom_list(name=\"white\", red=255, green=255, blue=255))");
    }

    protected void checkSolution(BufferedReader reader, String shade)
    {
        try
        {
            String line = reader.readLine();
            assertThat(shade).isEqualTo(line);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }

    }
}
