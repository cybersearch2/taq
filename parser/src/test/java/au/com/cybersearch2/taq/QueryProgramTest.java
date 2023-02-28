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
package au.com.cybersearch2.taq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.TemplateType;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.InitialProperties;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.QueryType;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.Solution;

/**
 * QueryProgramTest
 * @author Andrew Bowley
 * 9 Jan 2015
 */
public class QueryProgramTest 
{
	private static final String AXIOM_KEY = "AxiomKey";
	protected QualifiedName Q_AXIOM_NAME = new QualifiedName(SCOPE_NAME, AXIOM_KEY);
	private static final String AXIOM_KEY2 = "AxiomKey2";
    protected QualifiedName Q_AXIOM_NAME2 = new QualifiedName(SCOPE_NAME, AXIOM_KEY2);
	private static final String TEMPLATE_NAME = "TemplateName";
	protected QualifiedName Q_TEMPLATE_NAME = new QualifiedTemplateName(SCOPE_NAME ,TEMPLATE_NAME);
	private static final String TEMPLATE_NAME2 = "TemplateName2";
    protected QualifiedName Q_TEMPLATE_NAME2 = new QualifiedTemplateName(SCOPE_NAME ,TEMPLATE_NAME2);
	private static final String OPERAND_NAME = "OperandName";
	private static final String SCOPE_NAME = "ScopeName";
	private static final String QUERY_SPEC_NAME = "QuerySpec";
	private static final String VARIABLE_NAME = "VariableName";
    protected QualifiedName Q_VARIABLE_NAME = new QualifiedName(SCOPE_NAME, VARIABLE_NAME);

	@Before
	public void setUp()
	{
 	}
	
	@Test
	public void test_constructor()
	{
		QueryProgram queryProgram = new QueryProgram();
		assertThat(queryProgram.getScopes()).isNotNull();
		assertThat(queryProgram.getGlobalScope()).isNotNull();
	}

	@Test
	public void test_globalScope()
	{
	    QualifiedName GLOBAL_Q_AXIOM_NAME = QualifiedName.parseGlobalName(AXIOM_KEY);
	    QualifiedName GLOBAL_Q_TEMPLATE_NAME = QualifiedName.parseTemplateName(TEMPLATE_NAME);
		QueryProgram queryProgram = new QueryProgram();
		ParserAssembler parserAssembler = queryProgram.getGlobalScope().getParserAssembler();
		parserAssembler.getListAssembler().createAxiomItemList(GLOBAL_Q_AXIOM_NAME, false);
		parserAssembler.getAxiomAssembler().addAxiom(GLOBAL_Q_AXIOM_NAME, new Parameter("x", Integer.valueOf(3)));
		parserAssembler.getAxiomAssembler().saveAxiom(GLOBAL_Q_AXIOM_NAME);
        OperandMap operandMap = parserAssembler.getOperandMap();
		parserAssembler.getTemplateAssembler().createTemplate(GLOBAL_Q_TEMPLATE_NAME, TemplateType.template);
		parserAssembler.setQualifiedContextname(GLOBAL_Q_TEMPLATE_NAME);
		ITemplate template = parserAssembler.getTemplateAssembler().getTemplate(GLOBAL_Q_TEMPLATE_NAME);
		operandMap.addOperand(OPERAND_NAME, parserAssembler.getQualifiedContextname());
		assertThat(parserAssembler.getAxiomSource(GLOBAL_Q_AXIOM_NAME)).isNotNull();
		assertThat(parserAssembler.getTemplateAssembler().getTemplate(GLOBAL_Q_TEMPLATE_NAME)).isEqualTo(template);
		assertThat(parserAssembler.getOperandMap().get(QualifiedName.parseGlobalName(TEMPLATE_NAME + "." + OPERAND_NAME))).isNotNull();
	}
	
	@Test
	public void test_new_scope()
	{
		QueryProgram queryProgram = new QueryProgram();
		InitialProperties properties = new InitialProperties();
		Scope scope = queryProgram.scopeInstance(SCOPE_NAME, properties);
		assertThat(queryProgram.getScope(SCOPE_NAME)).isEqualTo(scope);
		try
		{
			InitialProperties properties2 = new InitialProperties();
	        queryProgram.scopeInstance(SCOPE_NAME, properties2);
			failBecauseExceptionWasNotThrown(ExpressionException.class);
		}
		catch(ExpressionException e)
		{
			assertThat(e.getMessage()).isEqualTo("Scope named \"" + SCOPE_NAME + "\" already exists");
		}
        InitialProperties properties2 = new InitialProperties();
        Object value = new Object();
	    QualifiedName qname = QualifiedName.parseGlobalName(AXIOM_KEY);
        properties2.initializerDeclaration(qname, value);
        Scope globalScope = queryProgram.scopeInstance(QueryProgram.GLOBAL_SCOPE, properties2);
        ListAssembler listAssembler = globalScope.getParserAssembler().getListAssembler();
        AxiomTermList scopeAxiomList = listAssembler.getAxiomTermList(new QualifiedName("global", "scope"));
        assertThat(scopeAxiomList).isNotNull();
        Axiom scopeAxiom = scopeAxiomList.getAxiom();
        assertThat(scopeAxiom.getTermCount()).isEqualTo(3);
        Term term = scopeAxiom.getTermByName("AxiomKey");
        assertThat(term).isNotNull();
        assertThat(term.getValue()).isEqualTo(value);
        assertThat(scopeAxiom.getTermByName("language")).isNotNull();
        assertThat(scopeAxiom.getTermByName("region")).isNotNull();
	}
	
