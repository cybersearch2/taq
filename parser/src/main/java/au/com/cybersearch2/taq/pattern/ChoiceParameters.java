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
package au.com.cybersearch2.taq.pattern;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Choice runtime parameters
 */
public class ChoiceParameters {

	private final Scope scope;
    /** Qualified name of template which contains this choice */
    private final QualifiedName contextName;
    /** List of operands, with each operand representing the selection term of one row */
	private final List<Operand> variableList;
	/** Selection term if the choice is declared at scope level */
    private Operand scopeOperand;
    /** Optional template to evaluate call parameters */
	private Template parameterTemplate;
	
    /**
     * Construct Choice object
     * @param scope Context scope
     * @param contextName Qualified name of template which contains this choice
     * @param operandList List of choice operands belonging to context template 
     */
	public ChoiceParameters(Scope scope, 
    		QualifiedName contextName, 
    		List<Operand> operandList) {
		this.scope = scope;
        this.contextName = contextName;
		// The variableList is populated from the operand map using axiom term name keys
		variableList = new ArrayList<Operand>();
        variableList.addAll(operandList);
	}

	public void setScopeOperand(Operand scopeOperand) {
		this.scopeOperand = scopeOperand;
	}

	public void setParameterTemplate(Template parameterTemplate) {
		this.parameterTemplate = parameterTemplate;
	}

	public Scope getScope() {
		return scope;
	}

	public QualifiedName getContextName() {
		return contextName;
	}

	public List<Operand> getVariableList() {
		return variableList;
	}

	public Operand getScopeOperand() {
		return scopeOperand;
	}

	public boolean hasScopeOperand() {
		return scopeOperand != null;
	}
	
	public Template getParameterTemplate() {
		return parameterTemplate;
	}

	public boolean hasParameterTemplate() {
		return parameterTemplate != null;
	}
}
