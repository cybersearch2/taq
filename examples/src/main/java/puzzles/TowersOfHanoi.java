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
package puzzles;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import utils.ResourceHelper;

/**
Demonstrates flow recursion and how to supply the initial parameters 
to start the recursion. The puzzle involves moving a stack of disks across three rods following 
some simple rules, the main one being that only one disc can be moved at a time.
 */
public class TowersOfHanoi
{
    private QueryProgramParser queryProgramParser;
 
    public TowersOfHanoi()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles the towers-of-hanoi.taq script and runs the "" queries.<br/>
     * The expected results:<br/>
         n=1<br/>
        Move disk 1 from rod A to rod C<br/>
<br/>        
         n=2<br/>
        Move disk 1 from rod A to rod B<br/>
        Move disk 2 from rod A to rod C<br/>
        Move disk 1 from rod B to rod C<br/>
<br/>        
         n=3<br/>
        Move disk 1 from rod A to rod C<br/>
        Move disk 2 from rod A to rod B<br/>
        Move disk 1 from rod C to rod B<br/>
        Move disk 3 from rod A to rod C<br/>
        Move disk 1 from rod B to rod A<br/>
        Move disk 2 from rod B to rod C<br/>
        Move disk 1 from rod A to rod C<br/>    
     */
    public void  calculateTowers()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("puzzles/towers-of-hanoi.taq");
        System.out.println(" n=1");
        queryProgram.executeQuery("towers_of_hanoi1");
        System.out.println("\n n=2");
        queryProgram.executeQuery("towers_of_hanoi2");
        System.out.println("\n n=3");
        queryProgram.executeQuery("towers_of_hanoi3");
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            TowersOfHanoi towersOfHanoi = new TowersOfHanoi();
            towersOfHanoi.calculateTowers();
        } 
        catch (Throwable e) 
        { 
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