	@Test
	public void test_execute_calculate_no_axiom_chainquery()
	{
		QueryProgram queryProgram = new QueryProgram();
        Scope scope = queryProgram.scopeInstance(SCOPE_NAME, new InitialProperties());
		ParserAssembler parserAssembler = scope.getParserAssembler();
		QuerySpec querySpec = new QuerySpec(QUERY_SPEC_NAME, true);
		KeyName keyname = mock(KeyName.class);
		when(keyname.getAxiomKey()).thenReturn(Q_AXIOM_NAME);
		when(keyname.getTemplateName()).thenReturn(Q_TEMPLATE_NAME);
		parserAssembler.getListAssembler().createAxiomItemList(Q_AXIOM_NAME, false);
		parserAssembler.getTemplateAssembler().createTemplate(Q_TEMPLATE_NAME, TemplateType.template);
		Variable variable = new Variable(Q_VARIABLE_NAME);
		parserAssembler.getTemplateAssembler().addTemplate(Q_TEMPLATE_NAME, variable);
		Parameter param = new Parameter(VARIABLE_NAME, "eureka!");
		parserAssembler.getAxiomAssembler().addAxiom(Q_AXIOM_NAME, param);
		parserAssembler.getAxiomAssembler().saveAxiom(Q_AXIOM_NAME);
		parserAssembler.getOperandMap().addOperand(OPERAND_NAME, parserAssembler.getQualifiedContextname());
		querySpec.addKeyName(keyname);
		QuerySpec querySpec2 = querySpec.chain();
		KeyName keyname2 = mock(KeyName.class);
		when(keyname2.getAxiomKey()).thenReturn(new QualifiedName(""));
		when(keyname2.getTemplateName()).thenReturn(Q_TEMPLATE_NAME2);
		Variable variable2 = new Variable(QualifiedName.parseGlobalName(TEMPLATE_NAME + "." + VARIABLE_NAME));
        Variable variable3 = new Variable(QualifiedName.parseName(VARIABLE_NAME + "3"), variable2);
		parserAssembler.getTemplateAssembler().createTemplate(Q_TEMPLATE_NAME2, TemplateType.calculator);
		parserAssembler.getTemplateAssembler().addTemplate(Q_TEMPLATE_NAME2, variable3);
		querySpec2.addKeyName(keyname2);
		querySpec2.setQueryType(QueryType.calculator);
		scope.addQuerySpec(querySpec);
		SolutionHandler solutionHandler = new SolutionHandler(){

			@Override
			public boolean onSolution(Solution solution) 
			{
				assertThat(solution.getString(Q_TEMPLATE_NAME.toString(), VARIABLE_NAME)).isEqualTo("eureka!");
				assertThat(solution.getString(Q_TEMPLATE_NAME2.toString(), VARIABLE_NAME + "3")).isEqualTo("eureka!");
				return true;
			}};
		queryProgram.executeQuery(SCOPE_NAME, QUERY_SPEC_NAME, solutionHandler);
		
	}

	@Test
	public void test_execute_query_missing_scope()
	{
		SolutionHandler solutionHandler = mock(SolutionHandler.class);
		QueryProgram queryProgram = new QueryProgram();
		try
		{
			queryProgram.executeQuery("x", QUERY_SPEC_NAME, solutionHandler);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		}
		catch(IllegalArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("Scope \"x\" does not exist");
		}
	}

	@Test
	public void test_execute_query_missing_querySpec()
	{
		SolutionHandler solutionHandler = mock(SolutionHandler.class);
		QueryProgram queryProgram = new QueryProgram();
        queryProgram.scopeInstance(SCOPE_NAME, new InitialProperties());
		try
		{
			queryProgram.executeQuery(SCOPE_NAME, "x", solutionHandler);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		}
		catch(IllegalArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("Query \"x\" does not exist");
		}
	}

}
