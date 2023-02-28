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
package au.com.cybersearch2.taq.query;

import au.com.cybersearch2.taq.Scope;

/**
 * ScopeNotifier
 * @author Andrew Bowley
 * 30May,2017
 */
public class ScopeNotifier
{
    protected Scope scope;
    protected ChainQueryExecuter executer;
    
    public ScopeNotifier(ChainQueryExecuter executer, Scope scope)
    {
        this.scope = scope;
        this.executer = executer;
    }
    
    public Scope getScope() {
		return scope;
	}

	/**
     * Update scope listeners with locale details and
     * copy all axiom listeners in scope to this executer
     */
    public void notifyScopes()
    {
        executer.setAxiomListeners(scope); 
        executer.onScopeChange(scope);
        executer.bindAxiomListeners(scope);
    }

    /**
     * Notify a change of scope
     */
	public void onScopeChange() {
        executer.onScopeChange(scope);
	}

}
