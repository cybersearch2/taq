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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.TemplateAssembler;
import au.com.cybersearch2.taq.compile.TemplateType;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.NameParser;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.scope.ScopeManager;

/**
 * ScopeTest
 * @author Andrew Bowley
 * 16 Feb 2015
 */
public class ScopeTest 
{
	static Map<QualifiedName, List<LocaleAxiomListener>> EMPTY_AXIOM_LISTENER_MAP = Collections.emptyMap();
	
	private static final String AXIOM_KEY = "AxiomKey";
	private static final String TEMPLATE_NAME = "TemplateName";
	private static final String SCOPE_NAME = "ScopeName";
    static QualifiedName Q_AXIOM_NAME = new QualifiedName(SCOPE_NAME, AXIOM_KEY);
    static QualifiedName Q_AXIOM1_NAME = new QualifiedName(SCOPE_NAME, AXIOM_KEY + 1);
    static QualifiedName Q_TEMPLATE_NAME = new QualifiedTemplateName(SCOPE_NAME, TEMPLATE_NAME);
    static QualifiedName GLOBAL_Q_AXIOM_NAME = new QualifiedName(QualifiedName.EMPTY, AXIOM_KEY);
    static QualifiedName GLOBAL_Q_TEMPLATE_NAME = new QualifiedTemplateName(QualifiedName.EMPTY, TEMPLATE_NAME);

	@Test
	public void test_global_scope_precedence()
	{
		Scope globalScope = mock(Scope.class);
		ScopeManager scopeManager = mock(ScopeManager.class);
		when(scopeManager.getGlobalScope()).thenReturn(globalScope);
		Scope scope = new Scope(scopeManager, SCOPE_NAME, Scope.EMPTY_PROPERTIES);
		when(scopeManager.getScopeByName(SCOPE_NAME)).thenReturn(scope);
		ParserAssembler globalParserAssembler = mock(ParserAssembler.class);
        TemplateAssembler templateAssembler = mock(TemplateAssembler.class);
        when(globalParserAssembler.getTemplateAssembler()).thenReturn(templateAssembler);
        when(globalScope.getName()).thenReturn(NameParser.GLOBAL_SCOPE);
		when(globalScope.getParserAssembler()).thenReturn(globalParserAssembler);
		scope.getParserAssembler().getListAssembler().createAxiomItemList(Q_AXIOM_NAME, false);
		scope.getParserAssembler().getAxiomAssembler().addAxiom(Q_AXIOM_NAME, new Parameter("x"));
		scope.getParserAssembler().getAxiomAssembler().saveAxiom(Q_AXIOM_NAME);
		scope.getParserAssembler().getTemplateAssembler().createTemplate(new QualifiedTemplateName(SCOPE_NAME, TEMPLATE_NAME), TemplateType.template);
		assertThat(scope.getAxiomSource(Q_AXIOM_NAME).iterator(null).next()).isNotNull();
		when(scopeManager.getScope("ScopeName")).thenReturn(scope);
		assertThat(scope.findTemplate(Q_TEMPLATE_NAME)).isNotNull();
		verify(globalParserAssembler, times(0)).getAxiomSource(Q_AXIOM_NAME);
		verify(templateAssembler, times(0)).getTemplate(new QualifiedTemplateName(QualifiedName.EMPTY, TEMPLATE_NAME));
	}

	@Test
	public void test_get_null_AxiomListenerMap()
	{
		Scope globalScope = mock(Scope.class);
		ScopeManager scopeManager = mock(ScopeManager.class);
		when(scopeManager.getGlobalScope()).thenReturn(globalScope);
        ParserAssembler parserAssembler = mock(ParserAssembler.class);
        ListAssembler listAssembler = mock(ListAssembler.class);
        when(parserAssembler.getListAssembler()).thenReturn(listAssembler);
        when(listAssembler.getAxiomListenerMap()).thenReturn(EMPTY_AXIOM_LISTENER_MAP);
        when(globalScope.getParserAssembler()).thenReturn(parserAssembler);
        Scope scope = new Scope(scopeManager, SCOPE_NAME, Scope.EMPTY_PROPERTIES);
		assertThat(scope.getAxiomListenerMap()).isNull();
	}
		
	@Test
	public void test_get_global_only_AxiomListenerMap()
	{
		Scope globalScope = mock(Scope.class);
		ScopeManager scopeManager = mock(ScopeManager.class);
		when(scopeManager.getGlobalScope()).thenReturn(globalScope);
		ParserAssembler globalParserAssembler = mock(ParserAssembler.class);
		when(globalScope.getParserAssembler()).thenReturn(globalParserAssembler);
		Map<QualifiedName, List<LocaleAxiomListener>> axiomListenerMap = new HashMap<>();
		LocaleAxiomListener axiomListener = mock(LocaleAxiomListener.class);
		axiomListenerMap.put(Q_AXIOM_NAME, Collections.singletonList(axiomListener));
        ListAssembler listAssembler = mock(ListAssembler.class);
        when(globalParserAssembler.getListAssembler()).thenReturn(listAssembler);
        when(listAssembler.getAxiomListenerMap()).thenReturn(EMPTY_AXIOM_LISTENER_MAP);
		when(listAssembler.getAxiomListenerMap()).thenReturn(axiomListenerMap);
        Scope scope = new Scope(scopeManager, SCOPE_NAME, Scope.EMPTY_PROPERTIES);
		assertThat(scope.getAxiomListenerMap().get(Q_AXIOM_NAME).get(0)).isEqualTo(axiomListener);
	}

}
