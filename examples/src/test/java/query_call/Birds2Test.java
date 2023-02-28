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
package query_call;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;

/**
 * BirdsTest
 * @author Andrew Bowley
 * 26Jul.,2017
 */
public class Birds2Test
{

	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testBirds2() throws IOException
    {
    	List<String> args = new ArrayList<>();
    	args.add("birds2");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
        taq.execute("list_waterfowl");
        Iterator<String> iterator = captureList.iterator();
        assertThat(iterator.hasNext());
        assertThat(iterator.next()).isEqualTo("bird=whistling swan,family=swan,color=white,flight=ponderous,voice=muffled musical whistle");
        assertThat(iterator.next()).isEqualTo("bird=trumpeter swan,family=swan,color=white,flight=ponderous,voice=loud trumpeting");
        assertThat(iterator.next()).isEqualTo("bird=snow goose,family=goose,color=white,size=plump,flight=powerful,voice=honks");
        assertThat(iterator.next()).isEqualTo("bird=pintail,family=duck,flight=agile,voice=short whistle");
    }

}
