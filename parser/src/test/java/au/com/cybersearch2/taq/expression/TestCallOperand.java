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
package au.com.cybersearch2.taq.expression;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.ResourceHelper;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.provider.CallHandler;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;

/**
 * CallOperandTest
 * @author Andrew Bowley
 * 30 Jul 2015
 */
public class TestCallOperand
{
    static class SystemFunctionProvider implements FunctionProvider
    {
        @Override
        public String getName()
        {
            return "system";
        }

        @Override
        public CallHandler getCallEvaluator(String identifier)
        {
            if (identifier.equals("print"))
                return new CallHandler("print"){
    
                    @Override
                    public boolean evaluate(List<Term> argumentList)
                    {
                        //for (Term term: argumentList)
                        //    System.out.print(term.getValue().toString());
                        //System.out.println();
                    	solutionAxiom.addTerm(new Parameter(Term.ANONYMOUS, new Null()));
                        return true;
                    }

            };
            throw new ExpressionException("Unknown function identifier: " + identifier);
        }
    }
    
    static class MathFunctionProvider implements FunctionProvider
    {

        @Override
        public String getName()
        {
            return "math";
        }

        @Override
        public CallHandler getCallEvaluator(String identifier)
        {
            if (identifier.equals("add"))
                return new CallHandler("add"){
    
                    @Override
                    public boolean evaluate(List<Term> argumentList)
                    {
                        if ((argumentList == null) || argumentList.isEmpty())
                        {
                            Axiom axiom = new Axiom("NaN");
                            axiom.addTerm(new Parameter(Term.ANONYMOUS, Double.NaN));
                            onNextAxiom(QualifiedName.ANONYMOUS, axiom, Locale.getDefault());
                            return false;
                        }
                        long addendum = 0;
                        for (int i = 0; i < argumentList.size(); i++)
                        {
                            Long param = (Long)argumentList.get(i).getValue();
                            addendum += param.longValue();
                        }
                        solutionAxiom.addTerm(new Parameter(Term.ANONYMOUS, addendum));
                        return true;
                    }

                };
            if (identifier.equals("avg"))
                return new CallHandler("avg"){

                    @Override
                    public boolean evaluate(List<Term> argumentList)
                    {
                        if ((argumentList == null) || argumentList.isEmpty())
                        {
                            solutionAxiom.addTerm(new Parameter(Term.ANONYMOUS, Double.NaN));
                            return false;
                        }
                        long average = 0;
                        for (int i = 0; i < argumentList.size(); i++)
                        {
                            Long param = (Long)argumentList.get(i).getValue();
                            average += param.longValue();
                        }
                        average /= argumentList.size();
                        solutionAxiom.addTerm(new Parameter(Term.ANONYMOUS, average));
                        return true;
                   }};
             throw new ExpressionException("Unknown function identifier: " + identifier);
        }
    }

    static class EduFunctionProvider implements FunctionProvider
    {

        @Override
        public String getName()
        {
            return "edu";
        }

        @Override
        public CallHandler getCallEvaluator(String identifier)
        {
            return new CallHandler("edu") {

                @Override
                public boolean evaluate(List<Term> argumentList)
                {
                    long total = 0;
                    for (Object letterGrade: argumentList)
                    {
                        String text = ((Term)letterGrade).getValue().toString();
                        char base = text.charAt(0);
                        if (base == 'f')
                            total += 2;
                        else if (base == 'e')
                            total += 5;
                        else if (base == 'd')
                            total += 8;
                        else if (base == 'c')
                            total += 11;
                        else if (base == 'b')
                            total += 14;
                        else if (base == 'a')
                            total += 17;
                        if (text.length() > 1)
                        {
                            char adjust = text.charAt(1);
                            total += adjust == '+' ? 1 : -1;
                        }
                    }
                    solutionAxiom.addTerm(new Parameter(Term.ANONYMOUS, total));
                    return true;
                }
            };
        }
        
    }

