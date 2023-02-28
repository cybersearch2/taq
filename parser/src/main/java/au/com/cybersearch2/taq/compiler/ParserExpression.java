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
package au.com.cybersearch2.taq.compiler;

import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.ExpressionArtifact;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.ParserTask;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.CompactLoopOperand;
import au.com.cybersearch2.taq.expression.CursorOperand;
import au.com.cybersearch2.taq.expression.Evaluator;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.LiteralListOperand;
import au.com.cybersearch2.taq.expression.Orientation;
import au.com.cybersearch2.taq.expression.ReferenceOperand;
import au.com.cybersearch2.taq.expression.RegExOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Group;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.TaqLiteral;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.CursorItemVariable;
import au.com.cybersearch2.taq.list.CursorList;
import au.com.cybersearch2.taq.list.ListParserRunner;
import au.com.cybersearch2.taq.pattern.Choice;

/**
 * Creates operands to evaluate expressions
 */
public class ParserExpression implements ExpressionArtifact {

	/** Parser context */
	private final ParserContext parserContext;
	
	/** Cursor parser helper created on demand */
	private ParserCursor parserCursor;

	/**
	 * Construct ParserExpression object
	 * @param parserContext Parser context
	 */
	public ParserExpression(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

    /**
     * Process UnaryExpression production
     * @param operator Unary operator symbol
     * @param unaryExression Expression subject to unary operation
     * @return Operand object
     */
	@Override
    public Operand unaryExpression(String operator, IOperand unaryExression) {
    	return new Evaluator((Operand)unaryExression, operator, Orientation.unary_prefix);
    }
    
    /**
     * Process PostfixExpression production
     * @param primaryExression Expression subject to postfix operation
     * @param operator Postfix operator symbol
     * @return Operand object
     */
	@Override
    public Operand postfixExpression(IOperand primaryExression, String operator) {
        return new Evaluator((Operand)primaryExression, operator, Orientation.unary_postfix);
    }

    /**
     * Process PrimaryExpression production for named variable
     * @param qname Qualified name of variable
     * @return Operand object
     */
	@Override
    public Operand primaryExpression(QualifiedName qname) {
    	Operand operand = getListOperand(qname);
    	if (operand == null) {
    	    ParserAssembler parserAssembler = getParserAssembler();
            operand = parserAssembler.getOperandMap().getOperand(qname);
        	if (operand == null) {
        		String scopeName = qname.getScope();
        		if (qname.isTemplateScope())
        			scopeName = scopeName.substring(0, scopeName.indexOf('@'));
        		Scope nameScope = parserContext.getScope().getScope(scopeName);
                operand = nameScope.getParserAssembler().getOperandMap().getOperand(qname);
        	} 
        	if (operand == null)
         	    operand = parserAssembler.addOperand(qname);
        	else if (operand instanceof Choice) {
        		// Special case. Create a shadow variable to isolate the Choice
        		Operand choiceOperand = operand;
        		operand = new Variable(QualifiedName.ANONYMOUS);
        		choiceOperand.addShadow(operand);
        	}
    	}
        return operand;
    }

    /**
     * Process ParameterExpression production
     * @param name 1-part name which may be empty string for an anonymous parameter
     * @param listName Full name
     * @return Operand object
     */
	@Override
    public Operand parameterExpression(String name, QualifiedName listName) {
        ParserAssembler parserAssembler = getParserAssembler();
        boolean isAnonymous = name.equals(Term.ANONYMOUS);
        QualifiedName qname = parserAssembler.getContextName(name);
        Operand var = null;
        if (listName != null) {
        	var = getListOperand(listName);
            if (var != null) {
            	if (!isAnonymous && var.getName().equals(Term.ANONYMOUS)) {
        	        var.setName(name);
        	        return var;
            	}
            	return new Variable(qname, var);
            }
        }
        OperandMap operandMap = getOperandMap();
        if (!isAnonymous)
            var = operandMap.getOperand(qname);
        if (var == null) {
	        if (!isAnonymous && operandMap.hasOperand(name, parserAssembler.getQualifiedContextname())) {
	            var = operandMap.addOperand(name, parserAssembler.getQualifiedContextname());
	        } else {
	        	VariableSpec varSpec = new VariableSpec(OperandType.UNKNOWN);
		        VariableFactory varoableFactory = new VariableFactory(varSpec);
		        var = varoableFactory.getContextInstance(name, parserAssembler);
	        }
        }
        return var;
    }

    /**
    * Process ParameterExpression production
    * @param name 1-part name which may be empty string for an anonymous parameter
    * @param parameter Parameter
    * @return Operand object
    */
	@Override
    public Operand parameterExpression(String name, Parameter parameter) {
        ParserAssembler parserAssembler = getParserAssembler();
        Operand var = new Variable(parserAssembler.getContextName(name));
        var.assign(parameter);
        return var;
   }

   /**
   * Process ParameterExpression production
   * @param name 1-part name which may be empty string for an anonymous parameter
   * @param expression Assignment expression
   * @return Operand object
   */
	@Override
    public Operand parameterExpression(String name, IOperand expression) {
    	Operand operand = (Operand)expression;
        ParserAssembler parserAssembler = getParserAssembler();
        OperandMap operandMap = getOperandMap();
        boolean isAnonymous = name.equals(Term.ANONYMOUS);
        Operand var;
        if (!isAnonymous && operandMap.hasOperand(name, parserAssembler.getQualifiedContextname())) {
            var = operandMap.addOperand(name, parserAssembler.getQualifiedContextname());
        	if (!(var instanceof CursorItemVariable))
    	        var = assignExpression(var, operand);
    	    else
    	        var = new Evaluator(var, "=", operand);
        } else {
        	if (!isAnonymous && operand.getName().equals(Term.ANONYMOUS)) {
        		operand.setName(name);
        		var = operand;
        	} else {
		    	VariableSpec varSpec = new VariableSpec(OperandType.UNKNOWN);
		        varSpec.setExpression(operand);
		        VariableFactory varoableFactory = new VariableFactory(varSpec);
		        var = varoableFactory.getContextInstance(name, parserAssembler);
        	}
	    }
        return var;
    }

    /**
     * Returns compact loop operand
     * @param factExpression Expression subject to fact check
     * @param executeExpression Expression to execute
     * @param runOnce Flag set true if only to run once
     * @return Operand object
     */
	@Override
	public IOperand compactLoop(IOperand factExpression, IOperand executeExpression, boolean runOnce) {
		return new CompactLoopOperand((Operand)factExpression, (Operand)executeExpression, runOnce);
	}

    /**
     * Process ShortCircuitExpression production
     * @param name Term name
     * @param rightName Name following question mark or null if none - also is an alias name
     * @param binaryOp Binary operator or null if none
     * @param expression Right hand side of binary operator or primary expression for match operator (single '?')
     * @param operator Short circuit operator - '?' or ':'
     * @return Operand object
     */
	@Override
    public Operand shortCircuitExpression(String name, String rightName, String binaryOp, IOperand expression, String operator) {
    	boolean hasRightName = (rightName != null) && !rightName.isEmpty();
        ParserAssembler parserAssembler = getParserAssembler();
        OperandMap operandMap = parserAssembler.getOperandMap();
        QualifiedName qname;
        Operand termOperand;
        boolean isSingleTerm = !hasRightName;
        if (isSingleTerm) {
    	    qname = name(name, true, false);
    		termOperand = operandMap.getOperand(qname);
    	    if (termOperand == null)
    	    	termOperand = parserAssembler.addOperand(qname);
        } else {
        	qname = new QualifiedName(name);
        	termOperand = parserAssembler.addOperand(qname);
        }
    	boolean isPrivate = termOperand.isPrivate();
    	if (expression == null)
        	return new Evaluator(termOperand, operator, termOperand);
       	Operand rightOperand = termOperand;
        QualifiedName rightQname = null;
    	if (hasRightName) 
    		rightQname = new QualifiedName(rightName);
    	if (rightQname != null)
    		rightOperand = parserAssembler.addOperand(rightQname);
     	if (binaryOp == null) {
    		binaryOp = "?".equals(operator) ? "==" : "!=";
     	} else if ("?".equals(binaryOp)) {
     		if (hasRightName) {
     			binaryOp = "==";
     		} else { // ?? unary prefix
     			VariableSpec varSpec = VariableSpec.variableSpec(TaqLiteral.taq_boolean);
     			varSpec.setExpression((Operand)expression);
                QualifiedName booleanQname = new QualifiedName("select_" + name);
                VariableFactory variableFactory = new VariableFactory(varSpec);
                Operand shortCircuit = variableFactory.getExpressionInstance(booleanQname, parserAssembler);
             	Evaluator evaluator = new Evaluator(shortCircuit, operator, Orientation.unary_prefix);
             	evaluator.setPrivate(isPrivate);
             	return evaluator;
    		}
     	}
     	Operand shortCircuit = evaluationExpression(rightOperand, binaryOp, (Operand)expression);
     	Evaluator evaluator = new Evaluator(qname, shortCircuit, operator, termOperand);
     	evaluator.setPrivate(isPrivate);
     	return evaluator;
    }
    
	/**
	 * Set list of literal terms to match on
     * @param name Term name
	 * @param isNot Flag set true to negate the logic
	 * @param literalList Parameter list
	 */
	@Override
	public IOperand literals(String name, boolean isNot, List<Parameter> literalList) {
		return new LiteralListOperand(name(name, true, false), literalList, isNot);
	}

    /**
     * Process RregularExpression production
     * @param qname Qualified name
     * @param pattern Reference to pattern
     * @return Operand object
     */
	@Override
    public Operand regularExpression(QualifiedName qname, String pattern) {
        Operand inputOp = getOperandMap().addOperand(qname.getName(), getParserAssembler().getQualifiedContextname());
       	return new RegExOperand(QualifiedName.ANONYMOUS, getQueryProgram().getPatternFactory(pattern), inputOp, null);
    }

	@Override
	public IOperand regularExpression(IOperand expression, String pattern, Group group) {
   	    return new RegExOperand(QualifiedName.ANONYMOUS, getQueryProgram().getPatternFactory(pattern), (Operand) expression, group);
	}

   /**
     * Process IdentifierPostfix production
     * @param name name of variable subject to postfix operation
     * @param operator Postfix operator symbol
     * @return Operand object
     */
	@Override
    public Operand identifierPostfix(String name, String operator) {
    	QualifiedName qname = name(name, true, false);
        ParserAssembler parserAssembler = getParserAssembler();
        ListAssembler listAssembler = parserAssembler.getListAssembler();
        OperandMap operandMap = parserAssembler.getOperandMap();
        Operand var;
        if (listAssembler.existsKey(ListType.cursor, qname)) {
        	CursorList cursorList = listAssembler.getCursorList(qname);
        	Operand typeOperand = null;
        	if (!cursorList.getQualifiedListName().equals(qname)) {
        		if (parserCursor == null)
        			parserCursor = new ParserCursor(parserContext);
        		typeOperand = parserCursor.getCursorOperand(cursorList.getCursor());
        	}
        	CursorOperand cursorOperand = new CursorOperand(cursorList, typeOperand);
    	    ParserTask parserTask = parserAssembler.addPending(cursorOperand);
    	    parserTask.setPriority(ParserTask.Priority.variable.ordinal());
    	    var = cursorOperand;
        } else
            var = operandMap.getOperand(qname);
        if (var == null)
        	throw new CompilerException(String.format("Variable named '%s' not found", name));
        return postfixExpression(var, operator);
    }

	/**
	 * Process a cursor postfix (++ or --) production
	 * @param qname Qualified name of cursor
	 * @param operator Postfix operator 
	 * @return Operand object
	 */
	@Override
	public Operand cursorPostfixExpression(QualifiedName qname, String operator) {
    	ParserAssembler parserAssembler = getParserAssembler();
		CursorList cursorList = parserAssembler.getListAssembler().getCursorList(qname);
		if (cursorList == null)
			throw new CompilerException(String.format("Cursor '%s' not found", qname));
    	Operand typeOperand = null;
    	if (!cursorList.getQualifiedListName().equals(qname)) {
    		if (parserCursor == null)
    			parserCursor = new ParserCursor(parserContext);
    	    typeOperand = parserCursor.getCursorOperand(cursorList.getCursor());
    	}
    	CursorOperand operand = new CursorOperand(cursorList, typeOperand);
	    ParserTask parserTask = parserAssembler.addPending(operand);
	    parserTask.setPriority(ParserTask.Priority.variable.ordinal());
		return postfixExpression(operand, operator);
	}

    /**
     * Process FlowExpression production
     * @param innerLoop Inner template to execute flow
     * @param expression Operand to apply
     * @param operator Unary operator
     * @return Operand object
     */
	@Override
    public Operand flowExpression(IOperand innerLoop, IOperand expression, String operator) {
    	Operand operand = innerLoop == null ? 
    			              new Evaluator((Operand)expression, operator, Orientation.unary_postfix) : 
    				          new Evaluator((Operand)expression, operator, (Operand)innerLoop);
        operand.setPrivate(true);
        return operand;
    }

    private Operand assignExpression(Operand var, Operand expression) {
  	    return new Variable(var, expression);
    }

	/**
	 * Process Name production
	 * @param name Identifier
	 * @param isContextName Flag set true to incorporate context details
	 * @param isDelaration Is part of a declaration
	 * @return QualifiedName object
	 */
	private QualifiedName name(String name, boolean isContextName, boolean isDeclaration) {
	    return getParserAssembler().name(name, isContextName, isDeclaration);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Operand getListOperand(QualifiedName qname) {
    	ParserAssembler parserAssembler = getParserAssembler();
        ListAssembler listAssembler = parserAssembler.getListAssembler();
        if (listAssembler.existsKey(ListType.cursor, qname)) {
        	// A cursor has it's own unique operations for which a custom type is provided
        	CursorList cursorList = listAssembler.getCursorList(qname);
        	Operand typeOperand = null;
        	if (!cursorList.getQualifiedListName().equals(qname)) {
        		if (parserCursor == null)
        			parserCursor = new ParserCursor(parserContext);
        	    typeOperand = parserCursor.getCursorOperand(cursorList.getCursor());
            }
        	CursorOperand operand = new CursorOperand(cursorList, typeOperand);
    	    ParserTask parserTask = parserAssembler.addPending(operand);
    	    parserTask.setPriority(ParserTask.Priority.variable.ordinal());
    	    return operand;
        } else if (listAssembler.getListType(qname) != ListType.none) {
        	// A list without a reference can only be assigned or appended.
         	OperandMap operandMap = getOperandMap();
            if (operandMap.existsName(qname.getName())) {
           	    Operand operand = operandMap.getOperand(qname);
           	    if (operand instanceof ListOperand) 
           	    	return new ReferenceOperand((ListOperand<?>) operand);
            }
    		ListParserRunner runner = new ListParserRunner(qname);
    		runner.run(parserAssembler);
    		ItemList<?> itemList = runner.getItemList();
    		if (itemList != null)
    		    return new ReferenceOperand(new ListOperand(itemList));
    		throw new CompilerException(String.format("List %s not found", qname.toString()));
        }
        return null;
    }
 
	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

	/**
	 * Returns operand Map
	 * @return OPerandMap object
	 */
	private OperandMap getOperandMap() {
		return parserContext.getParserAssembler().getOperandMap();
	}

    /**
     * Returns Query Program
     * @return QueryProgram object
     */
	private QueryProgram getQueryProgram() {
	    return parserContext.getQueryProgram();
    }

    /**
     * Process binary production, of which there are many, differing only by operator
     * @param left Left hand expression
     * @param operator Binary operator symbol
     * @param right Right hand expression
     * @return Operand object
     */
    public Operand evaluationExpression(Operand left, String operator, Operand right) {
        return new Evaluator(left, operator, right);
    }



}
