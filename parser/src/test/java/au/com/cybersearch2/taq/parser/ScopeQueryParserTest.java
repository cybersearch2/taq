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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.QueryProgramParser;
import au.com.cybersearch2.taq.ResourceHelper;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compiler.Compiler;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.model.TaqParser;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.result.Result;

/**
 * ScopeQueryParserTest
 * @author Andrew Bowley
 * 29 Dec 2014
 */
public class ScopeQueryParserTest 
{
    static final String CITY_EVELATIONS =
	    "include \"named_cities.taq\"\n" + 
	    "template high_city(string name, altitude ? altitude > 5000)\n" +
        "scope cities\n" +
	    "{\n" +
	    "  query high_cities (city : high_city)\n" + 
		"}\n";

	static final String[] HIGH_CITIES =
	{
		"high_city(name=addis ababa, altitude=8000)",
		"high_city(name=denver, altitude=5280)",
		"high_city(name=flagstaff, altitude=6970)",
		"high_city(name=leadville, altitude=10200)"
	};

    public static final String GREEK_CONSTRUCTION_DATA =
            "axiom list charge() \n" +
            "  {\"Athens\", 23 }\n" +
            "  {\"Sparta\", 13 }\n" +
            "  {\"Milos\", 17}\n" +
            "axiom list customer()\n" +
            "  {\"Marathon Marble\", \"Sparta\"}\n" +
            "  {\"Acropolis Construction\", \"Athens\"}\n" +
            "  {\"Agora Imports\", \"Sparta\"}\n" +
            "  {\"Spiros Theodolites\", \"Milos\"}\n" +
            "axiom list fee (name, fee)\n" +
            "  {\"Marathon Marble\", 61}\n" +
            "  {\"Acropolis Construction\", 47}\n" +
            "  {\"Agora Imports\", 49}\n" +
            "  {\"Spiros Theodolites\", 57}\n" + 
            "axiom list freight (city, freight) \n" +
            "  {\"Athens\", 5 }\n" +
            "  {\"Sparta\", 16 }\n" +
            "  {\"Milos\", 22}\n";
    
	static final String GREEK_CONSTRUCTION =
	
	    GREEK_CONSTRUCTION_DATA + 
		"template customer(name, city)\n" +
        "template charge(city ? city == customer.city,  charge)\n" +
		"template account(name ? name == customer.name, fee)\n" +
		"template delivery(city ? city == charge.city, freight)\n" +
		"scope greek_construction\n" +
	    "{\n" +
	    "  query greek_business(customer:customer, charge:charge)\n" + 
		"  -> (fee:account) -> (freight:delivery)" +
		"}\n"
	;

	static final String[] FEE_AND_FREIGHT =
	{
        "account(name=Marathon Marble, fee=61)",
        "delivery(city=Sparta, freight=16)",
		"account(name=Acropolis Construction, fee=47)",
		"delivery(city=Athens, freight=5)",
		"account(name=Agora Imports, fee=49)",
		"delivery(city=Sparta, freight=16)",
		"account(name=Spiros Theodolites, fee=57)",
		"delivery(city=Milos, freight=22)"
	};


    static final String FACTORIAL_CALCULATE_TAQ =
    	 	"flow factorial (\n" +
    	 	"  integer i,\n" +
    		"  integer n,\n" +
    		"  decimal factorial,\n" +
		    "  {\n" +
    		"    factorial *= i,\n" +
     		"    ? i++ < n\n" +
		    "  }\n" +
    		")(factorial = 1, i = 1)\n" +
	        "scope factorial_example\n" +
		    "{\n" +
		    "  query factorial(factorial)(n = 4)\n" + 
			"}"
		    ;
 
    static final String GERMAN_SCOPE_TAQ =
            "scope german (language=\"de\", region=\"DE\")\n" +
            "{\n" +
            "  flow format_summary(string summary = " +
            "  \"language = \" + scope.language + " +
            "  \", region = \" + scope.region)\n" +
            "  query properties_query(format_summary)\n" +
            "}";


    @Before
    public void setup() throws Exception
    {
    }

