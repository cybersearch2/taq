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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;
import au.com.cybersearch2.taq.provider.GlobalFunctions;

/**
 * TowersOfHanoiTest
 * @author Andrew Bowley
 * 22Jun.,2017
 */
public class SudokuTest
{
	private static final String[] solution =
	{
		"4, 1, 2, 3,",
		"2, 3, 4, 1,",
		"1, 2, 3, 4,",
		"3, 4, 1, 2,"	
	};

	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testSudoku() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("sudoku");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
        List<String> printList = GlobalFunctions.printCapture();
        taq.execute();
        int index = 0;
        assertThat(printList.size()).isEqualTo(4);
        for (String line: printList)
        	assertThat(line).isEqualTo(solution[index++]);
    }
}
