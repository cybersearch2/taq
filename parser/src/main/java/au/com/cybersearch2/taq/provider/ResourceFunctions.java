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
package au.com.cybersearch2.taq.provider;

import java.util.Locale;

import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.db.EntityCollector;
import au.com.cybersearch2.taq.db.EntityEmitter;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ResourceOperand;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.query.QueryExecutionException;

/**
 * Resource functions available by object interface.
 *
 */
public class ResourceFunctions {

	/** Provides object methods on a resource */
	public final ResourceOperand resourceOperand;
	/** Flag set true if provider is a database provider */
	public final boolean isDatabaseProvider;

	/**
	 * Construct ResourceFunctions object
	 * @param resourceOperand Resource operand
	 */
	public ResourceFunctions(ResourceOperand resourceOperand) {
		this.resourceOperand = resourceOperand;
		isDatabaseProvider = 
			resourceOperand.getProvider() instanceof DatabaseProvider;
	}

    /**
     * Open resource
     */
	public void open()throws ExpressionException {
    	if (!resourceOperand.isOpen()) {
    	    resourceOperand.getProvider().open();
    	    resourceOperand.setOpen(true);
    	}
    }

    /**
     * Close to free all resources used by provider
     */
    public void close() {
    	if (resourceOperand.isOpen()) {
    	    resourceOperand.getProvider().close();
    	    resourceOperand.setOpen(false);
    	}
    }

    /**
     * Write axiom contained in given term to database
     * @param term Term bearing axiom to emit
     */
    public void emit(Term term) {
    	//System.out.println(term.toString());
    	if (term.getValueClass() != Axiom.class)
    		throw new QueryExecutionException(String.format("Cannot emit type %s", term.getValueClass() .getName()));
    	Locale locale;
		if (term instanceof OperatorTerm) {
    		OperatorTerm operatorTerm = (OperatorTerm)term;
    		locale = operatorTerm.getOperator().getTrait().getLocale();
    	} else
    		locale = Locale.getDefault();
    	getDatabaseProvider().emit((Axiom)term.getValue(), locale);
    }
    
    /**
     * Drop all tables in the database. 
     * If the database needs to be opened, close it on completion.
     */
	public void drop_tables() {
    	if (isDatabaseProvider) {
    		boolean isOpen = resourceOperand.isOpen();
        	if (!isOpen) 
        		open();
    		getDatabaseProvider().dropAllTables();
        	if (!isOpen) 
        		close();
    	}
    }
    
	/**
     * Sets flag for auto connect before query launch
     * @param value Flag
     */
    public void auto(Boolean value) {
    	resourceOperand.setAutoConnect(value.booleanValue());
    }
 
    /**
     * Set properties given as one or more terms
     * @param terms Term array
     */
    public void set(Term... terms) {
    	resourceOperand.set(terms);
    }
 
    /**
     * Log to console axioms being written to the database
     */
    public void log_to_console() {
    	if (isDatabaseProvider)
    		getDatabaseProvider().logToConsole();
    }

    @SuppressWarnings("unchecked")
	private DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>> getDatabaseProvider() {
    	ResourceProvider provider = resourceOperand.getProvider();
		return ((DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>)provider);
	}

}
