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

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DelegateType;
import au.com.cybersearch2.taq.operator.EvaluatorOperator;

/**
 * Evaluator Performs evaluation of unary and binary expressions. The operator
 * is specified as a String eg. "+", and represented internally as an enum. As
 * an Operand, it is related to Variable, as value type may be unknown until an
 * evaluation has occurred. An Evaluator is allowed to be anonymous, in which
 * case it acts as a simple parameter of local scope.
 * 
 * @author Andrew Bowley
 *
 * @since 02/10/2010
 * @see DelegateOperand
 * @see Variable
 */
public class Evaluator extends TreeEvaluator {
	
	static OperatorMap operatorMap = QueryProgram.getSingleton(OperatorMap.class);

	/** Right hand operand. If null, then this is a unary postfix expression. */
	private Operand right;
	/** Left hand operand. If null, then this is a unary prefix expression. */
	private Operand left;
	/** Flag set true if value set by evaluation */
	private boolean isValueSet;
	/** Flag set true if evaluator is enclosed in parentheses */
	private boolean isEnclosed;

	/**
	 * Create Evaluator object for prefix/postfix unary expression
	 * 
	 * @param term             Operand left or right determined by orientation
	 * @param operatorNotation Text representation of operator
	 * @param orientation      Unary - prefix/postfix
	 */
	public Evaluator(Operand term, String operatorNotation, Orientation orientation) {
		this(QualifiedName.ANONYMOUS, operatorNotation, orientation);
		setUnaryTerm(term);
	}

	/**
	 * Create Evaluator object for binary expression
	 * 
	 * @param leftTerm         Left operand
	 * @param operatorNotation Text representation of operator
	 * @param rightTerm        Right operand
	 */
	public Evaluator(Operand leftTerm, String operatorNotation, Operand rightTerm) {
		this(QualifiedName.ANONYMOUS, operatorNotation, Orientation.binary);
		this.right = rightTerm;
		this.left = leftTerm;
		checkBinaryTerms(operatorNotation);
		postConstruct();
	}

	/**
	 * Create named Evaluator object for binary expression
	 * 
	 * @param qname            Qualified name of variable
	 * @param leftTerm         Left operand
	 * @param operatorNotation Text representation of operator
	 * @param rightTerm        Right operand
	 */
	public Evaluator(QualifiedName qname, Operand leftTerm, String operatorNotation, Operand rightTerm) {
		this(qname, operatorNotation, Orientation.binary);
		this.right = rightTerm;
		this.left = leftTerm;
		checkBinaryTerms(operatorNotation);
		postConstruct();
	}

	public void setEnclosed() {
		this.isEnclosed = true;
	}

	/**
	 * Returns operator enum corresponding to specified text
	 * 
	 * @param operator Operator
	 * @return OperatorEnum
	 */
	public static OperatorEnum convertOperator(String operator) {
		return operatorMap.get(operator);
	}

	/**
	 * Returns operator enum corresponding to specified char
	 * 
	 * @param operatorCharacter char
	 * @return OperatorEnum
	 */
	public static OperatorEnum convertOperatorChar(char operatorCharacter) {
		return operatorMap.get(operatorCharacter);
	}

	/**
	 * Construct named Evaluator object
	 * 
	 * @param qname            Qualified name of variable
	 * @param operatorNotation Text representation of operator
	 * @param orientation      Binary or unary - prefix/postfix
	 */
	protected Evaluator(QualifiedName qname, String operatorNotation, Orientation orientation) {
		super(qname, convertOperator(operatorNotation), orientation);
		setDelegateOperator(new EvaluatorOperator());
	}

	/**
	 * Complete construction after right and left terms set according to orientation
	 */
	protected void postConstruct() {
		// Delegate can be set in advance if result is boolean
		// Otherwise, delegate will be set on value assigment
		presetDelegate();
	}

	/**
	 * Execute expression and set Evaluator value with result
	 * There is a precedence for errors, in highest to lowest:
	 * Term is empty
	 * Number is NaN
	 * TreeEvaluator not permitted for type of Term value
	 * Term value is null
	 * 
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Flag set true if evaluation is to continue
	 */
	@Override
	public EvaluationStatus evaluate(int id) {
		EvaluationStatus evaluationStatus = super.evaluate(id, context);
		switch (evaluationStatus) {
		case SKIP: // Operator && or ||
			// if (right != null)
			if (orientation == Orientation.binary) {
				if ((operatorEnum == OperatorEnum.HOOK) || (operatorEnum == OperatorEnum.COLON)) {
					if (left.getValue() == Boolean.TRUE)
						setResult(right.getValue(), id);
					else
						return evaluationStatus;
				} else
					// Binary && and || assigns a value
					setResult(shortCircuitOnTrue, id);
			} else {
				if (empty)
					setValue(left.getValue());
				isValueSet = true;
				this.id = id;
			}
			return EvaluationStatus.COMPLETE;
		case SHORT_CIRCUIT: // Left term evaluates to trigger short circuit - false for && and true for ||
			this.id = id;
			return isShortCircuit() ? EvaluationStatus.SHORT_CIRCUIT : EvaluationStatus.SKIP;
		case FAIL:
			if ((left != null) && left.isEmpty() && (operatorEnum != OperatorEnum.ASSIGN))
				throw new ExpressionException(String.format("Left term '%s' is empty", left.toString()));
			throw new ExpressionException("Cannot evaluate " + toString());
		default:
		}
		// Now perform evaluation, depending on status of left and right terms
		Object result = null;
		switch (orientation) {
		case binary:
			result = evaluateBinary(id);
			break;
		case unary_prefix:
			result = evaluatePreFix(id);
			break;
		case unary_postfix:
			result = evaluatePostFix(id);
			break;
		}
		return setResult(result, id);
	}

