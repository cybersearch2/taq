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

import org.junit.Test;

import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.expression.StringOperand;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * TemplateArchetypeTest
 * @author Andrew Bowley
 * 17May,2017
 */
public class TemplateArchetypeTest
{
 
    @Test
    public void test_pair_axiom_by_position()
    {
        AxiomArchetype pairArchetype = new AxiomArchetype(QualifiedName.parseGlobalName("charge"));
        new Axiom(pairArchetype, "athens", 23);
        assertThat(pairArchetype.isAnonymousTerms()).isTrue();
        Template template = createChargeUnificationTarget();
        int[] termMapping = template.getTemplateArchetype().createTermMapping(pairArchetype, false);
        assertThat(termMapping.length).isEqualTo(2);
        assertThat(termMapping).isEqualTo(new int[]{ 0, 1 });
        assertThat(pairArchetype.isAnonymousTerms()).isFalse();
        assertThat(pairArchetype.getMetaData(0).getName()).isEqualTo("city");
        assertThat(pairArchetype.getMetaData(1).getName()).isEqualTo("fee");
    }
    
    @Test
    public void test_pair_axiom_extra_term_by_position()
    {
        AxiomArchetype pairArchetype = new AxiomArchetype(QualifiedName.parseGlobalName("charge"));
        new Axiom(pairArchetype, "sparta", 13, "Spiro");
        assertThat(pairArchetype.isAnonymousTerms()).isTrue();
        Template template = createChargeUnificationTarget();
        int[] termMapping = template.getTemplateArchetype().createTermMapping(pairArchetype, false);
        assertThat(termMapping.length).isEqualTo(2);
        assertThat(termMapping).isEqualTo(new int[]{ 0, 1 });
        assertThat(pairArchetype.isAnonymousTerms()).isFalse();
        assertThat(pairArchetype.getMetaData(0).getName()).isEqualTo("city");
        assertThat(pairArchetype.getMetaData(1).getName()).isEqualTo("fee");
        assertThat(pairArchetype.getMetaData(2).isAnonymous()).isTrue();
    }
    
    @Test
    public void test_pair_axiom_by_name()
    {
        AxiomArchetype pairArchetype = new AxiomArchetype(QualifiedName.parseGlobalName("charge"));
        new Axiom(pairArchetype, new Parameter("fee", 13), new Parameter("city", "athens"));
        assertThat(pairArchetype.isAnonymousTerms()).isFalse();
        Template template = createChargeUnificationTarget();
        int[] termMapping = template.getTemplateArchetype().createTermMapping(pairArchetype, false);
        assertThat(termMapping.length).isEqualTo(2);
        assertThat(termMapping).isEqualTo(new int[]{ 1,0 });
    }
    
    @Test
    public void test_pair_axiom_by_name_plus_anon_term()
    {
        AxiomArchetype pairArchetype = new AxiomArchetype(QualifiedName.parseGlobalName("charge"));
        new Axiom(pairArchetype, new Parameter("fee", 13), "Spiro", new Parameter("city", "athens"));
        assertThat(pairArchetype.isAnonymousTerms()).isFalse();
        Template template = createChargeUnificationTarget();
        int[] termMapping = template.getTemplateArchetype().createTermMapping(pairArchetype, false);
        assertThat(termMapping.length).isEqualTo(2);
        assertThat(termMapping).isEqualTo(new int[]{ 2,0 });
    }
    
    @Test
    public void test_pair_axiom_by_name_one_anon_pair()
    {
        AxiomArchetype pairArchetype = new AxiomArchetype(QualifiedName.parseGlobalName("charge"));
        new Axiom(pairArchetype, new Parameter("fee", 13), "Spiro", new Parameter("city", "athens"));
        assertThat(pairArchetype.isAnonymousTerms()).isFalse();
        Template template = createChargeUnificationTarget();
        template.addTerm(new StringOperand(QualifiedName.parseGlobalName("manager")));
        int[] termMapping = template.getTemplateArchetype().createTermMapping(pairArchetype, false);
        assertThat(termMapping.length).isEqualTo(3);
        assertThat(termMapping).isEqualTo(new int[]{ 2,0,-1 });
    }
    
   protected Template createChargeUnificationTarget()
    {
        QualifiedName contextName = parseTemplateName("charge");
        TemplateArchetype templateArchetype = new TemplateArchetype(contextName);
        Template template = new Template(templateArchetype);
        StringOperand city = new StringOperand(QualifiedName.parseName("city", contextName));
        template.addTerm(city);
        IntegerOperand fee = new IntegerOperand(QualifiedName.parseName("fee", contextName));
        template.addTerm(fee);
        return template;
    }
    
    protected static QualifiedName parseTemplateName(String name)
    {
        return new QualifiedTemplateName(QualifiedName.EMPTY, name);
    }

}
