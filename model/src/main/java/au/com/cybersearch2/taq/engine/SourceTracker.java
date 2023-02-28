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
package au.com.cybersearch2.taq.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.TaqLiteral;
import au.com.cybersearch2.taq.util.DualMap;

/**
 * Collects source information for debugging
 * @author Andrew Bowley
 *
 */
public class SourceTracker {

	public final static String INPUT = "Input";
	
    /** Bi-directional map between non-terminals and their keys */
    private final DualMap<String,Integer> nonTerninalMap;
    /** Stack to nest calls */
    private final Deque<SourceMarker> markerStack;
    /** Stack to retain nested source documents */
    private final Deque<Integer> documentStack;
    /** Set of source markers collated by qualified name */
    private final List<SourceMarker> sourceMarkers;
    /** Non-terminal index source */
    private final AtomicInteger nonTerminalIndex;
    
    /** Current source marker */
    private SourceMarker sourceMarker;
    /** Index of current source document in list */
    private int sourceDocumentId;
    /** List of source documents */
    private List<String> sourceDocumentList;

	public SourceTracker() {
        sourceMarkers = new ArrayList<>();
        markerStack = new ArrayDeque<>();
        documentStack = new ArrayDeque<>();
        nonTerminalIndex = new AtomicInteger();
        sourceDocumentList = new ArrayList<>();
        nonTerninalMap = new DualMap<>() {

        	private final Map<String,Integer> computeMap = new HashMap<>();
        	private final Map<Integer,String> inverseMap = new HashMap<>();
        	
			@Override
			public Integer computeIfAbsent(String nonTerminal) {
			    Integer nonTerminalKey = computeMap.computeIfAbsent(nonTerminal, 
			    			key -> { return nonTerminalIndex.incrementAndGet(); });
			    inverseMap.put(nonTerminalKey, nonTerminal);
				return nonTerminalKey;
			}

			@Override
			public String get(Integer key) {
				return inverseMap.get(key);
			}
        	
        };
        // Create initial source marker
        setSourceMarker(INPUT);
	}

	public SourceTracker(String sourceDocument) {
		this();
        sourceDocumentList.add(sourceDocument);
        documentStack.push(0);
	}
	
    /**
     * Returns current source marker
     * @return SourceMarker object
     */
    public SourceMarker getSourceMarker()
    {
        return sourceMarker;
    }

    /**
     * Create source marker for given non-terminal
     * @param nonTerminal Name of parser non-terminal
     * @return SourceMarker object
     */
    public SourceMarker setSourceMarker(String nonTerminal)
    {
        SourceMarker sourceMarker = 
        	new SourceMarker(getNonTerminalKey(nonTerminal),
        			         sourceDocumentId);
        setSourceMarker(sourceMarker);
        return sourceMarker;
    }

   /**
     * Set current source marker, set it's source document id and add to marker set
     * @param sourceMarker the sourceMarker to set
     * @return SourceMarker object
     */
    public SourceMarker setSourceMarker(SourceMarker sourceMarker)
    {
        this.sourceMarker = sourceMarker;
        sourceMarkers.add(sourceMarker);
        return sourceMarker;
    }
    
    /**
     * Push current source marker on a stack and replace it 
     * without adding to list of source markers
     * @param nonTerminal Name of next parser non-terminal
     * @return SourceMarker object of next parser non-terminal
     */
    public SourceMarker pushSourceMarker(String nonTerminal)
    {
        markerStack.push(sourceMarker);
        SourceMarker sourceMarker = 
            	new SourceMarker(getNonTerminalKey(nonTerminal),
            			         sourceDocumentId);
        this.sourceMarker = sourceMarker;
        return sourceMarker;
    }

    /**
     * Pop source marker off stack and link to tail of previous source item
     * @return SourceMarker object popped off stack
     */
    public SourceMarker popSourceMarker()
    {
        SourceMarker stackMarker = markerStack.pop();
        stackMarker.addSourceMarker(sourceMarker);
        sourceMarker = stackMarker;
        return stackMarker;
    }

