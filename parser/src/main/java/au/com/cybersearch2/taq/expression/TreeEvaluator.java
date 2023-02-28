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

import java.math.BigDecimal;
import java.util.Locale;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.helper.EvaluationUtils;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.StringCloneable;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.operator.CurrencyOperator;
import au.com.cybersearch2.taq.trait.BooleanTrait;
import au.com.cybersearch2.taq.trait.NumberTrait;

/**
 * TreeEvaluator
 * Evaluates left and right operand trees, performing on-the-fly type conversion, if required.
 * The right and left operands are set by the super class and are obtained using Operand interface.
 * @author Andrew Bowley
 * 18May,2017
 */
abstract class TreeEvaluator extends DelegateOperand
{
    /** Operator as an enumerated value */
    protected OperatorEnum operatorEnum;
    /** Orientation - binary or unary (prefix or postfix) */
    protected Orientation orientation;
    /** Short circuit on boolean false result */
    protected boolean shortCircuitOnFalse;
    /** Short circuit on boolean true result */
    protected boolean shortCircuitOnTrue;
    /** Left term is not an number */
    protected boolean leftIsNaN;
    /** Right term is not an number */
    protected boolean rightIsNaN;
    /** Evaluation helper methods */
    protected EvaluationUtils utils;
 
    abstract protected void setRightOperand(Operand right);
    
    /**
     * @param qname
     * @param operatorEnum
     */
    protected TreeEvaluator(QualifiedName qname, OperatorEnum operatorEnum, Orientation orientation)
    {
        super(qname);
        this.operatorEnum = operatorEnum;
        this.orientation = orientation;
        utils = new EvaluationUtils();
        // Short circuit adds logic to return false to evaluate().
        // It applies to operators "&&" and "||".
        // Note there is a unary form used for short circuit expressions ie. starting with '?'
        shortCircuitOnFalse = operatorEnum == OperatorEnum.SC_AND; 
        shortCircuitOnTrue = operatorEnum == OperatorEnum.SC_OR; 
    }
    
    /**
     * @return the shortCircuitOnFalse
     */
    public boolean isShortCircuitOnFalse()
    {
        return shortCircuitOnFalse;
    }

    /**
     * @return the shortCircuitOnTrue
     */
    public boolean isShortCircuitOnTrue()
    {
        return shortCircuitOnTrue;
    }

    /**
     * @return the shortCircuitOnTrue
     */
    public boolean isShortCircuit()
    {
        return shortCircuitOnTrue || shortCircuitOnFalse;
    }

    protected EvaluationStatus evaluate(int id, ExecutionContext context) {
        // Left hand operand. If null, then this is a unary prefix expression.
        Operand left = getLeftOperand();
        if ((left != null) && left.isShadow()) 
        	left = left.getHead();

         // Right hand operand. If null, then this is a unary postfix expression. */
        Operand right = getRightOperand();
        if ((right != null) && right.isShadow())
        	right = right.getHead();
        
        // Left term may trigger short circuit or skip (nothing more to do)
        EvaluationStatus evaluationStatus = 
            left == null ? EvaluationStatus.COMPLETE : evaluateLeft(left, right, id, context);
        if (evaluationStatus == EvaluationStatus.COMPLETE) 
        {
            // Remember if left is not a number
            leftIsNaN = utils.isNaN(left, operatorEnum);
            if (right != null)
            {
                evaluationStatus = evaluateRight(left, right, id, context);
                if (evaluationStatus == EvaluationStatus.COMPLETE)
                {
                	boolean isHook = operatorEnum == OperatorEnum.HOOK;
                    if ((isHook && (left != null)) || (operatorEnum == OperatorEnum.COLON))
                        return EvaluationStatus.SKIP;
                 	if (isHook && Boolean.FALSE.equals(right.getValue()))
                 		// Criterion ?? false
                		return EvaluationStatus.SHORT_CIRCUIT;
                }
            }
        }
        return evaluationStatus;
    }

