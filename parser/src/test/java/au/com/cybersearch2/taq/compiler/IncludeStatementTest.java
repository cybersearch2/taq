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
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.engine.SourceMarker;
import au.com.cybersearch2.taq.model.TaqParser;

public class IncludeStatementTest {

	private final static String INCLUDE = "include \"pattern.taq\"";

	private File resourcePath;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Before
	public void setUp() throws IOException {
		File petsXpl = folder.newFile("pattern.taq");
		Files.writeString(petsXpl.toPath(), PatternDeclarationTest.PETS, StandardCharsets.UTF_8);
        resourcePath = folder.getRoot();
	}
	
    @Test
    public void testInclude() throws Exception
    {
    	TaqParser parser = new TaqParser(new ByteArrayInputStream(INCLUDE.getBytes()));
    	QueryProgram queryProgram = new QueryProgram();
        queryProgram.setResourceBase(resourcePath);
        ParserContext context = new ParserContext(queryProgram);
    	Compiler compiler = new Compiler(parser.publish(), context);
    	compiler.compile();
        Iterator<SourceMarker> iterator = compiler.getSourceTracker().getSourceMarkers().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SourceMarker sourceMarker = iterator.next();
        assertThat(iterator.hasNext()).isTrue();
        sourceMarker = iterator.next();
        //System.out.println(sourceMarker.toString());
        assertThat(sourceMarker.toString()).isEqualTo("include \"pattern.taq\"  (1,1) (1,21)");
        assertThat(sourceMarker.getSourceDocumentId()).isEqualTo(0);
        assertThat(sourceMarker.getHeadSourceItem()).isNotNull();
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        assertThat(sourceItem).isNotNull();
        //System.out.println(sourceItem.toString());
        assertThat(sourceItem.toString()).isEqualTo("include \"pattern.taq\" (1,1) (1,21)");
    }

}
