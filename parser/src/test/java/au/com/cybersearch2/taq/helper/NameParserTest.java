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
package au.com.cybersearch2.taq.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import org.junit.Test;

import au.com.cybersearch2.taq.language.NameParser;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.SyntaxException;


/**
 * NameParserTest
 * @author Andrew Bowley
 * 19Jul.,2017
 */
public class NameParserTest
{
    static final String TEST_NAME1 = "one_part_name";
    static final String TEST_NAME_AT = "one_part_name@";
    static final String TEST_NAME2 = "part1.part2";
    static final String TEST_AT_NAME2 = "part2@part1";
    static final String TEST_NAME2_AT = "part1.part2@";
    static final String TEST_NAME3 = "part1.part2.part3";
    static final String TEST_AT_NAME3 = "part3@part2.part1";
    static final String TEST_NAME2_AT_NAME = "part2.part3@part1";
    static final String TEST_NAME4 = "part3.part2@part1.part4";

    @Test
    public void testEmptyName()
    {
        NameParser nameParser = new NameParser("");
        assertThat(nameParser.getQualifiedName()).isEqualTo(QualifiedName.ANONYMOUS);
    }

    @Test
    public void test1_partName()
    {
        NameParser nameParser = new NameParser(TEST_NAME1);
        assertThat(nameParser.getQualifiedName()).isEqualTo(new QualifiedName(TEST_NAME1));
    }
    
    @Test
    public void test1_partNameAt()
    {
        NameParser nameParser = new NameParser(TEST_NAME_AT);
        assertThat(nameParser.getQualifiedName()).isEqualTo(new QualifiedName(TEST_NAME1));
    }
    
    @Test
    public void test2_partName()
    {
        NameParser nameParser = new NameParser(TEST_NAME2);
        assertThat(nameParser.getQualifiedName()).isEqualTo(QualifiedName.parseGlobalName(TEST_NAME2));
    }
    
    @Test
    public void test2_partNameAt()
    {
        NameParser nameParser = new NameParser(TEST_NAME2_AT);
        assertThat(nameParser.getQualifiedName()).isEqualTo(QualifiedName.parseGlobalName("part1.part2"));
    }
    
    @Test
    public void test2_at_partName()
    {
        NameParser nameParser = new NameParser(TEST_AT_NAME2);
        assertThat(nameParser.getQualifiedName()).isEqualTo(QualifiedName.parseName(TEST_NAME2));
    }
    
    @Test
    public void test2_at_partName_at()
    {
        try
        {
            new NameParser(TEST_AT_NAME2 + "@");
            failBecauseExceptionWasNotThrown(SyntaxException.class);
        }
        catch(SyntaxException e)
        {
            assertThat(e.getMessage()).isEqualTo("Name \"" + TEST_AT_NAME2 + "@" + "\" with more than one \"@\" is invalid");
        }
    }
        
    @Test
    public void test3_partName()
    {
        NameParser nameParser = new NameParser(TEST_NAME3);
        assertThat(nameParser.getQualifiedName()).isEqualTo(QualifiedName.parseGlobalName(TEST_NAME3));
    }

    @Test
    public void test3_at_partName()
    {
        NameParser nameParser = new NameParser(TEST_AT_NAME3);
        assertThat(nameParser.getQualifiedName()).isEqualTo(QualifiedName.parseName(TEST_NAME3));
    }

    @Test
    public void test2_at_name()
    {
        NameParser nameParser = new NameParser(TEST_NAME2_AT_NAME);
        assertThat(nameParser.getQualifiedName()).isEqualTo(QualifiedName.parseName(TEST_NAME3));
    }
    
    @Test
    public void test_invalidName()
    {
        try
        {
            new NameParser("." +TEST_AT_NAME2);
            failBecauseExceptionWasNotThrown(SyntaxException.class);
        }
        catch(SyntaxException e)
        {
            assertThat(e.getMessage()).isEqualTo("Name \"" + "." +TEST_AT_NAME2 + "\" is invalid");
        }
    }
    
    @Test
    public void test_invalidNameAfterAt()
    {
        try
        {
            new NameParser(TEST_AT_NAME3 + ".");
            failBecauseExceptionWasNotThrown(SyntaxException.class);
        }
        catch(SyntaxException e)
        {
            assertThat(e.getMessage()).isEqualTo("Name \"" + TEST_AT_NAME3 + "." + "\" is invalid after \"@\"");
        }
    }
    
    @Test
    public void test_invalidName4()
    {
        try
        {
            new NameParser(TEST_NAME4);
            failBecauseExceptionWasNotThrown(SyntaxException.class);
        }
        catch(SyntaxException e)
        {
            assertThat(e.getMessage()).isEqualTo("Name \"" + TEST_NAME4 + "\" with more than 3 parts is invalid");
        }
    }
}
