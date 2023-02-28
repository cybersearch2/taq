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
package au.com.cybersearch2.taq.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import au.com.cybersearch2.taq.compiler.CompilerException;

/**
 * Class file loader to support finding provider classes that are not on the runtime classpath.
 * The actual class loader is a URLClassLoader configured to search for classes in one of 2
 * locations, one dedicated to TAQ providers, and another for supporting packages. The class
 * loader is also wired to find libraries in a specified location that is expected to be
 * different from the 2 given class locations.
 */
public class ClassFileLoader {

	/** The actual class loader */
	private static final class ProviderClassLoader extends URLClassLoader {

		private final String resourcePath;
		
		public ProviderClassLoader(URL[] urls, String resourcePath) {
			super("ProviderClassLoader",  urls, ClassFileLoader.class.getClassLoader());
			this.resourcePath = resourcePath;
		}

	    @Override
	    public URL findResource(String name) {
	    	Path path = Paths.get(resourcePath, name);
	    	URL url = null;
			try {
				url = path.toUri().toURL();
			} catch (MalformedURLException e) {
			}
	    	if (url == null)
	    		url = super.findResource(name);
	    	return url;
		}
    } 
	
    public static final String CLASSES_BASE = "classes_base";
    public static final String LIBRARIES = "libraries";
	public static final String CLASS_NOT_FOUND = "Entity class %s not found";
   
	private static final String PROPERTY_NOT_SET = "Property %s not set";

	/** The supporting classes location */
	private final File classesLocation;
	/** The libraries location */
	private final File librariesLocation;
	/** The resources location */
	private final File resourcesLocation;
	/** Flag set true if all classes belonging to a package are loaded if one is loaded */
	private boolean loadAllClasses;

	/**
	 * Construct a ClassFileLoader object with the default package loading strategy
	 * @param classesLocation The supporting classes location
	 * @param librariesLocation  The libraries location
	 */
	public ClassFileLoader(File classesLocation, File librariesLocation, File resourcesLocation) {
		this(classesLocation, librariesLocation, resourcesLocation, false);
	}

	/**
	 * Construct a ClassFileLoader object,which supports a greedy package loading strategy
	 * @param classesLocation The supporting classes location
	 * @param librariesLocation  The libraries location
	 * @param loadAllClasses Flag set true if all classes belonging to a package are loaded if one is loaded
	 */
	public ClassFileLoader(File classesLocation, File librariesLocation, File resourcesLocation, boolean loadAllClasses) {
		this.classesLocation = classesLocation;
		this.librariesLocation = librariesLocation;
		this.resourcesLocation = resourcesLocation;
		this.loadAllClasses = loadAllClasses;
	}
	
	public Class<?> loadClass(String classname) {
    	Class<?> clazz = null;
		int pos = classname.lastIndexOf('.');
		String simpleName = classname.substring(pos + 1);
		String packagePath = classname.substring(0, pos).replace('.','/');
    	if (classesLocation != null) {
        	File file = new File(classesLocation, packagePath + "/" + simpleName + ".class");
        	if (!file.exists()) {
            	if (librariesLocation != null) {
                	file = new File(librariesLocation, packagePath + "/" + simpleName + ".class");
            	    if (!file.exists())
            	    	clazz = classForName(classname);
             	}
        	}
    	}
        if (clazz == null)
	    	clazz = quietCassForName(classname);
        if (clazz == null) {
        	if ((classesLocation == null) && (librariesLocation == null))
    		    throw new IllegalStateException(String.format(PROPERTY_NOT_SET, CLASSES_BASE));
        	URL classpathUrl = null;
        	URL providerUrl = null;
        	try {
        		if (classesLocation != null)
        		    classpathUrl = classesLocation.toURI().toURL();
        		if (librariesLocation != null)
        		    providerUrl = librariesLocation.toURI().toURL();
    		} catch (MalformedURLException e) {
    		}
        	URL[] urls;
			if (classpathUrl != null)
				urls = providerUrl != null ? new URL[]{classpathUrl, providerUrl} : new URL[]{classpathUrl};
			else
				urls = new URL[]{providerUrl};
        	ProviderClassLoader classLoader = 
        		new ProviderClassLoader(urls, resourcesLocation.getAbsolutePath());
			try {
			    clazz =  classLoader.loadClass(classname);
			    // Get declared fields to load co-located field types 
			    clazz.getDeclaredFields();
			    clazz.getFields();
			    String packageName = classname.substring(0, pos);
				analyzeMethodParameterTypes(clazz, packageName, classLoader);
				if (classesLocation != null) {
				    File classesPackagePath = new File(classesLocation, packagePath);
				    if (classesPackagePath.exists())
			            defineOtherClasses(packageName, classname, simpleName, classesPackagePath, classLoader, false);
				}
				if (librariesLocation != null) {
			        File providerPackagePath =  new File(librariesLocation, packagePath);
			        if (providerPackagePath.exists())
			            defineOtherClasses(packageName, classname, simpleName, providerPackagePath, classLoader, false);
				}
			} catch (ClassNotFoundException e) {
				throw new CompilerException(String.format(CLASS_NOT_FOUND,  classname));
			} finally {
				try {
					classLoader.close();
				} catch (IOException e) {
				}
			}
    	} 
        return clazz;
	}

	private Class<?> classForName(String classname) {
		try {
			return Class.forName(classname, true, getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new CompilerException(String.format(CLASS_NOT_FOUND,  classname));
		}
	}
	
	private Class<?> quietCassForName(String classname) {
		try {
			return Class.forName(classname, true, getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
		}
		return null;
	}
	
	private void defineOtherClasses(String packageName, String classname, String simpleName,  File location, URLClassLoader classLoader, boolean innerOnly) {
    	File[] found = new File[] {null};
    	String fileName = simpleName + ".class";
		File[] innerClassFiles = location.listFiles(new FileFilter() {

			@Override
			public boolean accept(File hit) {
				if (!hit.isFile())
					return false;
				if (fileName.equals(hit.getName())) {
					found[0]= hit;
					return false;
				}
				if (!innerOnly || (loadAllClasses || hit.getName().startsWith(simpleName + "$")) )
				    return  hit.getName().endsWith(".class");
				return false;
			}});
		if (found[0] != null) {
		    Arrays.asList(innerClassFiles).forEach(file -> {
				try {
					int dot = file.getName().lastIndexOf('.');
					String innerClassName = packageName + "." + file.getName().substring(0, dot);
					Class<?> clazz = classLoader.loadClass(innerClassName);
				    clazz.getDeclaredFields();
					analyzeMethodParameterTypes(clazz, packageName, classLoader);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}});
		}
    }

	private void analyzeMethodParameterTypes(Class<?> clazz, String packageName, URLClassLoader classLoader) {
		Method[] methods = clazz.getDeclaredMethods();
		Arrays.asList(methods).forEach(item -> {
			Class<?>[] classes = item.getParameterTypes();
			Set<String> nameSet = new HashSet<>();
			Arrays.asList(classes).forEach(paramType -> {
				if (!paramType.isPrimitive()) {
					String paramPackage = paramType.getPackageName();
					if (!paramPackage.equals(packageName) && !nameSet.contains(paramPackage)) {
					    File providerPackagePath =  new File(classesLocation,paramPackage.replace('.','/'));
					    if (providerPackagePath.exists()) {
					    	nameSet.add(paramPackage);
					        defineOtherClasses(paramPackage, paramType.getName(), paramType.getSimpleName(), providerPackagePath, classLoader, false);
					    }
					}
				}
			});
		});
	}
}
