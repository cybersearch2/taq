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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.AxiomListListener;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.TermListIterable;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.list.Cursor;
import au.com.cybersearch2.taq.list.CursorList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.result.ResultList;

/**
 * ListAssembler
 * Assembles list details collected by parser
 * @author Andrew Bowley
 * 12May,2017
 */
public class ListAssembler
{
    /** An axiom which is declared within the enclosing scope */ 
    private final Map<QualifiedName, ProtoAxiom> axiomMap;
    /** The axioms which are declared within the enclosing scope */ 
    private final Map<QualifiedName, List<Axiom>> axiomListMap;
    /** AxiomTermLists in template scope */
    private final Map<QualifiedName, AxiomTermList> axiomTermListMap;
    /** The axiom listeners, all belonging to list variables */
    private final Map<QualifiedName, List<LocaleAxiomListener>> axiomListenerMap;
    /** The axiom list listeners */
    private final Map<QualifiedName, QualifiedName> axiomListAliases;
    /** Item lists */
    private final Map<QualifiedName, ItemList<?>> listMap;
    /** Maps qualified name of cursor to the cursor */
    private Map<QualifiedName, CursorList> cursorMap;
    /** Scope */
    private final Scope scope;

    /**
     * Construct ListAssembler object
     * @param scope Enclosing scope
     */
    public ListAssembler(Scope scope)
    {
        this.scope = scope;
        axiomMap = new HashMap<>();
        axiomListMap = new HashMap<>();
        axiomTermListMap = new HashMap<>();
        axiomListenerMap = new HashMap<>();
        axiomListAliases = new HashMap<>();
        listMap = new TreeMap<QualifiedName, ItemList<?>>();
    }
    
    /**
     * Add contents of another ListAssembler to this object
     * @param listAssembler Other ListAssembler object
     */
    public void addAll(ListAssembler listAssembler) 
    {
    	axiomMap.putAll(listAssembler.axiomMap);
        axiomListMap.putAll(listAssembler.axiomListMap);
        axiomTermListMap.putAll(listAssembler.axiomTermListMap);
        axiomListenerMap.putAll(Collections.unmodifiableMap(listAssembler.axiomListenerMap));
        axiomListAliases.putAll(Collections.unmodifiableMap(listAssembler.axiomListAliases));
        listMap.putAll(listAssembler.listMap);
    }

    /**
     * Returns flag set true if list specified by type and name exists
     * Both axiom dynamic and context list use the axiomListAliases map and
     * type is distinguished by the name of the alias scope - "scope" being
     * reserved for context lists.
     * @param listType List type
     * @param qualifiedName List name
     * @return boolean
     */
    public boolean existsKey(ListType listType, QualifiedName qualifiedName)
    {
        switch (listType)
        {
        case axiom_item: // Name of axiom list
            return axiomListMap.containsKey(qualifiedName);
        case axiom_dynamic: // Check for self reference in list aliases map
            return axiomListAliases.containsKey(qualifiedName) ;
        case term: // Name of axiom term list
            return axiomTermListMap.containsKey(qualifiedName);
        case basic: // Name of basic list ie not axiom list or axiom term list
            return listMap.containsKey(qualifiedName);
        case context: // Name of list with current scope in it's name
        {
            QualifiedName listName = axiomListAliases.get(qualifiedName);
            return (listName != null) && (listName.getScope().equals("scope"));
        }
        case cursor: return (cursorMap != null) && cursorMap.keySet().contains(qualifiedName);
        default:
            return false;
        }
    }

