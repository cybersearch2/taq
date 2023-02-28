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
package receiver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates a receiver template attached to function call. 
The scan_service_items query uses function service.amount() to convert a list of service 
charges expressed in text format into records containing separate service identity 
and amount fields. These records are exported by the query along with a total amount for all 
services.
 */
public class ReceiveServiceItems
{
    private QueryProgramParser queryProgramParser;

    public ReceiveServiceItems()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles service-items.taq and runs the "scan_service_items" query.<br/>
     * The expected results:<br/>
        charges(Service=83057, Amount=USD60.00)<br/>
        charges(Service=93001, Amount=USD0.00)<br/>
        charges(Service=10800, Amount=USD30.00)<br/>
        charges(Service=10661, Amount=USD45.00)<br/>
        charges(Service=00200, Amount=USD0.00)<br/>
        charges(Service=78587, Amount=USD15.00)<br/>
        charges(Service=99585, Amount=USD10.00)<br/>
        charges(Service=99900, Amount=USD5.00)<br/>
        accumulator(total=165.0)<br/>  
     * @return Axiom iterator
     */
    public List<Axiom>  scanServiceItems()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("receiver/receive-service-items.taq");
        Result result = queryProgram.executeQuery("scan_service_items");
        List<Axiom> axiomList = new ArrayList<Axiom>();
        Iterator<Axiom> iterator = result.axiomIterator("scan_items.charges@");
        while(iterator.hasNext())
            axiomList.add(iterator.next());
        axiomList.add(result.getAxiom("accumulator"));
        return axiomList;
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            ReceiveServiceItems receiveServiceItems = new ReceiveServiceItems();
            Iterator<Axiom> iterator = receiveServiceItems.scanServiceItems().iterator();
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
}
