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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.engine.SourceMarker;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.ArrayItemList;
import au.com.cybersearch2.taq.model.TaqParser;
import au.com.cybersearch2.taq.pattern.Axiom;

public class VariableTest {

	private final static String ARRAY1 = 
		"marks[15] = \"F-\"";
	private final static String AXIOM_ARRAY2 = 
			"high_cities[j][1] = 1500";
	
    @Test
    public void testVariableTypes() throws Exception
    {
    	doTestVariableType("integer", "count");
    	doTestVariableType("double", "percent");
    	doTestVariableType("boolean", "isFact");
    	doTestVariableType("decimal", "temerature");
    	//doTestVariableType("term", "country");
    	doTestVariableType("currency", "amount");
    	//doTestVariableType("declare", "value");
    }
 
    @Test 
    public void testListInitialization1() throws Exception {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(ARRAY1.getBytes()));
	    ParserContext context = new ParserContext(new QueryProgram());
	    QualifiedName qname = new QualifiedName("marks");
	    ArrayItemList<String> arrayList = new ArrayItemList<>(OperandType.STRING, qname);
	    arrayList.setSize(16);
		context.getParserAssembler().getListAssembler().addItemList(qname, arrayList);
		Compiler compiler = new Compiler(parser.publish(), context);
		compiler.compile();
	    Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
	    assertThat(iterator.hasNext()).isTrue();
	    SourceMarker sourceMarker = iterator.next();
	    assertThat(iterator.hasNext()).isTrue();
	    sourceMarker = iterator.next();
	    //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("marks[15]=\"F-\"  (1,1) (1,16)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("marks[15]=\"F-\" (1,1) (1,16)");
     }
    
    @Test 
    public void testListInitialization2() throws Exception {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(AXIOM_ARRAY2.getBytes()));
	    ParserContext context = new ParserContext(new QueryProgram());
	    QualifiedName qname = new QualifiedName("high_cities");
	    ArrayItemList<Axiom> arrayList = new ArrayItemList<Axiom>(OperandType.AXIOM , qname);
	    arrayList.setSize(2);
	    context.getParserAssembler().getListAssembler().addItemList(qname, arrayList);
		Compiler compiler = new Compiler(parser.publish(), context);
		compiler.compile();
	    Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
	    assertThat(iterator.hasNext()).isTrue();
	    SourceMarker sourceMarker = iterator.next();
	    assertThat(iterator.hasNext()).isTrue();
	    sourceMarker = iterator.next();
	    //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("high_cities[j][1]=1500  (1,1) (1,24)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("high_cities[j][1]=1500 (1,1) (1,24)");
     }
    
    private void doTestVariableType(String type, String name) {
    	List<String> parseList = new ArrayList<>();
    	List<String> resultList = new ArrayList<>();
    	String TAQ = String.format("%s %s", type, name);
    	parseList.add(TAQ);
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(TAQ.getBytes()));
	    ParserContext context = new ParserContext(new QueryProgram());
		Compiler compiler = new Compiler(parser.publish(), context);
		compiler.compile();
	    Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
	    assertThat(iterator.hasNext()).isTrue();
	    SourceMarker sourceMarker = iterator.next();
	    assertThat(iterator.hasNext()).isTrue();
	    sourceMarker = iterator.next();
	    //System.out.println(sourceMarker.toString());
        int end = type.length() + name.length() + 1;
	    String decl = String.format("%s %s  (1,1) (1,%d)", type, name, end);
        assertThat(sourceMarker.toString()).isEqualTo(decl);
        resultList.add(decl);
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
	    String result = String.format("%s %s (1,1) (1,%d)", type, name, end);
        resultList.add(result);
        assertThat(sourceItem.toString()).isEqualTo(result);
        StringBuilder builder = new StringBuilder();
        parseList.forEach(variable -> builder.append(variable).append('\n'));
    	parser = new TaqParser(new ByteArrayInputStream(builder.toString().getBytes()));
	    context = new ParserContext(new QueryProgram());
		compiler = new Compiler(parser.publish(), context);
		compiler.compile();
		Iterator<String> resultIterator = resultList.iterator();
	    iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        while (iterator.hasNext()) {
        	iterator.next();
        	sourceMarker = iterator.next();
            assertThat(sourceMarker.toString()).isEqualTo(resultIterator.next());
            sourceItem = sourceMarker.getHeadSourceItem();
            assertThat(sourceItem).isNotNull();
            assertThat(sourceItem.toString()).isEqualTo(resultIterator.next());
        }
        assertThat(resultIterator.hasNext()).isFalse();
    }

}
