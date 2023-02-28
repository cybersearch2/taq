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

import au.com.cybersearch2.taq.artifact.NestedFlowArtifact;
import au.com.cybersearch2.taq.artifact.TemplateArtifact;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.language.ITemplate;

/**
 * Helper for nested flow production. Creates inner template parser object and
 * holds outer template parser reference
 */
public class ParserNestedFlow implements NestedFlowArtifact {

	/** Inner template creation data */
	private final ParserTemplate parserTemplate;
	/** Outer template creation data */
	private final ParserTemplate outerTemplate;
	/** Inner template */
	private final ITemplate template;
	/** Flag set true if the inner flow executes one time only */
	private final boolean runOnce;

	/**
	 * Construct NestedFlow object 
	 * @param compiler Compiler agent
	 * @param outerTemplate Creation data of enclosing template 
	 * @param runOnce Flag set true if the inner flow executes one time only
	 */
	public ParserNestedFlow(Compiler compiler, TemplateArtifact outerTemplate, boolean runOnce) {
		this.outerTemplate = (ParserTemplate)outerTemplate;
		this.runOnce = runOnce;
		ParserAssembler parserAssembler = compiler.getParserAssembler();
		template = parserAssembler.getTemplateAssembler().chainTemplate(this.outerTemplate.getTemplate());
		parserTemplate = new ParserTemplate(compiler, template);
	}

	public ParserTemplate getOuterTemplate() {
		return outerTemplate;
	}

	@Override
	public ITemplate getTemplate() {
		return template;
	}

	@Override
	public boolean isRunOnce() {
		return runOnce;
	}

	@Override
	public ParserTemplate getParserTemplate() {
		return parserTemplate;
	}
}
