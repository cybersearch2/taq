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

public class FlowDeclarationTest {

	private final static String NESTED_LOOPS =
		"flow insert_sort \n"
		+ "{ export list<term> sorted = list unsorted }\n"
		+ "(\n"
		+ "  i = 1, \n"
		+ "  {\n"
		+ "    j = i - 1, \n"
		+ "    temp = sorted[i], \n"
		+ "    {\n"
		+ "      ? temp < sorted[j],\n"
		+ "      sorted[j + 1] = sorted[j],\n"
		+ "      ? --j >= 0\n"
		+ "    },\n"
		+ "    sorted[j + 1] = temp,\n"
		+ "    ? ++i < sorted.length\n"
		+ "  }\n"
		+ ")";

    
    @Test
    public void testNestedLoops() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(NESTED_LOOPS.getBytes()));
        ParserContext context = new ParserContext(new QueryProgram());
    	Compiler compiler = new Compiler(parser.publish(), context);
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("flow insert_sort{  (1,1) (16,1)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("flow insert_sort{ (1,1) (2,1)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("export list<term> sorted (2,3) (2,26)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("list unsorted}( (2,30) (3,1)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("i=1 (4,3) (4,7)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("{ // loop (5,3) (5,3)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("j=i-1 (6,5) (6,13)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("temp=sorted[i] (7,5) (7,20)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("{ // loop (8,5) (8,5)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("? temp<sorted[j] (9,7) (9,24)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("sorted[j+1]=sorted[j] (10,7) (10,31)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("? --j>=0} (11,7) (12,5)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("sorted[j+1]=temp (13,5) (13,24)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("? ++i<sorted.length}) (14,5) (16,1)");
    }
    
}
