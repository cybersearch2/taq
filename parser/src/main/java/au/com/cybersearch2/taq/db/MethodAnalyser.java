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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.persistence.Entity;

import au.com.cybersearch2.taq.axiom.NameMap;

/**
 * MethodAnalyser
 * Base class encapsulates mapping columns defined by Java persistence annotations
 * to entity-database translation objects
 *
 * @param <T> Entity type
 */
public class MethodAnalyser<T> {

	public enum MethodType {
		getter,
		setter
	}
	
	/** Structure containing translation objects */
	public static class MethodData {
		
		public MethodData(Method method, String term) {
			this.method = method;
			this.term = term;
		}		
		
		public Method method; // Getter/setter reflection method
		public String term;   // Term name or database column name
	}
	
	/** Maps Java field name to class of value */
	final private Map<String, Class<?>> typeMap;
	/** Maps Java field name to translation objects */
	final private Map<String,MethodData> fieldMap;
	/** Entity class */
	final private Class<T> entityClass;
	/** Field name of id column, which requires special treatment */
	private String idFieldName;
    /** Name of database table */
    private String tableName;

	/**
	 * Construct MethodAnalyser objet
	 * @param entityClass Entity class 
	 * @throws ExecutionException 
	 */
	public MethodAnalyser(MethodType methodType, Class<T> entityClass) throws ExecutionException {
		this.entityClass = entityClass;
		typeMap = new HashMap<>();
		fieldMap = new HashMap<>(); 
		// Default name in case no "Id" column is declared
		idFieldName = "id";
		try {
			switch(methodType) {
			case getter: getGetters(entityClass); break;
			case setter: getSetters(entityClass); break;
			}
		} catch (ProviderException e) {
			throw new ExecutionException(
				String.format("Error finding method by reflection for class %s", entityClass.getName()), e);
		}
    	mapTypes(fieldMap.keySet());
	}

	public Class<?> getType(String fieldName) {
		return typeMap.get(fieldName);
	}

	public String getIdFieldName() {
		return idFieldName;
	}

	public Map<String, MethodData> getFieldMap() {
		return fieldMap;
	}

	public String getTableName() {
		if (tableName == null) {
			tableName = getTableName(entityClass);
		}
		return tableName;
	}

	/**
	 * Returns name of table persisting objects of the set entity class
	 * @return name of table
	 */
	public static String getTableName(Class<?> entityClass) {
		// Assume table name is given in an annotation
		Entity entityAnnotation = entityClass.getAnnotation(javax.persistence.Entity.class);
		if (entityAnnotation != null)
			return getStringByInvocation(entityAnnotation, "name");
		else // Default to simple name of entity class
			return entityClass.getSimpleName();
	}

	/**
	 * Returns flag set true if given class is serializable
	 * @param clazz Class to analyze
	 * @return boolean
	 */
	public static boolean isSerializable(Class<?> clazz) {
		List<Class<?>> interfaces = Arrays.asList(clazz.getInterfaces());
		return interfaces.contains(Serializable.class);
	}

	/**
	 * Returns list of NameMap objects obtained from given entity class
	 * @param entityClass Entity class to analyze
	 * @return NameMap list
	 */
	public static List<NameMap> getNameMap(Class<?> entityClass) {
		List<NameMap> termNameList = new ArrayList<NameMap>();
		Field[] fields = entityClass.getDeclaredFields();
		int[] index = new int[] {0};
		Arrays.asList(fields).forEach(field -> {
			Annotation column = null;
            for (Annotation annotation : field.getAnnotations()) 
            {
                Class<?> annotationClass = annotation.annotationType();
                String annotName = annotationClass.getName();
                if (annotName.equals("javax.persistence.Column")) {
                	column = annotation;
                	break;
                }
            }
            if (column != null) {
            	String termName = getStringByInvocation(column, "name");
            	if (termName.isEmpty())
            		termName = field.getName();
            	NameMap nameMap = new NameMap(termName, field.getName());
            	nameMap.setPosition(index[0]++);
            	termNameList.add(nameMap);
             }
 		});
		return termNameList;
	}
	
	protected void setIdFieldName(String idFieldName) {
		this.idFieldName = idFieldName;
	}

	protected String methodName(Field field, String prefix) {
		String fieldName  = field.getName();
		return prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}


	protected void mapTypes(Set<String> fieldNames) throws ExecutionException {
    	for (String key: fieldNames) {
    		Class<?> fieldType;
			try {
				fieldType = entityClass.getDeclaredField(key).getType();
			} catch (NoSuchFieldException | SecurityException e) {
				throw new ExecutionException(String.format("Entity field %s reflection error", key), e);
			}
    		typeMap.put(key, fieldType);
    	}
	}
	
	/**
	 * Returns String from object obtained from annotation method invocation
	 * @param annotation Annotation object
	 * @param methodName Method name
	 * @return text or empty string if object returned is null 
	 */
	public static String getStringByInvocation(Annotation annotation, String methodName) throws ProviderException
	{
	    try
	    {
	        Object value = annotation.getClass().getMethod(methodName).invoke(annotation);
	        return (value != null) ? value.toString() : "";
	    }
	    catch (Throwable e)
	    {
	        throw new ProviderException("Annotation reflection error", e);
	    }
	}

