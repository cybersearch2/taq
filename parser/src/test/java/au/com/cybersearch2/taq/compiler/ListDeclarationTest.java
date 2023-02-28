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

public class ListDeclarationTest {

	private final static String AMOUNTS = 
			"list<string> euro_amounts =\n"
			+ "{\n"
			+ "  \"14.567,89\",\n"
			+ "  \"14 197,52\",\n"
			+ "  \"590,00\"\n"
			+ "}";
	private final static String EXPORT = "  export list<currency> amount_list";

    @Test
    public void testAmounts() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(AMOUNTS.getBytes()));
        ParserContext context = new ParserContext(new QueryProgram());
    	Compiler compiler = new Compiler(parser.publish(), context);
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("list<string> euro_amounts  (1,1) (6,1)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("list<string> euro_amounts (1,1) (1,25)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("{\"14.567,89\" (2,1) (3,13)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("\"14 197,52\" (4,3) (4,13)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("\"590,00\"} (5,3) (6,1)");
    }

    @Test
    public void testExports() throws Exception
    {
		TaqParser parser = new TaqParser(new ByteArrayInputStream(EXPORT.getBytes()));
	    ParserContext context = new ParserContext(new QueryProgram());
		Compiler compiler = new Compiler(parser.publish(), context);
		compiler.compile();
	    Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
	    assertThat(iterator.hasNext()).isTrue();
	    SourceMarker sourceMarker = iterator.next();
	    assertThat(iterator.hasNext()).isTrue();
	    sourceMarker = iterator.next();
	    //System.out.println(sourceMarker.toString());
	    assertThat(sourceMarker.toString()).isEqualTo("export list<currency> amount_list  (1,3) (1,35)");
	    assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
	    assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
	    SourceItem sourceItem = sourceMarker.getHeadSourceItem();
	    assertThat(sourceItem).isNotNull();
	    //System.out.println(sourceItem.toString());
	    assertThat(sourceItem.toString()).isEqualTo("export list<currency> amount_list (1,3) (1,35)");
    }
    
 }
