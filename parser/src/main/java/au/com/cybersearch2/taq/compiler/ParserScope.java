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
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.ScopeArtifact;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.InitialProperties;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Parser helper for creating scope artifacts
 */
public class ParserScope implements ScopeArtifact {

	/** Parser context */
	private final ParserContext parserContext;
	private ParserListFactory parserListFactory;

	/**
	 * Construct ParserScope object
	 * @param parserContext Parser context
	 */
	public ParserScope(ParserContext parserContext, ParserListFactory parserListFactory) {
		this.parserContext = parserContext;
		this.parserListFactory = parserListFactory;
	}
	
    /**
     * Switch to global scope
     */
	@Override
    public void resetScope() {
    	parserContext.resetScope();
    }

    /**
     * Process scope reference production
     * @param listReference Scope reference
     * @return Operand object
     */
	@Override
    public Operand createScopeParam(ListReference listReference) {
        if (listReference.dimension() > 1)
            throw new CompilerException("Scope cannot be accessed using axiom list variable");
        return parserListFactory.listReference(new QualifiedName(parserContext.getScope().getName(), "scope"), listReference);
    }

    /**
     * Returns an operand which references a scope parameter
     * @param listReference Reference to an axiom term list
     * @return Operand object
     */
	@Override
    public Operand createScopeTerm(ListReference listReference) {
        if (listReference.dimension() > 1)
            throw new CompilerException("Scope cannot be accessed using axiom list variable");
    	ListItemSpec[] indexExpression = 
    			parserListFactory.indexExpression(new QualifiedName("scope"), listReference);
       return parserListFactory.listItemOperand(null, indexExpression[0], null);
    }

    /**
     * Returns Scope given it's name and optional properties
     * @param name Scope name
     * @param properties Optional properties
     */
    @Override
    public void createScope(String name, InitialProperties properties) {
        Scope scope = getQueryProgram().scopeInstance(name, properties);
        parserContext.setScope(scope);
    }

    /**
     * Returns Query Program
     * @return QueryProgram object
     */
	private QueryProgram getQueryProgram() {
	    return parserContext.getQueryProgram();
    }
}