	private void getSetters(Class<T> entityClass) throws ProviderException {
		Field[] fields = entityClass.getDeclaredFields();
		Arrays.asList(fields).forEach(field -> {
			Annotation column = null;
			boolean isProperty = false;
            for (Annotation annotation : field.getAnnotations()) 
            {
                Class<?> annotationClass = annotation.annotationType();
                String annotName = annotationClass.getName();
                if (annotName.equals("javax.persistence.Id"))
               	    setIdFieldName(field.getName());
                else if (annotName.equals("javax.persistence.Column")) 
                	column = annotation;
                else if (annotName.equals("javax.persistence.Access"))
                	isProperty = true; // assume access type is "property", not "field"
            }
            if (column != null) {
            	String termName = getStringByInvocation(column, "name");
            	if (termName.isEmpty())
            		termName = field.getName();
            	Method method;
            	method = getSetter(field, entityClass, isProperty);
           	    fieldMap.put(field.getName(), new MethodData(method, termName));
            }
 		});
		String idFieldName = getIdFieldName();
		if (!idFieldName.isEmpty() && !fieldMap.keySet().contains(idFieldName)) {
			Field field = null;
			try {
				field = entityClass.getDeclaredField(idFieldName);
			} catch (NoSuchFieldException e) { // Class does not have @Id annotated field or default "id"
	    		throw new ProviderException(String.format("Setter for field '%s' not found", idFieldName));
			}
      	    fieldMap.put(idFieldName, new MethodData(getSetter(field, entityClass, false), field.getName()));
		}
	}
	
	private Method getSetter(Field field, Class<T> entityClass, boolean isProperty) throws ProviderException {
    	String fieldName = field.getName();
    	String setterName = methodName(field, "set");
    	Method method = null;
    	if (isProperty) {
    		// Select setter without regard to field type
    		// This means the setter is responsible for type conversion
    		Method[] methods = entityClass.getDeclaredMethods();
    		for (Method declared: methods)
    			if (declared.getName().equals(setterName)) {
    				method = declared;
    				break;
    		    }
    			
    	} else
	    	try {
	    	    method =  entityClass.getDeclaredMethod(setterName, field.getType());
	     	} catch (NoSuchMethodException e) {
	    	}
    	if (method == null)
    		throw new ProviderException(String.format("Setter for field '%s' not found", fieldName));
    	return method;
    }

	private void getGetters(Class<T> entityClass) throws ProviderException {
		Field[] fields = entityClass.getDeclaredFields();
		Arrays.asList(fields).forEach(field -> {
			Annotation column = null;
			boolean isProperty = false;
            for (Annotation annotation : field.getAnnotations()) 
            {
                Class<?> annotationClass = annotation.annotationType();
                String annotName = annotationClass.getName();
                if (annotName.equals("javax.persistence.Column")) 
                	column = annotation;
                else if (annotName.equals("javax.persistence.Access"))
                	isProperty = true;
                else if (annotName.equals("javax.persistence.Id"))
                	setIdFieldName(field.getName());
            }
            if (column != null) {
            	String setterName = methodName(field, "get");
            	Method method = findGetter(entityClass, setterName, field.getType(), isProperty);
            	if ((method == null) && field.getType() == Boolean.class) {
            		setterName = methodName(field, "is");
            		method = findGetter(entityClass, setterName, field.getType(), isProperty);
            	}
            	if (method == null)
    				throw new ProviderException(String.format("Entity column '%s' not found", field.getName()));
            	String termName = getStringByInvocation(column, "name");
            	if (termName.isEmpty())
            		termName = field.getName();
            	fieldMap.put(field.getName(), new MethodData(method, termName));
            } 
  		});
		String idFieldName = getIdFieldName();
		if (!idFieldName.isEmpty() && !fieldMap.keySet().contains(idFieldName)) {
			Field field = null;
			try {
				field = entityClass.getDeclaredField(idFieldName);
			} catch (NoSuchFieldException e) { // Never will happen
				throw new ProviderException(String.format("Entity field '%s' not found", idFieldName));
			}
			fieldMap.put(idFieldName, 
      	    		          new MethodData(
      	    		        		 findGetter(entityClass, methodName(field, "get"), field.getType(), false), 
      	    		        		 field.getName()));
		}
	}

	private Method findGetter(Class<T> entityClass, String setterName, Class<?> returnType, boolean isProperty) {
		// if 'isProperty is true the select getter without regard to field type
		// This means the getter is responsible for type conversion
		Method method = null;
    	for (Method declared: entityClass.getDeclaredMethods())
    		if (setterName.equals(declared.getName()) &&
    		    (declared.getParameterCount() == 0) &&
    			(isProperty || (declared.getReturnType() == returnType))) {
				    method = declared;
				    break;
		}
     	return method;
    }
	
}