    static final String TWO_ARG_CALC =
        " flow test (integer x = math.add(1,2))\n" +
        " query two_arg_query (test)";
    static final String THREE_ARG_CALC =
        " flow test (integer x = math.add(1,2,3))\n" +
        " query three_arg_query (test)";
    static final String FOUR_ARG_CALC =
        " flow test (integer x = math.add(12,42,93,55))\n" +
        " query four_arg_query (test)";
    static final String GRADES = 
        "axiom list grades (student, english, math, history)\n" +
            " {\"Amy\", 14, 16, 6}\n" +
            " {\"George\", 15, 13, 16}\n" +
            " {\"Sarah\", 12, 17, 15}\n";

    static final String GRADES_CALC = GRADES +
        " template score(student, total = math.add(english, math, history))\n" +
        " query marks(grades : score)";

    static final String[] GRADES_RESULTS = 
    {
        "score(student=Amy, english=14, math=16, history=6, total=36)",
        "score(student=George, english=15, math=13, history=16, total=44)",
        "score(student=Sarah, english=12, math=17, history=15, total=44)"
    };

    static final String[] GRADES_RESULTS2 = 
    {
        "score(student=Amy, total=36)",
        "score(student=George, total=44)",
        "score(student=Sarah, total=44)"
    };
    
    static final String[] STUDENTS =
    {
         "Amy",
         "George",
         "Sarah"
    };
    
    static final String[] MARKS_GRADES_RESULTS = 
    {
        "Total score: 36",
        "Total score: 44",
        "Total score: 44"
    };
 
    static final String[] MARKS_GRADES_RESULTS2 = 
    {
        "marks_total=36",
        "marks_total=44",
        "marks_total=44"
    };
    
    static final String[] MATH_SCORES =
    {
        "marks_list(Math, mark=a-)",
        "marks_list(Math, mark=b-)",
        "marks_list(Math, mark=a)"
    };
    
    static final String[] SCHOOL_REPORT = 
    {
        "English b",
        "Math a-",
        "History e+",
        "Total score: 36",
        "English b+",
        "Math b-",
        "History a-",
        "Total score: 44",
        "English c+",
        "Math a",
        "History b+",
        "Total score: 44"
    };
    
   static final String ALPHA_MARKS = 
    " axiom alpha_marks()\n" +
    "{\n" +
    " \"\",\n" +
    " \"f-\", \"f\", \"f+\",\n" +
    " \"e-\", \"e\", \"e+\",\n" +
    " \"d-\", \"d\", \"d+\",\n" +
    " \"c-\", \"c\", \"c+\",\n" +
    " \"b-\", \"b\", \"b+\",\n" +
    " \"a-\", \"a\", \"a+\"\n" +
    "}\n";
    
    static final String MARKS_CALC = GRADES + ALPHA_MARKS +
    "template score(\n" +
    "  student, english, math, history,\n" +
    "integer total = edu.add(alpha_marks[english], alpha_marks[math], alpha_marks[history]))\n" +
    "query marks(grades : score)";
    
    static final String CITY_EVELATIONS =
        "axiom list city (name, altitude)\n" + 
            "    {\"bilene\", 1718}\n" +
            "    {\"addis ababa\", 8000}\n" +
            "    {\"denver\", 5280}\n" +
            "    {\"flagstaff\", 6970}\n" +
            "    {\"jacksonville\", 8}\n" +
            "    {\"leadville\", 10200}\n" +
            "    {\"madrid\", 1305}\n" +
            "    {\"richmond\",19}\n" +
            "    {\"spokane\", 1909}\n" +
            "    {\"wichita\", 1305}\n";
    
    static final String CITY_AVERAGE_HEIGHT_CALC = CITY_EVELATIONS +
            "list city_list = list city\n" +
            "flow average (integer average_height = math.avg(" +
            "  city_list[0]->altitude,\n" +
            "  city_list[1]->altitude,\n" +
            "  city_list[2]->altitude,\n" +
            "  city_list[3]->altitude,\n" +
            "  city_list[4]->altitude,\n" +
            "  city_list[5]->altitude,\n" +
            "  city_list[6]->altitude,\n" +
            "  city_list[7]->altitude,\n" +
            "  city_list[8]->altitude,\n" +
            "  city_list[9]->altitude\n" +
            "))\n" +
            "query average_height (average)";
    
