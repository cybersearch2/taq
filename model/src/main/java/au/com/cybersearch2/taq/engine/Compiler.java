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
package au.com.cybersearch2.taq.engine;

import au.com.cybersearch2.taq.artifact.ArchetypeArtifact;
import au.com.cybersearch2.taq.artifact.AxiomArtifact;
import au.com.cybersearch2.taq.artifact.ChoiceArtifact;
import au.com.cybersearch2.taq.artifact.ChoiceFactoryArtifact;
import au.com.cybersearch2.taq.artifact.ComplexArtifact;
import au.com.cybersearch2.taq.artifact.CursorArtifact;
import au.com.cybersearch2.taq.artifact.ExpressionArtifact;
import au.com.cybersearch2.taq.artifact.FunctionArtifact;
import au.com.cybersearch2.taq.artifact.FunctionFactoryArtifact;
import au.com.cybersearch2.taq.artifact.ListArtifact;
import au.com.cybersearch2.taq.artifact.ListFactoryArtifact;
import au.com.cybersearch2.taq.artifact.LiteralArtifact;
import au.com.cybersearch2.taq.artifact.NestedFlowArtifact;
import au.com.cybersearch2.taq.artifact.QueryArtifact;
import au.com.cybersearch2.taq.artifact.RegularExpressionArtifact;
import au.com.cybersearch2.taq.artifact.ResourceArtifact;
import au.com.cybersearch2.taq.artifact.ScopeArtifact;
import au.com.cybersearch2.taq.artifact.TemplateArtifact;
import au.com.cybersearch2.taq.artifact.TermArtifact;
import au.com.cybersearch2.taq.artifact.TermFactoryArtifact;
import au.com.cybersearch2.taq.artifact.VariableArtifact;
import au.com.cybersearch2.taq.language.ExpressionIndex;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.SyntaxException;
import au.com.cybersearch2.taq.language.TaqLiteral;

/** 
 * Compiler
 * 
 * Creates artifacts from productions of the JavaCC Expression Pattern Language parser. 
 * Provides utilities such as source tracking and handles creation of miscellaneous artifacts
 * while creating the remaining artifacts are delegated to dedicated "Parser" classes.
 * @author Andrew Bowley
 */
public interface Compiler
{
	/**
	 * Input current document of parser
	 */
	void compile();


	/**
	 * Returns object which collects source information for debugging
	 * @return SourceTracker object
	 */
	SourceTracker getSourceTracker();

    /**
     * Push current source marker on a stack and replace it 
     * without adding to list of source markers
     * @param nonTerminal Name of next parser non-terminal
     * @return SourceMarker object of next parser non-terminal
     */
    SourceMarker pushSourceMarker(String nonTerminal);
 
    /**
     * Pop source marker off stack and link to tail of previous source item
     * @return SourceMarker object popped off stack
     */
    SourceMarker popSourceMarker();

    /**
     * Add SourceItem object to current source marker
     * @param information Text information
     * @param unit Unit
     * @param extent extent Extent
     * @return SourceItem object
     */
	SourceItem addSourceItem(String information, Unit unit, Extent extent) ;

    /**
     * Append to current source item given extent and information to append
     * @param extent Extent
     * @param information Variable text parameters
     */
    void appendSourceItem(Extent extent, String... information);
    
    /**
     * Add SourceItem object for an operand to current source marker
     * @param var IOperand
     * @param unit Unit
     * @param extent Extent
     * @return SourceItem object
     */
	SourceItem addSourceVariable(IOperand var, Unit unit, Extent extent);
	
    /**
     * Create source marker for given non-terminal for case start token is pending.
     * @param nonTerminal Name of parser non-terminal
     * @return SourceMarker object
     */
    SourceMarker setSourceMarker(String nonTerminal);

	OperandType getOperandType(TaqLiteral literal);
	
	String type(OperandType operandType);
	
