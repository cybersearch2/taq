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

import java.util.Collections;
import java.util.Deque;
import java.util.List;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * ChainQuery
 * Abstract base class for all chain queries. 
 * The chain is formed by each query linking to the next.
 * @author Andrew Bowley
 * 15 Dec 2014
 */
public abstract class ChainQuery 
{
	protected static List<Term> EMPTY_PROPERTIES = Collections.emptyList();
	
	/** Next query or null if tail of chain */
 	protected ChainQuery next;
 	/** Object to notify of scope change, null if not required */
    protected ScopeNotifier scopeNotifier;
    /** Query properties - may be empty */
    protected List<Term> properties;
    
    protected ChainQuery()
    {
		this.properties = EMPTY_PROPERTIES;
    }
    
    protected ChainQuery(ScopeNotifier scopeNotifier)
    {
    	this();
        this.scopeNotifier = scopeNotifier;
    }

 	/**
	 * Backup to state before previous unification
	 */
	abstract protected void backupToStart();

	/**
	 * Force reset to initial state
	 */
	abstract protected void reset();

	/**
	 * Set axiom listener to receive each solution as it is produced
	 * @param qname Reference to axiom by qualified name
	 * @param axiomListener The axiom listener object
	 */
	abstract void setAxiomListener(QualifiedName qname, LocaleAxiomListener axiomListener);

    /**
 	 * Execute query and if not tail, chain to next.
 	 * Sub classes override this method and call it upon completion to handle the chaining
 	 * @param solution The object which stores the query results
     * @param templateChain Template chain to manage same query repeated in different scopes
     * @param context Execution context
	 * @return EvaluationStatus enum: SHORT_CIRCUIT, SKIP or COMPLETE
 	 */
	public EvaluationStatus executeQuery(Solution solution, Deque<Template> templateChain, ExecutionContext context)
	{
	    EvaluationStatus status = EvaluationStatus.COMPLETE;
	    if (next != null)
	    {
	        ScopeNotifier scopeNotifier = next.getScopeNotifier();
	        if (scopeNotifier != null)
	            scopeNotifier.notifyScopes();
	        status =  next.executeQuery(solution, templateChain, context);
	    }
		return status;
 	}

    /**
     * @return the scopeNotifier
     */
    public ScopeNotifier getScopeNotifier()
    {
        return scopeNotifier;
    }

	/**
	 * Set next query in the chain
	 * @param next
	 */
	protected void setNext(ChainQuery next) 
	{
		this.next = next;
	}

	/**
	 * Returns next query in the chain or null if tail
	 * @return ChainQuery object or null if this is the tail 
	 */
	public ChainQuery getNext() 
	{
		return next;
	}

    /**
     * @return the properties
     */
    public List<Term> getProperties()
    {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(List<Term> properties)
    {
        this.properties = properties;
    }

    protected void backup()
    {
    } 
    
    protected void pushTemplateChain(Deque<Template> templateChain, Template template) 
    {
	    // A chain is used to detect if a template with same name as head template encountered.
	    // This may happen for same query repeated in different scopes.
	    if (templateChain.isEmpty())
	    {
            templateChain.push(template);
	    }
	    else
	    {
	        Template head = templateChain.peekLast();
	        // Compare archetypes to match replicates in addition to other template types
	        if (head.getTemplateArchetype() == template.getTemplateArchetype())
	        {   // New query, so reset template chain
	            while ((head = templateChain.pollLast()) != null)
	                head.reset();
	        }
            templateChain.push(template);
	    }
    }
}