    static final String CITY_AVERAGE_HEIGHT_CALC2 = CITY_EVELATIONS +
            "//list city_list(city)\n" +
            "scope city\n" +
            "{\n" +
            "  flow average_height\n" +
            "  {\n" +
            "    integer accum,\n" +
            "    integer index\n" +
            "  }\n" +
            "  (\n" +
            "    {\n" +
            "      accum += (city[index]->altitude),\n" +
            "      ? ++index < city.size()\n" +
            "    },\n" +
            "    average = accum / index\n" +
            "  )\n" +
            "}\n"  +
            "flow average_height\n" +
            "(\n" +
            "  flow city.average_height() {"
            + "  result@ = average },\n" +
            "  result\n" +
            ")\n" +
            "query average_height (average_height)"
           ;
    
    
    static String[] PERFECT_GEMINIS = 
    {
        "person(name=\"John\", sex=\"m\", age=23, starsign=\"gemini\")",
        "person(name=\"Jenny\", sex=\"f\", age=21, starsign=\"gemini\")",
        "person(name=\"Sonia\", sex=\"f\", age=33, starsign=\"gemini\")",
        "person(name=\"Fiona\", sex=\"f\", age=29, starsign=\"gemini\")"
    };

    static String[] PERFECT_GEMINIS2 = 
    {
        "person_list(name=\"John\", sex=\"m\", age=23, starsign=\"gemini\")",
        "person_list(name=\"Jenny\", sex=\"f\", age=21, starsign=\"gemini\")",
        "person_list(name=\"Sonia\", sex=\"f\", age=33, starsign=\"gemini\")",
        "person_list(name=\"Fiona\", sex=\"f\", age=29, starsign=\"gemini\")"
    };

   QueryProgram queryProgram;
    
    @Before
    public void setUp()
    {
    	queryProgram = new QueryProgram(getProvideFunctionManager());
    }

    @Test
    public void test_gemini_people()
    {
        File resourcePath = new File("src/test/resources");
        QueryProgramParser queryProgramParser = new QueryProgramParser(resourcePath);
        QueryProgram testProgram = queryProgramParser.loadScript("star-people.taq");
        Result result = testProgram.executeQuery("match");
        Iterator<Axiom> geminiIterator = result.axiomIterator("match_geminis.geminis");
        int index = 0;
        while(geminiIterator.hasNext()) {
        	Axiom gemini = geminiIterator.next();
            //System.out.println(gemini.toString());
            assertThat(gemini.toString()).isEqualTo(PERFECT_GEMINIS[index++]);
        }
        assertThat(index).isEqualTo(4);
    }
    
    @Test
    public void test_gemini_people2()
    {
        File resourcePath = new File("src/test/resources");
        QueryProgramParser queryProgramParser = new QueryProgramParser(resourcePath);
        QueryProgram testProgram = queryProgramParser.loadScript("star-people2.taq");
        Result result = testProgram.executeQuery("match");
        Iterator<Axiom> geminiIterator = result.axiomIterator("match.geminis");
        int index = 0;
        while(geminiIterator.hasNext()) {
        	Axiom gemini = geminiIterator.next();
            //System.out.println(gemini.toString());
            assertThat(gemini.toString()).isEqualTo(PERFECT_GEMINIS2[index++]);
        }
        assertThat(index).isEqualTo(4);
    }
    
   

    @Test
    public void test_two_argument()
    {
    	queryProgram.parseScript(TWO_ARG_CALC);
        queryProgram.executeQuery("two_arg_query", new SolutionHandler(){

            @Override
            public boolean onSolution(Solution solution)
            {
                Long result = (Long)solution.getValue("test", "x");
                assertThat(result).isEqualTo(3);
                return false;
            }});
    }

    @Test
    public void test_three_argument()
    {
    	queryProgram.parseScript(THREE_ARG_CALC);
        queryProgram.executeQuery("three_arg_query", new SolutionHandler(){

            @Override
            public boolean onSolution(Solution solution)
            {
                Long result = (Long)solution.getValue("test", "x");
                assertThat(result).isEqualTo(6);
                return false;
            }});
    }

