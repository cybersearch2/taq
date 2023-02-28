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
package regex;

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
 Displays pet names extracted from XML using a regular expression. 
 */
public class RegexPetNames
{
    private QueryProgramParser queryProgramParser;

    public RegexPetNames()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles the pets.taq script and runs the "pet_names" query.<br/>
     * The expected results:<br/>
        Lassie<br/>
        Cuddles<br/>
        Bruiser<br/>
        Rex<br/>
        Pixie<br/>
        Axel<br/>
        Amiele<br/>
        Fido<br/>
     * @return Dtring iterator
     */
    public Iterator<String>  petNames()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("regex/regex-pet-names.taq");
        Result result = queryProgram.executeQuery("pets");
        return result.stringIterator("pets.pet_names@");
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            RegexPetNames regexPetNames = new RegexPetNames();
            Iterator<String> iterator = regexPetNames.petNames();
            while (iterator.hasNext())
                System.out.println(iterator.next());
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
