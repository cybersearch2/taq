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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.IntegerOperator;
import au.com.cybersearch2.taq.operator.StringOperator;
import au.com.cybersearch2.taq.query.Solution;
import au.com.cybersearch2.taq.terms.TermMetaData;

/**
 * TemplateTest
 * @author Andrew Bowley
 * 11 Dec 2014
 */
public class TemplateTest 
{
	final static String NAME = "myStruct";
	final static String KEY = "myKey";

	TemplateArchetype templateArchetype;
	Operand parameter1;
	Operand parameter2;
	Operator operator1;
	Operator operator2;

	@Before
	public void setUp()
	{
	    operator1 = new StringOperator();
	    operator2 = new IntegerOperator();
	    templateArchetype = mock(TemplateArchetype.class);
	    when(templateArchetype.getQualifiedName()).thenReturn(parseTemplateName(NAME));
	    when(templateArchetype.getName()).thenReturn(NAME);
	    when(templateArchetype.isMutable()).thenReturn(true);
	}
	
	@Test
	public void test_Constructor_terms_list()
	{
	    TermMetaData termMetaData1 = mock(TermMetaData.class);
        TermMetaData termMetaData2 = mock(TermMetaData.class);
	    when(templateArchetype.analyseTerm(isA(Operand.class), eq(0))).thenReturn(termMetaData1);
        when(templateArchetype.analyseTerm(isA(Operand.class), eq(1))).thenReturn(termMetaData2);
		Template underTest = new Template(templateArchetype, getTermList());
		int id = underTest.getId();
		assertThat(underTest.getName()).isEqualTo(NAME);
		assertThat(underTest.getKey()).isEqualTo(NAME);
		assertThat(id).isEqualTo(Template.referenceCount.get());
		verify(templateArchetype, times(2)).addTerm(isA(TermMetaData.class));
		verify(templateArchetype, times(0)).clearMutable();
        when(parameter1.isEmpty()).thenReturn(true);
        when(parameter2.isEmpty()).thenReturn(true);
		underTest.evaluate(mock(ExecutionContext.class));
		verify(parameter1).evaluate(id);
		verify(parameter2).evaluate(id);
	}
	
	@Test
	public void test_Constructor_terms_list_key()
	{
	    List<Operand> terms = getTermList();
        TermMetaData termMetaData1 = mock(TermMetaData.class);
        TermMetaData termMetaData2 = mock(TermMetaData.class);
        when(templateArchetype.analyseTerm(parameter1, 0)).thenReturn(termMetaData1);
        when(templateArchetype.analyseTerm(parameter2, 1)).thenReturn(termMetaData2);
        Template underTest = new Template(KEY, templateArchetype, terms);
        int id = underTest.getId();
        assertThat(underTest.getName()).isEqualTo(NAME);
        assertThat(underTest.getKey()).isEqualTo(KEY);
        assertThat(id).isEqualTo(Template.referenceCount.get());
        verify(templateArchetype, times(2)).addTerm(isA(TermMetaData.class));
        verify(templateArchetype, times(0)).clearMutable();
        when(parameter1.isEmpty()).thenReturn(true);
        when(parameter2.isEmpty()).thenReturn(true);
        underTest.evaluate(mock(ExecutionContext.class));
        verify(parameter1).evaluate(id);
        verify(parameter2).evaluate(id);
	}
		
	@Test
	public void test_Constructor_terms_array()
	{
        TemplateArchetype templateArchetype2 = new TemplateArchetype(parseTemplateName(NAME));
        Template underTest = new Template(templateArchetype2, getParameterArray());
        int id = underTest.getId();
        assertThat(underTest.getName()).isEqualTo(NAME);
        assertThat(underTest.getKey()).isEqualTo(NAME);
        assertThat(id).isEqualTo(Template.referenceCount.get());
        when(parameter1.isEmpty()).thenReturn(true);
        when(parameter2.isEmpty()).thenReturn(true);
        underTest.evaluate(mock(ExecutionContext.class));
        verify(parameter1).evaluate(id);
        verify(parameter2).evaluate(id);
	}
		