    /**
     * Add SourceItem object for SourceInfo interface to current source marker 
     * @param sourceInfo Source information
     * @param unit Unit
     * @param extent Extent
     * @return SourceItem object
     */
    public SourceItem addSourceItem(SourceInfo sourceInfo, Unit unit, Extent extent)
    {
        SourceItem sourceItem = addSourceItem(sourceInfo.toString(), unit, extent);
        // Update information when operand is completed in a parser task
        sourceInfo.setSourceItem(sourceItem);
        return sourceItem;
    }
    
    /**
     * Add SourceItem object for operand to current source marker 
     * @param operand Operand object
     * @param unit Unit
     * @param extent Extent
     * @return SourceItem object
     */
    public SourceItem addSourceItem(IOperand operand, Unit unit, Extent extent)
    {
        SourceItem sourceItem = addSourceItem(operand.toString(), unit, extent);
        // Update information when operand is completed in a parser task
        if (operand instanceof SourceInfo)
            ((SourceInfo)operand).setSourceItem(sourceItem);
        return sourceItem;
    }
    
    /**
     * Add SourceItem object to current source marker
     * @param information Information
     * @param unit Unit
     * @param extent Extent
     * @return SourceItem object
     */
    public SourceItem addSourceItem(String information, Unit unit, Extent extent)
    {
        SourceItem sourceItem = new SourceItem(unit, extent, information);
        sourceMarker.addSourceItem(sourceItem);
        return sourceItem;
    }

    /**
     * Append to current source item given extent and information to append
     * @param extent Extent
     * @param information Variable text parameters
     */
    public void appendSourceItem(Extent extent, String... information) { 
    	sourceMarker.appendSourceItem(extent, information);
    }
    
    /**
     * Add SourceItem object for an operand to current source marker
     * @param var Operand
     * @param unit Unit
     * @param extent Extent
     * @return SourceItem object
     */
    public SourceItem addSourceVariable(IOperand var, Unit unit, Extent extent) {
    	TaqLiteral literal = unit.getKind();
        switch (literal)
        {
        case integer:
        case taq_boolean:
        case taq_double:
        case string:
        case decimal:
        case currency:
        {
            return addSourceItem(literal.name() + " " + var.toString(), unit, extent);
        }
        default:
            return addSourceItem(var, unit, extent);
        }
     }

    /**
     * @return the sourceDocumentList
     */
    public List<String> getSourceDocumentList()
    {
        return sourceDocumentList == null ? Collections.emptyList() : sourceDocumentList;
    }

    /**
     * @return the sourceDocumentId
     */
    public int getSourceDocumentId()
    {
        return sourceDocumentId;
    }

    /**
     * @return the sourceMarkers
     */
    public List<SourceMarker> getSourceMarkers()
    {
        return sourceMarkers;
    }

    /**
     * Push source document on stack
     * @param sourceDocument Source document resource name
     * @return id of source document
     */
    public int pushSourceDocument(String sourceDocument)
    {
        if (sourceDocumentList == null)
        {   // Opening source document not specified, so set to empty string
            sourceDocumentList = new ArrayList<String>();
            sourceDocumentList.add("");
            documentStack.push(0);
        }
        sourceDocumentId = sourceDocumentList.size();
        documentStack.push(sourceDocumentId);
        sourceDocumentList.add(sourceDocument);
        return sourceDocumentId;
    }
 
    /**
     * Pop source document stack to reference previous document
     */
    public void popSourceDocument()
    {
        if (documentStack.size() > 0)
        {
            documentStack.removeFirst();
            sourceDocumentId = documentStack.getFirst();
        }
    }
 
    protected int getNonTerminalKey(String nonTerminal) 
    {
    	return nonTerninalMap.computeIfAbsent(nonTerminal);
    }
 
    protected String getNonTerminal(int key) {
    	return nonTerninalMap.get(key);
    }
    
}