    /**
     * Calculate binary expression
     * @param leftTerm Left term
     * @param rightTerm Right term
     * @param modificationId Modification id
     * @return Result as Object
     */
    protected Object calculate(Operand leftTerm, Operand rightTerm, int modificationId) 
    {
        switch (operatorEnum)
        {
        case ASSIGN: // "="
            return utils.assignRightToLeft(leftTerm, rightTerm, modificationId);
        case LT: // "<"
        case GT: // ">"
        case EQ: // "=="
        case LE: // "<="
        case GE: // ">="
        case NE: // "!="
        case SC_OR: // "||"
        case SC_AND: // "&&"
            return rightTerm.getValue() == null ? // == or != null
                    rightTerm.getOperator().booleanEvaluation(leftTerm, operatorEnum, leftTerm) :
                    leftTerm.getOperator().booleanEvaluation(leftTerm, operatorEnum, rightTerm);
        case PLUS: // "+"
        case PLUSASSIGN: // "+"
            if (utils.isConcatenateValid(leftTerm, operatorEnum))
            {
                OperandType leftOperandType = leftTerm.getOperator().getTrait().getOperandType();
                if (leftOperandType == OperandType.STRING)
                    return leftTerm.getValue().toString() + rightTerm.getValue().toString();
                OperandType rightOperandType = rightTerm.getOperator().getTrait().getOperandType();
                if (leftOperandType == OperandType.AXIOM)
                {
                    AxiomList axiomList = (AxiomList)leftTerm.getValue();
                    if (rightOperandType == OperandType.AXIOM)
                        return axiomList.concatenate((AxiomList)rightTerm.getValue());
                    if (rightOperandType == OperandType.TERM)
                        return axiomList.concatenate(((AxiomTermList)rightTerm.getValue()).getAxiom());
                }
                else if (leftOperandType == OperandType.CURSOR)
                {
                    leftTerm.getOperator().numberEvaluation(leftTerm, operatorEnum, rightTerm);
                    return leftTerm.getValue();
                } else if (leftOperandType == OperandType.TERM_LIST) {
                	ListOperand<?> listOperand = ((ListOperand<?>)leftTerm.getValue());
                	return listOperand.concatenate(rightTerm);
                }
                throw new ExpressionException("Cannot concatenate " + leftTerm.toString() + " to " + rightTerm.toString());
            }
            // Deliberately fall through if not concatination
        case STAR: // "*"
            if ((leftTerm.getValueClass() == Boolean.class) || (rightTerm.getValueClass() == Boolean.class))
                return utils.calculateBoolean(leftTerm, rightTerm);
            // Deliberately fall through if not boolean "*"
        case MINUS: // "-"
        case SLASH: // "/"
        case BIT_AND: // "&"
        case BIT_OR: // "|"
        case XOR: // "^"
        case REM: // "%"
        case MINUSASSIGN: // "-"
        case STARASSIGN: // "*"
        case SLASHASSIGN: // "/"
        case ANDASSIGN: // "&"
        case ORASSIGN: // "|"
        case XORASSIGN: // "^"
        case REMASSIGN: // "%"
            // Prevent conversion of BigDecimal to Integer or Double by 
            // always selecting the BigDecimal term to perform the operation
            boolean leftIsBigDec = leftTerm.getValueClass() == BigDecimal.class;
            boolean rightIsBigDec = rightTerm.getValueClass() == BigDecimal.class;
            if (leftIsBigDec)
                return leftTerm.getOperator().numberEvaluation(leftTerm, operatorEnum, rightTerm);
            else if (rightIsBigDec)
                return rightTerm.getOperator().numberEvaluation(leftTerm, operatorEnum, rightTerm);
            // Prevent conversion of Double to Integer by 
            // always selecting the Double term to perform the operation
            boolean leftIsDouble = leftTerm.getValueClass() == Double.class;
            boolean rightIsDouble = rightTerm.getValueClass() == Double.class;
            if (leftIsDouble || !rightIsDouble)
                return leftTerm.getOperator().numberEvaluation(leftTerm, operatorEnum, rightTerm);
            else
                return rightTerm.getOperator().numberEvaluation(leftTerm, operatorEnum, rightTerm);
        case COMMA: // Comma operator builds a tree of operands instead of performing a calculation
            return new Null(); // Set dummy value so this variable is no longer empty
        default:
        }
        return null;
    }

    /**
     * When number operation is to be performed with string on one side, 
     * convert the string to a number of same type as opposite.
     * Also convert decimal to currency for binary currency operation.
     * Note that left or right operand in super class may be replaced by conversion.
     * @param left Left term
     * @param right Right term
     * @return boolean
     */
    private boolean performOnFlyConversion(Operand left, Operand right)
    {
        if (!performStringConversion(left, right))
            return performDecimalConversion(left, right);
        return true;
    }

