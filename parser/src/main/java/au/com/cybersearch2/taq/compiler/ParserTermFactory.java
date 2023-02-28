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

import au.com.cybersearch2.taq.artifact.TermArtifact;
import au.com.cybersearch2.taq.artifact.TermFactoryArtifact;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.ParserTask;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.AnonOperand;
import au.com.cybersearch2.taq.expression.AppenderVariable;
import au.com.cybersearch2.taq.expression.CursorOperand;
import au.com.cybersearch2.taq.expression.Evaluator;
import au.com.cybersearch2.taq.expression.FactOperand;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.Cursor;
import au.com.cybersearch2.taq.list.CursorList;

/**
 * Parser helper to create term artifacts
 *
 */
public class ParserTermFactory implements TermFactoryArtifact {

	/** Parser context */
	private final ParserContext parserContext;
	
	private ParserCursor parserCursor;
	private ParserListFactory parserListFactory;
	private ParserExpression parserExpression;

	/**
	 * Construct ParserFunctionFactory object
	 * @param parserContext Parser context
	 */
	public ParserTermFactory(ParserContext parserContext) {
		this.parserContext = parserContext;
	}
	
    /**
     * Returns operand to create an axiom term
     * @param parserTerm Term artifact
     * @param expression Optional expression to set the term value
     * @return Operand object
     */
    public Operand termExpression(ParserTerm parserTerm, Operand expression) {
        if (expression != null) {
        	parserTerm.setOperator("=");
    	    parserTerm.setExpression((Operand)expression);
        }
    	return templateTerm(parserTerm);
    }

	/**
	 * Returns list item operand or axiom container operand according to operand type
	 * @param parserTerm Helper to collect Term declaration details 
	 * @return ListItemOperand or AxiomContainerOperand according to operand type
	 */
	public Operand createListItemOperand(ParserTerm parserTerm) {
		Operand leftOperand = parserTerm.isReflexive() ? null : parserTerm.getExpression();
		QualifiedName qname = parserTerm.getQname();
       	if (parserListFactory == null)
    		parserListFactory = new ParserListFactory(parserContext);
		ListItemSpec[] indexExpression = parserTerm.getIndexExpression();
        if (indexExpression.length == 1)
        	return parserListFactory.listItemOperand(indexExpression[0], leftOperand);
        else {
         	return parserListFactory.axiomContainerOperand(qname.getName(), qname, indexExpression, leftOperand);
        }
	}

	@Override
	public IOperand templateTerm(IOperand expression) {
		Operand expressionValue = (Operand)expression;
		ParserAssembler parserAssembler = getParserAssembler();
    	OperandMap operandMap = parserAssembler.getOperandMap();
    	Operand operand = operandMap.get(expressionValue.getQualifiedName());
    	if (operand != null)
    		// If expression in in the Operand Map, then it is a variable
    		return new AnonOperand(operand);
        VariableSpec varSpec = new VariableSpec(OperandType.UNKNOWN);
        VariableFactory variableFactory = new VariableFactory(varSpec);
		return variableFactory.getInstance(QualifiedName.ANONYMOUS, (Operand)expression, getParserAssembler());
	}
    
   /**
	 * Returns fact operand
	 * @param parserTerm Parser term to analyze
	 * @return Operand object
	 */
	@Override
	public Operand createFact(TermArtifact parserTerm) {
        return new FactOperand(templateTerm((ParserTerm)parserTerm));
	}

	/**
	 * Returns a regular expression short circuit evaluator
	 * @param regexOp regular expression operand
	 * @return Operand object
	 */
	@Override
    public Operand createRegexTerm(IOperand regexOp) {
		Operand operand = (Operand)regexOp;
    	Operand inputOperand = operand.getLeftOperand();
    	operand = new Evaluator(inputOperand.getQualifiedName(), operand, "?", inputOperand);
    	operand.setPrivate(inputOperand.isPrivate());
    	return operand;
    }

