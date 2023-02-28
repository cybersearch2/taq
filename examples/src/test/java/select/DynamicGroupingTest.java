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
package select;

import static org.assertj.core.api.Assertions.*;
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

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.helper.Taq;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
 * DynamicMegaCitiesTest
 * @author Andrew Bowley
 * 11Apr.,2017
 */
public class DynamicGroupingTest
{
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
    @Test
    public void testDynamicMegaCities() throws Exception
    {
    	List<String> args = new ArrayList<>();
    	args.add("dynamic-grouping");
        Taq taq = new Taq(args);
        taq.getCaptureList();
		taq.findFile();
		taq.compile();
		taq.execute();
        Iterator<Axiom> iterator = getMegaCities(taq.getQueryProgram());
        File testFile = ResourceHelper.getResourceFile("select/dynamic-grouping.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        assertThat(iterator.hasNext());
        while (iterator.hasNext())
            checkSolution(reader, iterator.next().toString());
        reader.close();
    }
  
    /**
     * Compiles the dynamic-grouping.taq script and runs the "mega_cities_by_continent" query
     */
    private Iterator<Axiom> getMegaCities(QueryProgram queryProgram) 
    {
        Result result = queryProgram.executeQuery("mega_cities_by_continent");
        List<Axiom> axiomList = new ArrayList<Axiom>();
        Iterator<Axiom> iterator = result.axiomIterator("group_cities.asia");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
        iterator = result.axiomIterator("group_cities.africa");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
        iterator = result.axiomIterator("group_cities.europe");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
        iterator = result.axiomIterator("group_cities.south_america");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
        iterator = result.axiomIterator("group_cities.north_america");
        while (iterator.hasNext())
            axiomList.add(iterator.next());
       return axiomList.iterator();
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
