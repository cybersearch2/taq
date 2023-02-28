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
package au.com.cybersearch2.taq.expression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import au.com.cybersearch2.taq.compiler.ParserResource;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DelegateOperator;
import au.com.cybersearch2.taq.provider.ResourceFunctions;
import au.com.cybersearch2.taq.provider.ResourceMonitor;

/**
 * Provides object methods on a resource.
 */
public class ResourceOperand extends DelegateOperand {

	/** Resource qualified name. */
	private final QualifiedName qualifiedName;
	/** Resource provider */
	private final ResourceProvider provider;
    /** Name allocated to resource by provider */
	private final String resourceName;
    /** Flag set true if connection to resource is to happen before query is launched */
	private boolean isAutoConnect;
	/** Flag set true if connected */
	private boolean isOpen;

	/**
	 * Construct ResourceOperand object
	 * @param parserResource Parser resource 
	 * @param provider Resource provider
	 */
	public ResourceOperand(ParserResource parserResource, ResourceProvider provider) {
		super(QualifiedName.ANONYMOUS);
		this.provider = provider;
		qualifiedName = parserResource.getQualifiedName(); 
		resourceName = qualifiedName.getName();
		isAutoConnect = true;
	}

	public QualifiedName getQualifiedName() {
		return qualifiedName;
	}

	public ResourceProvider getProvider() {
		return provider;
	}

	public String getResourceName() {
		return resourceName;
	}

	public boolean isAutoConnect() {
		return isAutoConnect;
	}

	public void setAutoConnect(boolean isAutoConnect) {
		this.isAutoConnect = isAutoConnect;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public void set(Term... terms) {
    	Map<String, Object> properties = new HashMap<>();
    	Arrays.asList(terms).forEach(term -> {
        	String key = term.getName();
        	if (key.equals(Term.ANONYMOUS))
        		throw new ExpressionException("Key missing from resource property setting");
        	properties.put(key, term.getValue());
    	});
		if (!properties.isEmpty()) {
			if (provider instanceof ResourceMonitor)
			    ((ResourceMonitor)provider).setConnectionProperties(properties);
			else
				properties.forEach((key, value) -> provider.setProperty(key, value));
		}
    }

	/**
     * 
     * @see au.com.cybersearch2.taq.interfaces.Operand#assign(au.com.cybersearch2.taq.language.Parameter)
     */
    @Override
    public void assign(Parameter parameter)
    {
    }

    /**
     * 
     * @see au.com.cybersearch2.taq.interfaces.Operand#getRightOperand()
     */
    @Override
    public Operand getRightOperand()
    {
        return null;
    }

    /**
     * Returns object which defines operations that an Operand performs with other operands
     * @return Operator object
     */
    @Override
    public Operator getOperator()
    {
    	Operator proxyOperator = super.getOperator();
    	DelegateOperator resourceOperator = new DelegateOperator() {
    		
    		public Class<?> getObjectClass() {
    			return ResourceFunctions.class;
    		}
    	};
    	resourceOperator.setProxy(proxyOperator);
        return resourceOperator;
        
    }

}
