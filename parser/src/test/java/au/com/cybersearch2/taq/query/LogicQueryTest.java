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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import au.com.cybersearch2.taq.axiom.AxiomListSource;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.TestBigDecimalOperand;
import au.com.cybersearch2.taq.expression.TestBooleanOperand;
import au.com.cybersearch2.taq.expression.TestDoubleOperand;
import au.com.cybersearch2.taq.expression.TestIntegerOperand;
import au.com.cybersearch2.taq.expression.TestStringOperand;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.OperandVisitor;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.BigDecimalTerm;
import au.com.cybersearch2.taq.language.BooleanTerm;
import au.com.cybersearch2.taq.language.DoubleTerm;
import au.com.cybersearch2.taq.language.IntegerTerm;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.StringTerm;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.OperandWalker;
import au.com.cybersearch2.taq.pattern.SolutionPairer;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;

/**
 * LogicQueryTest
 * @author Andrew Bowley
 * 31 Dec 2014
 */
public class LogicQueryTest 
{
    static final String NAME = "axiom_name";
	static final String KEY = "axiom_key";
  
    @Test
    public void test_setQueryStatusComplete()
    {
		AxiomSource axiomSource = mock(AxiomSource.class);
		LogicQuery logicQuery = new LogicQuery(axiomSource);
		logicQuery.setQueryStatusComplete();
		assertThat(logicQuery.getQueryStatus()).isEqualTo(QueryStatus.complete);
    }

	@Test
	public void test_unifySolution()
	{
		AxiomSource axiomSource = mock(AxiomSource.class);
		LogicQuery logicQuery = new LogicQuery(axiomSource);
		Solution solution = mock(Solution.class);
	    SolutionPairer pairer = mock(SolutionPairer.class);
		Operand term1 = mock(Operand.class);
		Term term2 = mock(Term.class);
		when(term1.unifyTerm(term2, 1)).thenReturn(1);
		Operand term3 = mock(Operand.class);
		Term term4 = mock(Term.class);
		when(term3.unifyTerm(term4, 1)).thenReturn(1);
        Template template = mock(Template.class);
        when(template.getId()).thenReturn(1);
        when(template.getSolutionPairer(solution)).thenReturn(pairer);
		OperandWalker walker = mock(OperandWalker.class);
		when(walker.visitAllNodes(isA(OperandVisitor.class))).thenReturn(true);
		when(template.getOperandWalker()).thenReturn(walker);
		when(solution.size()).thenReturn(2);
		assertThat(logicQuery.unifySolution(solution, template)).isTrue();
        Template template2 = mock(Template.class);
        when(template2.getId()).thenReturn(1);
		OperandWalker walker2 = mock(OperandWalker.class);
		when(walker2.visitAllNodes(isA(OperandVisitor.class))).thenReturn(false);
		when(template2.getOperandWalker()).thenReturn(walker2);
		assertThat(logicQuery.unifySolution(solution, template2)).isFalse();
	}

