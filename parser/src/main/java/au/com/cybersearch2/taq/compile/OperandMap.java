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
package au.com.cybersearch2.taq.compile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * OperandMap
 * Operand and ItemList container. 
 * Items are referenced by qualified name. 
 * Provides a context qualified name for creation of intem qualified names
 * @author Andrew Bowley
 *
 * @since 19/10/2010
 */
public class OperandMap
{
	/** Tree map of operands for efficient lookup */
    protected Map<QualifiedName, Operand> operandMap;
    /** Names only for quick existence test. The key is 'scope@template' */
    protected Map<String, Set<String>> nameMap;
    
	/**
	 * Construct OperandMap object.
	 */
	public OperandMap()
	{
		operandMap = new TreeMap<QualifiedName, Operand>();
		nameMap = new HashMap<String, Set<String>>();
	}

    /**
	 * Returns all operand values in a map container
	 * @return Map with key of type Operand and value of type Object
	 */
	public  Map<Operand, Parameter> getOperandValues()
	{
		Map<Operand, Parameter> operandValueMap = new HashMap<Operand, Parameter>();
		for (Operand operand: operandMap.values())
		{
			if (!operand.isEmpty())
			{
			    Parameter parameter = new Parameter(operand.getName(), operand.getValue());
			    parameter.setId(operand.getId());
				operandValueMap.put(operand, parameter);
			}
		}
		return operandValueMap;
	}

	/**
	 * Set operand values
	 * @param operandValueMap Map container
	 */
	public void setOperandValues(Map<Operand, Parameter> operandValueMap)
	{
		for (Entry<Operand, Parameter> entry: operandValueMap.entrySet())
			if (entry.getValue().getValueClass() == Null.class) // Null special case
				entry.getKey().clearValue();
			else
			    entry.getKey().assign(entry.getValue());
		// Clear operands not included in operandValueMap
		for (Entry<QualifiedName, Operand> entry: operandMap.entrySet())
			if (!operandValueMap.containsKey(entry.getValue()))
				entry.getValue().clearValue();
	}

	/**
	 * Throws exception for 'Duplicate Operand name' if operand name is a duplicat
	 * @param name Name of operand in text format
	 * @param qualifiedContextname Qualified context name
     * @throws ExpressionException if check fails
	 */
	public void duplicateOperandCheck(String name, QualifiedName qualifiedContextname)
	{
	    if (hasOperand(name, qualifiedContextname))
	        throw new ExpressionException("Duplicate Operand name \"" + name + "\" encountered");
	}

	/**
     * Throws exception for 'Duplicate Operand name' if operand name is a duplicate
     * @param qname Qualified name of operand
     * @throws ExpressionException if check fails
	 */
    public void duplicateOperandCheck(QualifiedName qname)
    {
        if (operandMap.containsKey(qname))
            throw new ExpressionException("Duplicate Operand name \"" + qname.toString() + "\" encountered");
    }

    /**
     * Returns flag set true if given qualified name maps to an operand 
     * @param qname Qualified name key used to retrieve an operand
     * @return boolean
     */
    public boolean containsOperand(QualifiedName qname)   
    {
    	return operandMap.containsKey(qname);
    }

    /**
     * Attempt to remove given operand and return flag set true if operand was found
     * @param operand Operand object
     * @return boolean
     */
    public boolean removeOperand(Operand operand) {
    	return operandMap.remove(operand.getQualifiedName(), operand);
    }
    
    /**
     * Add new Variable operand of specified name, unless it already exists
     * @param name Name of new Opernand
	 * @param qualifiedContextname Qualified context name
     * @return New or existing Operand object
     */
    public Operand addOperand(String name, QualifiedName qualifiedContextname)
    {
        Operand operand = addOperand(QualifiedName.parseName(name, qualifiedContextname));
        return operand;
    }

    /**
     * Returns flag set true if given name exists in operand map
     * @param name Name
     * @return flag set true if name exists
     */
    public boolean existsName(String name) {
    	for (Set<String> nameSet: nameMap.values())
    		if (nameSet.contains(name))
     			return true;
        return false;
    }
    
    /**
     * Add new Variable operand of specified name, unless it already exists
     * @param qname Qualified name of new Operand
     * @return New or existing Operand object
     */
	public Operand addOperand(QualifiedName qname)
    {
		Operand param = operandMap.get(qname);
		if ((param == null) && !qname.getTemplate().isEmpty())
		{
		    QualifiedName sameScope = new QualifiedName(qname.getScope(), qname.getName());
		    param = operandMap.get(sameScope);
		    if (param != null)
		        qname = sameScope;
		}
        if ((param == null) && !qname.getScope().isEmpty())
        {
            QualifiedName globalScope = new QualifiedName(qname.getName());
            param = operandMap.get(globalScope);
            if (param != null)
                qname = globalScope;
        }
		if (param == null)
		{
			param = new Variable(qname);
			operandMap.put(qname, param);
	        addName(qname.getTemplateScope(), qname.getName());
		}
		return param;
    }

	/**
	 * Add supplied operand to map. Assumes operand is not annonymous.
	 * Note this will replace an existing operand with the same name
	 * @param operand Operand object
	 * @throws ExpressionException if operand name is empty
	 */
	public void addOperand(Operand operand)
    {
		QualifiedName qname = operand.getQualifiedName();
        if (qname.getName().isEmpty())
            throw new ExpressionException("addOperand() passed annonymous object");
        duplicateOperandCheck(qname);
		operandMap.put(qname, operand);
        addName(qname.getTemplateScope(), qname.getName());
    }

