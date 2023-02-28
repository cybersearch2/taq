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
package au.com.cybersearch2.taq.axiom;

import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * Updates "scope" list value on change of scope
 * @author Andrew Bowley
 * 29May,2017
 */
public class ScopePropertiesListener
        implements
            LocaleListener,
            ParserRunner
{
    protected QualifiedName qualifiedAxiomName;
    protected LocaleAxiomListener axiomListener;
    
    /**
     * Construct ScopePropertiesListener object
     * @param qualifiedAxiomName Qualified axiom name
     * @param axiomListener Axiom listener
     */
    public ScopePropertiesListener(QualifiedName qualifiedAxiomName, LocaleAxiomListener axiomListener)
    {
        this.qualifiedAxiomName = qualifiedAxiomName;
        this.axiomListener = axiomListener;
    }

    @Override
    public boolean onScopeChange(Scope scope) 
    {
        Scope globalScope = scope.getGlobalScope();
        // Register locale listener with Global scope in which all scope property axioms must be declared
        ParserAssembler parserAssembler = globalScope.getParserAssembler();
        Axiom scopePropsAxiom = null;
        QualifiedName qname = new QualifiedName(scope.getName(), qualifiedAxiomName.getName());
        AxiomSource axiomSource = scope.getParserAssembler().getAxiomSource(qname);
        if (axiomSource == null)
            axiomSource = parserAssembler.getAxiomSource(qname);
        if (axiomSource == null)
        {
            axiomSource = scope.getGlobalParserAssembler().getAxiomSource(qualifiedAxiomName);
            if (axiomSource != null)
                scopePropsAxiom = createUnknownAxiom(qname.toString(), axiomSource.getArchetype().getTermNameList());
            else if (scope.getName().equals(QueryProgram.GLOBAL_SCOPE))
                return true; // This is not an error when global scope context is being reset
            else
                throw new ExpressionException("Axiom source \"" + qualifiedAxiomName.toString() + "\" not found");
        }
        if (scopePropsAxiom == null)
        {
            Iterator<Axiom> iterator = axiomSource.iterator(null);
            if (iterator.hasNext())
                scopePropsAxiom = iterator.next();
            else
                scopePropsAxiom = createUnknownAxiom(qname.toString(), axiomSource.getArchetype().getTermNameList());
        }
        return true;
    }

    @Override
    public void run(ParserAssembler parserAssembler)
    {
        Scope scope = parserAssembler.getScope();
        for (String scopeName: scope.getScopeNames())
	        if (!scopeName.equals(QueryProgram.GLOBAL_SCOPE))
	            onScopeChange(scope.getScope(scopeName));
        // This is global scope as it is the only one to have a parser task
        onScopeChange(scope);
    }

    /**
     * Create placeholder axiom with "unknown" items
     * @param axiomName
     * @param termNameList List of term names
     * @return Axiom object
     */
    private Axiom createUnknownAxiom(String axiomName, List<String> termNameList)
    {
        Axiom axiom = new Axiom(axiomName);
        Unknown unknown = new Unknown();
        for (String termName: termNameList)
            axiom.addTerm(new Parameter(termName, unknown));
        return axiom;
    }


}
