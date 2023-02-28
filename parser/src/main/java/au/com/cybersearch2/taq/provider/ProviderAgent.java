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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compiler.ParserResource;
import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.db.EntityCollector;
import au.com.cybersearch2.taq.db.EntityEmitter;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;

/** Provides indirect access to optional ProviderManager for safety */
public class ProviderAgent {
	
	/** ProviderManager for resources. It may be supplied or created 
	    on first resource declaration encountered */
	private ProviderManager providerManager;

	/**
	 * \Construct ProviderAgent object with no supplied ProviderManager
	 */
	public ProviderAgent() {
	}
	
	/**
	 * \Construct ProviderAgent object with supplied ProviderManager
	 * @param providerManager Provider manager
	 */
	public ProviderAgent(ProviderManager providerManager) {
		this.providerManager = providerManager;
	}

    /**
     * Returns flag set true if worker service is required
     */
	public boolean activate() {
		return providerManager != null ? providerManager.activate() : false;
	}
	
    /**
     * Shutdown database providers and wait for all pending tasks to complete
     */
	public void close() {
		if (providerManager != null)
		    providerManager.close();
	}
	
	/**
	 * Returns external resources path
	 * @return File object
	 */
	public File getResourceBase() {
		// Default resource location is CWD
		if ((providerManager != null) && (providerManager.getResourceBase() != null))
			return providerManager.getResourceBase();
		else
			return new File("");
	}

	/**
	 * Returns external classes path
	 * @return File object
	 */
	public File getClassesBase() {
		// Default classes base location is CWD
		if ((providerManager != null) && (providerManager.getClassesBase() != null))
			return providerManager.getClassesBase();
		else
			return new File("");
	}

	/**
	 * Returns external libraries path
	 * @return File object
	 */
	public File getLibraries() {
		// Default libraries location is CWD
		if ((providerManager != null) && (providerManager.getClassesBase() != null))
			return providerManager.getLibraries();
		else
			return new File("");
	}

    /**
     * Return list of all database providers
     * @return DatabaseProvider list - may be empty
     */
	public List<DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>> getDatabaseProviders() {
		return providerManager != null ? providerManager.getDatabaseProviders() : Collections.emptyList();
	}

	/**
	 * Set provider manager in given scope
	 * @param scope Scope 
	 */
    public void injectScope(Scope scope)
    {
        if (providerManager != null) {
        	scope.getParserAssembler().setFunctionManager(providerManager);
        }
    }

    /**
     * Returns Resource Provider specified in resource declaration
     * @param parserResource Helper which collects resource declaration production
     * @param properties Resource properties
     * @return ResourceProvider implementation\
     * @throws ExpressionException if Resource not found or unavailable
     */
	public ResourceProvider getResourceProvider(ParserResource parserResource, Map<String, Object> properties) {
        ResourceProvider resourceProvider = null;
        if (providerManager == null)
        	providerManager = new ProviderManager();
        else
            resourceProvider = providerManager.getResourceProvider(parserResource.getSystemName());
        if (resourceProvider == null) {
        	resourceProvider = 
        	    providerManager.getResourceProvider(parserResource.getSystemName(), 
        	    		                            properties.get("database"), 
        	    		                            properties.get("provider"), 
        	    		                            properties.get("type"));
            if (resourceProvider == null) {
                String resourceName = parserResource.getQualifiedName().getName();
                throw new ExpressionException("Resource \"" + resourceName + "\" not found or unavailable");
            }
        }
        return resourceProvider;
	}

}
