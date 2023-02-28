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
package functions;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.helper.Taq;

/**
 * TypesTest
 * @author Andrew Bowley
 */
public class Types2Test
{
	private static final String[] TYPES = {
		"types(",
		" Boolean=true,",
		" String=penguins,",
		" Integer=12345,",
		" Double=123400.0,",
		" Decimal=1234.56,",
		" Currency=12345.67,",
		" Timestamp=Tue Nov 15 11:16:50 AEDT 2022,",
		" Type=String",
		")"
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
    	args.add("types2");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		List<String> captureList = taq.getCaptureList();
        QueryProgram queryProgram = taq.getQueryProgram();
        queryProgram.executeQuery("types");
        assertThat(captureList.size() == TYPES.length);
        for (int i = 0; i < captureList.size(); ++i)
        {
            checkSolution(captureList.get(i), TYPES[i]);
        }
    }
    
    protected void checkSolution(String line, String type)
    {
         if (line.startsWith(" Timestamp="))
        {
            assertThat(type).startsWith(" Timestamp=");
            return;
        }
        assertThat(type).isEqualTo(line);
    }
}
