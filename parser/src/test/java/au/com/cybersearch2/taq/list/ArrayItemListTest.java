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

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * ListOperandTest
 * @author Andrew Bowley
 * 15 Jan 2015
 */
public class ArrayItemListTest 
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
        
        @Override
        public void setSuffix(String suffix)
        {
            this.suffix = suffix;
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
	private static final QualifiedName QNAME = QualifiedName.parseGlobalName(NAME);
	   
	@Test
	public void test_constructor()
	{
		ArrayItemList<Integer> listOperand = new ArrayItemList<Integer>(OperandType.INTEGER, QNAME);
		assertThat(listOperand.getName()).isEqualTo(NAME);
		assertThat(listOperand.isEmpty()).isTrue();
	}
	
	@Test
	public void test_assign()
	{
		ArrayItemList<Long> listOperand = new ArrayItemList<Long>(OperandType.INTEGER, QNAME);
		listOperand.setOffset(0);
		listOperand.setSize(4);
		listOperand.assignItem(0, Long.valueOf(17));
		assertThat(listOperand.getItem(0)).isEqualTo(17);
		listOperand.assignItem(0, Long.valueOf(21));
		assertThat(listOperand.getItem(0)).isEqualTo(21);
		listOperand.assignItem(1, Long.valueOf(8));
		assertThat(listOperand.getItem(1)).isEqualTo(8);
		listOperand.assignItem(0, Long.valueOf(-1));
		assertThat(listOperand.getItem(0)).isEqualTo(-1);
		assertThat(listOperand.getItem(1)).isEqualTo(8);
		listOperand.assignItem(3, Long.valueOf(89));
		assertThat(listOperand.getItem(0)).isEqualTo(-1);
		assertThat(listOperand.getItem(1)).isEqualTo(8);
		assertThat(listOperand.getItem(3)).isEqualTo(89);
		try
		{
			listOperand.getItem(2);
		    failBecauseExceptionWasNotThrown(ExpressionException.class);
		}
		catch (ExpressionException e)
		{
			assertThat(e.getMessage()).isEqualTo(NAME + " item 2 not found");
		}
	}
/*	
	@Test
	public void test_new_variable_instance()
	{
		ArrayItemList<Long> intOperandList = new ArrayItemList<Long>(Long.class, QNAME);
		intOperandList.assignItem(0, Long.valueOf(21));
		ItemListVariable<Long> intListVariable = (ItemListVariable<Long>) intOperandList.newVariableInstance(new TestListItemSpec(0, "0"));
		intListVariable.evaluate(1);
		assertThat(intListVariable.getValue()).isEqualTo(21);
		intOperandList.assignItem(0, Long.valueOf(72));
		Operand expression = new TestIntegerOperand("test", Long.valueOf(0));
		intListVariable = (ItemListVariable<Long>) intOperandList.newVariableInstance(new TestListItemSpec(expression, "test"));
		intListVariable.evaluate(1);
		assertThat(intListVariable.getValue()).isEqualTo(72);
		ArrayItemList<Double> doubOperandList = new ArrayItemList<Double>(Double.class, QNAME);
		doubOperandList.assignItem(0, Double.valueOf(5.23));
		ItemListVariable<Double> doubListVariable = (ItemListVariable<Double>) doubOperandList.newVariableInstance(new TestListItemSpec(0, "0"));
		doubListVariable.evaluate(1);
		assertThat(doubListVariable.getValue()).isEqualTo(5.23);
		doubOperandList.assignItem(0, Double.valueOf(97.34));
		doubListVariable = (ItemListVariable<Double>) doubOperandList.newVariableInstance(new TestListItemSpec(expression, "test"));
		doubListVariable.evaluate(1);
		assertThat(doubListVariable.getValue()).isEqualTo(97.34);
		ArrayItemList<String> sOperandList = new ArrayItemList<String>(String.class, QNAME);
		sOperandList.assignItem(0, "testing123");
		ItemListVariable<String> sListVariable = (ItemListVariable<String>) sOperandList.newVariableInstance(new TestListItemSpec(0, "0"));
		sListVariable.evaluate(1);
		assertThat(sListVariable.getValue()).isEqualTo("testing123");
		sOperandList.assignItem(0, "xmas2014");
		sListVariable = (ItemListVariable<String>) sOperandList.newVariableInstance(new TestListItemSpec(expression, "test"));
		sListVariable.evaluate(1);
		assertThat(sListVariable.getValue()).isEqualTo("xmas2014");
		ArrayItemList<Boolean> boolOperandList = new ArrayItemList<Boolean>(Boolean.class, QNAME);
		boolOperandList.assignItem(0, Boolean.TRUE);
		ItemListVariable<Boolean> boolListVariable = (ItemListVariable<Boolean>) boolOperandList.newVariableInstance(new TestListItemSpec(0, "0"));
		boolListVariable.evaluate(1);
		assertThat(boolListVariable.getValue()).isTrue();
		boolOperandList.assignItem(0, Boolean.FALSE);
		boolListVariable = (ItemListVariable<Boolean>) boolOperandList.newVariableInstance(new TestListItemSpec(expression, "test"));
		boolListVariable.evaluate(1);
		assertThat(boolListVariable.getValue()).isFalse();
		ArrayItemList<BigDecimal> decOperandList = new ArrayItemList<BigDecimal>(BigDecimal.class, QNAME);
		decOperandList.assignItem(0, BigDecimal.TEN);
		ItemListVariable<BigDecimal> decListVariable = (ItemListVariable<BigDecimal>) decOperandList.newVariableInstance(new TestListItemSpec(0, "0"));
		decListVariable.evaluate(1);
		assertThat(decListVariable.getValue()).isEqualTo(BigDecimal.TEN);
		decOperandList.assignItem(0, BigDecimal.ONE);
		decListVariable = (ItemListVariable<BigDecimal>) decOperandList.newVariableInstance(new TestListItemSpec(expression, "test"));
		decListVariable.evaluate(1);
		assertThat(decListVariable.getValue()).isEqualTo(BigDecimal.ONE);
	}
	*/
}