	/**
	 * Returns fact operand
	 * @param parserTerm Parser term to analyse
	 * @param postFix Post fix operator
	 * @return Operand object
	 */
	@Override
	public Operand createFact(TermArtifact parserTerm, String postFix) {
        Operand operand = templateTerm((ParserTerm)parserTerm);
        if (parserExpression == null)
        	parserExpression = new ParserExpression(parserContext);
        Operand postOperand = parserExpression.postfixExpression(operand, postFix);
        if (operand instanceof CursorOperand) {
        	CursorOperand cursorOperand = (CursorOperand)operand;
            return new FactOperand(cursorOperand.getCursorItemVariable().getCursorList(), postOperand);
        } else
            return new FactOperand(postOperand);
	}
	
	/**
	 * Returns fact operand
	 * @param qualifiedName Qualified name
	 * @return Operand object
	 */
	@Override
	public Operand createFact(QualifiedName qualifiedName) {
		ParserAssembler parserAssembler = getParserAssembler();
    	ListAssembler listAssembler = parserAssembler.getListAssembler();
    	OperandMap operandMap = parserAssembler.getOperandMap();
		if (listAssembler.existsKey(ListType.cursor, qualifiedName))
    		// Cursor fact
        	return  new FactOperand(listAssembler.getCursorList(qualifiedName));

		ListType listType = listAssembler.getListType(qualifiedName);
		if ((listType == ListType.basic) || 
			(listType == ListType.term) || 
			(listType == ListType.axiom_item) ||
			(listType == ListType.axiom_dynamic)) {
			{
		        if (operandMap.existsName(qualifiedName.getName())) {
		       	    Operand operand = operandMap.getOperand(qualifiedName);
		       	    if (operand instanceof ListOperand)
		            	return  new FactOperand((ListOperand<?>)operand);
		    	    return new FactOperand(operand);
		        }
                return new FactOperand(listAssembler.getItemList(qualifiedName));
			}
        }
		Operand target = operandMap.getOperand(qualifiedName);
        if (target == null) // Operand not found
	        target = parserAssembler.addOperand(qualifiedName);
	    return new FactOperand(target);
	}

    /**
     * Process Expression production
     * @param param Expression packaged in an operand
     * @param assignOp Optional assignment symbol, possibly reflexive
     * @param assignOperand Assignment expression associated with assigOp
     * @return Operand object
     */
	@Override
    public Operand expression(IOperand param, String assignOp, IOperand assignOperand) {
    	Operand toOperand = (Operand)assignOperand;
    	AppenderVariable var = null;
        if (param instanceof AppenderVariable) {
            var = (AppenderVariable)param;
        } else {
        	if (assignOp.equals("=")) {
        		return new Evaluator((Operand)param, assignOp, toOperand);
        	} else {
        		return createReflexiveEvaluator((Operand)param, assignOp, toOperand);
        	}
        }
        if (assignOp.equals("+="))
            var.setDoConcatenate(true);
        var.setExpression(toOperand);
        return var;
    }

