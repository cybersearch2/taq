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
package receiver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;
import utils.ResourceHelper;

/**
 * ChargePlusTaxTest
 * @author Andrew Bowley
 */
public class ChargePlusTaxTest {

   private static final String[] REGION_CODES  = {
		   "de", "fr", "be_fr", "be_nl"
	   };

	private static final String ITEM_QUERY = "_item_query";
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testChargePlusTax() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("charge-plus-tax");
    	args.add("amount=\"12.345,67 €\"");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
        File testFile = ResourceHelper.getResourceFile("receiver/charge-plus-tax.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        for (String regionCode: REGION_CODES) 
        	taq.execute(regionCode + ITEM_QUERY);
        assertThat(captureList.size()).isEqualTo(REGION_CODES.length);
        for (String formatedTotal: captureList)
             checkSolution(reader, formatedTotal);
        reader.close();
   }

    protected void checkSolution(BufferedReader reader, String formatedTotal)
    {
        try
        {
            String line = reader.readLine();
            assertThat(formatedTotal).isEqualTo(line);
            //System.out.println(formatedTotal);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }

    }
}
