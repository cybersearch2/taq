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

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import au.com.cybersearch2.taq.compile.TemplateType;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;
import au.com.cybersearch2.taq.service.LoopMonitor;

@RunWith(MockitoJUnitRunner.class)
public class TestTemplateOperand {

	private static final String SCOPE_NAME = "ScopeName";
	private static final String TEMPLATE_NAME = "TemplateName";
	private static final int ID = 17;
	
	protected QualifiedName Q_TEMPLATE_NAME = new QualifiedTemplateName(SCOPE_NAME ,TEMPLATE_NAME);

	@Mock
	Template template;
	@Mock
	ExecutionContext executionContext;
	@Mock
	LoopMonitor loopMonitor;
	
	@Test
	public void evaluateRunOnceTest() {
		when(template.getQualifiedName()).thenReturn(Q_TEMPLATE_NAME);
		TemplateOperand templateOperand = new TemplateOperand(template, true);
		QualifiedName qname = templateOperand.getQualifiedName();
		assertThat(qname.getName()).isEqualTo("TemplateName_run_once");
		templateOperand.setExecutionContext(executionContext);
		when(template.evaluate(executionContext)).thenReturn(EvaluationStatus.COMPLETE);
		when(template.getNext()).thenReturn(null);
		assertThat(templateOperand.evaluate(ID)).isEqualTo(EvaluationStatus.COMPLETE);
		verify(template).backup(false);
		assertThat(templateOperand.getValue()).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void evaluateLoopThriceTest() {
		when(template.getQualifiedName()).thenReturn(Q_TEMPLATE_NAME);
		TemplateOperand templateOperand = new TemplateOperand(template, false);
		QualifiedName qname = templateOperand.getQualifiedName();
		assertThat(qname.getName()).isEqualTo("TemplateName_loop");
		templateOperand.setExecutionContext(executionContext);
		when(executionContext.getLoopMonitor()).thenReturn(loopMonitor);
		when(loopMonitor.tick()).thenReturn(true);
		when(template.evaluate(executionContext))
		    .thenReturn(EvaluationStatus.COMPLETE)
		    .thenReturn(EvaluationStatus.COMPLETE)
			.thenReturn(EvaluationStatus.SHORT_CIRCUIT);
		when(template.getNext()).thenReturn(null);
		assertThat(templateOperand.evaluate(ID)).isEqualTo(EvaluationStatus.COMPLETE);
		verify(loopMonitor).push();
		verify(template, times(2)).backup(true);
		verify(template, atLeast(1)).backup(false);
		verify(loopMonitor, times(3)).tick();
		verify(loopMonitor).pop();
		assertThat(templateOperand.getValue()).isEqualTo(Boolean.TRUE);
	}
	
	@Test
	public void loopTimeoutTest() {
		QualifiedName outerName = new QualifiedTemplateName("global", "outer");
		TemplateArchetype templateArchetype = new TemplateArchetype(outerName);
		Template outerTemplate = new Template(templateArchetype);
		Template innerTemplate = outerTemplate.innerTemplateInstance(TemplateType.calculator);
		TemplateOperand templateOperand = new TemplateOperand(innerTemplate);
		innerTemplate.addTerm(new SlowOperand("tortoise", 16));
		outerTemplate.addTerm(templateOperand);
		ExecutionContext executionContext = new ExecutionContext();
		executionContext.setLoopTimeout(2);
	    assertThat(outerTemplate.evaluate(executionContext)).isEqualTo(EvaluationStatus.FAIL);
	}

	@Test
	public void nestedLoopTimeoutTest() {
		QualifiedName outerName = new QualifiedTemplateName("global", "outer");
		TemplateArchetype templateArchetype = new TemplateArchetype(outerName);
		Template outerTemplate = new Template(templateArchetype);
		Template innerTemplate1 = outerTemplate.innerTemplateInstance(TemplateType.calculator);
		TemplateOperand templateOperand1 = new TemplateOperand(innerTemplate1);
		Template innerTemplate2 = outerTemplate.innerTemplateInstance(TemplateType.calculator);
		TemplateOperand templateOperand2 = new TemplateOperand(innerTemplate2);
		innerTemplate1.addTerm(templateOperand2);
		innerTemplate2.addTerm(new SlowOperand("tortoise", 16));
		outerTemplate.addTerm(templateOperand1);
		ExecutionContext executionContext = new ExecutionContext();
		executionContext.setLoopTimeout(2);
	    assertThat(outerTemplate.evaluate(executionContext)).isEqualTo(EvaluationStatus.FAIL);
	}
	
	@Test
	public void nestedSlowLoopTimeoutTest() {
		QualifiedName outerName = new QualifiedTemplateName("global", "outer");
		TemplateArchetype templateArchetype = new TemplateArchetype(outerName);
		Template outerTemplate = new Template(templateArchetype);
		Template innerTemplate1 = outerTemplate.innerTemplateInstance(TemplateType.calculator);
		TemplateOperand templateOperand1 = new TemplateOperand(innerTemplate1);
		Template innerTemplate2 = outerTemplate.innerTemplateInstance(TemplateType.calculator);
		TemplateOperand templateOperand2 = new TemplateOperand(innerTemplate2);
		innerTemplate1.addTerm(new SlowOperand("tortoise", 16));
		innerTemplate1.addTerm(templateOperand2);
		innerTemplate2.addTerm(new SlowOperand("tortoise", 4));
		outerTemplate.addTerm(templateOperand1);
		ExecutionContext executionContext = new ExecutionContext();
		executionContext.setLoopTimeout(2);
	    assertThat(outerTemplate.evaluate(executionContext)).isEqualTo(EvaluationStatus.FAIL);
	}
}
