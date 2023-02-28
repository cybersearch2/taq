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
package au.com.cybersearch2.taq.pattern;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

import org.junit.Test;

import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;

/**
 * AxiomTest
 * @author Andrew Bowley
 * 30 Nov 2014
 */
public class AxiomTest 
{
    
    private static final String TERM_NAME = "term";
    private static final String NAME = "axiom";

    @Test 
    public void test_serialization() throws Exception
    {
        int termCount = 10;
        int id = 0;
        Parameter[] testTerms = new Parameter[termCount];
        testTerms[id] = new Parameter(TERM_NAME + id, BigDecimal.TEN);
        testTerms[id].setId(++id);
        Axiom axiom = new Axiom(NAME);
        axiom.addTerm(testTerms[0]);
        File serializeFile = File.createTempFile("axiom_test_serialization", null, null);
        serializeFile.deleteOnExit();
        writeAxiom(axiom, serializeFile);
        Axiom marshalled = readAxiom(serializeFile);
        assertThat(marshalled.getName()).isEqualTo(NAME);
        assertThat(marshalled.getTermCount()).isEqualTo(1);
        Term term = marshalled.getTermByIndex(0);
        assertThat(term.getId()).isEqualTo(1);
        assertThat(term.getName()).isEqualTo(TERM_NAME + 0);
        assertThat(term.getValue()).isEqualTo(BigDecimal.TEN);
    }
    
    private Axiom readAxiom(File serializeFile) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(serializeFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object marshalled = ois.readObject();
        ois.close();
        return (Axiom)marshalled;
    }

    private void writeAxiom(Axiom axiom, File serializeFile) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(serializeFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(axiom);
        oos.flush();
        oos.close();
    }

    protected static QualifiedName parseTemplateName(String name)
    {
        return new QualifiedTemplateName(QualifiedName.EMPTY, name);
    }
}
