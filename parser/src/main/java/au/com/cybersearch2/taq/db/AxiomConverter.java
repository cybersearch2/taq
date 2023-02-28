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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.axiom.AxiomReflection;
import au.com.cybersearch2.taq.axiom.NameMap;
import au.com.cybersearch2.taq.db.MethodAnalyser.MethodData;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DelegateOperator;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * Translates between axioms of a particular archetype and corresponding Java beans
 */
public class AxiomConverter {

    /** Structure containing objects required to set entity fields */
	private static class ValueMethod {
		public final String fieldName;
		public final Object value;
		public final Method method;
		
		public ValueMethod(String fieldName, Object value, Method method) {
			this.fieldName = fieldName;
			this.value = value;
			this.method = method;
		}
	}
	
    /** Structure containing objects required for translation */
	private static class ConverterData {

		public Map<String, MethodData> columns; // Maps field name to reflection getter/setter method and term name
		public List<NameMap> termNameList;      // Term meta data

		public ConverterData(Map<String, MethodData> columns, List<NameMap> termNameList) {
			this.columns = columns;
			this.termNameList = Collections.unmodifiableList(termNameList);
		}
	}
	
    /**  Static empty Object array to represent no parameters in reflection method call */
    public static final Object[] NO_ARGS = new Object[] {};

	private static final String INVOKE_ERROR_MESSAGE = "Bean method call failed for class %s";
	private static final String MISSING_FIELD_MESSAGE = "Setter for class %s field %s not found";
    
	/** Translates between the term names of an Axiom Archetype and Java field names */
	private final AxiomReflection axiomReflection;
	/** Entity class mapped to entity-to-axiom translation objects */
    private final Map<Class<?>,ConverterData> fromEntityDataMap;
	/** Entity class mapped to axiom-to-entity translation objects */
    private final Map<Class<?>,ConverterData> fromAxiomDataMap;
    
    /** Flag set true if entity defines field names. It is cleared after first translation. */
    protected boolean isEntityNameMap;
	
    /**
     * Construct AxiomConverter object
     * @param axiomReflection Translates between the term names of an Axiom Archetype and Java field names
     */
	public AxiomConverter(AxiomReflection axiomReflection) {
		this.axiomReflection = axiomReflection;
		isEntityNameMap = axiomReflection.isArchetypeEmpty();
		fromEntityDataMap = new HashMap<>();
		fromAxiomDataMap = new HashMap<>();
	}

	/**
	 * Returns axiom archetype
	 * @return Archetype object
	 */
	public Archetype<Axiom, Term> getArchetype() {
		return axiomReflection.getArchetype();
	}

	/**
	 * Returns Axiom marshaled from entity object.
	 * The translation is subject to a specification of which terms to include
	 * and how to order them. 
	 * @param entity Object
	 * @return Axiom object
	 */
	public Axiom getAxiomFromEntity(Object entity) throws ExecutionException
	{
		ConverterData converterData = fromEntityDataMap.get(entity.getClass());
		if (converterData == null) {
			ColumnSetter<?> columnSetter = new ColumnSetter<>(entity.getClass());
			converterData = 
				new ConverterData(
						columnSetter.getFieldMap(),
                    axiomReflection.getTermNameList());
			fromEntityDataMap.put(entity.getClass(), converterData);
			if (!axiomReflection.getTermNameList().isEmpty())
				isEntityNameMap = false;
		}
        List<Term> termList = new ArrayList<>(converterData.columns.size());
		if (isEntityNameMap)
			converterData.termNameList = createNameMap(entity, converterData.columns, termList);
		else {
    		for (Map.Entry<String, MethodData> entry: converterData.columns.entrySet()) {
                String fieldName = entry.getKey();
    			for (NameMap nameMap: converterData.termNameList) 
    				if (nameMap.getFieldName().equalsIgnoreCase(fieldName)) {
    					assignItem(nameMap, entry.getValue().method, entity, termList);
    					break;
    				}
    		}
		}
		Axiom axiom;
		if (axiomReflection.isArchetypeEmpty()) {
	        axiom = axiomReflection.axiomInstance();
			for (Term term: termList)
				if (term != null) // Only relevant bean properties are set
					axiom.addTerm(term);
		} else {
			axiomReflection.clearMutable();
	        axiom = new Axiom(axiomReflection.getArchetype(), termList);
		}
        if (isEntityNameMap) {
            isEntityNameMap = false;
            axiomReflection.clearMutable();
        }
		return axiom;
	}