    /**
     * Evaluate left term.
     * @param left Left term
     * @param right Right term
     * @param id Identity of caller, which must be provided for backup()
     * @return EvaluationStatus enum: SHORT_CIRCUIT, SKIP or COMPLETE
     */
    private EvaluationStatus evaluateLeft(Operand left, Operand right, int id, ExecutionContext context) 
    {
         // Only evaluate once between backups
        if (left.isEmpty()) {
        		left.setExecutionContext(context);
            left.evaluate(id);
        }
        if (left.isEmpty() && operatorEnum != OperatorEnum.ASSIGN)
            // If left hand term is empty, then cannot proceed. Maybe unification failed for this term.
            //throw new ExpressionException("Left term is empty");
            return EvaluationStatus.FAIL;
        if (!isValidLeftOperation(left, right) || utils.isInvalidLeftUnaryOp(right, operatorEnum))
        {   // TreeEvaluator not permitted for type of Term value
            // NaN has precedence if operation has Numeric result
            if (!utils.isNaN(left, operatorEnum) && ((right == null) || !utils.isNaN(right, operatorEnum)))
                //throw new ExpressionException("Cannot evaluate " + (right == null ? unaryLeftToString() : binaryToString()));
                return EvaluationStatus.FAIL;
        }
        // Short circuit logic applies only to left term
        if (shortCircuitOnFalse) // Operator &&
        {
            if ("false".equals(left.getValue().toString()))
            {   // false && means short circuit
                if (right == null) // Unary && does not assign a value
                    return EvaluationStatus.SHORT_CIRCUIT;
                else
                    // Nothing more to do
                    return EvaluationStatus.SKIP;
            }
            else if (right == null)
                // Nothing more to do
                return EvaluationStatus.SKIP;
        }
        else if (shortCircuitOnTrue ) // Operator ||
        {   // true || means short circuit
            if ("true".equals(left.getValue().toString()))
            {
                if (right == null) // Unary || does not assign a value
                    return EvaluationStatus.SHORT_CIRCUIT;
                else
                    // Nothing more to do
                    return EvaluationStatus.SKIP;
            }
            else if (right == null)
                // Nothing more to do
                return EvaluationStatus.SKIP;
        }
        else if ((operatorEnum == OperatorEnum.HOOK) || (operatorEnum == OperatorEnum.COLON))
        {
            // Binary short circuit. Left has logic control.
            String leftValue = left.getValue().toString();
            if ((operatorEnum == OperatorEnum.HOOK) && leftValue.equals("false"))
                return EvaluationStatus.SHORT_CIRCUIT;
            if ((operatorEnum == OperatorEnum.COLON) && leftValue.equals("true"))
                return EvaluationStatus.SHORT_CIRCUIT;
        }
        return EvaluationStatus.COMPLETE;
    }

    private boolean isValidLeftOperation(Operand left, Operand right)
    {
        if (utils.isValidLeftOperand(left, right, operatorEnum))
            return true;
        return (orientation == Orientation.binary) && 
                performOnFlyConversion(left, right);
    }
    
    /**
     * Evaluate right term
     * @param left Left term
     * @param right Right term
     * @param id Identity of caller, which must be provided for backup()
     * @return EvaluationStatus enum
     */
    private EvaluationStatus evaluateRight(Operand left, Operand right, int id, ExecutionContext context)
    {
        // Only evaluate once between backups
        // Assume non-empty with id == 0 implies static initialization
        if (right.isEmpty() || (right.getId() == 0)) {
        		right.setExecutionContext(context);
           right.evaluate(id);
        }
        // If right hand term is empty, then cannot proceed. Maybe unification failed for this term.
        if (right.isEmpty())
            return EvaluationStatus.FAIL;
            // throw new ExpressionException("Right term is empty");
        if (orientation == Orientation.binary)
        {
            performOnFlyConversion(left, right);
            // Read back terms in case conversion occurred
            left = getLeftOperand();
            right = getRightOperand();
        }
        // Remember if right is not a number
        rightIsNaN = utils.isNaN(right, operatorEnum);
        if ((!utils.isValidRightOperand(left, right, operatorEnum) || utils.isInvalidRightUnaryOp(left, operatorEnum)) &&
             !((left != null) && utils.isConcatenateValid(left, operatorEnum))) 
        {   
            // TreeEvaluator not permitted for type of Term value
            // NaN has precedence if operation has Numeric result
            if (!rightIsNaN && ((left == null) || !leftIsNaN))
                return EvaluationStatus.FAIL;
               //throw new ExpressionException("Cannot evaluate " + (left == null ? unaryRightToString() : binaryToString()));
        }
        return EvaluationStatus.COMPLETE;
    }