    /**
     * Returns list type for given qualified name
     * @param qualifiedName List name
     * @return ListType which is "ListType.none" if name does not map to a list
     */
    public ListType getListType(QualifiedName qualifiedName) {
    	for (ListType listType: Arrays.asList(ListType.values())) {
    		if (listType == ListType.context) {
    	        if (qualifiedName.isGlobalName() && (scope != scope.getGlobalScope())) {
    	        	ListAssembler global = scope.getGlobalParserAssembler().getListAssembler();
    	            if (global.existsKey(ListType.basic, qualifiedName))
    	            	return ListType.basic;
    	            if (global.existsKey(ListType.term, qualifiedName))
    	            	return ListType.term;
    	            if (global.existsKey(ListType.axiom_item, qualifiedName))
    	            	return ListType.axiom_item;
    	            if (global.existsKey(ListType.axiom_dynamic, qualifiedName)) 
    	            	return ListType.axiom_dynamic;
    	        }
   	            return ListType.none;
    		}
    		if (existsKey(listType, qualifiedName))
    			return listType;
    	}
        return ListType.none;
    }
    
    /**
     * Add given axiom to axiom item list specified by name
     * @param qualifiedName List name
     * @param axiom The axiom to add
     */
    public void add(QualifiedName qualifiedName, Axiom axiom)
    {
    	if (axiomListMap.containsKey(qualifiedName)) {
    		axiomListMap.get(qualifiedName).add(axiom);
    	} else if (axiomMap.containsKey(qualifiedName)) {
    		ProtoAxiom protoAxiom = axiomMap.get(qualifiedName);
    		protoAxiom.add(axiom);
            if (axiomTermListMap.containsKey(qualifiedName)) {
            	AxiomTermList axiomTermList = axiomTermListMap.get(qualifiedName);
            	axiomTermList.setAxiom(axiom);
            	axiomTermList.setPublic(protoAxiom.isExported());
            }
        }
    }
 
    /**
     * Returns axiom specified by name
     * @param qualifiedName Axiom name
     * @return Axiom object
     */
    public Axiom getAxiom(QualifiedName qualifiedName)
    {
        ProtoAxiom prtoAxiom = axiomMap.get(qualifiedName);
        return prtoAxiom != null ? prtoAxiom.getAxiom() : null;
    }

    /**
     * Returns axiom item list specified by name
     * @param qualifiedName List name
     * @return list containing axioms
     */
    public List<Axiom> getAxiomItems(QualifiedName qualifiedName)
    {
       return axiomListMap.get(qualifiedName);
    }

    /**
     * Returns new axiom item
     * @param qualifiedName Axiom name
     * @return ProtoAxiom object
     */
    public ProtoAxiom axiomInstance(QualifiedName qualifiedName, boolean isExport)
    {
    	ProtoAxiom protoAxiom = new ProtoAxiom(isExport);
        axiomMap.put(qualifiedName, protoAxiom);
        return protoAxiom;
    }

    /**
     * Returns new axiom item list
     * @param qualifiedName List name
     * @return list to contain axioms
     */
    public List<Axiom> axiomItemsInstance(QualifiedName qualifiedName)
    {
    	List<Axiom> axiomList = new ArrayList<>();
        axiomListMap.put(qualifiedName, axiomList);
        return axiomList;
    }

    /**
     * Returns axiom term list specified by name
     * @param qualifiedName List name
     * @return AxiomTermList object, which is created if it does not already exist
     */
    public AxiomTermList getAxiomTerms(QualifiedName qualifiedName)
    {
    	return getAxiomTerms(qualifiedName, qualifiedName);
    }
    
    /**
     * Returns axiom term list specified by name
     * @param qualifiedName List name
     * @return AxiomTermList object, which is created if it does not already exist
     */
    public AxiomTermList getAxiomTerms(QualifiedName qualifiedName, QualifiedName key)
    {
    
        AxiomTermList axiomTermList = axiomTermListMap.get(qualifiedName);
        if (axiomTermList == null)
        {
            axiomTermList = new AxiomTermList(qualifiedName, key);
            axiomTermListMap.put(qualifiedName, axiomTermList);
        }
        return axiomTermList;
    }
    
    /**
     * Returns axiom term list specified by qualified name.
     * @param qualifiedName Qualified name of list
     * @return AxiomTermList object or null if list not found 
     */
    public AxiomTermList findAxiomTerms(QualifiedName qualifiedName)
    {
        return axiomTermListMap.get(qualifiedName);
    }
    
