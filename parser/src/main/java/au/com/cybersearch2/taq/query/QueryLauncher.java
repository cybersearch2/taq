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

import java.util.List;

import au.com.cybersearch2.taq.QueryParams;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.QueryType;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * QueryLauncher
 * @author Andrew Bowley
 * 1 Aug 2015
 */
abstract public class QueryLauncher
{
    /**
     * Execute query by specification
     * @param queryParams Query parameters
     */
    public void launch(QueryParams queryParams)
    {
        Scope scope = queryParams.getScope();
        QuerySpec querySpec = queryParams.getQuerySpec();
        SolutionHandler solutionHandler = queryParams.getSolutionHandler();
        ChainQueryExecuter headQuery = null;
        queryParams.initialize();
        boolean isCalculation = querySpec.getQueryType() == QueryType.calculator;
        if (!isCalculation || queryParams.hasAxiomSource()) {
            headQuery = new LogicQueryExecuter(queryParams);
            isCalculation = false;
            KeyName keyName = querySpec.getKeyNameList().get(0);
            List<Term> properties = querySpec.getProperties(keyName.getTemplateName().toString());
            if (properties != null)
            	((LogicQueryExecuter)headQuery).setProperties(properties);
        }
        else
        {   // QueryParams need to be initialized to set up parameter axioms
            headQuery = new ChainQueryExecuter(queryParams);
            if (queryParams.hasInitialSolution())
                headQuery.setSolution(queryParams.getInitialSolution());
            else
                headQuery.setSolution(new Solution(scope.getLocale()));
            chainCalculator(queryParams, querySpec, headQuery);
        }
        // Chained queries are optional
        if (querySpec.getQueryChainList() != null)
            for (QuerySpec chainQuerySpec: querySpec.getQueryChainList())
            {
                if (chainQuerySpec.getQueryType() == QueryType.calculator) 
                {
                    chainCalculator(queryParams, chainQuerySpec, headQuery);
                }
                else
                {
                    int keynameCount = chainQuerySpec.getKeyNameList().size();
                    if (keynameCount != 1)
                        throw new IllegalArgumentException("Logic chain query with " + keynameCount + " parts not allowed");
                    QueryParams chainQueryParams = new QueryParams(scope, chainQuerySpec);
                    chainQueryParams.initialize();
                    Template template = chainQueryParams.getTemplateList().get(0);
                    String scopeName = template.getQualifiedName().getScope();
                    Scope templateScope = queryParams.getScope();
                    if (!scopeName.isEmpty()) {
                        templateScope = queryParams.getScope().findScope(scopeName);
                        headQuery.addAxiomListeners(templateScope.getAxiomListenerMap());
                    }
                    ScopeNotifier scopeNotifier = getScopeNotification(headQuery, queryParams.getScope(), templateScope);
                    headQuery.chain(chainQueryParams.getAxiomCollection(), template, scopeNotifier);
                }
            }
        Solution solution = headQuery.getSolution();
        solution.setSolutionHandler(solutionHandler);
        while (headQuery.execute())
        {
            if ((solution.evaluate() == EvaluationStatus.SHORT_CIRCUIT) || isCalculation)
                break;
        }
        // Reset all query templates so they can be recycled
        headQuery.backup(isCalculation);
    }

    /**
     * Returns key name from Calculator query specification
     * @param querySpec Query specification
     * @return KyeName object
     * @throws IllegalArgumentException if not exactly 1 key name specified
     */
    public KeyName getCalculatorKeyName(QuerySpec querySpec)
    {
        List<KeyName> keyNameList = querySpec.getKeyNameList();
        return keyNameList.get(keyNameList.size() - 1);
    }

    /**
     * Add calculator to query chain
     * @param queryParams Query parameters
     * @param chainQuerySpec Query specification
     * @param headQuery Head of query chain
     * @return CalculateChainQuery object
     */
    protected CalculateChainQuery chainCalculator(QueryParams queryParams, QuerySpec chainQuerySpec, ChainQueryExecuter headQuery)
    {   // Calculator uses a single template
        KeyName keyName = getCalculatorKeyName(chainQuerySpec);
        String scopeName = keyName.getTemplateName().getScope();
        Scope templateScope = queryParams.getScope();
        Template calculatorTemplate = 
            templateScope.findTemplate(getCalculatorKeyName(chainQuerySpec).getTemplateName());
        boolean isReplicate = calculatorTemplate.isReplicate();
        if (!scopeName.isEmpty()) 
            templateScope = queryParams.getScope().findScope(scopeName);
        Axiom calculatorAxiom = queryParams.getParameter(calculatorTemplate.getQualifiedName());
        if (calculatorAxiom == null)
            calculatorAxiom = getCalculatorAxiom(queryParams.getScope(), chainQuerySpec);
        ScopeNotifier scopeNotifier = getScopeNotification(headQuery, queryParams.getScope(), templateScope);
        CalculateChainQuery chainQuery = headQuery.chainCalculator(calculatorAxiom, calculatorTemplate, scopeNotifier);
        QualifiedName templateName = keyName.getTemplateName();
        String propertyKey = isReplicate ? templateName.getTemplate() : templateName.toString();
        List<Term> properties = chainQuerySpec.getProperties(propertyKey);
        if (properties != null)
            chainQuery.setProperties(properties);
        return chainQuery;
    }

    /**
     * Returns Calculator axiom from supplied scope
     * @param scope Scope
     * @param querySpec QuerySpec
     * @return Axiom object
     */
    protected Axiom getCalculatorAxiom(Scope scope, QuerySpec querySpec)
    {
        QualifiedName axiomQualifiedName = getCalculatorKeyName(querySpec).getAxiomKey();
        if (!axiomQualifiedName.getName().isEmpty())
        {
            Axiom axiom = null;
            AxiomSource source = scope.findAxiomSource(axiomQualifiedName);
            if (source == null)
                // Return empty axiom as placeholder for axiom to come from solution
                axiom = new Axiom(axiomQualifiedName.getName());
            else
                axiom = source.iterator(null).next();
            return axiom;  
        }
        return null;
    }

    /**
     * Returns scope notifier for given parameters
     * @param headQuery Head query
     * @param queryScope Query scope
     * @param templateScope Template scope
     * @return ScopeNotifier object
     */
    static protected ScopeNotifier getScopeNotification(ChainQueryExecuter headQuery, Scope queryScope, Scope templateScope)
    {
        // Allow a global scope query to engage multiple scopes using 
        // first part of 2-part names to identify scope
        ScopeNotifier scopeNotifier = null;
        if (!templateScope.getName().equals(queryScope.getName()))
            // Create object to pre-execute scope localisation
            scopeNotifier = new ScopeNotifier(headQuery, templateScope);
        return scopeNotifier;
    }

}
