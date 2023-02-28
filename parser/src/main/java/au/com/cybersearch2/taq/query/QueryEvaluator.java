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
import au.com.cybersearch2.taq.compile.Library;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.QueryType;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.provider.CallHandler;
import au.com.cybersearch2.taq.scope.ScopeContext;

/**
 * QueryEvaluator
 * Performs query call 
 * @author Andrew Bowley
 * 1 Aug 2015
 */
public class QueryEvaluator extends InnerQueryLauncher implements ParserRunner
{

    /** Call evaluator implementation */
    private class Evaluator extends CallHandler {
        
		protected Evaluator(String name) {
			super(name);
		}
		
		public void setSolutionAxiom(Axiom axiom) {
			solutionAxiom = axiom;
		}

	    /**
	     * evaluate
	     * @see au.com.cybersearch2.taq.provider.CallHandler#evaluate(java.util.List)
	     */
	    @Override
	    public boolean evaluate(List<Term> argumentList)
	    {
	    	return launchCalculatorQuery(argumentList);
	    }

    }
    
    /** Library defined by qualified name and function scope */
    private final Library library;
    /** Name used to identify query */
    private final String queryName;
    /** Object invoked to perform function */
    private final Evaluator callHandler;
     /** Query parameters */
    private QueryParams queryParams;
    /** Caller's scope */
    private Scope callerScope;
    /** Flag to indicate if call to Calculator in same scope */
    private boolean isCallInScope;
    /** Return list of null if not specified */
    private ItemList<?> itemList;
    private Runnable unresolvedHandler;
 
    /**
     * Construct a QueryEvaluator object
     * @param queryName Name used to identify query
     * @param library Library defined by qualified name and function scope
     */
    public QueryEvaluator(String queryName, Library library)
    {
    	super();
        this.queryName = queryName;
        this.library = library;
        callHandler = new Evaluator("query");
      }

	public Runnable getUnresolvedHondler() {
		return unresolvedHandler;
	}

	public void setUnresolvedHandler(Runnable unresolvedHandler) {
		this.unresolvedHandler = unresolvedHandler;
	}

	@Override
    public CallHandler getCallHandler() {
    	return callHandler;  
    }
 
    /**
     * Returns name of library
     * @return String
     */
    @Override
    public String getLibrayName()
    {
        return library.getName();
    }
    
    /**
     * @see au.com.cybersearch2.taq.interfaces.ParserRunner#run(au.com.cybersearch2.taq.compile.ParserAssembler)
     */
    @Override
    public void run(ParserAssembler parserAssembler)
    {
        callerScope = parserAssembler.getScope();
        // The queyName may be 2-part, so extract name component using QualifiedName utility
    	QualifiedName qualifiedCallName = QualifiedName.parseName(queryName);
    	Scope libraryScope = library.getFunctionScope(callerScope);
        QualifiedName qualifiedAxiomName = new QualifiedName(library.getName(), qualifiedCallName.getName());
        itemList = libraryScope.getParserAssembler().getListAssembler().findAxionContainer(qualifiedAxiomName);
        if (itemList != null)
            callHandler.setAxiomContainer((AxiomContainer)itemList);
        QuerySpec querySpec = libraryScope.getQuerySpec(qualifiedCallName.getName());
        if (querySpec == null)
        {
            QualifiedName qualifiedTemplateName = new QualifiedTemplateName(library.getName(), qualifiedCallName.getName());
            if (libraryScope == callerScope)
            {
                if (parserAssembler.getTemplateAssembler().getTemplate(qualifiedTemplateName) == null)
                {
                    qualifiedTemplateName = new QualifiedTemplateName(QualifiedName.EMPTY, qualifiedCallName.getName());
                    if (parserAssembler.getScope().getGlobalTemplateAssembler().getTemplate(qualifiedTemplateName) == null) {
                    	if (unresolvedHandler != null)
                    	    unresolvedHandler.run();
                    	else
                            throw new ExpressionException("Query \"" + qualifiedCallName.getName() + "\" not found in scope \"" + library.getName() + "\"");
                    }
                }
            }
            String callName = library.getName() + "." + qualifiedCallName.getName();
            querySpec = new QuerySpec(callName, false);
            querySpec.addKeyName(new KeyName(QualifiedName.ANONYMOUS, qualifiedTemplateName));
            querySpec.setQueryType(QueryType.calculator);
        }
        queryParams = new QueryParams(libraryScope, querySpec);
        isCallInScope = queryParams.getScope() == callerScope;
    }

    /**
     * Launch calculator query
     * @param argumentList Call arguments consisting of List of terms
     */
    protected boolean launchCalculatorQuery(List<Term> argumentList)
    {
    	if (queryParams == null)
    		// This happens when an unresolved function call is made in a scope template
            throw new ExpressionException("Function \"" + queryName + "\" not found in library \"" + library.getName() + "\"");
        QuerySpec querySpec = queryParams.getQuerySpec();
        QualifiedName templateName = getCalculatorKeyName(querySpec).getTemplateName();
        String templateScopeName = templateName.getScope();
        Scope scope = queryParams.getScope();
        Template template = null;
        if (!templateScopeName.isEmpty())
        {
            Scope templateScope = scope.findScope(templateScopeName);
            if (templateScope == null)
                throw new ExpressionException("Template \"" + templateName.toString() + "\" not found");
            if (isCallInScope)
                isCallInScope = scope.getName().equals(templateScope.getName());
            scope = templateScope;
        }
        template = scope.findTemplate(templateName);
        if (template == null)
            throw new ExpressionException("Template \"" + templateName.toString() + "\" not found");
        final String solutionName =  template.getQualifiedName().toString();
        // Set SolutionHander to collect results
        boolean success[] = new boolean[] {false};
        SolutionHandler solutionHandler = new SolutionHandler() {
        	
    		@Override
    		public boolean onSolution(Solution solution) {
    		    Axiom solutionAxiom = solution.getAxiom(solutionName);
    		    callHandler.setSolutionAxiom(solutionAxiom);
    		    	success[0] = solutionAxiom != null;
    			return success[0];
    		}
        };//solutionName, template.getId());
        queryParams.setSolutionHandler(solutionHandler);
        // Save scope context if calling in another scope otherwise push operand values on call stack
        ScopeContext scopeContext = isCallInScope ? null : scope.getContext(true);
        if (isCallInScope)
            template.push();
        template.backup(false);
        // Marshal arguments provided as a list of Variables  
        List<Term>[] initData = null; 
        if (argumentList.size() > 0)
        {
            initData = template.getProperties().getInitData();
            template.getProperties().setInitData(argumentList);
        }
        try
        {
            launch(queryParams);
            return success[0];
        }
        finally
        {   
            if (initData != null)
                template.getProperties().setInitData(initData);
            if (scopeContext != null)
                // Scope restored to original state
                scopeContext.resetScope();
            else
                template.pop();
        }
    }

}