    /**
     * Returns container with all axiom listeners belonging to this scope mapped by name
     * @return map object
     */
    public Map<QualifiedName, List<LocaleAxiomListener>> getAxiomListenerMap()
    {
        return axiomListenerMap;
    }

    /**
     * Add given axiom listener to list specified by name
     * @param qualifiedName Name of list
     * @param axiomListener The axiom listener to add
     */
    public void add(QualifiedName qualifiedName, LocaleAxiomListener axiomListener)
    {
        List<LocaleAxiomListener> axiomListenerList = getAxiomListenerList(qualifiedName);
        if (axiomListenerList != null)
            axiomListenerList.add(axiomListener);
    }
 
    /**
     * Create new item list for an axiom list. Do not report a duplicate error if list already exists.
     * @param qualifiedName List name
     * @param isExport Flag set true if list is exported
     * @return flag set true if list created
     * @see AxiomAssembler#saveAxiom(QualifiedName)
     */
    public boolean createAxiomItemList(QualifiedName qualifiedName, boolean isExport)
    {   // Create new axiom list if one does not already exist
        boolean axiomItemListExists = existsKey(ListType.axiom_item, qualifiedName);
        if (!axiomItemListExists)
            axiomItemsInstance(qualifiedName);
        return !axiomItemListExists;
    }

    /**
     * Create new item list for an axiom term list. Do not report a duplicate error if list already exists.
     * @param qualifiedName List name
     * @return flag set true if list created
     * @see AxiomAssembler#saveAxiom(QualifiedName)
     */
    public boolean createTermList(QualifiedName qualifiedName)
    {   // Create new axiom list if one does not already exist
        boolean termListExists = existsKey(ListType.term, qualifiedName);
        if (!termListExists) {
        	AxiomTermList axiomTermList = new AxiomTermList(qualifiedName, qualifiedName);
            axiomTermListMap.put(qualifiedName, axiomTermList);
        }
        return !termListExists;
    }

    /**
     * Create new axiom.
     * @param qualifiedName List name
     * @param isExport Flag set true if list is exported
     * @param createTermList Flag set true if term list with same name to be created
      * @see AxiomAssembler#saveAxiom(QualifiedName)
     */
    public void createAxiom(QualifiedName qualifiedName, boolean isExport, boolean createTermList)
    {   // Create new axiom if one does not already exist
        axiomInstance(qualifiedName, isExport);
        if (createTermList && !existsKey(ListType.term, qualifiedName)) {
        	AxiomTermList axiomTermList = new AxiomTermList(qualifiedName, qualifiedName);
            axiomTermListMap.put(qualifiedName, axiomTermList);
        }
     }

   /**
     * Returns item list specified by qualified name.
     * @param qname Qualified name of list
     * @return ItemList object or null if an axiom parameter is named and is not yet set 
     * @throws ExpressionException if item list not found
     */
    public ItemList<?> getItemList(QualifiedName qname)
    {
        ItemList<?> itemList = findItemList(qname);
		if (itemList == null) {
		    AxiomTermList axiomTermList = axiomTermListMap.get(qname);
		    if (axiomTermList == null)
                throw new ExpressionException("List not found with name \"" + qname.toString() + "\"");
		    itemList = axiomTermList;
		}
        return itemList;
    }
 
    /**
     * Returns axiom or axiom term list cast to item list specified by qualified name.
     * @param qname Qualified name of list
     * @return ItemList object or null if list not found
     */
    public ItemList<?> findAxionContainer(QualifiedName qname)
    {
        ItemList<?> itemList = findItemList(qname);
		if (itemList == null) {
		    AxiomTermList axiomTermList = axiomTermListMap.get(qname);
		    if (axiomTermList != null)
		    	itemList = axiomTermList;
		}
        return itemList;
    }
 
