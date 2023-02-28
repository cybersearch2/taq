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
package names;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Shows how an identifier can refer to a specific scope by creating a 2-part name 
where the first part is the scope name. Here there are 2 scopes representing 
continents "Asia" and "Europe" and 2 scope-specific queries - asia_megacities 
and euro_megacities.
 */
public class ContinentScopes 
{
    private QueryProgramParser queryProgramParser;
	 
    public ContinentScopes()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
     }

    /**
     * Compiles continent-scopes.taq and runs the "asia_megacities" and "euro_megacities" queries
     */
    public List<Axiom> findAsiaEuroMegaCities() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("names/continent-scopes.taq");
        queryProgram.setCaseInsensitiveNames(true);
        List<Axiom> axiomList = new ArrayList<>();
        Result result = queryProgram.executeQuery("euro_megacities");
        result.axiomIterator("euro_megacities").forEachRemaining(city -> axiomList.add(city));
        result = queryProgram.executeQuery("asia_megacities");
        result.axiomIterator("asia_megacities").forEachRemaining(city -> axiomList.add(city));
        return axiomList;
    }

	/**
	 * Displays the asia_megacities query result followed by the result for euro_megacities<br/>
	 */
    public static void main(String[] args)
    {
        try 
        {
            ContinentScopes continentScopes = new ContinentScopes();
            continentScopes.findAsiaEuroMegaCities().forEach(axiom -> System.out.println(axiom.toString()));
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
