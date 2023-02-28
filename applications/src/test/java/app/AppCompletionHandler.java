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
package app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.assertj.core.util.Arrays;

import utils.ResourceHelper;

public abstract class AppCompletionHandler {
	
	abstract public void onAppComplete(List<String> lineBuffer);
	
	public void compareFile(List<String> lineBuffer, String filePath) {
		compareFile(lineBuffer, filePath, ResourceHelper.getResourceFile(filePath));
    }

	public void compareTestFile(List<String> lineBuffer, String filePath) {
		compareFile(lineBuffer, filePath, ResourceHelper.getTestResourceFile(filePath));
    }

	public void compareArray(String[] expected, List<String> lineBuffer) {
		Iterator<String> iterator = lineBuffer.iterator();
		Arrays.asList(expected).forEach(item -> {
			boolean hasNext = iterator.hasNext();
			if (!hasNext)
            	lineBuffer.forEach(err -> System.err.println(err));
			assertThat(hasNext).isTrue();
			String next = iterator.next();
			if (!next.equals(item))
	           	lineBuffer.forEach(err -> System.err.println(err));
			assertThat(next).isEqualTo(item);
		});
	}

	private void compareFile(List<String> lineBuffer, String filePath, File testFile) {
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
        try
        {
            String line = reader.readLine();
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
