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
package au.com.cybersearch2.taq;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.db.DbType;
import au.com.cybersearch2.taq.db.EntityCollector;
import au.com.cybersearch2.taq.db.EntityEmitter;
import au.com.cybersearch2.taq.db.h2.H2;
import au.com.cybersearch2.taq.db.sqlite.Sqlite;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.helper.ClassFileLoader;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.ProviderFactory;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.provider.generic.EntityPersistence;

/**
 * ProviderManager
 * Provider factory for resources and functions.
 * @author Andrew Bowley
 * 6 Mar 2015
 */
public class ProviderManager 
{
    public static final String RESOURCE_BASE = "resource_base";
	public static final String CLASS_LOAD_ERROR = "Error loading class %s";

	/** Axiom Provider set */
    private final Set<ProviderFactory> providerSet;
	
    /** Map Function Providers to their names */
    private final Map<String, FunctionProvider> functionProviderMap;
 
    /** Connection profile mapped to database provider */
    private  Map<ConnectionProfile, DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>> databaseProviderMap;
	/** Resource path */
    private File resourceBase;
	/** External classes path */
    private File classesBase;
	/** External libraries path */
    private File libraries;
    /** Contains axiom listeners to support external monitoring of axiom production */
    private Map<String,List<LocaleAxiomListener>> listenerMap;
    /** Properties passes to every resource and function provider on creation */
    private Map<String,Object> propertiesMap;
    /** Database Resource providers, needed to apply life-cycle management and properties propagation */
    private List<DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>> databaseProviderList;
	
	/**
	 * Construct ProviderManager object
	 */
	public ProviderManager() {
		this(null);
	}
	
	/**
	 * Construct ProviderManager object
	 * @param resourceBase Resource base
	 */
	public ProviderManager(File resourceBase) {
		this.resourceBase = resourceBase;
		providerSet = new HashSet<>();
        functionProviderMap = new HashMap<String, FunctionProvider>();
		databaseProviderList = new ArrayList<>();
		databaseProviderMap = Collections.emptyMap();
	}

	/**
	 * Returns external classes path
	 * @return File object or null if not set
	 */
	public File getClassesBase() {
		return classesBase;
	}

	/**
	 * Returns external libraries path
	 * @return File object or null if not set
	 */
	public File getLibraries() {
		return libraries;
	}

	/**
	 * Set external classes path
	 * @param classesBase External classes path 
	 */
	public void setClassesBase(File classesBase) {
		this.classesBase = classesBase;
    	if (propertiesMap == null)
    		propertiesMap = new HashMap<>();
    	// Add to properties so providers can access external classes
    	propertiesMap.put(ClassFileLoader.CLASSES_BASE, classesBase);
	}

	/**
	 * Set external libraries path
	 * @param libraries External classes path 
	 */
	public void setLibraries(File libraries) {
		this.libraries = libraries;
    	if (propertiesMap == null)
    		propertiesMap = new HashMap<>();
    	// Add to properties so providers can access external libraries
    	propertiesMap.put(ClassFileLoader.LIBRARIES, libraries);
	}

	/**
	 * Returns external resources path
	 * @return File object or null if not set
	 */
	public File getResourceBase() {
		return resourceBase;
	}

	/**
	 * Set external resources path
	 * @param resourceBase External resources path 
	 */
	public void setResourceBase(File resourceBase) {
		this.resourceBase = resourceBase;
    	if (propertiesMap == null)
    		propertiesMap = new HashMap<>();
    	propertiesMap.put(RESOURCE_BASE, resourceBase);
	}

	/** 
	 * Returns flag set true if no resource providers are configured
	 * @return boolean
	 */
	public boolean isEmpty() {
		return providerSet.size() == 0;
	}

    /**
     * Add resource provider
     * @param providerFactory Provider Factory object
     */
    public void putResourceProvider(final ProviderFactory providerFactory)
    {
    	providerSet.add(providerFactory) ;
    }

    /**
     * Returns flag set true if worker service is required
     */
    public boolean activate() {
    	return databaseProviderList.size() > 0;
	}

    /**
     * Set property to be available to to all providers on creation
     * @param key Key
     * @param value Value as Object
     */
    public void setProperty(String key, Object value) {
    	if (propertiesMap == null)
    		propertiesMap = new HashMap<>();
    	propertiesMap.put(key, value);
    	if (key.equals(RESOURCE_BASE))
		    setResourceBase(new File(value.toString()));
    	else if (key.equals(ClassFileLoader.CLASSES_BASE))
		    setClassesBase(new File(value.toString()));
    	else if (key.equals(ClassFileLoader.LIBRARIES))
		    setLibraries(new File(value.toString()));
    }

