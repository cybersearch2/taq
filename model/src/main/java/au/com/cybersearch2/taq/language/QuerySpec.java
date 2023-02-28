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
package au.com.cybersearch2.taq.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuerySpec
 * Query creation parameters
 * @author Andrew Bowley
 * 29 Dec 2014
 */
public class QuerySpec 
{
    /** Query name */
	private String name;
	/** Query type: logic, calculator, unknown (ie. unassigned) */
	private QueryType queryType;
	/** Return type or null if none */
	private OperandType operandType;
	/** List of axiom key / template name pairs */
	private ArrayList<KeyName> keyNameList;
	/** Specification list for chained queries */
	private List<QuerySpec> queryChainList;
	/** Properties for calculations referenced by template name */
	private Map<String, List<Term>> propertiesMap;
	/** Flag set true if head query specification */
	private boolean isHeadQuery;

	/**
	 * Construct a QuerySpec object 
	 * @param name Quern name
	 * @param isHeadQuery Flag set true if head query specification
	 */
	public QuerySpec(String name, boolean isHeadQuery) 
	{
		this.name = name;
		this.isHeadQuery = isHeadQuery;
		queryType = QueryType.logic;
		keyNameList = new ArrayList<KeyName>();
		propertiesMap = new HashMap<String, List<Term>>();
	}

	/**
	 * Add axiom key / template name pair for default logic query
	 * @param keyName KeyName object
	 */
	public void addKeyName(KeyName keyName)
	{
	    if ((!isHeadQuery) && (keyNameList.size() >= 1))
	        throw new SyntaxException("Limit of 1 part for query chain exceeded for " + keyNameList.get(0).toString());
        keyNameList.add(keyName);
	}

	/**
	 * Returns qualified name of final template in query chain
	 * @return qualified name 
	 */
	public QualifiedName getKey()
	{
	    QuerySpec querySpec = 
	        queryChainList != null ? 
	        queryChainList.get(queryChainList.size() - 1) : 
	        this;
	    List<KeyName> tailKeyNameList = querySpec.getKeyNameList();
	    if (tailKeyNameList.size() == 0)
	        // Key is qualified name of query until first keyname is added
	        return QualifiedName.parseName(name);
	    return tailKeyNameList.get(tailKeyNameList.size() - 1).getTemplateName();
	}
	
	/**
	 * Add axiom key / template name pair for specified query type
	 * @param queryType Query type
	 */
	public void setQueryType(QueryType queryType)
	{
		this.queryType = queryType;
	}

	public OperandType getOperandType() {
		return operandType;
	}

	public void setOperandType(OperandType operandType) {
		this.operandType = operandType;
	}

	/**
	 * Add axiom key / template name pair for calculator query, along with optional properties
	 * @param keyName KeyName object, axiomKey may be empty
	 * @param properties Calculator properties - may be empty
	 */
	public void putProperties(KeyName keyName, List<Term> properties) 
	{
		if ((properties != null) && properties.size() > 0)
			propertiesMap.put(keyName.getTemplateName().getTemplate(), properties);
	}

	/**
	 * Returns true if this query specification has no axiom key.
	 * This case is legal for a calculator type query.
	 * @return true
	 */
	public boolean hasNoAxiom()
	{
		return (keyNameList.size() == 1) && keyNameList.get(0).getAxiomKey().getName().isEmpty();
	}

	/**
	 * Returns query name
	 * @return String
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * Returns query type
	 * @return QueryType enum
	 */
	public QueryType getQueryType()
	{
		return queryType;
	}

	/**
	 * Returns List of axiom key / template name pairs
	 * @return List of KeyName objects
	 */
	public List<KeyName> getKeyNameList() 
	{
		return keyNameList;
	}

	/**
	 * Returns query chain list
	 * @return List of QuerySpec objects
	 */
	public List<QuerySpec> getQueryChainList()
	{
		return queryChainList;
	}

	/**
	 * Returns properties referenced by template name or null if no properties found
	 * @param tempateName Template name of calculator
	 * @return Term list
	 */
	public List<Term> getProperties(String tempateName) 
	{
		return propertiesMap.get(tempateName);
	}

	/**
     * @return the isHeadQuery
     */
    public boolean isHeadQuery()
    {
        return isHeadQuery;
    }

    /**
	 * Returns new QuerySpec object chained to this query specification
	 * @return QuerySpec object
	 */
	public QuerySpec chain() 
	{
		if (queryChainList == null)
			queryChainList = new ArrayList<>();

		QuerySpec queryChain = new QuerySpec(name + queryChainList.size(), false);
		queryChainList.add(queryChain);
		return queryChain;
	}
    /**
     * Returns new QuerySpec object chained to this query specification
     * @return QuerySpec object
     */
    public QuerySpec prependChain() 
    {
        if (queryChainList == null)
            queryChainList = new ArrayList<>();
        QuerySpec queryChain = new QuerySpec(name + queryChainList.size(), false);
        queryChainList.add(0, queryChain);
        return queryChain;
    }
}
