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
package au.com.cybersearch2.taq.terms;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.ResourceHelper;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compiler.Compiler;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.model.TaqParser;

public class DecimalTermTest {

	private static String TERM_QUERY = 
		"decimal x = decimal(\"123.45\")";
	
    @Test
    public void testDecimalTerm() {
		ParserAssembler parserAssembler = openScript(TERM_QUERY);
		OperandMap operandMap = parserAssembler.getOperandMap();
        Operand x = operandMap.get(QualifiedName.parseGlobalName("x"));
        Operand decimal = x.getLeftOperand();
        decimal.setExecutionContext(new ExecutionContext());
        decimal.evaluate(1);
	    assertThat(decimal.getValue().toString()).isEqualTo("123.45");
    }

	public static ParserAssembler openScript(String script)
	{
		InputStream stream = new ByteArrayInputStream(script.getBytes());
		TaqParser queryParser = new TaqParser(stream);
		queryParser.enable_tracing();
		QueryProgram queryProgram = new QueryProgram();
		queryProgram.setResourceBase(ResourceHelper.getTestResourcePath());
		Compiler compiler = new Compiler(queryParser.publish(), new ParserContext(queryProgram));
		compiler.compile();
	    compiler.runPending();
        compiler = null;
		ParserAssembler parserAssembler = queryProgram.getGlobalScope().getParserAssembler();
		return parserAssembler;
	}
	
}
