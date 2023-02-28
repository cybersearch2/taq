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
package au.com.cybersearch2.taq.expression;

import org.junit.Test;

import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;

import static org.assertj.core.api.Assertions.*;

/**
 * FactOperandTest
 * @author Andrew Bowley
 * 20 Aug 2015
 */
public class TestFactOperand
{
    static final String AXIOM_KEY = "key";
    static final String AXIOM_NAME = "axiom.key";
    static QualifiedName QNAME = QualifiedName.parseName(AXIOM_NAME);
    
    @Test 
    public void test_types()
    {
        BooleanOperand booleanOperand = new TestBooleanOperand("BooleanOperand");
        FactOperand factOperand = new FactOperand(booleanOperand);
        factOperand.evaluate(1);
        assertThat((Boolean)factOperand.getValue()).isFalse();
        assertThat(factOperand.getId()).isEqualTo(1);
        booleanOperand.assign(new Parameter(Term.ANONYMOUS, Boolean.FALSE));
        factOperand.evaluate(2);
        assertThat((Boolean)factOperand.getValue()).isTrue();
        assertThat(factOperand.getId()).isEqualTo(2);
        booleanOperand.assign(new Parameter(Term.ANONYMOUS, Boolean.TRUE));
        factOperand.evaluate(3);
        assertThat((Boolean)factOperand.getValue()).isTrue();
        assertThat(factOperand.getId()).isEqualTo(3);

        // Create AxiomTermList to contain query result. 
        AxiomTermList axiomTermList = new AxiomTermList(QNAME, new QualifiedName(AXIOM_KEY));
        // Create Variable to be axiomTermList container. Give it the same name as the inner Template 
        // so it is qualified by the name of the enclosing Template
        Variable listVariable = new Variable(QNAME);
        listVariable.assign(new Parameter(Term.ANONYMOUS, axiomTermList));
        factOperand = new FactOperand(listVariable);
        factOperand.evaluate(1);
        assertThat((Boolean)factOperand.getValue()).isFalse();
        Axiom axiom = new Axiom(AXIOM_NAME);
        Parameter x = new Parameter("x");
        axiom.addTerm(x);
        axiomTermList.setAxiom(axiom);
        factOperand.evaluate(2);
        assertThat((Boolean)factOperand.getValue()).isFalse();
        Parameter y = new Parameter("y");
        axiom.addTerm(y);
        factOperand.evaluate(3);
        assertThat((Boolean)factOperand.getValue()).isFalse();
        x.setValue(Long.valueOf(1));
        y.setValue(Long.valueOf(2));
        factOperand.evaluate(4);
        assertThat((Boolean)factOperand.getValue()).isTrue();
    }
}
