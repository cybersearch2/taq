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
package locales;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Shows the name and locale attributes of 2 separate scopes. The global 
locale is the computer system default, so results will vary according to where in the 
world the query is run.
*/
public class ShowLocale 
{
    private QueryProgramParser queryProgramParser;

    public ShowLocale()
    {
        queryProgramParser = 
           new QueryProgramParser(ResourceHelper.getResourcePath());
    }
    
	/**
	 * Compiles /show-locale.taq runs the "locale" query for the given scope, 
	 * displaying the solution on the console.<br/>
	 */
	public String getLocale(String scope)
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("locales/show-locale.taq");
        Result result = queryProgram.executeQuery(scope + "_locale");
	    return result.getAxiom(scope + "_locale").toString();
	}
	
    /**
     * Run tutorial
     * @param args
     */
	public static void main(String[] args)
	{
		try 
		{
	        ShowLocale showLocale = new ShowLocale();
	        // Luxenburg
            System.out.println(showLocale.getLocale("lux"));
            // System default locate
            System.out.println(showLocale.getLocale("global"));
		} 
		catch (Throwable e) 
		{    
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
