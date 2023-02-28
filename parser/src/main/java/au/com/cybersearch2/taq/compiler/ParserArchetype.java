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

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.artifact.ArchetypeArtifact;
import au.com.cybersearch2.taq.compile.TemplateType;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;

/**
 * Collects template archetype content 
 * 
 */
public class ParserArchetype implements ArchetypeArtifact {

	/** Template which hosts archetype */
	private final Template template;
	/** Flag set true if archetype is for axiom list, otherwise is for axiom term list */
	private final boolean isList;
	/** Compiler object */
	private final Compiler compiler;
	/** Qualified axiom name derived from template name */
	private final QualifiedName qualifiedAxiomName;
	/** Creates template terms */
	private final ParserTermFactory parserTermFactory;
	
	/**
	 * Construct ParserArchetype object
	 * @param name Name of archetype
	 * @param isList Flag set true if archetype is for axiom list, otherwise is for axiom term list
	 * @param compiler Compiler object
	 */
	public ParserArchetype(String name, boolean isList, Compiler compiler) {
		this.isList = isList;
		this.compiler = compiler;
		qualifiedAxiomName = new QualifiedName(QueryProgram.GLOBAL_SCOPE, QualifiedName.EMPTY, name);
		QualifiedTemplateName qualifiedTemplateName = new QualifiedTemplateName(QueryProgram.GLOBAL_SCOPE, name);
		template = compiler.getParserAssembler()
				           .getTemplateAssembler()
				           .createTemplate(qualifiedTemplateName, TemplateType.template);
		((TemplateArchetype)template.getArchetype()).setOperandType(isList ? OperandType.AXIOM : OperandType.TERM);
		parserTermFactory = new ParserTermFactory(compiler.getParserContext());
	}

	public ITemplate getTemplate() {
		return template;
	}

	/**
	 * Returns flag set true if archetype is for axiom list, otherwise is for axiom term list
	 * @return boolean
	 */
	public boolean isList() {
		return isList;
	}

	/**
	 * Returns qualified axiom name derived from template name 
	 * @return QualifiedName object
	 */
	@Override
	public QualifiedName getQualifiedAxiomName() {
		return qualifiedAxiomName;
	}

	/**
	 * Add a typed variable to the template which hosts archetype
	 * @param varSpec Variable type
	 * @param name Variable name
	 */
	@Override
	public void addTerm(IVariableSpec varSpec, String name) {
		ParserTerm parserTerm = new ParserTerm(compiler, varSpec, new QualifiedName(name, template.getQualifiedName()), null);
		Operand term = parserTermFactory.templateTerm(parserTerm);
		template.addTerm(term);
	}
}