	// Parser non-terminals
 
	/**
	 * Process Name production
	 * @param name Identifier
	 * @param isContextName Flag set true to incorporate context details
	 * @param isDeclaration Is part of a declaration
	 * @return QualifiedName object
	 */
	QualifiedName name(String name, boolean isContextName, boolean isDeclaration) ;

	/**
	 * Process AxiomName production.
	 * The given axiom name is canonicalized if constructed from a multi-part string.
	 * @param axiomName Qualified name
	 * @return QualifiedName object
	 */
	QualifiedName axiomName(QualifiedName axiomName);

    /**
     * Include TAQ script from named resource
     * @param resourceName Name of file or other resource to include
     * @throws SyntaxException if an I/O error occurs
     */
    void includeResource(String resourceName) throws SyntaxException;

    /**
     * Process binary production, of which there are many, differing only by operator
     * @param left Left hand expression
     * @param operator Binary operator symbol
     * @param right Right hand expression
     * @return IOperand object
     */
    IOperand evaluationExpression(IOperand left, String operator, IOperand right);

    /**
     * Returns variable specification of context list of given name
     * @return VariableSpec object or null if there is no context list of that name
     */
    IVariableSpec getContextListSpec(String name);
    
    /**
     * Returns operand to create an axiom term
     * @param name Term name
     * @param expression Optional expression to set the term value
     * @param listReference Optional list item reference
     * @return IOperand object
     */
    IOperand termExpression(String name, IOperand expression, ListReference listReference);
    
    /**
     * Process InnerFlow production
     * @param template Inner template
     * @param runOnce Flag set true if branch, otherwise is loop
     * @return IOperand object
     */
    IOperand innerFlow(ITemplate template, boolean runOnce);

    /**
     * Returns template chained to current template in context
     * @return Template object
     */
	ITemplate chainTemplate();

	/**
	 * Returns list length operand
	 * @param listName Name of referenced list
	 * @return IOperand object
	 */
	IOperand createListLength(QualifiedName listName) ;

	/**
	 * Returns key to identify a function
	 * @param callName Name of function
	 * @return key
	 */
	String functionKey(String callName);

	boolean flagEnclosedIfEvaluator(IOperand operand);

	ExpressionIndex expressionIndexInstance(IOperand expression);
	
	TemplateArtifact templateArtifactInstance(ITemplate template);
	
	TemplateArtifact templateArtifactInstance(String name, boolean isCalculator, boolean returnsTerm);

	NestedFlowArtifact nestedFlowInstance(TemplateArtifact outerTemplate, boolean runOnce);

	ArchetypeArtifact archetypeArtifactInstance(String name, boolean isList);

	ResourceArtifact resourceArtifactInstance(String name);
	
	FunctionArtifact functionArtifactInstance(QualifiedName qname, String sourceName);

	ChoiceArtifact choiceArtifactInstance(String choiceName, boolean isMap);

	ListArtifact listArtifactInstance(String listName, IVariableSpec varSpec, boolean isExport);
	
	TermArtifact termArtifactInstance(IVariableSpec varSpec, QualifiedName qname, ListReference listReferenc);
	
	QueryArtifact getQueryArtifact();
	
	ScopeArtifact getScopeArtifact();
	
	ComplexArtifact getComplexArtifact();
	
	TermFactoryArtifact getTermFactoryArtifact();

	FunctionFactoryArtifact getFunctionFactoryArtifact();

	ChoiceFactoryArtifact getChoiceFactoryArtifact();

	LiteralArtifact getLiteralArtifact();

	VariableArtifact getVariableArtifact();
	
	ExpressionArtifact getExpressionArtifact();

	AxiomArtifact getAxiomInterface();

	ListFactoryArtifact getListFactoryArtifact();
	
	CursorArtifact getCursorArtifact();
	
	RegularExpressionArtifact getRegularExpressionArtifact();

	String getScopeName();
}
