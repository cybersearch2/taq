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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compiler.Compiler;
import au.com.cybersearch2.taq.model.TaqParser;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
This example produces a text list with each item containing information originating from a
stock" axiom list
 */
public class SaleItems 
{
    private QueryProgramParser queryProgramParser;

    public SaleItems()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }
    
	/**
	 * Compiles the sale-items.taq script and runs the "format_stock" query, displaying the solution on the console.<br/>
	 * The expected result:<br/>
        mug USD 5.66<br/>
        cap USD 15.00<br/>
        t-shirt USD 25.89<br/>	
     */
	public Iterator<String> displaySaleItems()
	{
        QueryProgram queryProgram = queryProgramParser.loadScript("basic_lists/sale-items.taq");
        Result result = queryProgram.executeQuery("format_stock");
        return result.stringIterator("sale_format.memorabilia@");
	}

	protected QueryProgram compileScript(String script)
	{
		InputStream stream = new ByteArrayInputStream(script.getBytes());
		TaqParser queryParser = new TaqParser(stream);
		QueryProgram queryProgram = new QueryProgram();
		Compiler compiler = new Compiler(queryParser.publish(), new ParserContext(queryProgram));
		compiler.compile();
		return queryProgram;
	}
	
	public static void main(String[] args)
	{
		try 
		{
	        SaleItems listsDemo = new SaleItems();
			Iterator<String> iterator = listsDemo.displaySaleItems();
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