    /**
     * Returns axiom term list specified by qualified name.
     * @param qname Qualified name of list
     * @return AxiomTermList object
     * @throws ExpressionException if item list not found
     */
    public AxiomTermList getAxiomTermList(QualifiedName qname)
    {
        ItemList<?> itemList = axiomTermListMap.get(qname);
        if (itemList == null)
            throw new ExpressionException("List not found with name \"" + qname.toString() + "\"");
        if (!(itemList instanceof AxiomTermList))
            throw new ExpressionException("List with name \"" + qname.toString() + "\" not expected type of 'AxiomTermList'");
        return (AxiomTermList) itemList;
    }
    
    /**
     * Returns item list identified by scope and name. 
     * If scope not, global, search for list in global scope too.
     * @param scopeName The name of the scope
     * @param listName The name of the list
     * @return ItemList or null if list not found
     */
    public ItemList<?> getItemList(String scopeName, String listName)
    {
        QualifiedName qualifiedListName = new QualifiedName(scopeName, listName);
        ItemList<?> itemList = listMap.get(qualifiedListName);
        if ((itemList == null) && !scopeName.isEmpty())
        {
            qualifiedListName = new QualifiedName(listName);
            itemList = scope.getGlobalListAssembler().listMap.get(qualifiedListName);
        }
        return itemList;
    }
    
    /**
     * Returns item list specified by qualified name. 
     * Ensures scope part of name identifies an existing scope.
     * @param qname Qualified name of list
     * @return ItemList object or null if not found
     */
    public ItemList<?> findItemList(QualifiedName qname)
    {
        ItemList<?> itemList = null;
        Scope globalScope = scope.getGlobalScope();
        Scope nameScope = qname.getScope().isEmpty() ? globalScope : scope.findScope(qname.getScope());
        if (nameScope != null)
            itemList = nameScope.getParserAssembler().getListAssembler().listMap.get(qname);
        if ((itemList == null) && (globalScope != nameScope))
            itemList = globalScope.getParserAssembler().getListAssembler().listMap.get(qname);
        return itemList;
    }

    /**
     * Returns item list specified by qualified name. 
     * Ensures scope part of name identifies an existing scope.
     * @param qname Qualified name of list
     * @return ItemList object or null if not found
     */
    public AxiomList findAxiomItemList(QualifiedName qname)
    {
        Scope globalScope = scope.getGlobalScope();
        List<Axiom> axiomList = getAxiomItems(qname);
        if (axiomList != null)
        	return (AxiomList) createAxiomList(globalScope, qname, axiomList);
        else
        	return null;
    }

    public ItemList<?> getAxiomSource(QualifiedName qname) {
        Scope globalScope = scope.getGlobalScope();
        ParserAssembler globalParserAssembler = globalScope.getParserAssembler();
        Scope nameScope = qname.getScope().isEmpty() ? globalScope : scope.findScope(qname.getScope());
        if (nameScope == null)
            nameScope = scope;
        AxiomSource axiomSource = nameScope.getParserAssembler().getAxiomSource(qname);
        if (axiomSource == null)
            axiomSource = globalParserAssembler.getAxiomSource(qname);
        if (axiomSource != null) {
            Iterator<Axiom> iterator = axiomSource.iterator(null);
        	if (!iterator.hasNext())
                throw new ExpressionException("Axiom source \"" + qname.toString() + "\" is empty");
        	Axiom first = iterator.next();
	        if (iterator.hasNext()) {
	            List<Axiom> axiomList = axiomItemsInstance(qname);
	            axiomList.add(first);
	            while (iterator.hasNext())
	                axiomList.add(iterator.next());
	            return createAxiomList(globalScope, qname, axiomList);
	        } else {
	    	    AxiomTermList termList = getAxiomTerms(qname);
	    	    axiomInstance(qname, false);
	    	    add(qname, first);
	   	        return termList;
	        }
        }
        return null;
    }
    
