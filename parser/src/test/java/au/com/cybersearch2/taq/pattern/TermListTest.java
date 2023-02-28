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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.terms.TermMetaData;

/**
 * TermListTest
 * @author Andrew Bowley
 * 17 Nov 2014
 */
public class TermListTest  
{
	@SuppressWarnings("serial")
    static class TestTermList<T extends Term> extends TermList<T>
	{
		public TestTermList(TermListManager archetype)
		{
			super(archetype);
		}

        public TestTermList(TermListManager archetype, List<Term> paramList)
        {
            super(archetype);
            TermMetaData[] termMetaData = new TermMetaData[paramList.size()];
            for (int i = 0; i < termMetaData.length; ++i)
                termMetaData[i] = mock(TermMetaData.class);
            for (int i = 0; i < paramList.size(); ++i)
            {
                @SuppressWarnings("unchecked")
                T term = (T)paramList.get(i);
                when (archetype.analyseTerm(eq(term), eq(i))).thenReturn(termMetaData[i]);
                addTerm(term);
            }
        }

	}
	
	final static String NAME = "myStruct";

	private List<Term> getTermsList()
	{
		List<Term> paramList = new ArrayList<Term>();
		paramList.add(new Parameter("one", 1));
		paramList.add(new Parameter("two", 2));
		paramList.add(new Parameter("three", 3));
		return paramList;
	}
	
    @Test
    public void testConstructor()
    {
        TermList<Term> termList = new TestTermList<Term>(mock(TermListManager.class));
        assertThat(termList.archetype).isNotNull();
        assertThat(termList.termList).isNotNull();
        try
        {
            new TestTermList<Term>(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"archetype\" is null");
        }
    }

   @Test
    public void test_addTerm()
    {
        TermListManager archetype = mock(TemplateArchetype.class);
        when(archetype.isMutable()).thenReturn(true);
        TestTermList<Operand> underTest = new TestTermList<Operand>(archetype);
        Operand term1 = mock(Operand.class);
        when(term1.getName()).thenReturn(Term.ANONYMOUS);
        TermMetaData termMetaData1 = mock(TermMetaData.class);
        when(archetype.analyseTerm(eq(term1), eq(0))).thenReturn(termMetaData1);
        underTest.addTerm(term1);
        verify(archetype).addTerm(termMetaData1);
        assertThat(underTest.getTermCount()).isEqualTo(1);
        when(archetype.isMutable()).thenReturn(false);
        underTest = new TestTermList<Operand>(archetype);
        Operand term2 = mock(Operand.class);
        TermMetaData termMetaData2  = mock(TermMetaData.class);
        when(archetype.analyseTerm(eq(term2), eq(0))).thenReturn(termMetaData2);
        underTest.addTerm(term2);
        verify(archetype).checkTerm(termMetaData2);
        assertThat(underTest.getTermCount()).isEqualTo(1);
    }

   @Test
   public void test_getTermByIndex()
   {
       TermListManager archetype = mock(TemplateArchetype.class);
       when(archetype.isMutable()).thenReturn(true);
       TestTermList<Term> underTest = new TestTermList<Term>(archetype);
       assertThat(underTest.getTermByIndex(-1)).isNull();
       assertThat(underTest.getTermByIndex(0)).isNull();
       List<Term> paramList = getTermsList();
       underTest.termList.addAll(paramList);
       assertThat(underTest.getTermByIndex(0)).isEqualTo(paramList.get(0));
       assertThat(underTest.getTermByIndex(1)).isEqualTo(paramList.get(1));
       assertThat(underTest.getTermByIndex(2)).isEqualTo(paramList.get(2));
       assertThat(underTest.getTermByIndex(-1)).isNull();
       assertThat(underTest.getTermByIndex(3)).isNull();
   }
   
   @Test
   public void test_getTermByName()
   {
       TermListManager archetype = mock(TemplateArchetype.class);
       when(archetype.isMutable()).thenReturn(true);
       TestTermList<Term> underTest = new TestTermList<Term>(archetype);
       when(archetype.getIndexForName("")).thenReturn(-1);
       assertThat(underTest.getTermByName("")).isNull();
       List<Term> paramList = getTermsList();
       underTest.termList.addAll(paramList);
       when(archetype.getIndexForName("one")).thenReturn(0);
       assertThat(underTest.getTermByName("one")).isEqualTo(paramList.get(0));
       when(archetype.getIndexForName("two")).thenReturn(1);
       assertThat(underTest.getTermByName("two")).isEqualTo(paramList.get(1));
       when(archetype.getIndexForName("three")).thenReturn(2);
       assertThat(underTest.getTermByName("three")).isEqualTo(paramList.get(2));
       when(archetype.getIndexForName("ONE")).thenReturn(-1);
       assertThat(underTest.getTermByName("ONE")).isNull();
       when(archetype.getIndexForName(null)).thenReturn(-1);
       try
       {
           underTest.getTermByName(null);
           failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
       }
       catch(IllegalArgumentException e)
       {
           assertThat(e.getMessage()).isEqualTo("Parameter \"name\" is null");
       }
   }