    /**
     * Add axiom listener to support external monitoring of axiom production 
     * @param name Name of provider to receive the listener
     * @param axiomListener Axiom listener
     */
    public void chainAxiomListener(String name, LocaleAxiomListener axiomListener) {
    	if (listenerMap == null) 
    		listenerMap = new HashMap<>();
    	listenerMap.computeIfAbsent(name, key -> { return new ArrayList<>(); });
    	listenerMap.get(name).add(axiomListener);
    }
    
    /**
     * Returns flag set true if provider with given name supported
     * @param name Name of provider
     * @return flag set true if provider found
     */
    public boolean hasResourceProvider(String name) {
    	for (ProviderFactory factory: providerSet)
    		if (factory.isResourceName(name))
    			return true;
    	return false;
    }

    /**
     * Shutdown database providers and wait for all pending tasks to complete
     */
    public void close() {
    	if (databaseProviderList.size() > 0) {
        	databaseProviderList.forEach(provider -> {
        		provider.close();
        	});
     	}
    }
    
    /**
     * Returns Resource Provider specified by name
     * @param resourceName Resource Provider name
     * @return ResourceProvider implementation or null if not found
     */
    @SuppressWarnings("unchecked")
	public ResourceProvider getResourceProvider(String resourceName) {
    	ResourceProvider resourceProvider = null;
    	for (ProviderFactory factory: providerSet)
    		if (factory.isResourceName(resourceName)) {
    			resourceProvider = factory.createResourceProvider(resourceName);
				ResourceProvider provider = resourceProvider;
				// Apply external axiom listeners
    			if ((listenerMap != null) && listenerMap.containsKey(resourceName))
    				listenerMap.get(resourceName).forEach(listener -> 
    					provider.chainAxiomListener(listener));
    			// Apply all previously set properties. This is how classpath details are passed to providers.
    	    	if (propertiesMap != null)
    	        	propertiesMap.forEach((key,value) -> provider.setProperty(key, value));
    			break;
    		}
    	if (resourceProvider instanceof DatabaseProvider)
    		databaseProviderList.add((DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>)resourceProvider);
    	return resourceProvider;
    }

    /**
     * Returns database provider with given resource name
     * @param resourceName Resource name
     * @param classname Provider class name or null if using generic database provider
     * @param dbType Database characteristics. May be null if class name not null
     * @param databasePath Path to database file
     * @return DatabaseProvider object cast as ResourceProvider
     */
	public ResourceProvider getDatabaseProvider(String resourceName, String classname, DbType dbType, String databasePath)  {
    	if (getResourceBase() == null)
    		throw new IllegalStateException("Resource base location not set");
    	// Resolve path if not absolute
 		boolean isPathAbsolute = databasePath.startsWith("/");
		String absolutePath = databasePath;
 		if (!isPathAbsolute) {
 			String folder;
 			// The workspace is the official database root location
 			Object object = propertiesMap.get("workspace");
     		if (object != null) {
     			folder = object.toString();
     			absolutePath = folder + "/" +  databasePath;
     		} else { // Default to current working directory 
     			folder = new File("").getAbsolutePath();
     			absolutePath = new File("").getAbsolutePath() + "/" +  databasePath;
     		}
     		File databaseFile = new File(absolutePath);
     		if (!databaseFile.exists()) {
     			// Try resources, which is considered "read only"
     			File readOnlyFile = new File(resourceBase + "/" +  databasePath);
  			    if (readOnlyFile.exists())
 			    	absolutePath = readOnlyFile.getAbsolutePath();
 			    else {
 			    	// Ensure workspace location exists so the file can be created, if necessary
 			    	File baseLocation = new File(folder);
 			    	if (baseLocation.exists() || baseLocation.mkdirs()) {
 			    		int pos = databasePath.lastIndexOf('/');
 			    		if (pos != -1) {
 			    			File dbLocation = new File(baseLocation, databasePath.substring(0,pos));
 			    			dbLocation.mkdirs();
 			    		}
 			    	}
 			    }
     		}
 		}
 		//GenericProviderFactory providerFactory = null;
 		DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>> resourceProvider = null;
 		ConnectionProfile profile = new ConnectionProfile(resourceName, dbType, absolutePath);
 		if (databaseProviderMap.isEmpty()) 
 			databaseProviderMap = new HashMap<>();
 		 else
 			resourceProvider = databaseProviderMap.get(profile);
		if (resourceProvider == null) {
			if (classname == null)
			    resourceProvider = new EntityPersistence(profile);
			else {
		 		resourceProvider = 
		 		 	(DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>) 
		 		 		createResourceProvider(classname, profile);
			}
 			databaseProviderMap.put(profile, resourceProvider);
		}
		// Apply external axiom listeners
 		DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>> databaseProvider = resourceProvider;
		if ((listenerMap != null) && listenerMap.containsKey(resourceName)) 
			listenerMap.get(resourceName).forEach(listener -> databaseProvider.chainAxiomListener(listener));
		// Apply all previously set properties.
    	if (propertiesMap != null) 
        	propertiesMap.forEach((key,value) -> databaseProvider.setProperty(key, value));
		putResourceProvider(new ProviderFactory() {

			@Override
			public boolean isResourceName(String name) {
				return resourceName.equals(name);
			}

			@Override
			public ResourceProvider createResourceProvider(String name) {
				return databaseProvider;
			}});
		databaseProviderList.add(resourceProvider);
		return resourceProvider;
 	}

