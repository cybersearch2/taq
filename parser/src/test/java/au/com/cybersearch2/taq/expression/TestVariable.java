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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.math.BigDecimal;

import org.junit.Test;

import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DelegateType;

/**
 * VariableTest
 * @author Andrew Bowley
 * 17 Dec 2014
 */
public class TestVariable 
{
	static public String NAME = "Variable";

    @Test
	public void test_setDelegate()
	{
		Variable variable = new ParseNameVariable(NAME);
		Parameter otherTerm = new Parameter("x", Float.valueOf("1.0f"));
		assertThat(variable.unifyTerm(otherTerm, 1)).isEqualTo(1);
		assertThat(variable.getValue()).isEqualTo(otherTerm.getValue());
		assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.ASSIGN_ONLY);
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", BigDecimal.ONE);
		variable.unifyTerm(otherTerm, 1);
        assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.DECIMAL);
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", Boolean.TRUE);
		variable.unifyTerm(otherTerm, 1);
	    assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.BOOLEAN);
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", Double.valueOf("1.0"));
		variable.unifyTerm(otherTerm, 1);
	    assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.DOUBLE);
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", Integer.valueOf("1"));
		variable.unifyTerm(otherTerm, 1);
	    assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.INTEGER);
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", "1.0f");
		variable.unifyTerm(otherTerm, 1);
	    assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.STRING);
	}
	@Test
	public void test_assign()
	{
		Variable variable = new ParseNameVariable(NAME);
	    variable = new ParseNameVariable(NAME);
	    variable.assign(new Parameter(Term.ANONYMOUS, BigDecimal.ONE));
	    assertThat(variable.isEmpty()).isFalse();
	    assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.DECIMAL);
		assertThat(variable.getValue()).isEqualTo(BigDecimal.ONE);
	    variable = new ParseNameVariable(NAME);
	    variable.assign(new Parameter(Term.ANONYMOUS, Boolean.TRUE));
	    assertThat(variable.isEmpty()).isFalse();
        assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.BOOLEAN);
		assertThat(variable.getValue()).isEqualTo(Boolean.TRUE);
	    variable = new ParseNameVariable(NAME);
	    variable.assign(new Parameter(Term.ANONYMOUS, Double.valueOf("1.0")));
	    assertThat(variable.isEmpty()).isFalse();
        assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.DOUBLE);
		assertThat(variable.getValue()).isEqualTo(Double.valueOf("1.0"));
	    variable = new ParseNameVariable(NAME);
	    variable.assign(new Parameter(Term.ANONYMOUS, Integer.valueOf("1")));
	    assertThat(variable.isEmpty()).isFalse();
        assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.INTEGER);
		assertThat(variable.getValue()).isEqualTo(Integer.valueOf("1"));
	    variable = new ParseNameVariable(NAME);
	    variable.assign(new Parameter(Term.ANONYMOUS, "1.0f"));
	    assertThat(variable.isEmpty()).isFalse();
        assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.STRING);
		assertThat(variable.getValue()).isEqualTo("1.0f");
	}

	@Test
	public void test_delegate()
	{
		Variable variable = new ParseNameVariable(NAME);
		OperatorEnum[] assignOp = { OperatorEnum.ASSIGN , OperatorEnum.EQ, OperatorEnum.NE}; 
		assertThat(variable.getDelegateOperator().getLeftBinaryOps()).isEqualTo(assignOp);
		assertThat(variable.getDelegateOperator().getRightBinaryOps()).isEqualTo(assignOp);
		assertThat(variable.getDelegateOperator().booleanEvaluation(new NullOperand(), OperatorEnum.EQ, new NullOperand())).isTrue();
		Parameter otherTerm = new Parameter("x", (BigDecimal)null);
		assertThat(variable.unifyTerm(otherTerm, 1)).isEqualTo(1);
		variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", "1.0f");
		assertThat(variable.unifyTerm(otherTerm, 2)).isEqualTo(2);
		assertThat((variable.getDelegateOperator()).getDelegateType()).isEqualTo(DelegateType.STRING);
		
	}
    
	@Test
	public void test_empty_methods()
	{
		Variable variable = new ParseNameVariable(NAME);
		assertThat(variable.getLeftOperand()).isNull();
		assertThat(variable.getRightOperand()).isNull();
		try
		{   // Empty variable has a Null operator
			variable.getDelegateOperator().numberEvaluation(new TestIntegerOperand("L", Integer.valueOf(7)), OperatorEnum.XOR, new TestIntegerOperand("R", Integer.valueOf(5)));
			failBecauseExceptionWasNotThrown(ExpressionException.class);
		}
		catch(ExpressionException e)
		{
			assertThat(e.getMessage()).isEqualTo("Cannot evaluate 7^5");
		}
		variable.evaluate(0);
	    variable = new ParseNameVariable(NAME);
	    Parameter otherTerm = new Parameter("x", Boolean.TRUE);
		variable.unifyTerm(otherTerm, 1);
		// Boolean allows multiplication with number. Operator is ignored along with fact no boolean terms involved!
		assertThat(variable.getDelegateOperator().numberEvaluation(new TestIntegerOperand("L", Integer.valueOf(7)), OperatorEnum.XOR, new TestIntegerOperand("R", Integer.valueOf(5)))).isEqualTo(new BigDecimal("35"));
		assertThat(variable.getDelegateOperator().numberEvaluation(OperatorEnum.INCR, new TestIntegerOperand("R", Integer.valueOf(8)))).isEqualTo(Integer.valueOf(0));
	    variable = new ParseNameVariable(NAME);
	    otherTerm = new Parameter("x", "String");
		variable.unifyTerm(otherTerm, 1);
		assertThat(variable.getDelegateOperator().numberEvaluation(new TestIntegerOperand("L", Integer.valueOf(7)), OperatorEnum.XOR, new TestIntegerOperand("R", Integer.valueOf(5)))).isEqualTo(Integer.valueOf(0));
		assertThat(variable.getDelegateOperator().numberEvaluation(OperatorEnum.INCR, new TestIntegerOperand("R", Integer.valueOf(8)))).isEqualTo(Integer.valueOf(0));
	}
	
	@Test
	public void test_operand_ops()
	{
		Variable variable = new ParseNameVariable(NAME);
		Parameter otherTerm = new Parameter("x", BigDecimal.ONE);
		variable.unifyTerm(otherTerm, 1);
		assertThat(variable.getDelegateOperator().getLeftBinaryOps()).isEqualTo(new TestBigDecimalOperand("*").operator.getLeftBinaryOps());
		assertThat(variable.getDelegateOperator().getRightBinaryOps()).isEqualTo(new TestBigDecimalOperand("*").operator.getRightBinaryOps());
		assertThat(variable.getDelegateOperator().booleanEvaluation(new TestBigDecimalOperand("L", BigDecimal.ZERO), OperatorEnum.LT, new TestBigDecimalOperand("R", BigDecimal.TEN))).isTrue();
		assertThat(variable.getDelegateOperator().numberEvaluation(new TestBigDecimalOperand("L", BigDecimal.ONE), OperatorEnum.PLUS, new TestBigDecimalOperand("R", BigDecimal.TEN))).isEqualTo(new BigDecimal("11"));
		assertThat(variable.getDelegateOperator().numberEvaluation(OperatorEnum.MINUS, new TestBigDecimalOperand("R", BigDecimal.TEN))).isEqualTo(new BigDecimal("-10"));
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", Boolean.TRUE);
		variable.unifyTerm(otherTerm, 1);
		assertThat(variable.getDelegateOperator().getLeftBinaryOps()).isEqualTo(new TestBooleanOperand("*").operator.getLeftBinaryOps());
		assertThat(variable.getDelegateOperator().getRightBinaryOps()).isEqualTo(new TestBooleanOperand("*").operator.getRightBinaryOps());
		assertThat(variable.getDelegateOperator().booleanEvaluation(new TestBooleanOperand("L", Boolean.FALSE), OperatorEnum.NE, new TestBooleanOperand("R", Boolean.TRUE))).isTrue();
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", Double.valueOf("1.0"));
		variable.unifyTerm(otherTerm, 1);
		assertThat(variable.getDelegateOperator().getLeftBinaryOps()).isEqualTo(new TestDoubleOperand("*").operator.getLeftBinaryOps());
		assertThat(variable.getDelegateOperator().getRightBinaryOps()).isEqualTo(new TestDoubleOperand("*").operator.getRightBinaryOps());
		assertThat(variable.getDelegateOperator().booleanEvaluation(new TestDoubleOperand("L", Double.valueOf(1.0)), OperatorEnum.LT, new TestDoubleOperand("R", Double.valueOf(0.5)))).isFalse();
		assertThat(variable.getDelegateOperator().numberEvaluation(new TestDoubleOperand("L", Double.valueOf(2.0)), OperatorEnum.STAR, new TestDoubleOperand("R", Double.valueOf(3.0)))).isEqualTo(Double.valueOf("6.0"));
		assertThat(variable.getDelegateOperator().numberEvaluation(OperatorEnum.MINUS, new TestDoubleOperand("R", Double.valueOf(99.9)))).isEqualTo(Double.valueOf(-99.9));
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", Integer.valueOf("1"));
		variable.unifyTerm(otherTerm, 1);
		assertThat(variable.getDelegateOperator().getLeftBinaryOps()).isEqualTo(new TestIntegerOperand("*").operator.getLeftBinaryOps());
		assertThat(variable.getDelegateOperator().getRightBinaryOps()).isEqualTo(new TestIntegerOperand("*").operator.getRightBinaryOps());
		assertThat(variable.getDelegateOperator().booleanEvaluation(new TestIntegerOperand("L", Long.valueOf(2)), OperatorEnum.GE, new TestIntegerOperand("R", Long.valueOf(2)))).isTrue();
		assertThat(variable.getDelegateOperator().numberEvaluation(new TestIntegerOperand("L", Long.valueOf(7)), OperatorEnum.XOR, new TestIntegerOperand("R", Long.valueOf(5)))).isEqualTo(Long.valueOf(2));
		assertThat(variable.getDelegateOperator().numberEvaluation(OperatorEnum.INCR, new TestIntegerOperand("R", Long.valueOf(8)))).isEqualTo(Long.valueOf(9));
	    variable = new ParseNameVariable(NAME);
		otherTerm = new Parameter("x", "1.0f");
		variable.unifyTerm(otherTerm, 1);
		assertThat(variable.getDelegateOperator().getLeftBinaryOps()).isEqualTo(new TestStringOperand("*").operator.getLeftBinaryOps());
		assertThat(variable.getDelegateOperator().getRightBinaryOps()).isEqualTo(new TestStringOperand("*").operator.getRightBinaryOps());
		assertThat(variable.getDelegateOperator().booleanEvaluation(new TestStringOperand("L", "hello"), OperatorEnum.NE, new TestStringOperand("R", "world"))).isTrue();
	}
}