    /**
     * Process TemplateTerm production
     * @param parserTerm Term production
     * @return Operand object
     */
	@Override
    public Operand templateTerm(TermArtifact termArtifact) {
    	ParserTerm parserTerm = (ParserTerm)termArtifact;
    	// Eliminate special cases 
    	if (parserTerm.hasCallParameters())
    		return parserTerm.createCallOperand();
        if (parserTerm.isAppender())
            return parserTerm.createAppender();
        // The operand will be a variable or evaluator
        // Check if a variable of the same name and in any scope exists in the context
        String name = parserTerm.getQname().toString();
        ParserAssembler parserAssembler = getParserAssembler();
        OperandMap operandMap = parserAssembler.getOperandMap();
        ListAssembler listAssembler = parserAssembler.getListAssembler();
        boolean hasOperand = operandMap.hasOperand(name, parserAssembler.getQualifiedContextname());
        Operand expression = parserTerm.getExpression();
        // Establish operand to reference, which will need to be created depending on circumstances.
        Operand var = null;
        if (parserTerm.assignLiteral()) {
        	if (!hasOperand) {
        		QualifiedName qname = parserTerm.getQname();
	        	var = parserTerm.getLiteral();
	        	var.setName(qname.getName());
	        	operandMap.addOperand(qname, var);
	        	return var;
            }
        	else 
        		expression = parserTerm.getLiteral(); 
        }
       ListReference listReference = parserTerm.getListReference();
        if (listReference !=null)
        	// Create a list item variable
            var = createListItemOperand(parserTerm);
        else if (!parserTerm.isDeclaration() && listAssembler.existsKey(ListType.cursor, parserTerm.getQname())) {
        	CursorList cursorList = listAssembler.getCursorList(parserTerm.getQname());
        	Operand typeOperand = null;
        	if (!cursorList.getQualifiedListName().equals(parserTerm.getQname()))
        	    typeOperand = getCursorOperand(cursorList.getCursor());
        	CursorOperand cursorOperand = new CursorOperand(cursorList, typeOperand);
    	    ParserTask parserTask = parserAssembler.addPending(cursorOperand);
    	    parserTask.setPriority(ParserTask.Priority.variable.ordinal());
    	    var = cursorOperand;
        } else
        	// Get term if it already exists in current template
        	var = operandMap.getOperand(parserTerm.getQname());
        Variable shadow = null; // Prepare for possible creation of shadow variable
        if ((var != null) && parserTerm.isDeclaration()) {
        	// Check if operand already exists, is a variable and a shadow candidate
        	if (operandMap.containsOperand(parserTerm.getQname()) && 
        		(var instanceof Variable) && !var.isHead()) {
        		// Delegate var as a shadow and demote it as an operand
        		shadow = (Variable)var;
        		operandMap.removeOperand(var);
        		hasOperand = false;
        		var = null;
        	} else
        	    // If a type is specified for an existing term, then it is regarded as a duplicate
                operandMap.duplicateOperandCheck(parserTerm.getQname()); 
        }
        if ((var == null) && !hasOperand) {
        	// Create a new variable if there is nothing else in the context with the same name
            VariableSpec varSpec;
            if (!parserTerm.isDeclaration())
            	varSpec = new VariableSpec(OperandType.UNKNOWN);
            else
            	varSpec = parserTerm.getVarSpec();
            if (parserTerm.getReflexiveExpression()!= null)
            	varSpec.setReflexOp(Evaluator.convertOperator(parserTerm.getOperator()));
            VariableFactory variableFactory = new VariableFactory(varSpec);
            var = variableFactory.getInstance(parserTerm.getQname(), parserAssembler);
            if ((listReference == null) && (!var.getQualifiedName().equals(QualifiedName.ANONYMOUS)))
                operandMap.addOperand(var);       
        }
    	if (var == null)
    		// Finally, retrieve an operand in the context with the same name or if not found, create a new variable  
            var = operandMap.addOperand(name, parserAssembler.getQualifiedContextname());
    	// If term is an assignment using '=' operator, then create a variable to perform the operation
        if (parserTerm.isBinaryAssign() && (listReference == null)) {
            if (expression != null) {
            	if (!(var instanceof CursorOperand)) {
            		if (parserTerm.isDeclaration())
            			// A declaration uses the left-hand operand for assignment
            			var.setLeftOperand(expression);
            		else
            			// A shadow is created to assign to var indirectly
            	        var = assignExpression(var, expression);
            	} else
            		// A cursor requires an evaluator instead of a variable
            		var = new Evaluator(var, "=", expression);
            }
        } else if (parserTerm.isReflexive()) {
        	// A reflexive variable updates both the operand being referenced plus the term being inserted in the template
            var = createReflexiveEvaluator(var, parserTerm.getOperator(), expression);
        }
        if (shadow != null) {
        	var.addShadow(shadow);
        }
        return var;
    }


	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}

    private Operand assignExpression(Operand var, Operand expression) {
  	    return new Variable(var, expression);
    }
    
    private Operand getCursorOperand(Cursor cursor) {
    	if (parserCursor == null)
    		parserCursor = new ParserCursor(parserContext);
    	return parserCursor.getCursorOperand(cursor);
    }

    /**
     * Create evaluator for operation which assigns result to self.
     * Creates parser task to assign evaluator name.
     * @param term Target of operation
     * @param operator Reflexive operation
     * @param assignExpression Expression on right hand side
     * @return
     */
    private Operand createReflexiveEvaluator(Operand operand, String operator, Operand assignExpression) {
        QualifiedName qname = operand.getQualifiedName();
        QualifiedName referenceQname = new QualifiedName(qname.getName() + qname.incrementReferenceCount(), qname);
        Variable var = new Variable(referenceQname, qname.getName(), assignExpression);
        var.setReflexOp(Evaluator.convertOperator(operator), operand);
        return var;
    }

}
