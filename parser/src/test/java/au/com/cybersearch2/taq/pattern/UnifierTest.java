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

import org.junit.Test;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.query.Solution;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * UnifierTest
 * @author Andrew Bowley
 * 22 Dec 2014
 */
public class UnifierTest 
{
    static final String TEMPLATE_NAME = "TemplateName";
    static final String OPERAND_NAME = "x";
    
	@Test
	public void test_next_pair()
	{
		// Pairing case
		Axiom axiom = mock(Axiom.class);
		Operand operand = mock(Operand.class);
		Term term2 = mock(Term.class);
		when(operand.getName()).thenReturn(OPERAND_NAME);
        when(axiom.getTermByIndex(1)).thenReturn(term2);
		when(operand.isEmpty()).thenReturn(true);
		when(operand.getArchetypeIndex()).thenReturn(0);
        when(operand.getArchetypeId()).thenReturn(3);
		Template template = mock(Template.class);
		when(template.getId()).thenReturn(3);
		int[] termMapping = new int[] { 1, 0};
		Unifier underTest = new Unifier(template, axiom, termMapping, new Solution(Locale.getDefault()));
 		assertThat(underTest.next(operand, 1)).isTrue();
 		verify(operand).unifyTerm(term2, 3);
	}
	
    @Test
    public void test_non_empty_match()
    {
		// Operand not empty: axiom value == operand value
        Axiom axiom = mock(Axiom.class);
        Operand operand = mock(Operand.class);
        Term term2 = mock(Term.class);
        when(term2.isEmpty()).thenReturn(false);
        when(term2.getValue()).thenReturn(Integer.valueOf(2));
        when(operand.getName()).thenReturn(OPERAND_NAME);
        when(axiom.getTermByIndex(1)).thenReturn(term2);
        when(operand.isEmpty()).thenReturn(false);
        when(operand.getValue()).thenReturn(Integer.valueOf(2));
        when(operand.getArchetypeIndex()).thenReturn(0);
        when(operand.getArchetypeId()).thenReturn(3);
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(3);
        int[] termMapping = new int[] { 1, 0};
        Unifier underTest = new Unifier(template, axiom, termMapping, new Solution(Locale.getDefault()));
        assertThat(underTest.next(operand, 1)).isTrue();
        verify(operand, times(0)).unifyTerm(term2, 3);
    }
    
    @Test
    public void test_term_empty()
    {
		// Axiom parameter is empty - should never happen
        Axiom axiom = mock(Axiom.class);
        Operand operand = mock(Operand.class);
        Term term2 = mock(Term.class);
        when(term2.isEmpty()).thenReturn(true);
        when(term2.getValue()).thenReturn(Integer.valueOf(2));
        when(operand.getName()).thenReturn(OPERAND_NAME);
        when(axiom.getTermByIndex(1)).thenReturn(term2);
        when(operand.isEmpty()).thenReturn(false);
        when(operand.getValue()).thenReturn(Integer.valueOf(2));
        when(operand.getArchetypeIndex()).thenReturn(0);
        when(operand.getArchetypeId()).thenReturn(3);
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(3);
        int[] termMapping = new int[] { 1, 0};
        Unifier underTest = new Unifier(template, axiom, termMapping, new Solution(Locale.getDefault()));
        assertThat(underTest.next(operand, 1)).isTrue();
        verify(operand, times(0)).unifyTerm(term2, 3);
    }
    
    @Test
    public void test_non_empty_non_match()
    {
 		// Axiom value != Template value
        // Operand not empty: axiom value == operand value
        Axiom axiom = mock(Axiom.class);
        Operand operand = mock(Operand.class);
        Term term2 = mock(Term.class);
        when(term2.isEmpty()).thenReturn(false);
        when(term2.getValue()).thenReturn(Integer.valueOf(3));
        when(operand.getName()).thenReturn(OPERAND_NAME);
        when(axiom.getTermByIndex(1)).thenReturn(term2);
        when(operand.isEmpty()).thenReturn(false);
        when(operand.getValue()).thenReturn(Integer.valueOf(2));
        when(operand.getArchetypeIndex()).thenReturn(0);
        when(operand.getArchetypeId()).thenReturn(3);
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(3);
        int[] termMapping = new int[] { 1, 0};
        Unifier underTest = new Unifier(template, axiom, termMapping, new Solution(Locale.getDefault()));
        assertThat(underTest.next(operand, 1)).isFalse();
        verify(operand, times(0)).unifyTerm(term2, 3);
    }
    
    @Test
    public void test_empty_name_operand()
    {
        // Operand is anonymous
        Axiom axiom = mock(Axiom.class);
        Operand operand = mock(Operand.class);
        Term term2 = mock(Term.class);
        when(operand.getName()).thenReturn("");
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(3);
        int[] termMapping = new int[] { 1, 0};
        Unifier underTest = new Unifier(template, axiom, termMapping, new Solution(Locale.getDefault()));
        assertThat(underTest.next(operand, 1)).isTrue();
        verify(operand, times(0)).unifyTerm(term2, 3);
    }

    @Test
    public void test_no_pair()
    {
        // Term not paired
        Axiom axiom = mock(Axiom.class);
        Operand operand = mock(Operand.class);
        Term term2 = mock(Term.class);
        when(axiom.getTermByIndex(1)).thenReturn(term2);
        when(operand.getName()).thenReturn(OPERAND_NAME);
        when(operand.getArchetypeIndex()).thenReturn(-1);
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(3);
        TermListManager archetype = mock(TermListManager.class);
        when(template.getArchetype()).thenReturn(archetype);
        when(archetype.getIndexForName(OPERAND_NAME)).thenReturn(0);
        int[] termMapping = new int[] { 1, 0};
        when(operand.getValue()).thenReturn("TestValue");
        when(term2.getValue()).thenReturn("TestValue");
        Unifier underTest = new Unifier(template, axiom, termMapping, new Solution(Locale.getDefault()));
        assertThat(underTest.next(operand, 1)).isTrue();
        verify(operand, times(0)).unifyTerm(term2, 3);
    }
    
    @Test
    public void test_solution_pair()
    {
        // Term not paired
        Axiom axiom = mock(Axiom.class);
        Operand operand = mock(Operand.class);
        Term term2 = mock(Term.class);
        when(axiom.getTermByIndex(1)).thenReturn(term2);
        when(operand.getName()).thenReturn(OPERAND_NAME);
        when(operand.getArchetypeIndex()).thenReturn(-1);
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(3);
        TermListManager archetype = mock(TermListManager.class);
        when(template.getArchetype()).thenReturn(archetype);
        when(archetype.getIndexForName(OPERAND_NAME)).thenReturn(0);
        int[] termMapping = new int[] { 1, 0};
        Solution solution = mock(Solution.class);
        Set<String> keyset = new HashSet<String>();
        keyset.add("key");
        when(solution.keySet()).thenReturn(keyset);
        SolutionPairer solutionPairer = mock(SolutionPairer.class);
        when(template.getSolutionPairer(solution)).thenReturn(solutionPairer);
        when(solutionPairer.next(operand, 0)).thenReturn(true);
        when(operand.getValue()).thenReturn("TestValue");
        when(term2.getValue()).thenReturn("TestValue");
        Unifier underTest = new Unifier(template, axiom, termMapping, solution);
        assertThat(underTest.next(operand, 1)).isTrue();
        verify(operand, times(0)).unifyTerm(term2, 3);
    }

}