    /**
     * Find item list by name
     * @param listName Qualified name of list
     * @return ItemList object
     */
    public ItemList<?> findItemListByName(QualifiedName listName)
    {
       // Look up list by name from item lists
        ItemList<?> itemList = findItemList(listName); 
        String contextScopeName = scope.getParserAssembler().getQualifiedContextname().getScope();
        if ((itemList == null) && !contextScopeName.equals(scope.getName()))
        {   // Search for item list using context scope
            QualifiedName qualifiedListName = new QualifiedName(contextScopeName, listName);
            itemList = findItemList(qualifiedListName);
        }
        if (itemList == null) {
        	if (listName.isTemplateEmpty()) {
	            Axiom axiom = getAxiom(listName);
	            if (axiom != null) {
	            	// Axiom mapped to list name indicates it is wrapped in axiom list with same name
	            	if (axiomTermListMap.containsKey(listName))
	            		return axiomTermListMap.get(listName);
	            }
            } 
        	List<Axiom> axiomList = getAxiomItems(listName);
        	if (axiomList != null)
                // Create axiom list to access context list
                itemList = createAxiomList(scope.getGlobalScope(), listName, axiomList);
        }
        if ((itemList == null) && listName.getScope().isEmpty()) {
        	// Check for name of function with template archetype
        	String scopeName = listName.getTemplate();
        	Scope functionScope = scope.findScope(scopeName);
        	if (functionScope != null) {
        		// Archetype will have associated axiom list or axiom term list
        		ListAssembler listAssembler = functionScope.getParserAssembler().getListAssembler();
        		itemList = listAssembler.findAxionContainer(new QualifiedName(listName.getTemplate(), listName.getName(), QualifiedName.EMPTY));
        	}
        }
        return itemList;
    }

    /**
     * Add ItemList object to it's container identified by name
     * @param qname Qualified name of list
     * @param itemList ItemList object to add
     */
    public void addItemList(QualifiedName qname, ItemList<?> itemList)
    {
    	if (itemList.getOperandType() != OperandType.TERM) {
	        if (listMap.containsKey(qname))
	            throw new ExpressionException("ItemList name \"" + qname.toString() + "\" clashes with existing list");
	        listMap.put(qname, itemList);
    	} else {
	        if (axiomTermListMap.containsKey(qname))
	            throw new ExpressionException("Axiom Term List name \"" + qname.toString() + "\" clashes with existing list");
	    	if (itemList instanceof ListOperand)
	    		itemList = (ItemList<?>) ((ListOperand<?>)itemList).getValue();
	        axiomTermListMap.put(qname, (AxiomTermList)itemList);
	        if (itemList.isPublic())
	        	axiomInstance(itemList.getQualifiedName(), true);
    	}
    		
    }

    /**
     * Copy axiom lists as iterables to supplied container
     * @param axiomIterableMap Container to receive lists
     */
    public void copyLists(Map<QualifiedName, Iterable<Axiom>> axiomIterableMap) 
    {
        for (Entry<QualifiedName, ItemList<?>> entry: listMap.entrySet())
        {
           ItemList<?> itemList = entry.getValue();
            if ((itemList.isPublic()) && (itemList instanceof TermListIterable))
                axiomIterableMap.put(entry.getKey(), getAxiomIterable((TermListIterable)itemList, itemList.getLength()));
        }
        if (!scope.getName().equals(QueryProgram.GLOBAL_SCOPE))
            scope.getGlobalListAssembler().copyLists(axiomIterableMap);
    }

    public Map<QualifiedName,ResultList<?>> getListResults() {
    	Map<QualifiedName,ResultList<?>> itemLists = new HashMap<>();
        for (Entry<QualifiedName, ItemList<?>> entry: listMap.entrySet())
        {
           ItemList<?> itemList = entry.getValue();
            if ((itemList.isPublic())) {
            	itemLists.put(itemList.getQualifiedName(), itemList.getSolution());
            }
        }
        return itemLists;
    }
    