	/**
	 * Returns entity marshaled from axiom object.
	 * @param <E> Entity type
	 * @param axiom Axiom object
	 * @param clazz Entity class 
	 * @param locale Locale
	 * @return Entity object
	 * @throws ExecutionException
	 */
	public <E> E getEntityFromAxiom(Axiom axiom, Class<E> clazz, Locale locale) throws ExecutionException {
		E entity = null;
		ConverterData converterData = fromAxiomDataMap.get(clazz);
		if (converterData == null) {
			converterData = 
				new ConverterData(
					new ColumnGetter<>(clazz).getFieldMap(),
                    axiomReflection.getTermNameList());
			fromAxiomDataMap.put(clazz, converterData);
		}
		try {
			entity = clazz.getConstructor((Class<?>[])null).newInstance(NO_ARGS);
		} catch (Throwable e) {
            throw new ExecutionException("Error creating instance of class " + clazz.getName(), e);
		}
		List<ValueMethod> setterList = new ArrayList<>();;
		for (Map.Entry<String, MethodData> entry: converterData.columns.entrySet())
		{
            String fieldName = entry.getKey();
			for (NameMap nameMap: converterData.termNameList)
			{
				if (nameMap.getFieldName().equalsIgnoreCase(fieldName))
				{
					Object value = axiom.getValueByName(nameMap.getTermName());
					Method method = entry.getValue().method;
					if (method.getParameterCount() == 1) {
						boolean isSetter = false;
						Class<?> paramClass = method.getParameterTypes()[0];
						if (toPrimitiveClass(paramClass.getName())
							.equals(toPrimitiveClass(value.getClass().getName())))
                            isSetter = true;
						else if (value instanceof String){
							if (paramClass.isEnum()) {
	                            isSetter = true;
							    value = ColumnGetter.findMatchingEnumVal(paramClass, value.toString());
							} else if ((paramClass == Long.class) || (paramClass == long.class)) {
						        Scanner scanner = new Scanner(value.toString());
						        scanner.useLocale(locale);
                                if (scanner.hasNextLong()) {
								    value = scanner.nextLong();
	                                isSetter = true;
                                }
							} else if ((paramClass == Double.class) || (paramClass == double.class)) {
						        Scanner scanner = new Scanner(value.toString());
                                if (scanner.hasNextDouble()) {
								    value = scanner.nextDouble();
	                                isSetter = true;
                                }
							} else if (paramClass == BigDecimal.class) {
						        Scanner scanner = new Scanner(value.toString());
                                if (scanner.hasNextBigDecimal()) {
								    value = scanner.nextBigDecimal();
	                                isSetter = true;
                                }
							}
						}
						if (!isSetter && MethodAnalyser.isSerializable(value.getClass()))
                            isSetter = true;
						if (isSetter)
						    setterList.add(new ValueMethod(fieldName, value, method));
						break;
					}
				}
			}
		}
		// Check all fields will be set
		if (setterList.size() != converterData.columns.size()) {
			for (ValueMethod valueMethod: setterList) {
				boolean nameFound = false;
				for (String fieldName: converterData.columns.keySet()) {
					if (valueMethod.fieldName.equalsIgnoreCase(fieldName)) {
						nameFound = true;
						break;
					}
				}
				if (!nameFound)
                    throw new ProviderException(String.format(MISSING_FIELD_MESSAGE, entity.getClass().getName(), valueMethod.fieldName));
			}
		}
		for (ValueMethod valueMethod: setterList)
            try {
            	valueMethod.method.invoke(entity, valueMethod.value);
			} catch (Throwable e) {
	            throw new ExecutionException(String.format(INVOKE_ERROR_MESSAGE, entity.getClass().getName()), e);
			} 
		return entity;
	}

	private String toPrimitiveClass(String className) {
		if (className.startsWith("java.lang."))
			return className.substring(10).toLowerCase();
		return className;
	}
	
	/**
	 * Returns term metadata list, each item specifying term name, position and corresponding Java field name
	 * @param entity Entity object
	 * @param columns Maps field name to reflection getter/setter method and term name
	 * @param termList Parameter list to set axiom terms
	 * @return
	 * @throws ExecutionException
	 */
    private List<NameMap> createNameMap(Object entity, 
    		                            Map<String, MethodData> columns, 
                                        List<Term> termList) throws ExecutionException {
	    List<NameMap> termNameList = new ArrayList<>();
        int position = -1;
        boolean termsDeclared = !axiomReflection.getTermNameList().isEmpty();
	    for (Map.Entry<String, MethodData> entry: columns.entrySet())
	    {
	        String termName = null;
	        String fieldName = entry.getKey();
	        Object value = null;
	        try {
	        	value = entry.getValue().method.invoke(entity, NO_ARGS);
			} catch (Throwable e) {
	            throw new ExecutionException(String.format(INVOKE_ERROR_MESSAGE, entity.getClass().getName()), e);
			} 
	        if ((value == null) || !DelegateOperator.isDelegateClass(value.getClass()))
	            continue;
	        if (termsDeclared) {
	            position = -1;
		        for (NameMap nameMap: termNameList)
		            if (nameMap.getFieldName().equalsIgnoreCase(fieldName)) {
		                termName = nameMap.getTermName();
		                position = nameMap.getPosition();
		                break;
		            }
		        if (position == -1)
		            continue;
	        } else {
                termName = entry.getValue().term;
                ++position;
	        }
	        Parameter param = new Parameter(termName, value == null ? new Null() : value);
	        axiomReflection.assignItem(termList, position, param);
	        NameMap nameMap = new NameMap(termName, fieldName);
	        nameMap.setPosition(position);
	        termNameList.add(nameMap);
	    }
	    if (termNameList.isEmpty())
          	throw new IllegalStateException(String.format(AxiomReflection.EMPTY_ARCHETYPE_ERROR, axiomReflection.getArchetype().getName()));
	    return termNameList;
    }

    /**
     * Create a parameter from an entity by reflection and adds it to an axiom assignment list
     * @param nameMap Term meta data
     * @param method Reflection getter method
     * @param entity Entity object
     * @param termList Parameter list
     * @throws ExecutionException
     */
	private void assignItem(NameMap nameMap, 
            Method method, 
            Object entity, 
            List<Term> termList) throws ExecutionException {
		String termName = nameMap.getTermName();
        Object value = null;
        try {
        	value = method.invoke(entity, NO_ARGS);
		} catch (Throwable e) {
            throw new ExecutionException(String.format(INVOKE_ERROR_MESSAGE, entity.getClass().getName()), e);
		} 
        axiomReflection.assignItem(termList, 
        		                   nameMap.getPosition(), 
        		                   new Parameter(termName, value == null ? new Null() : value));
	}


    
}