    /**
     * Perform string conversion to number type, if appropriate
     * @param left Left term
     * @param right Right term
     * @return flag set true in conversion occurred
     */
    private boolean performStringConversion(Operand left, Operand right) {
        boolean isLeftString = left.getValueClass() == String.class;
        boolean isRightString = right.getValueClass() == String.class;
        if ((!isLeftString && !isRightString) || (isLeftString && isRightString))
            return (isLeftString && isRightString);
        if ((operatorEnum == OperatorEnum.PLUS) || (operatorEnum == OperatorEnum.PLUSASSIGN))
            // Exclude strings from conversion when performing concatenation
            return true;
        if (isRightString) {
            Trait trait = left.getOperator().getTrait();
            if (utils.isValidOperand(operatorEnum, left.getOperator().getLeftBinaryOps()) &&
                (trait instanceof StringCloneable)) {
            	OperandType rightOperandType = right.getOperator().getTrait().getOperandType();
                if (rightOperandType == OperandType.STRING) {
                    StringCloneable stringCloneable = (StringCloneable)left.getOperator().getTrait();
                    setRightOperand(stringCloneable.cloneFromOperand(right));
                } else if (rightOperandType == OperandType.BOOLEAN) {
                    value = ((BooleanTrait)trait).parseValue(right.getValue().toString());
                    right.assign(new Parameter(Term.ANONYMOUS, value));
                    setRightOperand(right);
                } else {
                    value = ((NumberTrait<?>)trait).parseValue(right.getValue().toString());
                    right.assign(new Parameter(Term.ANONYMOUS, value));
                    setRightOperand(right);
                }
            }
            return true;
        }
        Trait trait = right.getOperator().getTrait();
        if (utils.isValidOperand(operatorEnum, right.getOperator().getRightBinaryOps()) &&
             (trait instanceof StringCloneable)) {
        	OperandType leftOperandType = left.getOperator().getTrait().getOperandType();
            if (leftOperandType == OperandType.STRING) {
                StringCloneable stringCloneable = (StringCloneable)right.getOperator().getTrait();
                setLeftOperand(stringCloneable.cloneFromOperand(left));
            } else if (leftOperandType == OperandType.BOOLEAN) {
                value = ((BooleanTrait)trait).parseValue(left.getValue().toString());
                left.assign(new Parameter(Term.ANONYMOUS, value));
                setLeftOperand(left);
            } else {
                value = ((NumberTrait<?>)trait).parseValue(left.getValue().toString());
                left.assign(new Parameter(Term.ANONYMOUS, value));
                setLeftOperand(left);
            }
        }
        return true;
    }

    /**
     * Perform decimal conversion to currency type, if appropriate
     * @param left Left term
     * @param right Right term
     * @return flag set true in conversion occurred
     */
    private boolean performDecimalConversion(Operand left, Operand right)
    {
        boolean isLeftDecimal = left instanceof BigDecimalOperand;
        boolean isRightDecimal = right instanceof BigDecimalOperand;
        if ((!isLeftDecimal && !isRightDecimal) || (isLeftDecimal && isRightDecimal))
            return true ;
        if (isRightDecimal)
        {
            if (left.getOperator().getTrait().getOperandType() == OperandType.CURRENCY)
                convertToCurrency((BigDecimalOperand)left);
        }
        else
        {
            if (right.getOperator().getTrait().getOperandType() == OperandType.CURRENCY)
                convertToCurrency((BigDecimalOperand)right);
        }
        return true;
    }

    /**
     * Convert given decimal to currency type
     * @param bigDecimalOperand Decimal term to convert
     */
    private void convertToCurrency(BigDecimalOperand bigDecimalOperand)
    {
        Locale locale = bigDecimalOperand.getOperator().getTrait().getLocale();
        CurrencyOperator currencyOperator = new CurrencyOperator();
        bigDecimalOperand.setOperator(currencyOperator);
        currencyOperator.getTrait().setLocale(locale);
    }

}
