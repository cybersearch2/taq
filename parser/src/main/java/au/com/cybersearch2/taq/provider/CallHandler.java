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

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.CallEvaluator;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * CallHandler
 * Base class for function calls provides framework for returning results
 * @author Andrew Bowley
 * 30 Jul 2015
 */
public abstract class CallHandler implements CallEvaluator
{
	/** Function name */
	private final String name;
	/** One or two axiom listeners which allow returned values to be passed dynamically */
	private final LocaleAxiomListener[] axiomListeners;
	/** Axiom to pass a returned value at the conclusion of evaluation */
	protected Axiom solutionAxiom;
	/** Return type can be declared and defaults to AXIOM meaning none-to-many values returned */
    protected OperandType returnType;
    /** Axiom container (AxiomList or AxiomTermList) to listen for result and is cleared on backup */
	private AxiomContainer axiomContainer;
	/** Template assigned to receive solution or null if none specified */ 
	private Template solutionTemplate;
    /** ExecutionContext */
    protected ExecutionContext context;
	
 
	/**
	 * Construct CallHandler using default return type
	 * @param name Function name
	 */
    protected CallHandler(String name) {
	    // Default return type axiom meaning none-to-many values returned
   	    this(name, OperandType.AXIOM);
    }

	/**
	 * Construct CallHnndler using given return type
	 * @param name Function name
	 * @param returnType OperandType enum
	 */
    protected CallHandler(String name, OperandType returnType) {
		this.name = name;
		this.returnType = returnType;
		axiomListeners = new LocaleAxiomListener[2];
		solutionAxiom = new Axiom(name);
    }

    /**
     * Returns return type, either declared or default AXIOM
     * @return OperandType enum or null if no return type
     */
    public OperandType getReturnType() {
		return returnType;
	}

    /**
     * Set axiom listener to dynamically receive results. Up to 2 allowed
     * @param axiomListener Axiom listener
     */
	public void setAxiomListener(LocaleAxiomListener axiomListener) {
		if (axiomListener != null) {
		   	if (axiomListeners[0] == null)
		    		axiomListeners[0] = axiomListener;
		    	else
	 	    		axiomListeners[1] = axiomListener;
        }
    }

	/**
	 * Set axiom container to dynamically receive results
	 * @param axiomContainer Axiom container
	 */
	public void setAxiomContainer(AxiomContainer axiomContainer) {
		setAxiomListener(axiomContainer.getAxiomListener());
		this.axiomContainer = axiomContainer;
	}
		
	public void setSolutionTemplate(Template template) {
		solutionTemplate = template;
	}
	
	public AxiomContainer getAxiomContainer() {
		return axiomContainer;
	}

	public Axiom getSolution() {
     	if ((axiomContainer != null) && (axiomContainer.getOperandType() == OperandType.TERM)) {
     		AxiomTermList axiomTermList = (AxiomTermList)axiomContainer;
     		return axiomTermList.getAxiom();
     	}
		return solutionAxiom;
	}

	public void setExecutionContext(ExecutionContext context) {
		this.context = context;
	}
	
    /**
     * Returns name of function. Must be unique in context.
     * @return String
     */
    @Override
    public String getName() {
    	return name;
    }
  
    @Override
	public void backup(int id)  {
    	if (axiomContainer != null)
    		axiomContainer.clear();
		solutionAxiom = new Axiom(name);
    }
	
    @Override
    public Object getValue() {
     	if (axiomContainer != null)
     		return axiomContainer;
     	else if (solutionAxiom.isFact())
   	        return solutionAxiom.getValueByIndex(0);
     	else
   			return new Null();
    }

	/**
	 * Handle next axiom loaded by processor
	 * @param qname Qualified name of axiom
	 * @param axiom Axiom object
	 */
	protected void onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) {
    	if (axiomListeners[0] != null) {
    		axiomListeners[0].onNextAxiom(qname, axiom, locale);
        	if (axiomListeners[1] != null)
        		axiomListeners[1].onNextAxiom(qname, axiom, locale);
    	}
	}

	protected Template getSolutionTemplate() {
		return solutionTemplate;
	}

	public ExecutionContext getExectionContext() {
		return context;
	}

}
