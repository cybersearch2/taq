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
package au.com.cybersearch2.taq.helper;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.operator.DelegateOperator;

/**
 * EvaluationUtils
 * @author Andrew Bowley
 * 3 Sep 2015
 */
public class EvaluationUtils
{
    /** Not a number */
    public  static final String NAN = "NaN"; //Double.valueOf(Double.NaN).toString();
    
   /**
     * Returns true if left operand permitted for operator used in expression
     * @param leftTerm Left Operand
     * @param operatorEnum Operator enum
     * @param rightTerm Right operand
     * @return Flag set true to indicate valid left operand
     */
    public boolean isValidLeftOperand(Operand leftTerm, Operand rightTerm, OperatorEnum operatorEnum) 
    {
    	OperatorEnum[] leftOps = 
    		rightTerm != null ? 
    				leftTerm.getOperator().getLeftBinaryOps() : 
    				leftTerm.getOperator().getLeftUnaryOps();
        for (OperatorEnum operatorEnum2: leftOps)
            if (operatorEnum2 == operatorEnum)
                return true;
        if (isConcatenateValid(leftTerm, operatorEnum) || 
            // Comma operator valid if right operand present    
            ((operatorEnum == OperatorEnum.COMMA) && (rightTerm != null)))
            return true;
        if ((operatorEnum == OperatorEnum.HOOK) || (operatorEnum == OperatorEnum.COLON))
            return true;
        return false;
    }

    /**
     * Returns flag to indicate if supplied term is allowed to perform concatenation
     * @param term Term
     * @param operatorEnum Operator enum
     * @return boolean
     */
    public boolean isConcatenateValid(Operand term, OperatorEnum operatorEnum)
    {
        for (OperatorEnum operatorEnum2: term.getOperator().getConcatenateOps())
            if (operatorEnum2 == operatorEnum)
                return true;
        return false;
    }
    
    /**
     * Returns true if right operand permitted for operator used in expression
     * @param leftTerm Left Operand. If not null then operation is binary
     * @param operatorEnum Operator enum
     * @return Flag set true to indicate invalid right operand
     */
    public boolean isInvalidRightUnaryOp(Operand leftTerm, OperatorEnum operatorEnum)
    {
        if (leftTerm != null)
            return false;
        if ((operatorEnum == OperatorEnum.INCR) || 
             (operatorEnum == OperatorEnum.DECR) ||
             (operatorEnum == OperatorEnum.NOT) ||
             (operatorEnum == OperatorEnum.TILDE) || 
             (operatorEnum == OperatorEnum.PLUS) || 
             (operatorEnum == OperatorEnum.MINUS) || 
             (operatorEnum == OperatorEnum.HOOK))
            return false;
        return true;
    }

    /**
     * Returns true if right operand permitted for operator used in expression
     * @param rightTerm Right Operand
     * @param operatorEnum Operator enum
     * @return Flag set true to indicate valid right operand
     */
    public boolean isValidRightOperand(Operand leftTerm, Operand rightTerm, OperatorEnum operatorEnum) 
    {
    	OperatorEnum[] righttOps = 
    			leftTerm != null ? 
    					rightTerm.getOperator().getRightBinaryOps() : 
    						rightTerm.getOperator().getRightUnaryOps();
        for (OperatorEnum operatorEnum2: righttOps)
            if (operatorEnum2 == operatorEnum)
                return true;
        // Only comma, ? and : operators are valid at this point
        return (operatorEnum == OperatorEnum.COMMA) || (operatorEnum == OperatorEnum.HOOK) || (operatorEnum == OperatorEnum.COLON);
    }

    /**
     * Returns flag set true if given operand is valid
     * @param operatorEnum Operator enum to match on
     * @param operatorEnums Array containing all Operator enums
     * @return boolean
     */
    public boolean isValidOperand(OperatorEnum operatorEnum, OperatorEnum[] operatorEnums) 
    {
        for (OperatorEnum operatorEnum2: operatorEnums)
            if (operatorEnum2 == operatorEnum)
                return true;
        return false;
    }
    
    /**
     * Returns true if left operand permitted for operator used in expression
     * @param rightTerm Right Operand. If not null then operation is binary
     * @param operatorEnum Operator enum
     * @return Flag set true to indicate invalid left operand
     */
    public boolean isInvalidLeftUnaryOp(Operand rightTerm, OperatorEnum operatorEnum)
    {
        if (rightTerm != null)
            return false;
        if ((operatorEnum == OperatorEnum.INCR) || 
            (operatorEnum == OperatorEnum.DECR) ||
            (operatorEnum == OperatorEnum.SC_AND) ||
            (operatorEnum == OperatorEnum.SC_OR) ||
            (operatorEnum == OperatorEnum.COMMA) || 
            (operatorEnum == OperatorEnum.HOOK) )
            return false;
        return true;
    }


    /**
     * Assign right term value to left term
     * @param leftTerm Left Operand
     * @param rightTerm Riht Operand
     * @param modificationId Modification version
     * @return Value as Object
     */
    public Object assignRightToLeft(Operand leftTerm, Operand rightTerm, int modificationId)
    {
        Object value = rightTerm.getValue();
        leftTerm.assign(rightTerm);
        // When the value class is not supported as a delegate, substitute a Null object.
        // This is defensive only as Operands are expected to only support Delegate classes
        return DelegateOperator.isDelegateClass(rightTerm.getValueClass()) ? value : new Null();
    }

    /**
     * Calculate a number using a boolean term converted to 1.0 for true and 0.0 for false.
     * At least one parameter is expected to contain a Boolean object
     * @param leftTerm Left operand
     * @param rightTerm Right operand
     * @return Number object (actualy BigDecimal)
     */
    public Number calculateBoolean(Operand leftTerm, Operand rightTerm)
    {
        if (leftTerm.getValueClass() == Boolean.class)
            return (Number) leftTerm.getOperator().numberEvaluation(leftTerm, OperatorEnum.STAR, rightTerm);
        return (Number) rightTerm.getOperator().numberEvaluation(leftTerm, OperatorEnum.STAR, rightTerm);
    }

    /**
     * Returns flag true if operand value is NaN 
     * @param operand Operand to test
     * @param operatorEnum Operator enum
     * @return Flag set true to indicate not a number
     */
    public boolean isNaN(Operand operand, OperatorEnum operatorEnum)
    {
        if ((operand == null) || operand.isEmpty())
            return false;
        Object value = operand.getValue();
        switch (operatorEnum)
        {
        case ASSIGN: // "="
        case PLUS: // "+"
        case MINUS: // "-"
        case STAR: // "*"
        case SLASH: // "/"
        case BIT_AND: // "&"
        case BIT_OR: // "|"
        case XOR: // "^"
        case REM: // "%"
        case TILDE: // "~"
        case INCR:
        case DECR:
        case PLUSASSIGN: // "+"
        case MINUSASSIGN: // "-"
        case STARASSIGN: // "*"
        case SLASHASSIGN: // "/"
        case ANDASSIGN: // "&"
        case ORASSIGN: // "|"
        case XORASSIGN: // "^"
        case REMASSIGN: // "%"
            return (value instanceof Number) && value.toString().equals(NAN);
        default:
        }
        return false;
    }

    /**
     * Returns flag true if number is not suitable for Number evaluation. 
     * Checks for number converted to double has value = Double.NaN
     * @param number Object value perporting to be Number subclass
     * @return Flag set true to indicate not a number
     */
    public static boolean isNaN(Object number)
    {
        if ((number == null) || (!(number instanceof Number || number instanceof Boolean)))
            return true;
        return number.toString().equals(NAN);
    }

}
