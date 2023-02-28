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

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;
import utils.ResourceHelper;

/**
Displays all of the list types referenced by index,  */
public class Lists 
{
    private QueryProgramParser queryProgramParser;
     
    public Lists()
    {
        queryProgramParser = 
           	new QueryProgramParser(ResourceHelper.getResourcePath());
    }

    /**
     * Reveals template contents of lists.taq
     */
    public List<Axiom> checkLists() 
    {
        List<Axiom> result = new ArrayList<Axiom>();
        queryProgramParser.loadProgram("basic_lists/lists.taq");
        ParserContext parserContext = queryProgramParser.getContext();
        TemplateAssembler templateAssembler = parserContext.getParserAssembler().getTemplateAssembler();
        ExecutionContext context = new ExecutionContext();
        Template template = templateAssembler.getTemplate("greatest");
        template.evaluate(context);
        template = templateAssembler.getTemplate("fruit");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("dice");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("dimensions");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("huges");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("flags");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("stars");
        template.evaluate(context);
        result.add(template.toAxiom());
        template = templateAssembler.getTemplate("movies");
        template.evaluate(context);
        result.add(template.toAxiom());
        return result;
    }

    /**
     * Displays types solution on the console.
     */
    public static void main(String[] args)
    {
        try 
        {
            Lists types = new Lists();
            List<Axiom> axiomList = types.checkLists();
            for (Axiom axiom: axiomList)
            {
                System.out.println("\nList: " + axiom.getName());
                for (int i = 0; i < axiom.getTermCount(); ++i)
                {
                    Term term = axiom.getTermByIndex(i);
                    System.out.println(term.toString());
                }
            }
        } 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
        System.exit(0);
    }
}
