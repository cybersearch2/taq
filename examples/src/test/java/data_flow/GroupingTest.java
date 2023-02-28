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
package data_flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;
import utils.ResourceHelper;

/**
 * GroupedingTest
 * @author Andrew Bowley
 * 5Feb.,2017
 */
public class GroupingTest
{
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testGroupedMegaCities() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("grouping");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
		taq.execute();
        File testFile = ResourceHelper.getTestResourceFile("data_flow/grouping.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(testFile))) {
	        Iterator<String> cityIterator =  captureList.iterator();
	        assertThat((cityIterator.hasNext())).isTrue();
	        while (cityIterator.hasNext())
	            checkSolution(reader, cityIterator.next());
        }
     }
    
    private void checkSolution(BufferedReader reader, String city)
    {
        try
        {
            String line = reader.readLine();
            assertThat(city).isEqualTo(line);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
}
