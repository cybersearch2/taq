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
package au.com.cybersearch2.taq.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.ResourceHelper;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compiler.Compiler;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.BigDecimalOperand;
import au.com.cybersearch2.taq.expression.BooleanOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.model.TaqParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.provider.CallHandler;
import au.com.cybersearch2.taq.provider.GlobalFunctions;
import au.com.cybersearch2.taq.query.Calculator;
import au.com.cybersearch2.taq.query.LogicQueryExecuter;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;
import au.com.cybersearch2.taq.service.LoopMonitor;

/**
 * QueryParserTest
 * @author Andrew Bowley
 * 6 Dec 2014
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryParserTest 
{
    static class SystemFunctionProvider implements FunctionProvider
    {
    	public List<String> printList = new ArrayList<>();
    	
        @Override
        public String getName()
        {
            return "system";
        }

        @Override
        public CallHandler getCallEvaluator(String identifier)
        {
            if (identifier.equals("print"))
                return new CallHandler("print") {
    
                    @Override
                    public boolean evaluate(List<Term> argumentList)
                    {
                    	StringBuilder builder = new StringBuilder();;
                        for (Term term: argumentList) {
                        	builder.append(term.getValue().toString());
                        }
                        printList.add(builder.toString());
                        return true;
                    }
            };
            throw new ExpressionException("Unknown function identifier: " + identifier);
        }
    }
    
	static final String SCRIPT1 =
	    "integer twoFlip = ~2\n" +
        "integer mask = 2\n" +
	    "integer maskFlip = ~mask";

	static final String SCRIPT2 =
		"template city(string name, integer height)\n";
	
	static final String SCRIPT3 =
		"axiom list city()" +
		"{\"bilene\", 1718}" +
		"{\"denver\", 5280}" +
		"";

	static final String SCRIPT4 =
	    "integer x = 1\n" +
        "integer y = 2\n" +
	    "integer x_y = x + y\n" +
        "integer a = x * 2\n" +
        "integer b = 7 * 2\n" +
        "boolean c = (y * 2) < (x * 5)\n" +
        "decimal d = 1234\n" +
	    "decimal e = d + b";

	static final String CITY_EVELATIONS =
	    "include \"cities.taq\"" + 		
		"template high_city(string name, integer altitude, boolean is_high = altitude > 5000)";

	static final String[] CITY_NAME_HEIGHT =
	{
		"city(name=\"bilene\", altitude=1718)",
		"city(name=\"addis ababa\", altitude=8000)",
		"city(name=\"denver\", altitude=5280)",
		"city(name=\"flagstaff\", altitude=6970)",
		"city(name=\"jacksonville\", altitude=8)",
		"city(name=\"leadville\", altitude=10200)",
		"city(name=\"madrid\", altitude=1305)",
		"city(name=\"richmond\", altitude=19)",
		"city(name=\"spokane\", altitude=1909)",
		"city(name=\"wichita\", altitude=1305)"
	};

	static final String[] HIGH_CITY =
	{
		"high_city(name=\"bilene\", is_high=false)",
		"high_city(name=\"addis ababa\", is_high=true)",
		"high_city(name=\"denver\", is_high=true)",
		"high_city(name=\"flagstaff\", is_high=true)",
		"high_city(name=\"jacksonville\", is_high=false)",
		"high_city(name=\"leadville\", is_high=true)",
		"high_city(name=\"madrid\", is_high=false)",
		"high_city(name=\"richmond\", is_high=false)",
		"high_city(name=\"spokane\", is_high=false)",
		"high_city(name=\"wichita\", is_high=false)"	
	};

	static final String[] HIGH_CITY_v2 =
	{
		"high_city(name=\"bilene\", altitude=1718, is_high=false)",
		"high_city(name=\"addis ababa\", altitude=8000, is_high=true)",
		"high_city(name=\"denver\", altitude=5280, is_high=true)",
		"high_city(name=\"flagstaff\", altitude=6970, is_high=true)",
		"high_city(name=\"jacksonville\", altitude=8, is_high=false)",
		"high_city(name=\"leadville\", altitude=10200, is_high=true)",
		"high_city(name=\"madrid\", altitude=1305, is_high=false)",
		"high_city(name=\"richmond\", altitude=19, is_high=false)",
		"high_city(name=\"spokane\", altitude=1909, is_high=false)",
		"high_city(name=\"wichita\", altitude=1305, is_high=false)"
	};

    static final String SIMPLE_CALCULATE =
    		"flow increment_n (" +
    		"integer n, " +
    		"integer limit, " +
    		"{\n" +
    		"  ? ++n < limit\n" +
    		"}\n" +
    		") ( n = 1, limit = 3)";

    static final String FACTORIAL_CALCULATE =
    	 	"flow factorial (" +
    	 	"integer i, " +
    		"integer n, " +
    		"decimal factorial," +
    		"{\n" +
    		"  factorial *= i," +
     		"  ? i++ < n" +
    		"})\n" +
    		"(factorial = 1, n = 4, i = 1)"
    		;

    static final String ONE_SHOT_CALCULATE =
    		"flow km2_to_mi2\n" +
    		"(\n" +
    		"  decimal km2,\n" +
    		"  decimal mi2 = km2 * 0.3861)\n" +
    		"(km2 = 1323.98)"
    		;

    static final String SIMPLE_LIST_CALCULATE =
            "list<integer> number_list\n" +
    		"flow increment_n (\n" +
    		"integer n, \n" +
    		"integer limit, \n" +
    		"{\n" +
    		"  number_list@[0] = n++,\n" +
    		"  number_list@[1] = n++,\n" +
    		"  number_list@[2] = n++,\n" +
    		"  ? number_list@[2] < limit" +
    		"}\n" +
    		") (n = 1, limit = 3)";

    static final String SIMPLE_VARIABLE_INDEX_LIST_CALCULATE =
            "list<integer> number_list\n" +
    		"flow increment_n (\n" +
    		"integer n, \n" +
    		"integer i, \n" +
    		"integer limit, \n" +
    		"{\n" +
    		"  number_list@[i++] = n++,\n" +
    		"  ? i < limit" +
    		"})\n" +
    		"(n = 1, i = 0, limit = 3)";

    static final String SIMPLE_LIST_LENGTH_CALCULATE =
            "list<integer> number_list\n" +
    		"flow increment_n (\n" +
    		"integer n, \n" +
    		"integer i, \n" +
    		"integer limit, \n" +
    		"{\n" +
    		"  number_list@[i++] = n++,\n" +
    		"  ? number_list.size() < limit\n" +
    		"}\n" +
    		")\n" +
    		"(n = 1, i = 0, limit = 3)";

	static final String HIGH_CITIES_JPA_TAQ =
			"axiom city (name, altitude): resource \"cities\"\n" +
			"template high_city(string name, altitude ? altitude > 5000)\n"
			;

	static final String STAMP_DUTY_TAQ =
            "select bracket " +
            "( amount,           threshold, base, percent)\n" +
            "{\n" +
            "    ? <  12000:      0,     0.00, 1.00\n" +
            "    ? <  30000:  12000,   120.00, 2.00\n" +
            "    ? <  50000:  30000,   480.00, 3.00\n" +
            "    ? < 100000:  50000,  1080.00, 3.50\n" +
            "    ? < 200000: 100000,  2830.00, 4.00\n" +
            "    ? < 250000: 200000,  6830.00, 4.25\n" +
            "    ? < 300000: 250000,  8955.00, 4.75\n" +
            "    ? < 500000: 300000, 11330.00, 5.00\n" +
            "    ? > 500000: 500000, 21330.00, 5.50\n" +
            "}\n" +
			"\n" +
			"axiom transacton_amount (amount) { 123458 }\n" +
			"flow payable(\n" +
			".currency amount,\n" +
			".flow bracket(amount),\n" +
			"duty = base + (amount - threshold) * (percent / 100))\n" +
			"query stamp_duty_query (transacton_amount : payable)\n";

            /** Named query to find all cities */
    static public final String ALL_CITIES = "all_cities";

    static String DIVIDE_TEST =
        "flow divide ( integer i = 35, integer j = 7, integer result = i/j )\n " +
        "query<term> divide_query (divide)";
    
    static String LOOP_TEST =
            "flow loop\n" +
            "( \n" +
            "  i = 0,\n" +
            "  {\n" +
            "    j = 0,\n" +
            "    {\n" +
            "      ?: ++j == 2\n" +
            "    },\n" +
            "    ?: (i += 2) == 4\n" +
            "  }\n" +
            ")\n " +
            "flow loop1 ( i = 0, integer k = 0, { ?: ++i == 3, ?: ++k == 3 } )\n " +
            "query<term> loop_query (loop)\n" +
            "query<term> loop1_query (loop1)";

    private AxiomArchetype archetype;
	@Mock
	ExecutionContext executionContext;
	@Mock
	LoopMonitor loopMonitor;

    @Before
    public void setUp()
    {
        archetype = new AxiomArchetype(QualifiedName.parseGlobalName("lexicon"));
        archetype.addTermName("word");
        archetype.addTermName("definition");
        archetype.clearMutable();
    }
    
    @Test
    public void test_loop()
    {
    	ProviderManager functionManager = new ProviderManager();
        SystemFunctionProvider systemFunctionProvider = new SystemFunctionProvider();
        functionManager.putFunctionProvider(systemFunctionProvider.getName(), systemFunctionProvider);
        QueryProgram queryProgram = new QueryProgram(functionManager);
        //queryProgram.setExecutionContext(new ExecutionContext());
        queryProgram.parseScript(LOOP_TEST);
        Result result = queryProgram.executeQuery("loop1_query");
        //System.out.println(result.getAxiom("loop1_query").toString());
        assertThat(result.getAxiom("loop1_query").toString()).isEqualTo("loop1_query(i=3, k=2)");
        //System.out.println("Dual inner loop test");
        result = queryProgram.executeQuery("loop_query");
        //System.out.println(result.getAxiom("loop_query").toString());
        assertThat(result.getAxiom("loop_query").toString()).isEqualTo("loop_query(i=4)");
    }
    
   @Test
    public void test_divide()
    {
        QueryProgram queryProgram = new QueryProgram();
        queryProgram.parseScript(DIVIDE_TEST);
        Result result = queryProgram.executeQuery("divide_query");
        //System.out.println(result.getAxiom("divide_query").toString());
        assertThat(result.getAxiom("divide_query").toString()).isEqualTo("divide_query(i=35, j=7, result=5)");
    }

    @Test
    public void test_sudoku() throws IOException
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("sudoku.taq");
        List<String> printList = GlobalFunctions.printCapture();
        queryProgram.executeQuery("sudoku");
        assertThat(printList.size()).isEqualTo(4);
        assertThat(printList.get(0)).isEqualTo("4, 1, 2, 3,");
        assertThat(printList.get(1)).isEqualTo("2, 3, 4, 1,");
        assertThat(printList.get(2)).isEqualTo("1, 2, 3, 4,");
        assertThat(printList.get(3)).isEqualTo("3, 4, 1, 2,");
    }
    
    @Test
    public void test_towers_of_hanoi() throws IOException
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("towers-of-hanoi.taq");
        List<String> printList = GlobalFunctions.printCapture();
        printList.add(" n=1");
        queryProgram.executeQuery("towers_of_hanoi1");
        printList.add(" n=2");
        queryProgram.executeQuery("towers_of_hanoi2");
        printList.add(" n=3");
        queryProgram.executeQuery("towers_of_hanoi3");
        //printList.forEach(line -> System.out.println(line));
        File testFile = ResourceHelper.getTestResourceFile("towers-of-hanoi.txt");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        printList.forEach(line -> checkSolution(reader, line));
        reader.close();
    }
    
    @Test
    public void test_mega_cities() throws IOException
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("mega_city2.taq");
        queryProgram.executeQuery("group_query", new SolutionHandler(){
          @Override
          public boolean onSolution(Solution solution) {
              //System.out.println(solution.getAxiom("group").toString());
              return true;
        }});
        //System.out.println();
        Result result = queryProgram.executeQuery("group_query");
        Iterator<Axiom> iterator = result.axiomIterator(QualifiedName.parseGlobalName("group_query"));
        assertThat(iterator.hasNext()).isTrue();
        File testFile = ResourceHelper.getTestResourceFile("cities-group.lst");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        iterator.forEachRemaining(axiom -> checkSolution(reader, axiom.toString()));
        reader.close();
    }

    @Test
    public void test_choice_string_colors()
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("choice_colors3.taq");
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "color_query");
        // Add a shade Axiom with a single "aqua" term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("name", "aqua")));
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                //System.out.println(solution.getAxiom("swatch").toString());
                assertThat(solution.getAxiom("shader").toString()).isEqualTo("shader(color=aqua, red=0, green=255, blue=255, index=0)");
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("name", "blue")));
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getAxiom("shader").toString()).isEqualTo("shader(color=blue, red=0, green=0, blue=255, index=2)");
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        // Test select short circuit on no match
        initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("name", "orange")));
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getAxiom("shader").getTermCount()).isEqualTo(0);
                return true;
            }});
        queryProgram.executeQuery(queryParams);
    }
    

    @Test
    public void test_choice_hex_colors()
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("choice_colors2.taq");
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "color_query");
        // Add a shade Axiom with a single "aqua" term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("rgb", Long.decode("0x00ffff"))));
        boolean[] hasSolution = new boolean[] {false};
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                //System.out.println(solution.getAxiom("swatch").toString());
                assertThat(solution.getAxiom("shader").toString()).isEqualTo("shader(rgb=" + Long.decode("0x00ffff").toString() + ", color=aqua, red=0, green=255, blue=255, index=0)");
                hasSolution[0] = true;
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        assertThat(hasSolution[0]).isTrue();
        initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("rgb", Long.decode("0x0000ff"))));
        hasSolution[0] = false;
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getAxiom("shader").toString()).isEqualTo("shader(rgb=255, color=blue, red=0, green=0, blue=255, index=2)");
                hasSolution[0] = true;
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        assertThat(hasSolution[0]).isTrue();
        // Test select short circuit on no match
        initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("rgb", Long.decode("0x77ffff"))));
        hasSolution[0] = false;
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getAxiom("shader").getTermCount()).isEqualTo(0);
                hasSolution[0] = true;
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        assertThat(hasSolution[0]).isTrue();
    }
    
    @Test
    public void test_choice_unknown_hex_color()
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("choice_colors.taq");
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        QueryParams queryParams = queryProgram.getQueryParams(QueryProgram.GLOBAL_SCOPE, "color_query");
        // Add a shade Axiom with a single "aqua" term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("rgb", Long.decode("0x00ffff"))));
        boolean[] hasSolution = new boolean[] {false};
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
            	if (solution.getCurrentKey().equals("shader")) {
                    //System.out.println(solution.getAxiom("swatch").toString());
                    assertThat(solution.getAxiom("shader").toString()).isEqualTo("shader"            
                		+ "(rgb=" + Long.decode("0x00ffff").toString() + ", color=aqua, red=0, green=255, blue=255, index=0)");
                    hasSolution[0] = true;
                }
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        assertThat(hasSolution[0]).isTrue();
        initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("rgb", Long.decode("0x0000ff"))));
        hasSolution[0] = false;
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getAxiom("shader").toString()).isEqualTo("shader(rgb=255, color=blue, red=0, green=0, blue=255, index=2)");
                hasSolution[0] = true;
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        assertThat(hasSolution[0]).isTrue();
        // Test default select on no match
        initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("rgb", Long.decode("0x77ffff"))));
        hasSolution[0] = false;
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getAxiom("shader").toString()).isEqualTo("shader(rgb=" + Long.decode("0x77ffff").toString() + ", color=unknown, red=0, green=0, blue=0, index=4)");
                hasSolution[0] = true;
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        assertThat(hasSolution[0]).isTrue();
    }
    
   @Test
    public void test_stamp_duty()
    {
		QueryProgram queryProgram = new QueryProgram();
        //queryProgram.setExecutionContext(new ExecutionContext());
		queryProgram.parseScript(STAMP_DUTY_TAQ);
		queryProgram.executeQuery("stamp_duty_query", new SolutionHandler(){
			@Override
			public boolean onSolution(Solution solution) {
				//System.out.println(solution.getAxiom("payable").toString());
				assertThat(solution.getAxiom("payable").toString()).isEqualTo("payable(duty=3768.32)");
				return true;
			}});
    }
 
    @Test
    public void test_world_currency_Format() throws IOException
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("world_currency_query.taq");
		queryProgram.executeQuery("price_query");
		Result result = queryProgram.executeQuery("price_query");
		Iterator<Axiom> iterator = result.axiomIterator(QualifiedName.parseGlobalName("world_list"));
    	File worldCurrencyList = ResourceHelper.getTestResourceFile("world_currency.lst");
     	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(worldCurrencyList), "UTF-8"));
        while(iterator.hasNext())
        {
            String text = iterator.next().toString();
		    //System.out.println(text);
 	    	String line = reader.readLine();
  	    	assertThat(text).isEqualTo(line);
        }
 	    reader.close();
    }
    
    @Test
    public void test_currency_Format()
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("currency_query.taq");
		queryProgram.executeQuery("item_query", new SolutionHandler(){
			@Override
			public boolean onSolution(Solution solution) {
				System.out.println(solution.getAxiom("format_total").toString());
				assertThat(solution.getAxiom("format_total").toString()).isEqualTo("format_total(total_text=Total + gst: AUD1,358.02)");
				return true;
			}});
    }
    
    @Test
    public void test_simple_calculate()
    {
		ParserAssembler parserAssembler = openScript(SIMPLE_CALCULATE);
		assertThat(parserAssembler.getTemplateAssembler().getTemplate("increment_n").toString()).isEqualTo("increment_n(n, limit, increment_n1(?++n<limit))");
		//System.out.println(parserAssembler.getTemplateAssembler().getTemplate("increment_n"));
        Template calcTemplate = parserAssembler.getTemplateAssembler().getTemplate("increment_n");
        Solution solution = new Solution(Locale.getDefault());
        Calculator calculator = new Calculator();
		when(executionContext.getLoopMonitor()).thenReturn(loopMonitor);
		when(loopMonitor.tick()).thenReturn(true);
        calculator.iterate(solution, calcTemplate, executionContext);
        assertThat(solution.getAxiom("increment_n").toString()).isEqualTo("increment_n(n=3, limit=3)");
    }
    
    @Test
    public void test_simple_list_calculate()
    {
		ParserAssembler parserAssembler = openScript(SIMPLE_LIST_CALCULATE);
		Template calcTemplate =  parserAssembler.getTemplateAssembler().getTemplate("increment_n");
		assertThat(calcTemplate.toString()).isEqualTo(
				"increment_n(n, limit, increment_n1(number_list.0=n++ ... ?number_list<limit))");
		//System.out.println(parserAssembler.getTemplateAssembler().getTemplate("increment_n"));
        Solution solution = new Solution(Locale.getDefault());
        Calculator calculator = new Calculator();
		when(executionContext.getLoopMonitor()).thenReturn(loopMonitor);
		when(loopMonitor.tick()).thenReturn(true);
        calculator.iterate(solution, calcTemplate, executionContext);
        //System.out.println(solution.getAxiom("increment_n").toString());
        assertThat(solution.getAxiom("increment_n").toString()).isEqualTo("increment_n(n=4, limit=3)");
    }
    
    @Test
    public void test_simple_variable_index_list_calculate()
    {
		ParserAssembler parserAssembler = openScript(SIMPLE_VARIABLE_INDEX_LIST_CALCULATE);
		assertThat(parserAssembler.getTemplateAssembler().getTemplate("increment_n").toString()).isEqualTo("increment_n(n, i, limit, increment_n1(number_list.i=n++ ... ?i<limit))");
		//System.out.println(parserAssembler.getTemplateAssembler().getTemplate("increment_n"));
        Template calcTemplate = parserAssembler.getTemplateAssembler().getTemplate("increment_n");
        Solution solution = new Solution(Locale.getDefault());
        Calculator calculator = new Calculator();
		when(executionContext.getLoopMonitor()).thenReturn(loopMonitor);
		when(loopMonitor.tick()).thenReturn(true);
        calculator.iterate(solution, calcTemplate, executionContext);
        //System.out.println(solution.getAxiom("increment_n").toString());
        assertThat(solution.getAxiom("increment_n").toString()).isEqualTo("increment_n(n=4, i=3, limit=3)");
    }
        
    @Test
    public void test_simple_list_length_calculate()
    {
		ParserAssembler parserAssembler = openScript(SIMPLE_LIST_LENGTH_CALCULATE);
		assertThat(parserAssembler.getTemplateAssembler().getTemplate("increment_n")
				.toString()).isEqualTo(
						"increment_n(n, i, limit, increment_n1(number_list.i=n++ ... ?number_list.size()<limit))");
		//System.out.println(parserAssembler.getTemplateAssembler().getTemplate("increment_n"));
        Template calcTemplate = parserAssembler.getTemplateAssembler().getTemplate("increment_n");
        Solution solution = new Solution(Locale.getDefault());
        Calculator calculator = new Calculator();
		when(executionContext.getLoopMonitor()).thenReturn(loopMonitor);
		when(loopMonitor.tick()).thenReturn(true);
        calculator.iterate(solution, calcTemplate, executionContext);
        //System.out.println(solution.getAxiom("increment_n").toString());
        assertThat(solution.getAxiom("increment_n").toString()).isEqualTo("increment_n(n=4, i=3, limit=3)");
    }
    
    @Test
    public void test_factorial_calculate()
    {
		ParserAssembler parserAssembler = openScript(FACTORIAL_CALCULATE);
		assertThat(parserAssembler.getTemplateAssembler().getTemplate("factorial").toString()).isEqualTo("factorial(i, n, factorial, factorial1(factorial*=i ... ?i++<n))");
		//System.out.println(parserAssembler.getTemplateAssembler().getTemplate("factorial").toString());
        Template calcTemplate = parserAssembler.getTemplateAssembler().getTemplate("factorial");
        Solution solution = new Solution(Locale.getDefault());
        Calculator calculator = new Calculator();
		when(executionContext.getLoopMonitor()).thenReturn(loopMonitor);
		when(loopMonitor.tick()).thenReturn(true);
        calculator.iterate(solution, calcTemplate, executionContext);
        //System.out.println(solution.getAxiom("factorial").toString());
        assertThat(solution.getAxiom("factorial").toString()).isEqualTo("factorial(i=5, n=4, factorial=24)");
    }
    
    @Test
    public void test_one_shot_calculate()
    {
		ParserAssembler parserAssembler = openScript(ONE_SHOT_CALCULATE);
		Template calcTemplate = parserAssembler.getTemplateAssembler().getTemplate("km2_to_mi2");
		//System.out.println(calcTemplate.toString());
		assertThat(calcTemplate.toString()).isEqualTo("km2_to_mi2(km2, mi2=km2*0.3861)");
		//System.out.println(parserAssembler.getTemplateAssembler().getTemplate("km2_to_mi2"));
        parserAssembler.getTemplateAssembler().getTemplate("km2_to_mi2");
        Solution solution = new Solution(Locale.getDefault());
        Calculator calculator = new Calculator();
        calculator.iterate(solution, calcTemplate, executionContext);
        System.out.println(solution.getAxiom("km2_to_mi2").toString());
        assertThat(solution.getAxiom("km2_to_mi2").toString()).isEqualTo("km2_to_mi2(km2=1323.98, mi2=511.188678)");
    }
    

    @Test
	public void testGlobalVariables() throws Exception
	{
		ParserAssembler parserAssembler = openScript(SCRIPT1);
		OperandMap operandMap = parserAssembler.getOperandMap();
        IntegerOperand twoFlip = (IntegerOperand) operandMap.get(QualifiedName.parseGlobalName("twoFlip"));
        twoFlip.evaluate(1);
	    assertThat(twoFlip.getValue()).isEqualTo(-3L);
        IntegerOperand mask = (IntegerOperand) operandMap.get(QualifiedName.parseGlobalName("mask"));
        mask.evaluate(1);
        IntegerOperand maskFlip = (IntegerOperand) operandMap.get(QualifiedName.parseGlobalName("maskFlip"));
        maskFlip.evaluate(1);
	    assertThat(maskFlip.getValue()).isEqualTo(-3L);
	}
	
	@Test
	public void testTemplates() throws Exception
	{
	    openScript(SCRIPT2);
	}
	
	@Test
	public void testAxioms() throws Exception
	{
	    openScript(SCRIPT3);
	}


	@Test 
	public void testBinaryOps() throws Exception
	{
	    ParserAssembler parserAssembler = openScript(SCRIPT4);
		OperandMap operandMap = parserAssembler.getOperandMap();
        IntegerOperand x = (IntegerOperand) operandMap.get(QualifiedName.parseGlobalName("x"));
        x.evaluate(1);
	    assertThat(x.getValue()).isEqualTo(1L);
        IntegerOperand y = (IntegerOperand) operandMap.get(QualifiedName.parseGlobalName("y"));
        y.evaluate(1);
	    assertThat(y.getValue()).isEqualTo(2L);
        IntegerOperand x_y = (IntegerOperand) operandMap.get(QualifiedName.parseGlobalName("x_y"));
        x_y.evaluate(1);
	    assertThat(x_y.getValue()).isEqualTo(3L);
        IntegerOperand a = (IntegerOperand) operandMap.get(QualifiedName.parseGlobalName("a"));
        a.evaluate(1);
	    assertThat(a.getValue()).isEqualTo(2L);
        IntegerOperand b = (IntegerOperand) operandMap.get(QualifiedName.parseGlobalName("b"));
        b.evaluate(1);
	    assertThat(b.getValue()).isEqualTo(14L);
        BooleanOperand c = (BooleanOperand) operandMap.get(QualifiedName.parseGlobalName("c"));
        c.evaluate(1);
	    assertThat(c.getValue()).isEqualTo(true);
        BigDecimalOperand d = (BigDecimalOperand) operandMap.get(QualifiedName.parseGlobalName("d"));
        d.evaluate(1);
	    assertThat(d.getValue()).isEqualTo(BigDecimal.valueOf((long)1234));
        BigDecimalOperand e = (BigDecimalOperand) operandMap.get(QualifiedName.parseGlobalName("e"));
        e.evaluate(1);
	    assertThat(e.getValue()).isEqualTo(BigDecimal.valueOf((long)1248));
	}

	@Test
	public void test_highCities() throws Exception
	{
		QueryProgram queryProgram = new QueryProgram(provideProviderManager());
		queryProgram.parseScript(CITY_EVELATIONS);
	    ParserAssembler parserAssembler = queryProgram.getGlobalScope().getParserAssembler();
	    Template highCities = parserAssembler.getTemplateAssembler().getTemplate("high_city");
	    highCities.setKey("city");
        QuerySpec querySpec = new QuerySpec("TEST", true);
		KeyName keyName = new KeyName("city", "high_city");
		querySpec.addKeyName(keyName);
        QueryParams queryParams = new QueryParams(queryProgram.getGlobalScope(), querySpec);
        queryParams.initialize();
        LogicQueryExecuter highCitiesQuery = new LogicQueryExecuter(queryParams);
    	assertThat(highCitiesQuery.toString()).isEqualTo("high_city(name, altitude, is_high=altitude>5000)");
    	int index = 0;
 	    while (highCitiesQuery.execute())
  	    	assertThat(highCitiesQuery.toString()).isEqualTo(HIGH_CITY_v2[index++]);
	}

	public static ParserAssembler openScript(String script)
	{
		InputStream stream = new ByteArrayInputStream(script.getBytes());
		TaqParser queryParser = new TaqParser(stream);
		queryParser.enable_tracing();
		QueryProgram queryProgram = new QueryProgram();
		queryProgram.setResourceBase(ResourceHelper.getTestResourcePath());
		Compiler compiler = new Compiler(queryParser.publish(), new ParserContext(queryProgram));
		compiler.compile();
	    compiler.runPending();
        compiler = null;
		ParserAssembler parserAssembler = queryProgram.getGlobalScope().getParserAssembler();
		return parserAssembler;
	}
	
    ProviderManager provideProviderManager()
    {
        return new TestResourceProvider();
    }

	private void checkSolution(BufferedReader reader, String line)
	{
	  try
	  {
	      String expected = reader.readLine();
	      assertThat(expected).isEqualTo(line);
	  }
	  catch (IOException e)
	  {
	      fail(e.getMessage());
	  }
	}
}
