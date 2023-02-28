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
package au.com.cybersearch2.taq.list;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Test;

import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * AxiomListTest
 * @author Andrew Bowley
 * 28 Jan 2015
 */
public class AxiomListTest 
{
    class TestListItemSpec implements ListItemSpec
    {
        String suffix;
        int index;
        Operand indexExpression;
 
        public TestListItemSpec(int index, String suffix)
        {
            this.index = index;
            this.suffix = suffix;
        }
        
        public TestListItemSpec(Operand indexExpression, String suffix)
        {
            this.indexExpression = indexExpression;
            index = -1;
            this.suffix = suffix;
        }
        
        @Override
        public String getListName()
        {
            return NAME;
        }

        @Override
        public QualifiedName getQualifiedListName()
        {
            return new QualifiedName(getVariableName(NAME, suffix), QNAME);
        }

        @Override
        public ListIndex getListIndex()
        {
            return new ListIndex(index);
        }

        @Override
        public Operand getItemExpression()
        {
            return indexExpression;
        }

        @Override
        public String getSuffix()
        {
            return suffix;
        }
        
        /**
         * Returns variable name given list name and suffix
         * @param listName
         * @param suffix
         * @return String
         */
        protected String getVariableName(String listName, String suffix)
        {
            return NAME + "_" + suffix;
        }

        @Override
        public QualifiedName getVariableName()
        {
            return null;
        }

        @Override
        public void assemble(ItemList<?> itemList)
        {
        }

        @Override
        public boolean evaluate(ItemList<?> itemList, int id)
        {
            return false;
        }

        @Override
        public void setSuffix(String suffix)
        {
            this.suffix = suffix;
        }

        @Override
        public void setListIndex(ListIndex appendIndex)
        {
        }

        @Override
        public void setQualifiedListName(QualifiedName qualifiedListName)
        {
        }

		@Override
		public void setOffset(int offset) {
		}

		@Override
		public int getOffset() {
			return 0;
		}

     }
    
	private static final String NAME = "ListOperandName";
	private static QualifiedName QNAME = QualifiedName.parseName(NAME);
	private static final String KEY = "AxiomKey";
	private static QualifiedName Q_KEY = QualifiedName.parseName(KEY);
    private static QualifiedName Q_KEY1 = QualifiedName.parseName(KEY + 1);

	@Test
	public void test_constructor()
	{
		AxiomList axiomList = new AxiomList(QNAME, Q_KEY);
		assertThat(axiomList.getName()).isEqualTo(NAME);
		assertThat(axiomList.getLength()).isEqualTo(0);
		assertThat(axiomList.hasItem(new ListIndex(0))).isFalse();
		assertThat(axiomList.isEmpty()).isTrue();
	}
	
	@Test
	public void test_assign()
	{
		AxiomList axiomList = new AxiomList(QNAME, Q_KEY);
		AxiomTermList axiomTermList = new AxiomTermList(QNAME, Q_KEY);
		axiomList.assignItem(0, axiomTermList.getAxiom());
		assertThat(axiomList.getItem(0).toString()).isEqualTo("ListOperandName()");
		AxiomTermList axiomOperandList2 = new AxiomTermList(QualifiedName.parseName(KEY + 1), Q_KEY1);
		axiomList.assignItem(0, axiomOperandList2.getAxiom());
		assertThat(axiomList.getItem(0).toString()).isEqualTo("AxiomKey1()");
		assertThat(axiomList.getLength()).isEqualTo(1);
		assertThat(axiomList.hasItem(new ListIndex(0))).isTrue();
		assertThat(axiomList.isEmpty()).isFalse();
	}

	@Test
	public void test_axiom_listener()
	{
		Locale locale = Locale.getDefault();
		AxiomList axiomList = new AxiomList(QNAME, Q_KEY);
		LocaleAxiomListener axiomListener = axiomList.getAxiomListener();
		Axiom axiom1 = new Axiom("one");
		axiomListener.onNextAxiom(Q_KEY, axiom1, locale);
		Axiom listAxiom1 = axiomList.getItem(0);
		assertThat(listAxiom1.toString()).isEqualTo("one()");
		Axiom axiom2 = new Axiom("two");
		axiomListener.onNextAxiom(Q_KEY, axiom2, locale);
		Axiom listAxiom2 = axiomList.getItem(1);
		assertThat(listAxiom2.toString()).isEqualTo("two()");
	}
}
