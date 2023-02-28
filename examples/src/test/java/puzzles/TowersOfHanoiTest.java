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
package puzzles;

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
import au.com.cybersearch2.taq.provider.GlobalFunctions;
import utils.ResourceHelper;

/**
 * TowersOfHanoiTest
 * @author Andrew Bowley
 * 22Jun.,2017
 */
public class TowersOfHanoiTest
{
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testTowersOfHanoi() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("towers-of-hanoi");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
        List<String> printList = GlobalFunctions.printCapture();
        printList.add(" n=1");
        taq.execute("towers_of_hanoi1");
        printList.add(" n=2");
        taq.execute("towers_of_hanoi2");
        printList.add(" n=3");
        taq.execute("towers_of_hanoi3");
        //printList.forEach(line -> System.out.println(line));
        File testFile = ResourceHelper.getResourceFile("puzzles/towers-of-hanoi.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        printList.forEach(line -> checkSolution(reader, line));
        reader.close();
    }

	protected void checkSolution(BufferedReader reader, String line)
	{
	  try
	  {
	      String expected = reader.readLine();
	      assertThat(expected).isEqualTo(line);
	  }
	  catch (IOException e)
	  {
	      fail(e.getMessage());
	  }
	
	}
}
