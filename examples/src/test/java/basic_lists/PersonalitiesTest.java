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
package basic_lists;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;

/**
 * PersonalitiesTest
 */
public class PersonalitiesTest
{
	private static final Object JOHN = "name_match(name=John, age=23, starsign=Gemini, traits(gentle, affectionate, curious))";

	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testPersonalities1() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("personalities");
    	args.add("search_name=John");
        Taq taq = new Taq(args);
        List<String> captureList = taq.getCaptureList();
		taq.findFile();
		taq.compile();
		taq.execute();
        Iterator<String> iterator = captureList.iterator();
        assertThat(iterator.hasNext());
        assertThat(iterator.next().equals(JOHN));
    }
    
}
