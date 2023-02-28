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

import java.util.Collections;
import java.util.List;

import au.com.cybersearch2.taq.artifact.TermArtifact;
import au.com.cybersearch2.taq.compile.AxiomListEvaluator;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.AxiomOperand;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.ReferenceOperand;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.ListParserRunner;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * Helper to collect Term declaration details 
 * @author Andrew Bowley
 *
 */
public class ParserTerm implements TermArtifact {

	/** Creates artifacts from parser productions */
	private final Compiler compiler;
	/** Qualified name of term */
	private final QualifiedName qname;
	/** Variable type or null if not specified */
	private final VariableSpec varSpec;

	/** Parser helper to create function artifacts */
	private ParserFunctionFactory parserFunctionFactory;
	/** Parser helper to create list artifacts */
	private ParserListFactory parserListFactory;
	/** List type enum */
	private ListType listType;
	/** Axiom name - defaults to term qualified name */
	private QualifiedName axiomQname;
	/** List reference or null if none */
	private ListReference listReference;
	/** Index expression or null if none */
	private ListItemSpec[] indexExpression;
	/** List of literal terms or null if none */
	private List<Parameter> literalList;
	/** Function parameters or null if none */
	private ITemplate parameterTemplate;
	/** Expression on left hand side of operation such as assignment */
	private Operand expression;
	/** Literal on left hand side of operation such as assignment */
	private Operand literal;
	/** List operand referenced by variable */
	private ListOperand<?> listOperand;
	/** Operator verbatim from source */
	private String operator;
	/** Flag set true if term is a list */
	private boolean isList;
	/** Flag set true if format matches function call */
	private boolean hasCallParameters;

	/**
	 * Construct ParserTerm object
	 * @param compiler Applies compiler logic to the javacc token stream
	 * @param varSpec Variable type - can be null for unkonwn or shadow
	 * @param qname Qualified name
	 * @param listReference List item reference
	 */
	public ParserTerm(Compiler compiler, IVariableSpec varSpec, QualifiedName qname, ListReference listReference) {
		this.compiler = compiler;
		this.varSpec = (VariableSpec)varSpec;
		this.qname = qname;
		this.listReference = listReference;
		axiomQname = qname;
		ParserAssembler parserAssembler = compiler.getParserAssembler();
		ListAssembler listAssembler = parserAssembler.getListAssembler();
     	OperandMap operandMap = parserAssembler.getOperandMap();
        if (operandMap.existsName(qname.getName())) {
       	    Operand operand = operandMap.getOperand(qname);
       	    isList = operand instanceof ListOperand;
       	    if (isList) {
       	         listOperand = (ListOperand<?>) operand;
       	         listType = listOperand.getlistType();
       	    }
        }
        if (listOperand == null) {
	     	if (listAssembler.existsKey(ListType.cursor, qname)) 
	     		listType = ListType.cursor;
	     	else {
		     	listType = (varSpec != null) ? ListType.none : listAssembler.getListType(qname);
				if ((listType != ListType.none) && (listType != ListType.cursor)) {
			         isList = 
			             (listType == ListType.basic) || 
			             (listType == ListType.term) || 
			             ((listReference != null) && listType == ListType.axiom_item) ||
			             (listType == ListType.axiom_dynamic);
			    }
	     	}
        }
	    if (listReference != null) {
	 	    QualifiedName listQname = compiler.getListFactoryArtifact().getListName(qname);	   
	    	indexExpression = 
	    			compiler.getListFactory().indexExpression(listQname, this.listReference);
	        if (listAssembler.existsKey(ListType.context, qname))
	        	indexExpression[0].getQualifiedListName().toContextName();
	    }
	}

	public ListItemSpec[] getIndexExpression() {
		return indexExpression;
	}

	public QualifiedName getAxiomQname() {
		return axiomQname;
	}
	
	public void setAxiomQname(QualifiedName axiomQname) {
		this.axiomQname = axiomQname;
	}

	public void setLiteralList(List<Parameter> literalList) {
		this.literalList = literalList;
	}

	public ITemplate getParameterTemplate() {
		return parameterTemplate;
	}

	public Operand getLiteral() {
		return literal;
	}

	public boolean assignLiteral() {
		return literal != null;
	}
	