	/**
	 * Backup to initial state if given id matches id assigned on unification or
	 * given id = 0.
	 * 
	 * @param modifierId Identity of caller.
	 * @return boolean true if backup occurred
	 * @see au.com.cybersearch2.taq.language.Parameter#unify(Term otherParam, int id)
	 */
	@Override
	public boolean backup(int modifierId) {
		isValueSet = false;
		boolean backupOccurred = super.backup(modifierId);
		if ((right != null) && right.backup(modifierId))
			backupOccurred = true;
		if ((left != null) && left.backup(modifierId))
			backupOccurred = true;
		return backupOccurred;
	}

	/**
	 * Returns left child of Operand
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.Operand#getLeftOperand()
	 */
	@Override
	public Operand getLeftOperand() {
		return left;
	}

	/**
	 * Returns right child of Operand
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.Operand#getRightOperand()
	 */
	@Override
	public Operand getRightOperand() {
		return right;
	}

	@Override
	public void setLeftOperand(Operand leftOperand) {
		left = leftOperand;
	}

	/**
	 * Returns what is to be evaluated, if empty, otherwise the value
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isEnclosed)
			return "(" + internalToString() + ")";
		else
			return internalToString();
	}

	/**
	 * setRightOperand
	 * 
	 * @see au.com.cybersearch2.taq.expression.TreeEvaluator#setRightOperand(au.com.cybersearch2.taq.interfaces.Operand)
	 */
	@Override
	protected void setRightOperand(Operand right) {
		this.right = right;
	}

	private String internalToString() {
		if (!isValueSet && !(!empty && isShortCircuit())) { // Return evaluation to perform
			if (orientation == Orientation.unary_prefix)
				return unaryRightToString();
			else if (orientation == Orientation.unary_postfix)
				return unaryLeftToString();
			return binaryToString();
		}
		// Return value
		return super.toString();
	}

	/**
	 * Evaluate binary operation
	 * 
	 * @param id Modification id
	 * @return result object
	 */
	private Object evaluateBinary(int id) {
		Object result = null;
		if (!leftIsNaN && !rightIsNaN)
			// Delegate calculation to sub class
			result = calculate(left, right, id);
		else if (leftIsNaN)
			result = left.getValue();
		else
			result = right.getValue();
		return result;
	}

	/**
	 * Evaluate prefix unary operation
	 * 
	 * @param modificationId Notification id
	 * @return result object
	 */
	private Object evaluatePreFix(int modificationId) {
		Object result = null;
		// Prefix unary operation.
		if (rightIsNaN)
			result = right.getValue();
		else
			result = doPrefixUnary(modificationId);
		return result;
	}

	/**
	 * Evaluate postfix unary operation
	 * 
	 * @param modificationId Modification id
	 * @return result object
	 */
	private Object evaluatePostFix(int modificationId) {
		// Postfix unary operation.
		// Result will automatically be NaN if left Term value is NaN
		Object result = left.getValue();
		if (!leftIsNaN) {
			Object post = left.getOperator().numberEvaluation(operatorEnum, left);
			if (!(left instanceof CursorOperand))
				left.setValue(post);
			else
				left.setId(modificationId);
		}
		return result;
	}

	/**
	 * Perform prefix unary operation with right term: ++, --, !, ~, + or -
	 * 
	 * @param modificationId Modification id
	 * @return Object Result
	 */
	private Object doPrefixUnary(int modificationId) {
		if ((operatorEnum == OperatorEnum.INCR) || (operatorEnum == OperatorEnum.DECR)) { // ++ or --
			Object pre = right.getOperator().numberEvaluation(operatorEnum, right);
			right.setValue(pre);
			return pre;
		} else if (operatorEnum == OperatorEnum.NOT) { // !
			boolean flag = true;
			if (right.getValue() instanceof Boolean)
				flag = ((Boolean) (right.getValue())).booleanValue();
			return Boolean.valueOf(!flag);
		} else if ((operatorEnum == OperatorEnum.HOOK)) {
			// Criterion ??
			return ((Boolean) (right.getValue())).booleanValue();
		} else if ((operatorEnum == OperatorEnum.TILDE) || (operatorEnum == OperatorEnum.PLUS)
				|| (operatorEnum == OperatorEnum.MINUS))
		// ~ is unary so left is ignored
		{
			Object unary = right.getOperator().numberEvaluation(operatorEnum, right);
			if (right.getOperator().getTrait().getOperandType() == OperandType.CURSOR) {
				right.setId(modificationId);
				return right.getValue();
			}
			return unary;
		}
		return null;
	}

