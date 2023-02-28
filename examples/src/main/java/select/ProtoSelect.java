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

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Exposes how the selection operation works. This example maps an account number 
(1, 2 or 3) to an account type ("cre", "sav" or "chq")
*/
public class ProtoSelect 
{
    private QueryProgramParser queryProgramParser;
    
    public ProtoSelect()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles the prot-select.taq script and runs each account type query
     */
    public List<Axiom> checkChoices() 
    {
    	List<Axiom> choices = new ArrayList<>();
        QueryProgram queryProgram = getQueryProgram();
        for (int i = 1; i <= 3; ++i) {
            Result result = queryProgram.executeQuery("account" + i);
            choices.add(result.getAxiom("account" + i));
        }
        return choices;
    }

    /**
     * Displays solution on the console. 
     * <br/>
     * The expected result:<br/>
	map_account(account_type=accounts(account_type="cre"))
	map_account(account_type=accounts(account_type="sav"))
	map_account(account_type=accounts(account_type="chq"))
     */
    public static void main(String[] args)
    {
        try 
        {
            ProtoSelect protoSelect = new ProtoSelect();
            protoSelect.checkChoices().forEach(account -> System.out.println(account.toString()));
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
    
    protected QueryProgram getQueryProgram() {
        return queryProgramParser.loadScript("select/proto-select.taq");
    }
}
