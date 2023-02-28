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
package au.com.cybersearch2.taq.compiler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import org.junit.Test;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.engine.SourceMarker;
import au.com.cybersearch2.taq.model.TaqParser;

public class QueryChainTest {

	private final static String MADRID = 
	    "template madrid(city ? {\"madrid\"}, altitude)\n" +	
		"query<axiom> madrid (city_altitude : madrid)";

	private final static String CUSTOMER_CHARGE =
		"template freight(city, charge)\n" +
		"template customer_freight(name, city ? city == freight.city, charge)\n" +
		"\n" +
		"query customer_charge(charge:freight, customer:customer_freight)";

	private final static String GREEK_BUSINESS =
		"template customer(name, city)\n" +
		"template account(name ? name == customer.name, city = customer.city, fee)\n" +
		"template delivery(name = account.name, city ? city == account.city, freight)\n" +
		"\n" +
		"query<axiom> greek_business(customer:customer)\n" +
		"  -> (fee:account) -> (freight:delivery)";

	private final static String SUDOKU =
		"export list<axiom> matrix {}\n" +
		"template puzzle" +	
		"{ list<axiom> matrix = matrix@ }\n" 
		+ "(\n"
		+ "  integer s11, integer s12, integer s13, integer s14,\n"
		+ "  integer s21, integer s22, integer s23, integer s24,\n"
		+ "  integer s31, integer s32, integer s33, integer s34,\n"
		+ "  integer s41, integer s42, integer s43, integer s44,\n"
		+ "  matrix += axiom { s11, s12,  s13, s14 },\n"
		+ "  matrix += axiom { s21, s22,  s23, s24 },\n"
		+ "  matrix += axiom { s31, s32,  s33, s34 },\n"
		+ "  matrix += axiom { s41, s42,  s43, s44 }\n"
		+ ")\n"
		+ "query sudoku(puzzle)\n"
		+ "(\n"
		+ "  0, 0, 2, 3,\n"
		+ "  0, 0, 0, 0,\n"
		+ "  0, 0, 0, 0,\n"
		+ "  3, 4, 0, 0\n"
		+ ")";
    @Test
    public void testMadrid() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(MADRID.getBytes()));
        ParserContext context = new ParserContext(new QueryProgram());
    	Compiler compiler = new Compiler(parser.publish(), context);
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("template madrid  (1,1) (1,15)");
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("query<axiom> madrid  (2,1) (2,44)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("query<axiom> madrid (2,1) (2,19)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("(city_altitude:madrid) (2,38) (2,44)");
    }
//
    @Test
    public void testCustomerCharge() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(CUSTOMER_CHARGE.getBytes()));
        ParserContext context = new ParserContext(new QueryProgram());
    	Compiler compiler = new Compiler(parser.publish(), context);
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("template freight  (1,1) (1,16)");
        sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("template customer_freight  (2,1) (2,25)");
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("query customer_charge  (4,1) (4,64)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("query customer_charge (4,1) (4,21)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("(charge:freight (4,30) (4,36)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("customer:customer_freight) (4,48) (4,64)");
    }

    @Test
    public void testGreekBusiness() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(GREEK_BUSINESS.getBytes()));
        ParserContext context = new ParserContext(new QueryProgram());
    	Compiler compiler = new Compiler(parser.publish(), context);
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("template customer  (1,1) (1,17)");
        sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("template account  (2,1) (2,16)");
        sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("template delivery  (3,1) (3,17)");
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("query<axiom> greek_business  (5,1) (6,40)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("query<axiom> greek_business (5,1) (5,27)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("(customer:customer)-> (5,38) (6,4)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("(fee:account)-> (6,11) (6,21)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("(freight:delivery) (6,32) (6,40)");
    }

    @Test
    public void testSudoku() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(SUDOKU.getBytes()));
        ParserContext context = new ParserContext(new QueryProgram());
    	Compiler compiler = new Compiler(parser.publish(), context);
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("export list<axiom> matrix{}  (1,1) (1,28)");
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("template puzzle{  (2,1) (2,47)");
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        assertThat(sourceMarker.toString()).isEqualTo("query sudoku  (13,1) (19,1)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("query sudoku (13,1) (13,12)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("(puzzle)(0,0,2,3,0,0,0,0,0,0,0,0,3,4,0,0) (13,14) (19,1)");
    }
}