	/**
	 * Add given operand to map using given qualified name as key.
	 * This operand may already exist in the map under a different key
     * @param qname Qualified name
	 * @param operand Operand object
	 */
	public void addOperand(QualifiedName qname, Operand operand) {
		operandMap.put(qname, operand);
        addName(qname.getTemplateScope(), qname.getName());
	}


   /**
     * Add new Variable operand of specified name with specified key unless it already exists. 
     * These variables are to receive solution terms and have keys with scope values consisting
     * of format scope.template.
     * @param key Qualified name key
     * @param qname Qualified name
     * @return Operand object
     */
    public Operand addOperand(QualifiedName key, QualifiedName qname)
    {
        Operand operand = operandMap.get(key);
        if (operand == null)
        {
            operand = new Variable(qname);
            operandMap.put(key, operand);
            addName(key.getTemplateScope(), key.getName());

        }
        return operand;
    }

    /**
     * Add operand to, or update this map
     * @param operand
     */
	public void putOperand(Operand operand) {
        operandMap.put(operand.getQualifiedName(), operand);
	}

	/**
	 * Returns flag to indicate if this map contains operand with specified name
	 * @param name name
	 * @param qualifiedContextname Qualified context name
	 * @return boolean
	 */
	public boolean hasOperand(String name, QualifiedName qualifiedContextname)
	{
        if ((name.indexOf('.') == -1) && !existsName(name)) 
            return false;
        QualifiedName qname = QualifiedName.parseName(name, qualifiedContextname);
        if (operandMap.containsKey(qname))
            return true;
        if (!qname.getTemplate().isEmpty())
        {
            qname.clearTemplate();
            if (operandMap.containsKey(qname))
                return true;
        }
        if (!qname.getScope().isEmpty())
        {
            qname.clearScope();
            if (operandMap.containsKey(qname))
                return true;
        }
 		return false;
	}

    /**
     * Returns flag to indicate if this map contains operand with specified name
     * @param qname Qualified name
     * @return boolean
     */
    public boolean hasOperand(QualifiedName qname)
    {
        return operandMap.containsKey(qname);
    }

	/**
	 * Returns operand of specified name or null if not found
     * @param qname Qualified name
	 * @return Operand
	 */
	public Operand getOperand(QualifiedName qname)
	{
		return operandMap.get(qname);
	}
	
	/**
	 * Returns operand referenced by qualified name
	 * @param qname Qualified name
	 * @return Operand object or null if not exists
	 */
	public Operand get(QualifiedName qname)
	{
		return operandMap.get(qname);
	}

	/**
	 * Returns qualified name of operand for given 'scope@template' and name
	 * @param name One-part name
	 * @param scopeTemplate Scope and template with "@" separator
	 * @return
	 */
	public QualifiedName findTemplateScopeName(String name, String scopeTemplate) 
	{
		Set<String> nameSet = nameMap.get(scopeTemplate);
		if ((nameSet != null) && nameSet.contains(name))
		{
			int pos = scopeTemplate.lastIndexOf("@");
			if (pos == -1) // No template part
				return null;
			String template = scopeTemplate.substring(pos + 1);
			String scope = scopeTemplate.substring(0, pos);
			if (scope.isEmpty())
				scope = QueryProgram.GLOBAL_SCOPE;
			for (QualifiedName qname: operandMap.keySet())
			{
				if (qname.getName().equals(name) &&
					qname.getTemplate().equals(template) &&
					(qname.getScope().equals(scope) ||   
					    (qname.getScope().isEmpty() && 
							scope.equals(QueryProgram.GLOBAL_SCOPE))))
					return qname;
			}
		}
		return null;
	}
	
	/**
	 * Returns qualified name of operand for given 'scope@template' and name
	 * @param name One-part name
	 * @param scopeTemplate Scope and template with "@" separator
	 * @return
	 */
	public QualifiedName findScopeName(String name, String scopeTemplate) 
	{
		int pos = scopeTemplate.lastIndexOf("@");
		String scope = pos == -1 ? scopeTemplate : scopeTemplate.substring(0, pos);
		Set<String> nameSet;
		if (scope.isEmpty())
			nameSet = nameMap.get(QueryProgram.GLOBAL_SCOPE);
		else
		    nameSet = nameMap.get(scope);
		if ((nameSet != null) && nameSet.contains(name))
		{
			for (QualifiedName qname: operandMap.keySet())
			{
				if (qname.getName().equals(name) &&
					qname.getTemplate().isEmpty() &&
					(qname.getScope().equals(scope) ||   
					    (qname.getScope().isEmpty() && 
					     scope.equals(QueryProgram.GLOBAL_SCOPE))))
					return qname;
			}
		}
		return null;
	}
	
	/**
	 * Returns list of operand names
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString()
    {
    	StringBuilder builder = new StringBuilder();
    	boolean firstTime = true;
		for (QualifiedName name: operandMap.keySet())
		{
			if (firstTime)
				firstTime = false;
			else
				builder.append(", ");
			builder.append(name);
		}
		return builder.toString();
    }
    
    /**
     * Given name and template scope, adds an entry to the name map
     * @param templateScope
     * @param name
     */
    private void addName(String templateScope, String name)
    {
    	if (templateScope.isEmpty())
    		templateScope = QueryProgram.GLOBAL_SCOPE;
    	Set<String> nameSet = nameMap.get(templateScope);
    	if (nameSet == null) {
    		nameSet = new HashSet<>();
    		nameMap.put(templateScope, nameSet);
    	}
    	nameSet.add(name);
    }


}