	@Test
	public void test_Constructor_terms_array_key()
	{
        TermMetaData termMetaData1 = mock(TermMetaData.class);
        TermMetaData termMetaData2 = mock(TermMetaData.class);
        when(templateArchetype.analyseTerm(parameter1, 0)).thenReturn(termMetaData1);
        when(templateArchetype.analyseTerm(parameter2, 1)).thenReturn(termMetaData2);
        Template underTest = new Template(KEY, templateArchetype, getParameterArray());
        int id = underTest.getId();
        assertThat(underTest.getName()).isEqualTo(NAME);
        assertThat(underTest.getKey()).isEqualTo(KEY);
        assertThat(id).isEqualTo(Template.referenceCount.get());
        when(parameter1.isEmpty()).thenReturn(true);
        when(parameter2.isEmpty()).thenReturn(true);
        underTest.evaluate(mock(ExecutionContext.class));
        verify(parameter1).evaluate(id);
        verify(parameter2).evaluate(id);
	}
	
	@Test
	public void test_Constructor_no_terms()
	{
		List<Operand> paramList = new ArrayList<Operand>();
		try
		{
			new Template(templateArchetype, paramList);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		}
		catch (IllegalArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("Parameter \"terms\" is empty");
	    }
		try
		{
			new Template(templateArchetype, new Operand[0]);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		}
		catch (IllegalArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("Parameter \"terms\" is empty");
	    }
	}
	  
	@Test
	public void test_backup()
	{
		List<Operand> paramList = new ArrayList<Operand>();
		Operand parameter = mock(Operand.class);
		when(parameter.getName()).thenReturn("test-parameter1");
        when(parameter.getOperator()).thenReturn(operator1);
		paramList.add(parameter);
        TermMetaData termMetaData1 = mock(TermMetaData.class);
        when(templateArchetype.analyseTerm(parameter, 0)).thenReturn(termMetaData1);
		Template underTest = new Template(templateArchetype, paramList);	
		when(parameter.backup(Template.referenceCount.get())).thenReturn(true);
		assertThat(underTest.backup(true)).isTrue();
		when(parameter.backup(Template.referenceCount.get())).thenReturn(false);
		assertThat(underTest.backup(true)).isFalse();
		when(parameter.backup(0)).thenReturn(true);
		assertThat(underTest.backup(false)).isTrue();
		when(parameter.backup(0)).thenReturn(false);
		assertThat(underTest.backup(false)).isFalse();
		Operand parameter2 = mock(Operand.class);
		when(parameter2.getName()).thenReturn("test-parameter2");
        when(parameter2.getOperator()).thenReturn(operator1);
		paramList.add(parameter2);
        TermMetaData termMetaData2 = mock(TermMetaData.class);
        when(templateArchetype.analyseTerm(parameter2, 0)).thenReturn(termMetaData2);
		underTest = new Template(templateArchetype ,paramList);	
		when(parameter2.backup(Template.referenceCount.get())).thenReturn(false);
		when(parameter.backup(Template.referenceCount.get())).thenReturn(true);
		assertThat(underTest.backup(true)).isTrue();
		when(parameter.backup(Template.referenceCount.get())).thenReturn(false);
		assertThat(underTest.backup(true)).isFalse();
		when(parameter2.backup(0)).thenReturn(false);
		when(parameter.backup(0)).thenReturn(true);
		assertThat(underTest.backup(false)).isTrue();
		when(parameter.backup(0)).thenReturn(false);
		assertThat(underTest.backup(false)).isFalse();
	}
	
	@Test
	public void test_setKey()
	{
        TermMetaData termMetaData1 = mock(TermMetaData.class);
        TermMetaData termMetaData2 = mock(TermMetaData.class);
        when(templateArchetype.analyseTerm(parameter1, 0)).thenReturn(termMetaData1);
        when(templateArchetype.analyseTerm(parameter2, 1)).thenReturn(termMetaData2);
		Template underTest = new Template(KEY, templateArchetype, getTermList());
		underTest.setKey(KEY +"!");
		assertThat(underTest.getKey()).isEqualTo(KEY+"!");
	}