	/**
	 * Set value and determine evaluation return value
	 * 
	 * @param result Value to set
	 * @param id     Identity of caller, which must be provided for backup()
	 * @return Flag set true to continue, false to short circuit
	 */
	private EvaluationStatus setResult(Object result, int id) {
		boolean continueFlag = true;
		boolean trueResult = false;
		boolean falseResult = false;
		if (result != null) {
			setValue(result);
			isValueSet = true;
			this.id = id;
			// Check for boolean result
			switch (operatorEnum) {
			case SC_OR: // "||"
			case SC_AND: // "&&"
				if (orientation == Orientation.binary)
					break;
			case NOT:// "!"
			case LT: // "<"
			case GT: // ">"
			case EQ: // "=="
			case LE: // "<="
			case GE: // ">="
			case NE: // "!="
				boolean flag = ((Boolean) result).booleanValue();
				trueResult = flag;
				falseResult = !flag;
				break;
			default:
			}
			if (shortCircuitOnTrue && trueResult)
				continueFlag = false;
			else if (shortCircuitOnFalse && falseResult)
				continueFlag = false;
		}
		return continueFlag ? EvaluationStatus.COMPLETE : EvaluationStatus.SHORT_CIRCUIT;
	}

	/**
	 * Set delegate in advance if result is boolean
	 */
	private void presetDelegate() {
		switch (operatorEnum) {
		case NOT:// "!"
		case LT: // "<"
		case GT: // ">"
		case EQ: // "=="
		case LE: // "<="
		case GE: // ">="
		case NE: // "!="
		case SC_OR: // "||"
		case SC_AND: // "&&"
		{
			if (getDelegateOperator().getDelegateType() != DelegateType.BOOLEAN)
				getDelegateOperator().setDelegateType(DelegateType.BOOLEAN);
		}
		default:
		}
	}

	/**
	 * Set left or right term to given operand according to orientation
	 * 
	 * @param term The operand to set
	 * @throws ExpressionException if orientation set to binary
	 */
	private void setUnaryTerm(Operand term) {
		switch (orientation) {
		case unary_prefix:
			right = term;
			break;
		case unary_postfix:
			left = term;
			break;
		case binary:
			throw new ExpressionException("Invalid Evaluator binary orientation where unary required");
		}
	}

	/**
	 * Check for null binary terms
	 * 
	 * @param operator Operator in text format
	 * @throws ExpressionException if null term encountered
	 */
	private void checkBinaryTerms(String operator) {
		if (orientation == Orientation.binary) {
			String invalidTerm = null;
			if (left == null)
				invalidTerm = "left";
			if (right == null)
				invalidTerm = "right";
			if (invalidTerm != null)
				throw new ExpressionException(
						"Binary operator \"" + operator + "\" cannot have null " + invalidTerm + " term");
		}
	}

	/**
	 * Represent a binary evaluator as a String
	 * 
	 * @return String
	 */
	private String binaryToString() {
		// By default, show left by name, if empty, otherwise by value
		String leftTerm = (left.isEmpty() ? formatLeft() : left.getValue().toString());
		if (left.getName().isEmpty() && left.isEmpty())
			// Possibly recurse if left is an Evaluator with no name and empty
			leftTerm = left.toString();
		// By default, show right by name, if empty, otherwise by value
		String rightTerm = (right.isEmpty() ? formatRight() : right.getValue().toString());
		if (right.getName().isEmpty() && right.isEmpty())
			// Possibly recurse if right is an Evaluator with no name and empty
			rightTerm = right.toString();
		return leftTerm + operatorEnum.toString() + rightTerm;
	}

	private String formatLeft() {
		if ((operatorEnum == OperatorEnum.HOOK) || (operatorEnum == OperatorEnum.COLON))
			return left.toString();
		return left.getName();
	}

	private String formatRight() {
		if ((operatorEnum == OperatorEnum.HOOK) || (operatorEnum == OperatorEnum.COLON))
			return right.toString();
		return right.getName();
	}

	/**
	 * Represent a postfix unary evaluator as a String
	 * 
	 * @return String
	 */
	private String unaryLeftToString() {
		if (shortCircuitOnFalse || shortCircuitOnTrue)
			return getName() + (shortCircuitOnFalse ? "?" : ":") + left.toString();
		if (left.isEmpty()) {
			if (left.getName().isEmpty())
				return super.toString();
			return left.getName() + operatorEnum.toString();
		}
		return left.getValue() + operatorEnum.toString();
	}

	/**
	 * Represent a prefix unary evaluator as a String
	 * 
	 * @return String
	 */
	private String unaryRightToString() {
		if (right.isEmpty())
			return operatorEnum.toString() + (right.getName().isEmpty() ? right.toString() : right.getName());
		return operatorEnum.toString() + right.getValue();
	}

}