    @Test
    public void test_choice_string_colors()
    { 
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("scope/german_colors.taq");
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        QueryParams queryParams = queryProgram.getQueryParams("german", "color_query");
        // Add a shade Axiom with a single "aqua" term
        // This axiom goes into the Global scope and is removed at the start of the next query.
        Solution initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("name", "Wasser"))); // aqua
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                //System.out.println(solution.getAxiom("shade").toString());
                assertThat(solution.getAxiom("german.shade").toString()).isEqualTo("shade(name=Wasser, Red=0, Green=255, Blue=255, 0)");
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        queryParams = queryProgram.getQueryParams("german", "color_query");
        initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("name", "blau"))); // blue
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getAxiom("german.shade").toString()).isEqualTo("shade(name=blau, Red=0, Green=0, Blue=255, 2)");
                return true;
            }});
        queryProgram.executeQuery(queryParams);
        // Test select short circuit on no match
        queryParams  = queryProgram.getQueryParams("german", "color_query");
        initialSolution = queryParams.getInitialSolution();
        initialSolution.put("shade", new Axiom("shade", new Parameter("name", "Orange"))); // orange
        queryParams.setSolutionHandler(new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                assertThat(solution.getAxiom("german.swatch").getTermCount()).isEqualTo(0);
                return true;
            }});
        queryProgram.executeQuery(queryParams);
    }
    

    @Test
    public void test_mega_cities() throws IOException
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("scope/megacity.taq");
        Result result = queryProgram.executeQuery("german.group_query");
        Iterator<Axiom> iterator = result.axiomIterator("german", "group_query");
        assertThat(iterator.hasNext()).isTrue();
        File testFile = ResourceHelper.getTestResourceFile("cities-group.lst");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
        iterator.forEachRemaining(axiom -> checkSolution(reader, axiom.toString()));
        //iterator.forEachRemaining(axiom -> System.out.println(axiom.toString()));
        reader.close();
    }
    
    @Test
    public void test_german_currency_format()
    {
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("scope/german-currency.taq");
		queryProgram.executeQuery("german.item_query", new SolutionHandler(){
			@Override
			public boolean onSolution(Solution solution) {
				//System.out.println(solution.getAxiom("format_total").toString());
				//System.out.println(solution.getAxiom("charge_plus_gst").toString());
				assertThat(solution.getAxiom("charge_plus_gst").toString()).isEqualTo("charge_plus_gst(total=13580.237)");
				assertThat(solution.getAxiom("format_total").toString()).isEqualTo("format_total(total_text=Gesamtkosten + gst: 13.580,24\u00a0EUR)");
				return true;
			}});
    }
    
    @Test
    public void test_german_scope()
    {
        InputStream stream = new ByteArrayInputStream(GERMAN_SCOPE_TAQ.getBytes());
        TaqParser queryParser = new TaqParser(stream);
        QueryProgram queryProgram = new QueryProgram();
		Compiler compiler = new Compiler(queryParser.publish(), new ParserContext(queryProgram));
		compiler.compile();
        queryProgram.executeQuery("german.properties_query", new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution) {
                //System.out.println(solution.getAxiom("german.format_summary").toString());
                assertThat(solution.getAxiom("german.format_summary").toString()).isEqualTo("format_summary(summary=language = de, region = DE)");
                return true;
            }});
    }
    
    @Test
	public void test_factorial() throws IOException
	{
		QueryProgram queryProgram = new QueryProgram();
		openScript(FACTORIAL_CALCULATE_TAQ, queryProgram);
		Scope factorialScope = queryProgram.getScope("factorial_example");
		assertThat(factorialScope).isNotNull();
		SolutionHandler solutionHandler = new SolutionHandler(){
			@Override
			public boolean onSolution(Solution solution) {
				//System.out.println(solution);
				//System.out.println(solution.getAxiom("factorial_example.factorial").toString());
				assertThat(solution.getAxiom("factorial_example.factorial")
						.toString()).isEqualTo("factorial(i=5, n=4, factorial=24)");
				return true;
			}};
		queryProgram.executeQuery("factorial_example.factorial", solutionHandler);
	}
	

	@Test
	public void test_city_elevation() throws IOException
	{
		QueryProgram queryProgram = new QueryProgram();
		queryProgram.setResourceBase(ResourceHelper.getTestResourcePath());
		openScript(CITY_EVELATIONS, queryProgram);
		Scope cityScope = queryProgram.getScope("cities");
		Scope globalScope = queryProgram.getGlobalScope();
		assertThat(cityScope).isNotNull();
		ParserAssembler parserAssembler = cityScope.getParserAssembler();
		assertThat(parserAssembler).isNotNull();
		QuerySpec highCitiesSpec = cityScope.getQuerySpec("high_cities");
		assertThat(highCitiesSpec).isNotNull();
		List<KeyName> keynameList = highCitiesSpec.getKeyNameList();
		assertThat(keynameList).isNotNull();
		assertThat(keynameList.size()).isEqualTo(1);
		KeyName keyName = keynameList.get(0);
		assertThat(keyName.getAxiomKey()).isEqualTo(new QualifiedName("cities", "city"));
		assertThat(keyName.getTemplateName().getTemplate()).isEqualTo("high_city");
		assertThat(globalScope.getParserAssembler().getTemplateAssembler().getTemplate("high_city")).isNotNull();
		QualifiedName cityName = parserAssembler.getContextName("city");
		assertThat(parserAssembler.getAxiomSource(cityName)).isNull();
		cityName = new QualifiedName("city");
		assertThat(queryProgram.getGlobalScope().getParserAssembler().getAxiomSource(cityName)).isNotNull();
		SolutionHandler solutionHandler = new SolutionHandler(){
            int index = 0;
			@Override
			public boolean onSolution(Solution solution) {
				assertThat(solution.getAxiom("cities.high_city").toString()).isEqualTo(HIGH_CITIES[index++]);
				return true;
			}};
		queryProgram.executeQuery("cities.high_cities", solutionHandler);
	}

	@Test
	public void test_agricultural_land() throws IOException
	{
    	QueryProgramParser queryProgramParser = 
                new QueryProgramParser(ResourceHelper.getTestResourcePath());
		QueryProgram queryProgram = queryProgramParser.loadScript("scope/agricultural-land.taq");
		SolutionHandler solutionHandler = new SolutionHandler(){
	 	    File surfaceAreaList = ResourceHelper.getTestResourceFile("surface-area.lst");
	  	    LineNumberReader reader = new LineNumberReader(new FileReader(surfaceAreaList));
			@Override
			public boolean onSolution(Solution solution) {
				//System.out.println(solution.getAxiom("countries.surface_area_increase").toString());
 	 	    	String line = "";
				try {
					line = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
 	    		assertThat(solution.getAxiom("countries.surface_area_increase").toString()).isEqualTo(line);
				return true;
			}};
		queryProgram.executeQuery("countries.more_agriculture", solutionHandler);
 	    File surfaceAreaList = ResourceHelper.getTestResourceFile("scope/surface_area_mi2.lst");
  	    LineNumberReader reader = new LineNumberReader(new FileReader(surfaceAreaList));
  	    solutionHandler = new SolutionHandler(){
			@Override
			public boolean onSolution(Solution solution) 
			{
				//System.out.println(solution.getString("countries.surface_area_increase", "country") + " " + solution.getString("countries.km2_to_mi2", "mi2") + " mi2");
 	 	    	String line = "";
				try {
					line = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
 	    		assertThat(solution.getString("countries.surface_area_increase", "country") + " " + solution.getString("countries.km2_to_mi2", "mi2") + " mi2").isEqualTo(line);
				return true;
			}};
		queryProgram.executeQuery("countries.more_agriculture_mi2", solutionHandler);
		reader.close(); 
	}

	@Test 
	public void test_greek_business() throws IOException
	{
		QueryProgram queryProgram = new QueryProgram();
		openScript(GREEK_CONSTRUCTION, queryProgram);
		SolutionHandler solutionHandler = new SolutionHandler(){
        int index = 0;
			@Override
			public boolean onSolution(Solution solution) 
			{
			    //System.out.println(solution.getAxiom("account").toString());
			    //System.out.println(solution.getAxiom("delivery").toString());
				assertThat(solution.getAxiom("greek_construction.account").toString()).isEqualTo(FEE_AND_FREIGHT[index++]);
				assertThat(solution.getAxiom("greek_construction.delivery").toString()).isEqualTo(FEE_AND_FREIGHT[index++]);
				return true;
			}};
		queryProgram.executeQuery("greek_construction.greek_business", solutionHandler);
	}

	private void openScript(String script, QueryProgram queryProgram)
	{
		InputStream stream = new ByteArrayInputStream(script.getBytes());
		TaqParser queryParser = new TaqParser(stream);
		queryParser.enable_tracing();
		Compiler compiler = new Compiler(queryParser.publish(), new ParserContext(queryProgram));
		compiler.compile();
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
