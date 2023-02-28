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
package au.com.cybersearch2.taq.scope;

import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * ScopeContext
 * Information to restore a scope to it's original state plus axiom lists of the
 * QueryProgram which owns the context.
 * @author Andrew Bowley
 * 2 Mar 2015
 */
public class ScopeContext 
{
	protected Scope scope;
	/** Map of operands and values used to save and restore initial state */
    protected Map<Operand, Parameter> operandValueMap;
	/** Map of global operands and values used to save and restore initial state */
    protected Map<Operand, Parameter> globalOperandValueMap;
    /** Names of lists which are empty at time of object construction */
    protected List<QualifiedName> emptyListNames;
    /** Names of global lists which are empty at time of object construction */
    protected List<QualifiedName> emptyGlobalListNames;
    /** Flag to indicate function scope */
    protected boolean isFunctionScope;

	/**
	 * Construct ScopeContext object
	 * @param scope Scope
	 * @param isFunctionScope Flag set true if this is a function scope
	 */
	public ScopeContext(Scope scope, boolean isFunctionScope) 
	{
		this.scope = scope;
		this.isFunctionScope = isFunctionScope;
        ParserAssembler parserAssembler = scope.getParserAssembler();
        OperandMap operandMap = parserAssembler.getOperandMap();
		operandValueMap = operandMap.getOperandValues();
		emptyListNames = parserAssembler.getListAssembler().getEmptyListNames();
		if (!isFunctionScope && !QueryProgram.GLOBAL_SCOPE.equals(scope.getName()))
		{
			OperandMap globalOperandMap = scope.getGlobalScope().getParserAssembler().getOperandMap();
			globalOperandValueMap = globalOperandMap.getOperandValues();
	        emptyGlobalListNames = scope.getGlobalListAssembler().getEmptyListNames();
		}
	}

	/**
	 * Reset scope to initial state by clearing all operands and then assigning default values
	 */
	public void resetScope()
	{
	    ParserAssembler parserAssembler = scope.getParserAssembler();
		OperandMap operandMap = parserAssembler.getOperandMap();
		operandMap.setOperandValues(operandValueMap);
		parserAssembler.getListAssembler().clearLists(emptyListNames);
		parserAssembler.clearScopeAxioms();
		if (globalOperandValueMap != null)
		{
			OperandMap globalOperandMap = scope.getGlobalScope().getParserAssembler().getOperandMap();
			globalOperandMap.setOperandValues(operandValueMap);
			scope.getGlobalListAssembler().clearLists(emptyGlobalListNames);
			scope.getGlobalParserAssembler().clearScopeAxioms();
		}
		boolean isMultiScopes = scope.getScopeNames().size() > 1;
		if (isMultiScopes && QueryProgram.GLOBAL_SCOPE.equals(scope.getName())) {
			parserAssembler.changeScopeTemplate(scope);
			parserAssembler.notifyScopeChange(scope);
		}
	}


}
