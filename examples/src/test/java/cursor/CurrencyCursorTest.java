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
package cursor;

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
 * CurrencyCursorTest
 * @author Andrew Bowley
 * 12Apr.,2017
 */
public class CurrencyCursorTest
{
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testCurrencyCursor() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("currency-cursor");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
		taq.execute();
        File testFile = ResourceHelper.getResourceFile("cursor/currency-cursor.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        captureList.forEach(amount ->  checkSolution(reader, amount));
        reader.close();
   }

    private void checkSolution(BufferedReader reader, String info)
    {
        try
        {
            String line = reader.readLine();
            assertThat(info).isEqualTo(line);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }

    }
}
