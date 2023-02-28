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
package au.com.cybersearch2.taq.compile;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compiler.CompilerException;
import au.com.cybersearch2.taq.language.QualifiedName;

/** 
 * External or internal Library defined by qualified name and function scope 
 */
public class Library {
    /** Qualified name used to identify the library */
    private final QualifiedName qname;
    /** Scope in which library calls will be made */
    private Scope functionScope;
    /** Library name */
    private String name;

    /**
     * Construct Library object
     * @param qname Qualified name used to identify the library
     * @param scope Calling scope
     */
	public Library(QualifiedName qname, Scope scope) {
		this.qname = qname;
		// Library name is first part of 2=part name
		name = qname.getTemplate();
        if (name.isEmpty()) {
            name = qname.getScope();
            if (name.isEmpty())
				name = QueryProgram.GLOBAL_SCOPE;
            functionScope = scope.findScope(name);
        } else {
            functionScope = scope.findScope(name);
		    if (functionScope == null) 
        	    functionScope = scope;
        }
	    if (functionScope == null) 
			throw new CompilerException(String.format("Scope \"%s\" not found", name));
 	}

	public String getName() {
		return name;
	}

	public Scope getFunctionScope(Scope callerScope) {
		return functionScope;
	}

	public QualifiedName getQname() {
		return qname;
	}

}