	/**
     * Return list of all database providers
     * @return DatabaseProvider list
     */
	public List<DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>> getDatabaseProviders() {
		return databaseProviderList;
	} 

    /**
     * Add function provider with given name
     * @param name Name of library
     * @param functionProvider The library object which implements FunctionProvider interface
     * @see au.com.cybersearch2.taq.interfaces.FunctionProvider
     */
    public void putFunctionProvider(String name, FunctionProvider functionProvider) {
        functionProviderMap.put(name, functionProvider);
    }

    /**
     * Returns function provider specified by name
     * @param name The provider name
     * @return FunctionProvider implementation or null if not found
     */
    public FunctionProvider findFunctionProvider(String name)
    {
    	FunctionProvider functionProvider = functionProviderMap.get(name);
    	if ((functionProvider != null) && (propertiesMap != null))
        	propertiesMap.forEach((key,value) -> functionProvider.setProperty(key, value));
    	return functionProvider;
    }
    
    /**
     * Returns function library specified by name
     * @param name The library name
     * @return FunctionProvider implementation
     * @throws ExpressionException if provider not found
     */
    public FunctionProvider getFunctionProvider(String name) {
        FunctionProvider functionProvider = findFunctionProvider(name);
        if (functionProvider == null)
            throw new ExpressionException("FunctionProvider \"" + name + "\" not found");
        return functionProvider;
    }

    /**
     * Returns resource provider determined by given properties
     * @param resourceName Name of resource
     * @param databaseObject Database property or null if none
     * @param providerObject Provider property. May be null if Database property is not null.
     * @param dbTypeObject Optional database type property
     * @return ResourceProvider object
     */
    public ResourceProvider getResourceProvider(String resourceName, Object databaseObject, Object providerObject, Object dbTypeObject) {
    	String classname = providerObject != null ? providerObject.toString() : null;
    	ResourceProvider resourceProvider = null;
     	if (databaseObject != null) {
     		// Database type defaults to H2
     		DbType dbType = null;
     		if (dbTypeObject != null)
     			dbType = getDbType(dbTypeObject.toString());
     		if (dbType == null) {
     		    if (classname == null)
         			dbType = new H2();
     		    else
     		    	throw new ProviderException("Database provider property must be accompanied by type property");
     		}
     		String databasePath = databaseObject.toString();
     		resourceProvider = getDatabaseProvider(resourceName, classname, dbType, databasePath);
     	} else {
    	    if (providerObject != null) {
				ClassFileLoader loader = new ClassFileLoader(classesBase, libraries, resourceBase);
				Class<?> clazz = loader.loadClass(classname);	
				ProviderFactory providerFactory;
				try {
					providerFactory = (ProviderFactory)clazz.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new ProviderException(String.format(CLASS_LOAD_ERROR, classname), e);
				}
				putResourceProvider(providerFactory);
				resourceProvider = getResourceProvider(resourceName);
    	    }
    	}
    	return resourceProvider;
    }

	private DbType getDbType(String string) {
		if (string.equalsIgnoreCase("Sqlite"))
			return new Sqlite();
		if (string.equalsIgnoreCase("H2")) 
			return new H2(); 
		throw new ProviderException(String.format("Unknown database type %s", string));
	}

    @SuppressWarnings("unchecked")
	private DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>> 
            createResourceProvider(String classname, ConnectionProfile profile) {
		ClassFileLoader loader = new ClassFileLoader(classesBase, libraries, resourceBase, true);
		Class<?> clazz = loader.loadClass(classname);	
		DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>> resourceProvider = null;
		try {
			resourceProvider = 
			    (DatabaseProvider<? extends EntityCollector<?>, ? extends EntityEmitter<?>>) 
			        clazz.getDeclaredConstructor(ConnectionProfile.class).newInstance(profile);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new ProviderException(String.format(ProviderManager.CLASS_LOAD_ERROR, classname), e);
		}
		return resourceProvider;
	}

}