	public boolean isAppender() {
		boolean isAppender = ("+=".equals(operator) || "=".equals(operator)) && (listReference == null);
		if (isAppender) 
		    isAppender = ((listType != ListType.none) && (listType != ListType.cursor)) || (listOperand != null);
		return isAppender;
	}

	public Operand createAppender() {
		if (parserListFactory == null)
			parserListFactory = new ParserListFactory(compiler.getParserContext());
	    if (listOperand != null) {
	    	if ((expression instanceof ReferenceOperand) && operator.equals("=") && (indexExpression == null)) {
	    		ReferenceOperand referenceOperand = (ReferenceOperand)expression;
	    		referenceOperand.setAssignee(listOperand);
	    		return referenceOperand;
	    	}
	    	return parserListFactory.getListItemAppender(listOperand, indexExpression, operator, expression);
	    }
        return parserListFactory.getListItemAppender(qname, listType, indexExpression, operator, expression);
	}

	public boolean isDeclaration() {
		return varSpec != null;
	}

	public boolean isReflexive() {
		return (operator != null) && !operator.equals("=");
	}
	
	public boolean isBinaryAssign() {
		return (operator != null) && operator.equals("=");
	}
	
	public Operand getReflexiveExpression() {
		return isReflexive() ? expression : null;
	}

	public String getOperator() {
		return operator;
	}

	public Operand getExpression() {
		return expression;
	}

	public ListReference getListReference() {
		return listReference;
	}

	public boolean hasLiteralList() {
		return literalList != null;
	}

	public boolean hasCallParameters() {
		return hasCallParameters;
	}

	public Operand createCallOperand() {
		ParserFunction parserFunction = new ParserFunction(qname, qname.getSource());
		parserFunction.setParametersTemplate(parameterTemplate);
		if (parserFunctionFactory == null)
			parserFunctionFactory = new ParserFunctionFactory(compiler.getParserContext());
		return parserFunctionFactory.createCallOperand(parserFunction);
	}

	/**
	 * Returns operand to reference a list by value
	 * @return Operand object
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Operand getListOperand() {
		if (listOperand != null)
		    return new ReferenceOperand(listOperand);
		ParserAssembler parserAssembler = compiler.getParserAssembler();
		ListParserRunner runner = new ListParserRunner(qname);
		runner.run(parserAssembler);
		ItemList<?> itemList = runner.getItemList();
		if (itemList !=null)
		    return new ReferenceOperand(new ListOperand(itemList));
		throw new CompilerException(String.format("List %s not found", qname.toString()));
	}

	@Override
	public QualifiedName getQname() {
		return qname;
	}

	@Override
	public VariableSpec getVarSpec() {
		return varSpec;
	}

	/**
	 * Set assignment operator ("=" or reflexive eg. "+=")
	 * @param operator Assignment operator
	 */
	@Override
	public void setOperator(String operator) {
		this.operator = operator;
	}

	@Override
	public void setLiteral(IOperand literal) {
		if (operator.equals("="))
		    this.literal = (Operand)literal;
		else
			expression = (Operand)literal;
	}

	@Override
	public void setExpression(IOperand expression) {
		Operand operand = (Operand)expression;
	  	this.expression = operand;
	}

	@Override
	public void setParameterTemplate(ITemplate parameterTemplate) {
		this.parameterTemplate = parameterTemplate;
		hasCallParameters = true;
	}
	
	/**
	 * Assign given axiom list to this term
	 * @param axiomQname Axiom key
	 * @param initializeTemplate Template to evaluate the axiom list
	 */
	@Override
	public void assignAxiomList(QualifiedName axiomName, ITemplate initializeTemplate) {
		if (axiomName == QualifiedName.ANONYMOUS) {
			if (axiomQname != null)
				axiomName = axiomQname;
			else
			    axiomName = new QualifiedName("anon." + QualifiedName.ANONYMOUS.incrementReferenceCount());
		}
		AxiomList aiomList = new AxiomList(axiomName, axiomName);
        AxiomListEvaluator axiomListEvaluator = 
        	new AxiomListEvaluator(aiomList, Collections.singletonList((Template)initializeTemplate), null);
	    expression = new AxiomOperand(axiomListEvaluator, null);
	}

	@Override
	public void reflexiveAssign(String operator, IOperand expression) {
		this.operator = operator;
		this.expression = (Operand)expression;
	}
	

}