    @Test
    public void test_four_argument()
    {
    	queryProgram.parseScript(FOUR_ARG_CALC);
        queryProgram.executeQuery("four_arg_query", new SolutionHandler(){

            @Override
            public boolean onSolution(Solution solution)
            {
                Long result = (Long) solution.getValue("test", "x");
                assertThat(result).isEqualTo(12+42+93+55);
                return false;
            }});
    }

    @Test
    public void test_three_variables()
    {
    	queryProgram.parseScript(GRADES_CALC);
        queryProgram.executeQuery("marks", new SolutionHandler(){
            int index = 0;  
            @Override
            public boolean onSolution(Solution solution)
            {
                //System.out.println(solution.getAxiom("score").toString());
                assertThat(solution.getAxiom("score").toString()).isEqualTo(GRADES_RESULTS2[index++]);
                return true;
            }});
    }

    @Test
    public void test_term_variables()
    {
        queryProgram.parseScript(MARKS_CALC);
        queryProgram.executeQuery("marks", new SolutionHandler(){
            int index = 0;  
            @Override
            public boolean onSolution(Solution solution)
            {
                //System.out.println(solution.getAxiom("score").toString());
                assertThat(solution.getAxiom("score").toString()).isEqualTo(GRADES_RESULTS[index++]);
                return true;
            }});
    }

    @Test
    public void test_list_variables()
    {
        queryProgram.parseScript(CITY_AVERAGE_HEIGHT_CALC);
        queryProgram.executeQuery("average_height", new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution)
            {
                //System.out.println(solution.getAxiom("average").toString());
                long averageHeight = (1718+8000+5280+6970+8+10200+1305+19+1909+1305)/10;
                Long result = (Long)solution.getValue("average", "average_height");
                assertThat(result).isEqualTo(averageHeight);
                return true;
            }});
    }

    @Test
    public void test_calculator()
    {
        final long averageHeight = (1718+8000+5280+6970+8+10200+1305+19+1909+1305)/10;
        queryProgram.parseScript(CITY_AVERAGE_HEIGHT_CALC2);
        queryProgram.executeQuery("average_height", new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution)
            {
                //System.out.println(solution.getAxiom("average_height").toString());
                Axiom result = solution.getAxiom("average_height");
                assertThat(result.toString()).isEqualTo("average_height(result=" + averageHeight + ")");
                return true;
            }});
    }

    @Test
    public void test_choice_german_colors()
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram testProgram = queryProgramParser.loadScript("scope/german-colors.taq");
        Result result = testProgram.executeQuery("colors");
        Iterator<Axiom> iterator = result.axiomIterator("german_colors.color_list");
        //System.out.println(iterator.next().toString());
        assertThat(iterator.hasNext()).isTrue();
        //System.out.println(iterator.next().toString());
        assertThat(iterator.next().toString()).isEqualTo("german_colors.swatch(swatch=Wasser, Red=0, Green=255, Blue=255)");
        assertThat(iterator.hasNext()).isTrue();
        //System.out.println(iterator.next().toString());
        assertThat(iterator.next().toString()).isEqualTo("german_colors.swatch(swatch=blau, Red=0, Green=0, Blue=255)");
    }
    
    ProviderManager getProvideFunctionManager()
    {
    	ProviderManager functionManager = new ProviderManager();
        MathFunctionProvider mathFunctionProvider = new MathFunctionProvider();
        functionManager.putFunctionProvider(mathFunctionProvider.getName(), mathFunctionProvider);
        EduFunctionProvider eduFunctionProvider = new EduFunctionProvider();
        functionManager.putFunctionProvider(eduFunctionProvider.getName(), eduFunctionProvider);
        SystemFunctionProvider systemFunctionProvider = new SystemFunctionProvider();
        functionManager.putFunctionProvider(systemFunctionProvider.getName(), systemFunctionProvider);
        return functionManager;
    }
}
