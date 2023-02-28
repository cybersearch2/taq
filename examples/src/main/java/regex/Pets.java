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
import au.com.cybersearch2.taq.query.QueryExecutionException;
import au.com.cybersearch2.taq.result.Result;
import utils.ResourceHelper;

/**
Demonstrates case-insensitive regular pattern matching. The "pet_query" query 
produces a list of statements containing the name of a dog and what color it is. The 
source is an XML document containing information on cats and dogs. As an extra challenge 
for text pattern matching, the species, dog or cat, appears in mixed case.
 */
public class Pets
{
    private QueryProgramParser queryProgramParser;
 
    public Pets()
    {
        queryProgramParser = 
            new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Compiles pets.taq and runs the "pet_query" query.<br/>
     * The expected results:<br/>
        Lassie is a blonde dog.<br/>
        Bruiser is a brindle dog.<br/>
        Rex is a black and tan dog.<br/>
        Axel is a white dog.<br/>
        Fido is a brown dog.<br/>    
     * @return Axiom iterator
     */
    public Iterator<String>  dogs()
    {
        QueryProgram queryProgram = queryProgramParser.loadScript("regex/pets.taq");
        Result result = queryProgram.executeQuery("pet_query");
        return result.stringIterator("dogs_only.dogs@");
    }

    /**
     * Run tutorial
     * @param args
     */
    public static void main(String[] args)
    {
        try 
        {
            Pets pets = new Pets();
            Iterator<String> iterator = pets.dogs();
            while (iterator.hasNext())
                System.out.println(iterator.next());
        } 
        catch (QueryExecutionException e) 
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
