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

import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates both a **map** and a **select** operation. The "account" 
query has a map which matches an account number to an account type. The "bank" query
goes further and also has a select thet matches a numeric prefix to a bank name and branch.
 */
public class BankAccounts 
{
    private QueryProgramParser queryProgramParser;
    
    public BankAccounts(String argument)
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles bank-accounts.taq and runs the requested query
     */
    public Iterator<Axiom> checkChoices(String queryName) 
    {
        QueryProgram queryProgram = getQueryProgram();
        Result result = queryProgram.executeQuery(queryName);
        return result.axiomIterator(queryName);
    }

    /**
     * Displays solution on the console. 
     * <br/>
     * The expected result for "bank" query:<br/>
bank_account(prefix=456448, Bank=Bank of Queensland, BSB=124-001, Account=cre)<br/>
bank_account(prefix=456445, Bank=Commonwealth Bank Aust., BSB=527-146, Account=sav)<br/>
bank_account(prefix=456443, Bank=Bendigo Bank LTD, BSB=633-000, Account=chq)
     */
    public static void main(String[] args)
    {
        try 
        {
            BankAccounts bankAccounts = new BankAccounts(args.length == 0 ? "" : args[0]);
            Iterator<Axiom> iterator = bankAccounts.checkChoices("account");
            while (iterator.hasNext())
                System.out.println(iterator.next().toString());
            System.out.println();
            iterator = bankAccounts.checkChoices("bank_details");
            while (iterator.hasNext())
                System.out.println(iterator.next().toString());
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
    
    protected QueryProgram getQueryProgram() {
        return queryProgramParser.loadScript("select/bank-accounts.taq");
    }
}
