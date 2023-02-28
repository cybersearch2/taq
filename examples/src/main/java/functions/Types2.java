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
package functions;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import system.SystemFunctionProvider;
import utils.ResourceHelper;

/**
Demonstrates a function declaration requesting a alternative return type 
from the default. Function system.timestamp() calls system library function timestamp()
and normally it returns a Date object. The function declaration here puts the return type 
as string and this is supported.
 */
public class Types2 
{
    private QueryProgramParser queryProgramParser;
    
    public Types2()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath(), getProviderManager());
    }

    ProviderManager getProviderManager()
    {
    	ProviderManager functionManager = new ProviderManager();
    	SystemFunctionProvider systemFunctionProvider = new SystemFunctionProvider();
        functionManager.putFunctionProvider(systemFunctionProvider.getName(), systemFunctionProvider);
        return functionManager;
    }

    /**
     * Compiles types2.taq and runs the "types" query
     */
    public Axiom checkTypes() 
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("functions/types2.taq");
        Result result = queryProgram.executeQuery("types");
        return result.getAxiom("types");
    }

    /**
     * Displays types solution on the console. Note timestamp details will vary.
     * <br/>
     * The expected result:<br/>
        Boolean=true (Boolean)<br/>
        String=penguins (String)<br/>
        Integer=12345 (Long)<br/>
        Double=123400.0 (Double)<br/>
        Decimal=1234.56 (BigDecimal)<br/>
        Currency=12345.67( BigDecimal)<br/>
        Timestamp=timestamp (String)<br/>	 
      */
    public static void main(String[] args)
    {
        try 
        {
            Types2 types2 = new Types2();
            Axiom axiom = types2.checkTypes();
            for (int i = 0; i < axiom.getTermCount() - 2; ++i)
            {
                Term term = axiom.getTermByIndex(i);
                System.out.println(term.toString() + " (" + term.getValue().getClass().getSimpleName() + ")");
            }
            int index = axiom.getTermCount() - 2;
            System.out.println(axiom.getTermByIndex(index++).toString() + " (" +
            		           axiom.getTermByIndex(index).toString() + ")");        
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
