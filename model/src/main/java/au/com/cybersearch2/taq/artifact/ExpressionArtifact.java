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
package au.com.cybersearch2.taq.artifact;

import java.util.List;

import au.com.cybersearch2.taq.language.Group;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Operands which evaluates expressions
 */
public interface ExpressionArtifact {

    /**
     * Process UnaryExpression production
     * @param operator Unary operator symbol
     * @param unaryExression Expression subject to unary operation
     * @return Operand object
     */
    IOperand unaryExpression(String operator, IOperand unaryExression);
    
    /**
     * Process PostfixExpression production
     * @param primaryExression Expression subject to postfix operation
     * @param operator Postfix operator symbol
     * @return Operand object
     */
    IOperand postfixExpression(IOperand primaryExression, String operator);

    /**
     * Process PrimaryExpression production for named variable
     * @param qname Qualified name of variable
     * @return Operand object
     */
    IOperand primaryExpression(QualifiedName qname);

    /**
     * Process ParameterExpression production
     * @param name 1-part name which may be empty string for an anonymous parameter
     * @param listName Full name
     * @return Operand object
     */
    IOperand parameterExpression(String name, QualifiedName listName);

    /**
    * Process ParameterExpression production
    * @param name 1-part name which may be empty string for an anonymous parameter
    * @param parameter Parameter
    * @return Operand object
    */
    IOperand parameterExpression(String name, Parameter parameter);

    /**
     * Process ParameterExpression production
     * @param name 1-part name which may be empty string for an anonymous parameter
     * @param expression Assignment expression
     * @return Operand object
     */
    IOperand parameterExpression(String name, IOperand expression) ;

    /**
     * Process ShortCircuitExpression production
     * @param name Term name
     * @param rightName Name following question mark or null if none - also is an alias name
     * @param binaryOp Binary operator or null if none
     * @param expression Right hand side of binary operator or primary expression for match operator (single '?')
     * @param operator Short circuit operator - '?' or ':'
     * @return Operand object
     */
    IOperand shortCircuitExpression(String name, 
     		                        String rightName, 
    		                        String binaryOp, 
    		                        IOperand expression, 
    		                        String operator);

    /**
     * Returns compact loop operand
     * @param factExpression Expression subject to fact c
     * @param executeExpression Expression to execute
     * @param runOnce Flag set true if only to run once
     * @return Operand object
     */
    IOperand compactLoop(IOperand factExpression, IOperand executeExpression, boolean runOnce);
    
	/**
	 * Set list of literal terms to match on
     * @param name Term name
	 * @param isNot Flag set true to negate the logic
	 * @param literalList Parameter list
	 */
	IOperand literals(String name, boolean isNot, List<Parameter> literalList);
	
    /**
     * Process RregularExpression production
     * @param qname Qualified name
     * @param pattern Reference to pattern
     * @return Operand object
     */
    IOperand regularExpression(QualifiedName qname, String pattern);

    /**
     * Process RregularExpression production
     * @param expression Input expression
     * @param pattern Reference to pattern
     * @param group Group or null if none
     * @return Operand object
     */
	IOperand regularExpression(IOperand expression, String pattern, Group group);


    /**
     * Process IdentifierPostfix production
     * @param name name of variable subject to postfix operation
     * @param operator Postfix operator symbol
     * @return Operand object
     */
    IOperand identifierPostfix(String name, String operator);

  	/**
  	 * Process a cursor postfix (++ or --) production
  	 * @param qname Qualified name of cursor
  	 * @param operator Postfix operator 
  	 * @return Operand object
  	 */
  	IOperand cursorPostfixExpression(QualifiedName qname, String operator);

    /**
     * Process FlowExpression production
     * @param innerLoop Inner template to execute flow
     * @param expression IOperand to apply
     * @param operator Unary operator
     * @return IOperand object
     */
    IOperand flowExpression(IOperand innerLoop, IOperand expression, String operator);

}