    /**
     * Copy axioms from all item lists containing single axioms to supplied container
     * @param axiomMap Container to receive axioms
     */
    public void copyAxioms(Map<QualifiedName, AxiomTermList> axiomMap)
    {
        // Each Axiom term list is wrapped in an axiom. Unwrap to extract the axiom
        // and name it by combining the list name context with the axiom key name.
        // Note that axiom term list contents are exported by default.
        axiomTermListMap.values().forEach(list -> {
        	Axiom item = list.getAxiom();
        	if (list.isPublic() && (item.getTermCount() > 0)) 
            	    axiomMap.put(list.getQualifiedName(), list);
        });
    	for (Map.Entry<QualifiedName,ProtoAxiom> entry: this.axiomMap.entrySet()) {
    		ProtoAxiom protoAxiom = entry.getValue();
    		if (protoAxiom.isExported() && !axiomMap.containsKey(entry.getKey())) {
    			AxiomTermList atl = new AxiomTermList(entry.getKey(), entry.getKey());
    			// A ProtoAxioms objects may never be assigned the actual axiom being identified 
    			Axiom axiom = protoAxiom.getAxiom();
    			if (axiom != null) {
    			    atl.setAxiom(axiom);
        	        axiomMap.put(entry.getKey(), atl);
    			}
    		}
    	}
    }
    
    /**
     * Clear item lists identified by name
     * @param listNames List of qualified list names
     */
    public void clearLists(List<QualifiedName> listNames) 
    {
        for (QualifiedName listName: listNames)
        {
            ItemList<?> itemList= listMap.get(listName);
            itemList.clear();
        }
    }

    /**
     * Returns collection of the names of lists that are empty
     * @return List of names
     */
    public List<QualifiedName> getEmptyListNames()
    {
        List<QualifiedName> listNames = new ArrayList<QualifiedName>();
        for (ItemList<?> itemList: listMap.values())
            if (itemList.isEmpty())
                listNames.add(itemList.getQualifiedName());
        return listNames;
    }

    /**
     * Copy AxiomList to container holding iterable objects
     * @param termListIterable AxiomTermList iterable source of axioms
     * @param listSize List size
     * @return axiom iterable 
     */
    public Iterable<Axiom> getAxiomIterable(TermListIterable termListIterable, int listSize)
    {
        final List<Axiom> axiomList = new ArrayList<Axiom>(listSize);
        Iterator<Axiom> axiomTermListIterator = termListIterable.iterator();
        while (axiomTermListIterator.hasNext())
            axiomList.add(axiomTermListIterator.next());
        return new Iterable<Axiom>(){
        
            @Override
            public Iterator<Axiom> iterator()
            {
                return axiomList.iterator();
            }};
    }

    /**
     * Returns list of axiom listeners for specified name, creating the list if it does not already exist.
     * @param qualifiedName Name of list
     * @return List containing AxiomListener objects or null if list not found
     */
    public List<LocaleAxiomListener> getAxiomListenerList(QualifiedName qualifiedName)
    {
        List<LocaleAxiomListener> axiomListenerList = axiomListenerMap.get(qualifiedName);
        if (axiomListenerList == null)
        {
            axiomListenerList = new ArrayList<>();
            axiomListenerMap.put(qualifiedName, axiomListenerList);
        }
        return axiomListenerList;
    }

    /** 
     * Adds axiom list name alias
     * @param aliasName Alias name used to access list from other namespace
     * @param listName Actual list name
     */
    public void mapAxiomList(QualifiedName aliasName, QualifiedName listName)
    {
        axiomListAliases.put(aliasName, listName);
    }

    /**
     * Returns actual axiom list name for alias name
    * @param aliasName Alias name used to access list from other namespace
     * @return QualifiedName - may be alias if no mapping found
     */
    public QualifiedName getAxiomListMapping(QualifiedName aliasName)
    {
        QualifiedName listName = axiomListAliases.get(aliasName);
        return listName != null ? listName : aliasName;
    }
    