   @Test
   public void test_isFact()
   {
       List<Term> paramList = new ArrayList<Term>();
       Parameter parameter1 = mock(Parameter.class);
       when(parameter1.getName()).thenReturn("parameter1");
       Parameter parameter2 = mock(Parameter.class);
       when(parameter2.getName()).thenReturn("parameter2");
       paramList.add(parameter1);
       paramList.add(parameter2);
       when(parameter1.isEmpty()).thenReturn(true);
       TermListManager archetype = mock(TemplateArchetype.class);
       when(archetype.isMutable()).thenReturn(true);
       TermList<Term> testTermList = new TestTermList<Term>(archetype, paramList);
       assertThat(testTermList.isFact()).isFalse();
       when(parameter1.isEmpty()).thenReturn(false);
       when(parameter2.isEmpty()).thenReturn(true);
       assertThat(testTermList.isFact()).isFalse();
       when(parameter1.isEmpty()).thenReturn(false);
       when(parameter2.isEmpty()).thenReturn(false);
       assertThat(testTermList.isFact()).isTrue();
       TermList<Term> testTermList2 = new TestTermList<Term>(archetype);
       assertThat(testTermList2.isFact()).isFalse();
   }
   

       /* Move to axiom test
	@Test
	public void testNamedConstructorWithTermsContainingValues()
	{
		Object[] values = new Object[] { "One", Integer.valueOf(2), Boolean.TRUE };
		TermList testTermList = new TermList(NAME, values);
		assertThat(testTermList.getName()).isEqualTo(NAME);
		assertThat(testTermList.isFact()).isTrue();
		assertThat(testTermList.toString()).isEqualTo(NAME + "(One, 2, true)");
	}

	@Test
	public void testNamedConstructorWithEmptyValues()
	{
		TermList testTermList = new TermList(NAME, new Object[0]);
		assertThat(testTermList.getName()).isEqualTo(NAME);
		assertThat(testTermList.isFact()).isTrue();
		assertThat(testTermList.toString()).isEqualTo(NAME + "()");
	}

	@Test
	public void testNamedConstructorWithNullObjectsArray()
	{
		try
		{
		    new TermList(NAME, (Object[])null);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		}
		catch(IllegalArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("Parameter \"terms\" is null");
		}
	}


	@Test
	public void testNamedConstructorWithTermsArray()
	{
		Parameter[] terms = getTermsList().toArray(new Parameter[3]); 
		TermList testTermList = new TermList(NAME, terms);
		assertThat(testTermList.getName()).isEqualTo(NAME);
		assertThat(testTermList.isFact()).isTrue();
		assertThat(testTermList.toString()).isEqualTo(NAME + "(one=1, two=2, three=3)");
		assertThat(testTermList.getTermByName("one")).isEqualTo(terms[0]);
		assertThat(testTermList.getTermByName("two")).isEqualTo(terms[1]);
		assertThat(testTermList.getTermByName("three")).isEqualTo(terms[2]);
	}

	@Test
	public void testNamedConstructorWithEmptyTermsArray()
	{
		TermList testTermList = new TermList(NAME, new Parameter[0]);
		assertThat(testTermList.getName()).isEqualTo(NAME);
		assertThat(testTermList.isFact()).isTrue();
		assertThat(testTermList.toString()).isEqualTo(NAME + "()");
	}

	@Test
	public void testNamedConstructorWithObjectArray()
	{
		Object[] objectArray = new Object[]
		{
			new Integer(23), new Parameter("Test", "Value")
		};
		TermList testTermList = new TermList(NAME, objectArray);
		assertThat(testTermList.getName()).isEqualTo(NAME);
		assertThat(testTermList.isFact()).isTrue();
		assertThat(testTermList.toString()).isEqualTo(NAME + "(23, Test=Value)");
	}
*/
/*
	@Test
	public void testNamedConstructorWithNullTermsArray()
	{
		try
		{
		    new TermList(NAME, (Parameter[])null);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		}
		catch(IllegalArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("Parameter \"terms\" is null");
		}
	}

	@Test
	public void testNamedConstructorWithTermsList()
	{
		List<Term> paramList = getTermsList();
		TermList testTermList = new TermList(NAME, paramList);
		assertThat(testTermList.getName()).isEqualTo(NAME);
		assertThat(testTermList.isFact()).isTrue();
		assertThat(testTermList.toString()).isEqualTo(NAME + "(one=1, two=2, three=3)");
		assertThat(testTermList.getTermByName("one")).isEqualTo(paramList.get(0));
		assertThat(testTermList.getTermByName("two")).isEqualTo(paramList.get(1));
		assertThat(testTermList.getTermByName("three")).isEqualTo(paramList.get(2));
	}

	@Test
	public void testNamedConstructorWithEmptyTermsList()
	{
		TermList testTermList = new TermList(NAME, new ArrayList<Term>());
		assertThat(testTermList.getName()).isEqualTo(NAME);
		assertThat(testTermList.isFact()).isTrue();
		assertThat(testTermList.toString()).isEqualTo(NAME + "()");
	}

	@Test
	public void testNamedConstructorWithNullTermsList()
	{
		try
		{
		    new TermList(NAME, (List<Term>)null);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		}
		catch(IllegalArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("Parameter \"terms\" is null");
		}
	}

*/	
}

