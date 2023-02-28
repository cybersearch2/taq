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

public class PatternDeclarationTest {

	private final static String PET_REGEX = "^.*<species>dog.*<name>([a-zA-z']*)[^a-zA-z'].*<color>([a-zA-z' ]*)[^a-zA-z' ]";
	public final static String PETS = "pattern matchDog (case_insensitive) \"" + PET_REGEX + "\"";
	// TODO - Test with variable pattern
    @Test
    public void testInWords() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(PETS.getBytes()));
        ParserContext context = new ParserContext(new QueryProgram());
    	Compiler compiler = new Compiler(parser.publish(), context);
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("pattern matchDog  (1,1) (1,116)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("pattern matchDog (1,1) (1,16)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("( case_insensitive ) (1,18) (1,35)");
        sourceItem = sourceItem.getNext();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("\"^.*<species>dog.*<name>([a-zA-z']*)[^a-zA-z'].*<color>([a-zA-z' ]*)[^a-zA-z' ]\" (1,37) (1,116)");
    }
}
