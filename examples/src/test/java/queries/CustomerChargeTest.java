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
package queries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;
import utils.ResourceHelper;

/**
 * CustomerChargeTest
 * @author Andrew Bowley
 * 5Feb.,2017
 */
public class CustomerChargeTest
{
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testCustomerCharge() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("customer-charge");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
		taq.execute();
        Iterator<String> iterator = captureList.iterator();
        File testFile = ResourceHelper.getResourceFile("queries/customer-charge.txt");
        assertThat(iterator.hasNext()).isTrue();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        iterator.forEachRemaining(delivery -> checkSolution(reader, delivery));
        reader.close();
    }
    
    private void checkSolution(BufferedReader reader, String delivery)
    {
        try
        {
            String line = reader.readLine();
            assertThat(delivery).isEqualTo(line);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
}
