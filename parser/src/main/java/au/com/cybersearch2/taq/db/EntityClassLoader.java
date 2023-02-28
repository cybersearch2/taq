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
package au.com.cybersearch2.taq.db;

import java.io.File;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.compiler.CompilerException;
import au.com.cybersearch2.taq.helper.ClassFileLoader;
import au.com.cybersearch2.taq.log.LogManager;

public class EntityClassLoader {

	private static final Logger logger = LogManager.getLogger(EntityClassLoader.class);
	
	private File classesBase;
	private File libraries;
	private File resourcesBase;
	
    public boolean setProperty(String key, Object value) {
    	if (ClassFileLoader.CLASSES_BASE.equals(key) && (value instanceof File)) {
    		classesBase = (File)value;
    		return true;
    	} else if (ClassFileLoader.LIBRARIES.equals(key) && (value instanceof File)) {
    		libraries = (File)value;
    		return true;
    	} else if (ProviderManager.RESOURCE_BASE.equals(key) && (value instanceof File)) {
    		resourcesBase = (File)value;
    		return true;
    	} 
    	return false;
	}
    
    public Class<?> loadClass(String classname)  {
    	if ((classesBase == null) && (libraries == null) && (resourcesBase == null)) {
    		logger.warn("EntityClassLoader not set from properties");
    		try {
				return Class.forName(classname);
			} catch (ClassNotFoundException e) {
				throw new CompilerException(String.format(ClassFileLoader.CLASS_NOT_FOUND,  classname));
			}
    	}
    	// Check for nulls, defaulting to current working directory as workaround
    	if (classesBase == null)
    		classesBase = new File(".");
    	if (libraries == null)
    		libraries = new File(".");
    	if (resourcesBase == null)
    		resourcesBase = new File(".");
    	ClassFileLoader classFileLoader = new ClassFileLoader(classesBase, libraries, resourcesBase, true);
        return classFileLoader.loadClass(classname);
    }
}
