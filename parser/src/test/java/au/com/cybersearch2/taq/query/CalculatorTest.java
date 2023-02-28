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
package au.com.cybersearch2.taq.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.OperandVisitor;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.OperandWalker;
import au.com.cybersearch2.taq.pattern.SolutionPairer;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * CalculatorTest
 * @author Andrew Bowley
 * 11 Jan 2015
 */
public class CalculatorTest 
{

	private static final String KEY = "AxiomKey";
	private static final String TEMPLATE_NAME = "TemplateName";

	@Test
	public void test_unifySolution()
	{
        Calculator calculator = new Calculator();
		Solution solution = mock(Solution.class);
        SolutionPairer pairer = mock(SolutionPairer.class);
        //calculator.pairer = pairer;
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(1);
		OperandWalker walker = mock(OperandWalker.class);
		when(walker.visitAllNodes(pairer)).thenReturn(true);
		when(template.getOperandWalker()).thenReturn(walker);
		when(template.getSolutionPairer(solution)).thenReturn(pairer);
		when(walker.visitAllNodes(isA(OperandVisitor.class))).thenReturn(true);
		when(solution.size()).thenReturn(2);
		calculator.unifySolution(solution, template);
        Template template2 = mock(Template.class);
        when(template2.getId()).thenReturn(1);
		OperandWalker walker2 = mock(OperandWalker.class);
		when(walker2.visitAllNodes(pairer)).thenReturn(false);
		when(template2.getOperandWalker()).thenReturn(walker2);
		calculator.unifySolution(solution, template2);
	}

	@Test
	public void test_completeSolution()
	{
        Calculator calculator = new Calculator();
		Solution solution = mock(Solution.class);
        Template template = mock(Template.class);
        when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
        when(template.getKey()).thenReturn(KEY);
        when(template.getName()).thenReturn(TEMPLATE_NAME);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(TEMPLATE_NAME));
        Axiom axiom = mock(Axiom.class);
        when(axiom.getName()).thenReturn(TEMPLATE_NAME);
        when(template.toAxiom()).thenReturn(axiom);
        when(template.getId()).thenReturn(1);
        assertThat(calculator.completeSolution(solution, template, mock(ExecutionContext.class))).isTrue();
        verify(solution).put(TEMPLATE_NAME, axiom);
       
	}
	
	@Test
	public void test_short_circuit_completeSolution()
	{
        Calculator calculator = new Calculator();
		Solution solution = mock(Solution.class);
        Template template = mock(Template.class);
        when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.SHORT_CIRCUIT);
        assertThat(calculator.completeSolution(solution, template, mock(ExecutionContext.class))).isTrue();
	}

	@Test
	public void test_skip_completeSolution()
	{
        Calculator calculator = new Calculator();
		Solution solution = mock(Solution.class);
        Template template = mock(Template.class);
        when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.SKIP);
        assertThat(calculator.completeSolution(solution, template, mock(ExecutionContext.class))).isFalse();
	}
	
	@Test
	public void test_exception_completeSolution()
	{
        Calculator calculator = new Calculator();
		Solution solution = mock(Solution.class);
        Template template = mock(Template.class);
        when(template.toString()).thenReturn("MyTemplate()");
        ExpressionException expressionException = new ExpressionException("Parser error");
        when(template.evaluate(isA(ExecutionContext.class))).thenThrow(expressionException);
        try
        {
            calculator.completeSolution(solution, template, mock(ExecutionContext.class));
            failBecauseExceptionWasNotThrown(QueryExecutionException.class);
        }
        catch(QueryExecutionException e)
        {
        	assertThat(e.getMessage()).isEqualTo("Error evaluating: MyTemplate()");
        	assertThat(e.getCause()).isEqualTo(expressionException);
        }
	}

	@Test
	public void test_execute()
	{
        Calculator calculator = new Calculator();
		Solution solution = mock(Solution.class);
        Template template = mock(Template.class);
        when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
        when(template.getKey()).thenReturn(KEY);
        when(template.getName()).thenReturn(TEMPLATE_NAME);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(TEMPLATE_NAME));
        Axiom axiom = mock(Axiom.class);
        when(axiom.getName()).thenReturn(KEY);
        when(template.unify(axiom, solution)).thenReturn(true);
        Axiom solutionAxiom = mock(Axiom.class);
        when(solutionAxiom.getName()).thenReturn(TEMPLATE_NAME);
        when(template.toAxiom()).thenReturn(solutionAxiom);
        when(template.getId()).thenReturn(1);
        calculator.execute(axiom, template, solution, mock(ExecutionContext.class));
        verify(solution).put(TEMPLATE_NAME, solutionAxiom);
	}
	
	@Test
	public void test_execute_no_seed_axiom()
	{
        Calculator calculator = new Calculator();
		Solution solution = mock(Solution.class);
		when(solution.size()).thenReturn(2);
		QualifiedName qualifiedTemplateName = QualifiedName.parseTemplateName(TEMPLATE_NAME);
		Template template = mock(Template.class);
        SolutionPairer pairer = new SolutionPairer(solution, 1, qualifiedTemplateName);
        //calculator.pairer = pairer;
		Operand term1 = mock(Operand.class);
		Term term2 = mock(Term.class);
		when(term1.unifyTerm(term2, 1)).thenReturn(1);
		Operand term3 = mock(Operand.class);
		Term term4 = mock(Term.class);
		when(term3.unifyTerm(term4, 1)).thenReturn(1);
        when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
        when(template.getKey()).thenReturn(KEY);
        when(template.getName()).thenReturn(TEMPLATE_NAME);
        when(template.getQualifiedName()).thenReturn(qualifiedTemplateName);
		OperandWalker walker = mock(OperandWalker.class);
		when(walker.visitAllNodes(pairer)).thenReturn(true);
		when(template.getOperandWalker()).thenReturn(walker);
        Axiom solutionAxiom = mock(Axiom.class);
        when(solutionAxiom.getName()).thenReturn(KEY);
        when(template.toAxiom()).thenReturn(solutionAxiom);
        when(template.getId()).thenReturn(1);
        calculator.execute(template, solution, mock(ExecutionContext.class));
        verify(solution).put(TEMPLATE_NAME, solutionAxiom);
	}
	
    protected static QualifiedName parseTemplateName(String name)
    {
        return new QualifiedTemplateName(QualifiedName.EMPTY, name);
    }
}
