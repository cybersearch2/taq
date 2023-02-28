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

import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.engine.SourceMarker;
import au.com.cybersearch2.taq.model.TaqParser;

public class ExpressionTest {

	private final static String ONO_PLUS_TWO = "term q = one + two";
	private final static String ONO_PLUS_TWO_IN_PARENTHESES = "term q = (one + two)";
	private final static String[] OPERATORS = {
		"||",
		"&&",
		"|",
		"^",
		"&",
		"==",
		"!=",
		"<",
		">",
		"<=",
		">=",
		"<<",
		">>",
		">>>",
		"+",
		"-",
		"*",
		"/",
		"%"
	};

    @Test
    public void testOnePlusTwo() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(ONO_PLUS_TWO.getBytes()));
    	Compiler compiler = new Compiler(parser.publish());
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("term q=one+two  (1,1) (1,18)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("term q=one+two (1,1) (1,18)");
    }
    
    @Test
    public void testParentheses() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(ONO_PLUS_TWO_IN_PARENTHESES.getBytes()));
    	Compiler compiler = new Compiler(parser.publish());
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("term q=(one+two)  (1,1) (1,20)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("term q=(one+two) (1,1) (1,20)");
    }
    
    @Test
    public void testBinaryEvaluation() throws Exception
    {
    	for (String operator: OPERATORS) {
    		String TAQ = String.format("term q = one %s two", operator);
	    	TaqParser parser = new TaqParser(new ByteArrayInputStream(TAQ.getBytes()));
	    	Compiler compiler = new Compiler(parser.publish());
	    	compiler.compile();
	        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
	        assertThat(iterator.hasNext()).isTrue();
	        SourceMarker sourceMarker = iterator.next();
	        assertThat(iterator.hasNext()).isTrue();
	        sourceMarker = iterator.next();
	        //System.out.println(sourceMarker.toString());
	        String input =  String.format("term q=one%stwo  (1,1) (1,%d)", operator, 17 + operator.length());
	        assertThat(sourceMarker.toString()).isEqualTo(input);
	        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
	        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
	        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
	        assertThat(sourceItem).isNotNull();
	        //System.out.println(sourceItem.toString());
    		String expected = String.format("term q=one%stwo (1,1) (1,%d)", operator, 17 + operator.length());
    		assertThat(sourceItem.toString()).isEqualTo(expected);
    	}
    }
   
    @Test
    public void testUnaryPrefixEvaluation() throws Exception
    {
    	String[] UNARIES = { "~" , "+" , "-", "++", "--", "!" };
    	for (String operator: UNARIES) {
    		String TAQ = String.format("term q = %sx", operator);
	    	TaqParser parser = new TaqParser(new ByteArrayInputStream(TAQ.getBytes()));
	    	Compiler compiler = new Compiler(parser.publish());
	    	compiler.compile();
	        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
	        assertThat(iterator.hasNext()).isTrue();
	        SourceMarker sourceMarker = iterator.next();
	        assertThat(iterator.hasNext()).isTrue();
	        sourceMarker = iterator.next();
	        //System.out.println(sourceMarker.toString());
    		String input = String.format("term q=%sx  (1,1) (1,%d)", operator, 10 + operator.length());
	        assertThat(sourceMarker.toString()).isEqualTo(input);
	        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
	        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
	        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
	        assertThat(sourceItem).isNotNull();
	        //System.out.println(sourceItem.toString());
    		String expected = String.format("term q=%sx (1,1) (1,%d)", operator, 10 + operator.length());
    		assertThat(sourceItem.toString()).isEqualTo(expected);
    	}
    }

    @Test
    public void testUnaryPostfixEvaluation() throws Exception
    {
    	String[] UNARIES = { "++", "--" };
    	for (String operator: UNARIES) {
    		String TAQ = String.format("term q = x%s", operator);
	    	TaqParser parser = new TaqParser(new ByteArrayInputStream(TAQ.getBytes()));
	    	Compiler compiler = new Compiler(parser.publish());
	    	compiler.compile();
	        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
	        assertThat(iterator.hasNext()).isTrue();
	        SourceMarker sourceMarker = iterator.next();
	        assertThat(iterator.hasNext()).isTrue();
	        sourceMarker = iterator.next();
	        //System.out.println(sourceMarker.toString());
	        // term q=x++  (1,1) (1,12)
    		String input = String.format("term q=x%s  (1,1) (1,%d)", operator, 10 + operator.length());
	        assertThat(sourceMarker.toString()).isEqualTo(input);
	        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
	        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
	        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
	        assertThat(sourceItem).isNotNull();
	        //System.out.println(sourceItem.toString());
	        // term q=x++ (1,1) (1,12)
    		String expected = String.format("term q=x%s (1,1) (1,%d)", operator, 10 + operator.length());
    		assertThat(sourceItem.toString()).isEqualTo(expected);
    	}
    }
}
