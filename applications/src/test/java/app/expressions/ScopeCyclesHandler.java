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
package app.expressions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import app.AppCompletionHandler;
import utils.ResourceHelper;

public class ScopeCyclesHandler extends AppCompletionHandler {

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		File testFile = ResourceHelper.getResourceFile("expressions/scope-cycles.txt");
		assertThat(lineBuffer.isEmpty()).isFalse();
        try ( BufferedReader reader = 
            	new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"))) {
                lineBuffer.forEach(line -> checkSolution(reader, line, lineBuffer));
            } catch (IOException e) {
    			e.printStackTrace();
    			fail();
    		}
	}

    private void checkSolution(BufferedReader reader, String item, List<String> lineBuffer)
    {
    	int pos = item.indexOf("random=");
    	if (pos == -1)
    		fail(String.format("No random variable in %s", item));
    	item = item.substring(0, pos);
        try
        {
            String line = reader.readLine();
            assertThat(line.length() == item.length());
        	line = line.substring(0, pos);
            if (!item.equals(line))
            	lineBuffer.forEach(err -> System.err.println(err));
            assertThat(line).isEqualTo(item);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
}