    /**
     * Set axiom container from supplied axiom(s). 
     * @param axiomContainer AxiomList object or AxiomTermList object
     * @param axiomItems A single axiom if setting an AxiomTermList, otherwise an axiom collection
     */
    public void setAxiomContainer(AxiomContainer axiomContainer, List<Axiom> axiomItems)
    {
        LocaleAxiomListener axiomListener = axiomContainer.getAxiomListener();
        QualifiedName axiomKey = axiomContainer.getKey();
        if (axiomContainer.getOperandType() == OperandType.AXIOM)
            // Populate axiom list if already created by the script being compiled
            // No listener required
            for (Axiom axiom: axiomItems)
               axiomListener.onNextAxiom(axiomKey, axiom, scope.getLocale());
        else
        {
            if (!axiomItems.isEmpty())
                axiomListener.onNextAxiom(axiomKey, axiomItems.get(0), scope.getLocale());
            add(axiomKey, axiomListener);
        }
    }

    /**
     * Removes axiom item list specified by name
     * @param qualifiedName Qualified name
     * @return list removed from container or null if list not found
     */
    public List<Axiom> removeAxiomItems(QualifiedName qualifiedName)
    {
    	return axiomListMap.remove(qualifiedName);
    }

    /**
     * Register cursor and it's referenced list
     * @param cursorQname Qualified name of cursor variable
     * @param cursorList Contains Cursor and list it references
     */
    public void registerCursor(QualifiedName cursorQname, CursorList cursorList) 
    {
    	if (cursorMap == null)
    		cursorMap = new HashMap<>();
    	cursorMap.put(cursorQname, cursorList);
    }

    /**
     * Returns cursor identified by name
     * @param cursorQname Cursor name
     * @return Cursor object
     */
    public Cursor getCursor(QualifiedName cursorQname) {
    	if (cursorMap != null) {
    		CursorList cursorList = cursorMap.get(cursorQname);
    		return cursorList == null ? null : cursorList.getCursor();
    	}
    	return null;
    }

    /**
     * Returns object which contains Cursor and list it references
     * @param cursorQname Cursor name
     * @return CursorList object
     */
    public CursorList getCursorList(QualifiedName cursorQname) {
    	return cursorMap != null ? cursorMap.get(cursorQname) : null;
    }

    /**
     * Returns set of all dynamic axiom lists and axiom term lists
     * @return AxiomContainer set
     */
	public Set<AxiomContainer> getAxiomContainers() {
		Set<AxiomContainer> axiomsSet = new HashSet<>();
		axiomListAliases.forEach((key, value) -> {
			if (key == value) {
				AxiomContainer list = (AxiomContainer) findItemList(key);
				if (list != null)
				    axiomsSet.add(list);
			}
		});
		axiomTermListMap.values().forEach(list ->  axiomsSet.add(list));
		return axiomsSet;
	}

	public List<QualifiedName> getExportListNames() {
		List<QualifiedName> exported = new ArrayList<>();
		listMap.values().forEach(list -> {
			if (list.isPublic()) 
				exported.add(list.getQualifiedName());
		});
		axiomMap.forEach((key,protoAxiom) -> {
			if (protoAxiom.isExported()) 
				exported.add(key);
		});
		return exported;
	}

    /**
     * Returns axiom list listener
     * @return AxiomListListener object
     */
    protected AxiomListListener axiomListListenerInstance()
    {
        AxiomListListener axiomListListener = new AxiomListListener(){

            @Override
            public void addAxiomList(QualifiedName qname, AxiomList axiomList)
            {
                if (!listMap.containsKey(qname))
                    listMap.put(qname, axiomList);
            }
        };
        return axiomListListener;
    }

    /**
     * Create axiom list to access context list
     * @param globalScope Global scope
     * @param key List name used as key to map item list
     * @param axiomList Context list
     * @return ItemList object
     */
    private AxiomList createAxiomList(Scope globalScope, QualifiedName key, List<Axiom> axiomList)
    {
        AxiomArchetype archetype = globalScope.getGlobalAxiomAssembler().getAxiomArchetype(key);
        AxiomList axiomContainer = new AxiomList(key, key);
        if (archetype != null)
            axiomContainer.setAxiomTermNameList(archetype.getTermNameList());
        globalScope.getGlobalListAssembler().setAxiomContainer(axiomContainer, axiomList);
        addItemList(key, (ItemList<Axiom>) axiomContainer);
        return axiomContainer;
    }



}
