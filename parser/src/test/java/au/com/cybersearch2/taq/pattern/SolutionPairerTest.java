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
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.query.Solution;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Locale;

/**
 * SolutionPairerTest
 * @author Andrew Bowley
 * 23 Dec 2014
 */
public class SolutionPairerTest 
{
    static final String TEMPLATE_NAME = "TemplateName";
    static final String ONE = "one";
    static final String TWO = "two";
    static QualifiedName CONTEXT_NAME = QualifiedName.parseTemplateName(TEMPLATE_NAME);
    static QualifiedName ONE_QNAME = QualifiedName.parseName(ONE, CONTEXT_NAME);
    static QualifiedName TWO_QNAME = QualifiedName.parseName(TWO, CONTEXT_NAME);
    

	@Test
	public void test_next_Solution_operand_empty()
	{
		// Pairing case
		Axiom oneAxiom = mock(Axiom.class);
		Operand templateOperand = mock(Operand.class);
		OperatorTerm solutionTerm = mock(OperatorTerm.class);
		Operator operator = mock(Operator.class);
        Operator operator2 = mock(Operator.class);
		when(solutionTerm.getOperator()).thenReturn(operator);
		Trait trait = mock(Trait.class);
		when(trait.getOperandType()).thenReturn(OperandType.INTEGER);
		when(operator.getTrait()).thenReturn(trait);
		when(templateOperand.isEmpty()).thenReturn(true);
		when(templateOperand.getOperator()).thenReturn(operator2);
		when(oneAxiom.getTermByName(TWO)).thenReturn(solutionTerm);
		when(templateOperand.getQualifiedName()).thenReturn(TWO_QNAME);
		when(solutionTerm.getName()).thenReturn(TWO);
		Solution solution = new Solution(Locale.getDefault());
		solution.put(CONTEXT_NAME.toString(), oneAxiom);
		Template template = mock(Template.class);
		when(template.getQualifiedName()).thenReturn(CONTEXT_NAME);
		when(template.getId()).thenReturn(3);
		SolutionPairer solutionPairer = new SolutionPairer(solution, 3, CONTEXT_NAME);
		assertThat(solutionPairer.next(templateOperand, 1)).isTrue();
		verify(templateOperand).unifyTerm(solutionTerm, 3);
		verify(operator2).setTrait(trait);
	}
    
	@Test
	public void test_next_operand_non_empty_non_match()
	{
		Axiom oneAxiom = mock(Axiom.class);
		Operand templateOperand = mock(Operand.class);
		OperatorTerm solutionTerm = mock(OperatorTerm.class);
		when(solutionTerm.isEmpty()).thenReturn(false);
		when(oneAxiom.getTermByName(TWO)).thenReturn(solutionTerm);
		when(templateOperand.getQualifiedName()).thenReturn(TWO_QNAME);
		when(solutionTerm.getName()).thenReturn(TWO);
		when(templateOperand.isEmpty()).thenReturn(false);
		when(templateOperand.getValue()).thenReturn(Long.valueOf(99l));
        when(solutionTerm.getValue()).thenReturn(Long.valueOf(71l));
		Solution solution = new Solution(Locale.getDefault());
		solution.put(CONTEXT_NAME.toString(), oneAxiom);
	    ITemplate template = mock(Template.class);
	    when(template.getQualifiedName()).thenReturn(CONTEXT_NAME);
		SolutionPairer solutionPairer = new SolutionPairer(solution, 3, CONTEXT_NAME);
		assertThat(solutionPairer.next(templateOperand, 1)).isFalse();
        verify(templateOperand, times(0)).unifyTerm(solutionTerm, 3);
	}
	
	@Test
    public void test_next_operand_non_empty_match()
    {
        Axiom oneAxiom = mock(Axiom.class);
        Operand templateOperand = mock(Operand.class);
        OperatorTerm solutionTerm = mock(OperatorTerm.class);
        when(solutionTerm.isEmpty()).thenReturn(false);
        when(oneAxiom.getTermByName(TWO)).thenReturn(solutionTerm);
        when(templateOperand.getQualifiedName()).thenReturn(TWO_QNAME);
        when(solutionTerm.getName()).thenReturn(TWO);
        when(templateOperand.isEmpty()).thenReturn(false);
        when(templateOperand.getValue()).thenReturn(Long.valueOf(99l));
        when(solutionTerm.getValue()).thenReturn(Long.valueOf(99l));
        Solution solution = new Solution(Locale.getDefault());
        solution.put(CONTEXT_NAME.toString(), oneAxiom);
        ITemplate template = mock(Template.class);
        when(template.getQualifiedName()).thenReturn(CONTEXT_NAME);
        SolutionPairer solutionPairer = new SolutionPairer(solution, 3, CONTEXT_NAME);
        assertThat(solutionPairer.next(templateOperand, 1)).isTrue();
    } 

	// Note: ".two" and "one." are not parseable by QualifiedName
	@Test
	public void test_next_bad_name()
	{
		Solution solution = new Solution(Locale.getDefault());
		SolutionPairer solutionPairer = new SolutionPairer(solution, 3, CONTEXT_NAME);
		Operand templateOperand = mock(Operand.class);
		templateOperand = mock(Operand.class);
		when(templateOperand.getQualifiedName()).thenReturn(QualifiedName.parseGlobalName("one.three"));
		solutionPairer = new SolutionPairer(solution, 3, CONTEXT_NAME);
		assertThat(solutionPairer.next(templateOperand, 1)).isFalse();
		templateOperand = mock(Operand.class);
		when(templateOperand.getQualifiedName()).thenReturn(QualifiedName.parseGlobalName("one"));
		solutionPairer = new SolutionPairer(solution, 3, CONTEXT_NAME);
		assertThat(solutionPairer.next(templateOperand, 1)).isFalse();
		when(templateOperand.getQualifiedName()).thenReturn(QualifiedName.parseGlobalName("one.two"));
		solutionPairer = new SolutionPairer(solution, 3, CONTEXT_NAME);
		assertThat(solutionPairer.next(templateOperand, 1)).isFalse();
	} 
}
