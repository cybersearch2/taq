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
package au.com.cybersearch2.taq.artifact;

import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.InitialProperties;
import au.com.cybersearch2.taq.language.ListReference;

public interface ScopeArtifact {

    /**
     * Switch to global scope
     */
    void resetScope();

    /**
     * Process scope reference production
     * @param listReference Scope reference
     * @return IOperand object
     */
    IOperand createScopeParam(ListReference listReference);

    /**
     * Returns an operand which references a scope parameter
     * @param listReference Reference to an axiom term list
     * @return IOperand object
     */
    IOperand createScopeTerm(ListReference listReference);

    /**
     * Returns Scope given it's name and optional properties
     * @param name Scope name
     * @param properties Optional properties
     */
    void createScope(String name, InitialProperties properties);
    
}