	@Test
	public void test_iterate_empty_source()
	{
		Solution solution = new Solution(Locale.getDefault());
		AxiomSource axiomSource = mock(AxiomSource.class);
		Template template = mock(Template.class);
	    final Iterator<Axiom> iterator = 	new Iterator<Axiom>(){
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public Axiom next() {
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		LogicQuery logicQuery = new LogicQuery(axiomSource);
		assertThat(logicQuery.iterate(solution, template, context)).isFalse();
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
	}


	@Test
	public void test_iterate_soution_found()
	{
		AxiomSource axiomSource = mock(AxiomSource.class);
		final Axiom axiom = mock(Axiom.class);
		Axiom solutionAxiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(KEY);
		Template template = mock(Template.class);
		when(template.getName()).thenReturn(NAME);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
		when(template.getKey()).thenReturn(KEY);
		when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
		when(template.isFact()).thenReturn(true);
		when(template.toAxiom()).thenReturn(solutionAxiom);
		Solution solution = new Solution(Locale.getDefault());
		when(template.unify(axiom, solution)).thenReturn(true);
		final Iterator<Axiom> iterator = 	new Iterator<Axiom>(){

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Axiom next() {
				return axiom;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		LogicQuery logicQuery = new LogicQuery(axiomSource);
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
		assertThat(logicQuery.iterate(solution, template, context)).isTrue();
		assertThat(solution.getAxiom(NAME)).isEqualTo(solutionAxiom);
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.in_progress);
	}

	@Test
	public void test_iterate_solution_with_handler()
	{
		Solution solution = new Solution(Locale.getDefault());
		SolutionHandler solutionHandler = mock(SolutionHandler.class);
        // SolutionHandler returns true, so retain solution
		when(solutionHandler.onSolution(solution)).thenReturn(true);
		AxiomSource axiomSource = mock(AxiomSource.class);
		final Axiom axiom = mock(Axiom.class);
		Axiom solutionAxiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(KEY);
		Template template = mock(Template.class);
		when(template.getKey()).thenReturn(KEY);
		when(template.getName()).thenReturn(NAME);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
		when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
		when(template.isFact()).thenReturn(true);
		when(template.toAxiom()).thenReturn(solutionAxiom);
		when(template.unify(axiom, solution)).thenReturn(true);
		final Iterator<Axiom> iterator = 	new Iterator<Axiom>(){

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Axiom next() {
				return axiom;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		LogicQuery logicQuery = new LogicQuery(axiomSource, solutionHandler);
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
		assertThat(logicQuery.iterate(solution, template, context)).isTrue();
		assertThat(solution.getAxiom(NAME)).isEqualTo(solutionAxiom);
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.in_progress);
	}
	
	@Test
	public void test_iterate_no_solution_with_handler()
	{
		Solution solution = new Solution(Locale.getDefault());
		SolutionHandler solutionHandler = mock(SolutionHandler.class);
		// SolutionHandler returns false, so discard solution
		when(solutionHandler.onSolution(solution)).thenReturn(false);
		AxiomSource axiomSource = mock(AxiomSource.class);
		final Axiom axiom = mock(Axiom.class);
		Axiom solutionAxiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(NAME);
		Template template = mock(Template.class);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
        when(template.getTermCount()).thenReturn(1);
		when(template.getKey()).thenReturn(NAME);
		when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
		when(template.isFact()).thenReturn(true);
		when(template.toAxiom()).thenReturn(solutionAxiom);
		when(template.unify(axiom, solution)).thenReturn(true);
		final Iterator<Axiom> iterator = new Iterator<Axiom>(){
            int count = 0;
			@Override
			public boolean hasNext() {
				return count < 1;
			}

			@Override
			public Axiom next() {
				++count;
				return axiom;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		LogicQuery logicQuery = new LogicQuery(axiomSource, solutionHandler);
		assertThat(logicQuery.iterate(solution, template, context)).isFalse();
        Axiom blankAxiom = solution.getAxiom(NAME);
		assertThat(blankAxiom.getTermCount()).isEqualTo(0);
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
		verify(template).backup(true);
	}

	@Test
	public void test_iterate_not_fact()
	{
		Solution solution = new Solution(Locale.getDefault());
		SolutionHandler solutionHandler = mock(SolutionHandler.class);
		AxiomSource axiomSource = mock(AxiomSource.class);
		final Axiom axiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(NAME);
		Template template = mock(Template.class);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
        when(template.getTermCount()).thenReturn(1);
		when(template.getKey()).thenReturn(NAME);
		when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
		when(template.isFact()).thenReturn(false);
		when(template.unify(axiom, solution)).thenReturn(true);
		final Iterator<Axiom> iterator = 	new Iterator<Axiom>(){
            int count = 0;
			@Override
			public boolean hasNext() {
				return count < 1;
			}

			@Override
			public Axiom next() {
				++count;
				return axiom;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		LogicQuery logicQuery = new LogicQuery(axiomSource, solutionHandler);
		assertThat(logicQuery.iterate(solution, template, context)).isFalse();
		Axiom blankAxiom = solution.getAxiom(NAME);
		assertThat(blankAxiom.getTermCount()).isEqualTo(0);
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
		verify(template).backup(true);
	}

	@Test
	public void test_iterate_end()
	{
		OperandWalker operandWalker = mock(OperandWalker.class);
		when(operandWalker.visitAllNodes(isA(OperandVisitor.class))).thenReturn(true);
		AxiomSource axiomSource = mock(AxiomSource.class);
		final Axiom axiom = mock(Axiom.class);
		Axiom solutionAxiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(NAME);
		Template template = mock(Template.class);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
        when(template.getTermCount()).thenReturn(1);
		when(template.getKey()).thenReturn(NAME);
		when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
		when(template.isFact()).thenReturn(true);
		when(template.toAxiom()).thenReturn(solutionAxiom);
		when(template.getOperandWalker()).thenReturn(operandWalker);
		Solution solution = new Solution(Locale.getDefault());
		when(template.unify(axiom, solution)).thenReturn(true);
		final Iterator<Axiom> iterator = 	new Iterator<Axiom>(){
            int count = 0;
			@Override
			public boolean hasNext() {
				return count < 1;
			}

			@Override
			public Axiom next() {
				++count;
				return axiom;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		LogicQuery logicQuery = new LogicQuery(axiomSource);
		assertThat(logicQuery.iterate(solution, template, context)).isTrue();
		assertThat(logicQuery.iterate(solution, template, context)).isFalse();
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
	}

	@Test
	public void test_iterate_short_circuit()
	{
		AxiomSource axiomSource = mock(AxiomSource.class);
		final Axiom axiom = mock(Axiom.class);
		Axiom solutionAxiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(NAME);
		Template template = mock(Template.class);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
		when(template.getTermCount()).thenReturn(1);
		when(template.getKey()).thenReturn(NAME);
		when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.SHORT_CIRCUIT);
		when(template.isFact()).thenReturn(true);
		when(template.toAxiom()).thenReturn(solutionAxiom);
		Solution solution = new Solution(Locale.getDefault());
		when(template.unify(axiom, solution)).thenReturn(true);
		final Iterator<Axiom> iterator = 	new Iterator<Axiom>(){
            int count = 0;
			@Override
			public boolean hasNext() {
				return count < 1;
			}

			@Override
			public Axiom next() {
				++count;
				return axiom;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		LogicQuery logicQuery = new LogicQuery(axiomSource);
		assertThat(logicQuery.iterate(solution, template, context)).isFalse();
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
		verify(template).backup(true);
	}

	@Test
	public void test_iterate_unify_false()
	{
		AxiomSource axiomSource = mock(AxiomSource.class);
		final Axiom axiom = mock(Axiom.class);
		Axiom solutionAxiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(NAME);
		Template template = mock(Template.class);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
        when(template.getTermCount()).thenReturn(1);
		when(template.getKey()).thenReturn(NAME);
		when(template.isFact()).thenReturn(true);
		when(template.toAxiom()).thenReturn(solutionAxiom);
		Solution solution = new Solution(Locale.getDefault());
		when(template.unify(axiom, solution)).thenReturn(false);
		final Iterator<Axiom> iterator = 	new Iterator<Axiom>(){
            int count = 0;
			@Override
			public boolean hasNext() {
				return count < 1;
			}

			@Override
			public Axiom next() {
				++count;
				return axiom;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		LogicQuery logicQuery = new LogicQuery(axiomSource);
		assertThat(logicQuery.iterate(solution, template, context)).isFalse();
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
		verify(template).backup(true);
	}


	@Test
	public void test_iterate_evaluation_exception()
	{
		AxiomSource axiomSource = mock(AxiomSource.class);
		LogicQuery logicQuery = new LogicQuery(axiomSource);
		Solution solution = new Solution(Locale.getDefault());
		Template template = mock(Template.class);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
        when(template.getTermCount()).thenReturn(1);
		when(template.getKey()).thenReturn(NAME);
		when(template.evaluate(isA(ExecutionContext.class))).thenThrow(new ExpressionException("Syntax error"));
		when(template.toString()).thenReturn("surface_area(km2)");
		final Axiom axiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(NAME);
		when(template.unify(axiom, solution)).thenReturn(true);
		final Iterator<Axiom> iterator = 	new Iterator<Axiom>(){

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Axiom next() {
				return axiom;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		ExecutionContext context = mock(ExecutionContext.class);
		when(axiomSource.iterator(context)).thenReturn(iterator);
		try
		{
		    logicQuery.iterate(solution, template, context);
		    failBecauseExceptionWasNotThrown(QueryExecutionException.class);
		}
		catch(QueryExecutionException e)
		{
			assertThat(e.getMessage()).isEqualTo("Error evaluating: surface_area(km2)");
			assertThat(e.getCause().getMessage()).isEqualTo("Syntax error");
		}
	}

	@Test
	public void test_iterate_sole_soution_empty()
	{
		final Axiom axiom = mock(Axiom.class);
		Axiom solutionAxiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(NAME);
		Template template = mock(Template.class);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
		when(template.getKey()).thenReturn(NAME);
		when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
		when(template.isFact()).thenReturn(true);
		when(template.toAxiom()).thenReturn(solutionAxiom);
		Solution solution = new Solution(Locale.getDefault());
		when(template.unify(axiom, solution)).thenReturn(true);
		LogicQuery logicQuery = new LogicQuery();
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
		assertThat(logicQuery.iterate(solution, template, mock(ExecutionContext.class))).isTrue();
        Axiom blankAxiom = solution.getAxiom(NAME);
		assertThat(blankAxiom.getTermCount()).isEqualTo(0);
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.complete);
	}

	@Test
	public void test_iterate_sole_soution_found()
	{
        Solution solution = new Solution(Locale.getDefault());
		final Axiom axiom = mock(Axiom.class);
		Axiom solutionAxiom = mock(Axiom.class);
		when(axiom.getName()).thenReturn(NAME);
		Template template = mock(Template.class);
		OperandWalker operandWalker = mock(OperandWalker.class);
		SolutionPairer pairer = mock(SolutionPairer.class);
        when(template.getSolutionPairer(solution)).thenReturn(pairer);
		when(operandWalker.visitAllNodes(isA(OperandVisitor.class))).thenReturn(true);
        when(template.getQualifiedName()).thenReturn(QualifiedName.parseTemplateName(NAME));
		when(template.getOperandWalker()).thenReturn(operandWalker);
		when(template.getKey()).thenReturn(NAME);
		when(template.evaluate(isA(ExecutionContext.class))).thenReturn(EvaluationStatus.COMPLETE);
		when(template.isFact()).thenReturn(true);
		when(template.toAxiom()).thenReturn(solutionAxiom);
		solution.put(NAME, solutionAxiom);
		when(template.unify(axiom, solution)).thenReturn(true);
		LogicQuery logicQuery = new LogicQuery();
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.start);
		assertThat(logicQuery.iterate(solution, template, mock(ExecutionContext.class))).isTrue();
		assertThat(solution.getAxiom(NAME)).isEqualTo(solutionAxiom);
		assertThat(logicQuery.queryStatus).isEqualTo(QueryStatus.complete);
	}

	@Ignore
	@Test 
	public void test_axiom_listener()
	{
        TemplateArchetype testArchetype = new TemplateArchetype(new QualifiedTemplateName(QualifiedName.EMPTY, "template_name"));
		Template template = new Template(testArchetype);
		template.setKey(KEY);
		template.addTerm(new TestStringOperand("string"));
		template.addTerm(new TestIntegerOperand("integer"));
		template.addTerm(new TestDoubleOperand("string"));
		template.addTerm(new TestBooleanOperand("boolean"));
		template.addTerm(new TestBigDecimalOperand("decimal"));
		Solution solution = new Solution(Locale.getDefault());
		final Axiom[] axioms = getTestAxioms();
		List<Axiom> axiomList = new ArrayList<Axiom>();
		axiomList.add(axioms[0]);
		axiomList.add(axioms[1]);
		final int[] count = new int[1];
        LocaleAxiomListener axiomListener = new LocaleAxiomListener(){
			@Override
			public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) 
			{
				assertThat(axiom).isEqualTo(axioms[count[0]++]);
				return true;
			}};
		LogicQuery query = new LogicQuery(new AxiomListSource(axiomList));
		query.setAxiomListener(axiomListener);
		query.iterate(solution, template, mock(ExecutionContext.class));
		template.backup(true);
		assertThat(count[0]).isEqualTo(2);
	}
	
	protected Axiom[] getTestAxioms()
	{
		Axiom[] axiomArray = new Axiom[2];
		Axiom axiom = new Axiom(KEY);
		axiom.addTerm(new StringTerm("String term 1"));
		axiom.addTerm(new IntegerTerm(1));
		axiom.addTerm(new DoubleTerm(1.0d));
		axiom.addTerm(new BooleanTerm(Boolean.TRUE));
		axiom.addTerm(new BigDecimalTerm("11111"));
		axiomArray[0] = axiom;
		Axiom axiom2 = new Axiom(KEY);
		axiom2.addTerm(new StringTerm("String term 2"));
		axiom2.addTerm(new IntegerTerm(2));
		axiom2.addTerm(new DoubleTerm(2.0d));
		axiom2.addTerm(new BooleanTerm(Boolean.FALSE));
		axiom2.addTerm(new BigDecimalTerm("22222"));
		axiomArray[1] = axiom2;
		return axiomArray;
	}

}