    @Test
    public void test_toAxiom()
    {
        Template underTest = new Template(KEY, templateArchetype);
        IntegerOperand term = new IntegerOperand(QualifiedName.parseGlobalName("x"), Integer.valueOf(2));
        TermMetaData termMetaData1 = mock(TermMetaData.class);
        when(templateArchetype.analyseTerm(term, 0)).thenReturn(termMetaData1);
        underTest.addTerm(term);
        Axiom axiom = underTest.toAxiom();
        assertThat(axiom.getName()).isEqualTo(NAME);
        assertThat(axiom.getTermCount()).isEqualTo(1);
        assertThat(axiom.getTermByName("x")).isEqualTo(term);
    }
    
    @Ignore
    @Test
    public void test_unify()
    {
        final Axiom testAxiom = mock(Axiom.class);
        when(testAxiom.getArchetype()).thenReturn(mock(AxiomArchetype.class));
        final int[] testTermMapping = new int[]{ 0, 1 };
        when(templateArchetype.getTermMapping(isA(AxiomArchetype.class))).thenReturn(testTermMapping);
        final Solution testSolution = mock(Solution.class);
        @SuppressWarnings("serial")
        Template underTest = new Template(KEY, templateArchetype)
        {
            /**
             * Unify template using given axiom and solution
             * @param axiom Axiom with which to unify as TermList object 
             * @param solution Contains result of previous unify-evaluation steps
             * @param termMapping Maps operands to axiom terms
             * @return Flag set true if unification completed successfully
             */
            @Override
            protected boolean unify(TermList<Term> axiom, Solution solution, int[] termMapping, boolean caseSensitive)
            {
                assertThat(axiom).isEqualTo(testAxiom);
                assertThat(solution).isEqualTo(testSolution);
                assertThat(termMapping).isEqualTo(testTermMapping);
                return true;
            }

        };
        Operand operand1 = mock(Operand.class);
        Operand operand2 = mock(Operand.class);
        getParserTask(underTest).run();
        verify(templateArchetype).clearMutable();
        assertThat(underTest.unify(testAxiom, testSolution)).isTrue();
        verify(operand1).setArchetypeIndex(1);
        verify(operand1).setArchetypeIndex(-1);
        verify(operand2).setArchetypeIndex(0);
        verify(operand2).setArchetypeIndex(4);
    }
    
	protected List<Operand> getTermList()
	{
		List<Operand> paramList = new ArrayList<Operand>();
		parameter1 = mock(Operand.class);
		when(parameter1.getName()).thenReturn("parameter1");
		when(parameter1.evaluate(anyInt())).thenReturn(EvaluationStatus.COMPLETE);
		when(parameter1.getOperator()).thenReturn(operator1);
		parameter2 = mock(Operand.class);
		when(parameter2.getName()).thenReturn("parameter2");
		when(parameter2.evaluate(anyInt())).thenReturn(EvaluationStatus.COMPLETE);
        when(parameter2.getOperator()).thenReturn(operator2);
		paramList.add(parameter1);
		paramList.add(parameter2);
		return paramList;
	}
	
	protected Operand[] getParameterArray()
	{
	    Operand[] paramArray = new Operand[2];
		parameter1 = mock(Operand.class);
		when(parameter1.getName()).thenReturn("parameter1");
		when(parameter1.evaluate(anyInt())).thenReturn(EvaluationStatus.COMPLETE);
        when(parameter1.getOperator()).thenReturn(operator1);
		parameter2 = mock(Operand.class);
		when(parameter2.getName()).thenReturn("parameter2");
		when(parameter2.evaluate(anyInt())).thenReturn(EvaluationStatus.COMPLETE);
        when(parameter2.getOperator()).thenReturn(operator2);
		paramArray[0] = parameter1;
		paramArray[1] = parameter2;
		return paramArray;
	}

    protected static QualifiedName parseTemplateName(String name)
    {
        return new QualifiedTemplateName(QualifiedName.EMPTY, name);
    }

	private Runnable getParserTask(Template template)
    {
        return new Runnable(){

            @Override
            public void run()
            {   // Use helper to set archive index and id in all operands
                ArchiveIndexHelper archiveIndexHelper = new ArchiveIndexHelper(template);
                archiveIndexHelper.setOperandTree(1);
                archiveIndexHelper.setOperandTree(2);
                // Lock archetype
                template.getTemplateArchetype().clearMutable();
            }};
    }
}
