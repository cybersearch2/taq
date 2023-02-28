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

import au.com.cybersearch2.taq.language.SyntaxException;

/**
 * SourceMarker
 * Associates parser non-terminals with source location ie. line and column
 * @author Andrew Bowley
 */
public class SourceMarker implements Comparable<SourceMarker>
{
	/** Key to select non-terminal name */
    private final int key;
    /** Source document identity */
    private final int sourceDocumentId;
    
    /** Head source item in list of contained items */
    private SourceItem headSourceItem;
    /** Tail source item in list of contained items */
    private SourceItem tailSourceItem;
   

    /**
     * Construct SourceMarker object
     * @param key Key to select non-terminal name
     * @param sourceDocumentId Source document identity
     */
    public SourceMarker(
            int key,
            int sourceDocumentId)
    {
        super();
        this.key = key;
        this.sourceDocumentId = sourceDocumentId;
    }

    /**
     * Returns non-terminal 
     * @return non-terminal
     */
    public int getNonTerminalKey()
    {
        return key;
    }

    /**
     * Returns start line number
     * @return the line
     */
    public int getStartLine()
    {
        return headSourceItem != null ? headSourceItem.getBeginLine() : 0;
    }

    /**
     * Returns start column number
     * @return the column
     */
    public int getStartColumn()
    {
        return headSourceItem != null ? headSourceItem.getBeginColumn() : 0;
    }

    /**
     * Returns last line number
     * @return the line
     */
    public int getLastLine()
    {
        return tailSourceItem != null ? tailSourceItem.getEndLine() : 0;
    }

    /**
     * Returns last column number
     * @return the column
     */
    public int getLastColumn()
    {
        return tailSourceItem != null ? tailSourceItem.getEndColumn() : 0;
    }

    /**
     * Returns source document id
     * @return the sourceDocumentId
     */
    public int getSourceDocumentId()
    {
        return sourceDocumentId;
    }

    /**
     * Add source item
     * @param sourceItem Source item
     */
    public void addSourceItem(SourceItem sourceItem)
    {
        if (headSourceItem == null)
        {
            headSourceItem = sourceItem;
            tailSourceItem = sourceItem;
        }
        else
        {
            tailSourceItem.setNext(sourceItem);
            tailSourceItem = sourceItem;
        }
    }

    /**
     * Add items contained in given source marker
     * @param sourceMarker Source marker
     */
    public void addSourceMarker(SourceMarker sourceMarker)
    {
        SourceItem sourceItem = sourceMarker.getHeadSourceItem();
        if (sourceItem != null) {
        	addSourceItem(sourceItem);
        	tailSourceItem = sourceMarker.getTailSourceItem();
        }
    }
    
    /**
     * Append to current source item given extent and information to append
     * @param extent Extent
     * @param information Information
     */
    public void appendSourceItem(Extent extent, String... information)
    {
    	SourceItem sourceItem = getLastSourceItem();
    	if (sourceItem != null)
    		sourceItem.append(extent, information);
    }
    
    /**
     * Returns head source item
     * @return the headSourceItem
     */
    public SourceItem getHeadSourceItem()
    {
        return headSourceItem;
    }

    /**
     * Returns tail source item
     * @return the tailSourceItem
     */
    public SourceItem getTailSourceItem()
    {
        return tailSourceItem;
    }

    /**
     * Returns last source item
     * @return SourceItem object or null if source marker is empty
     */
    public SourceItem getLastSourceItem() {
    	return  tailSourceItem != null ? tailSourceItem : headSourceItem;
    }
    
    /**
     * Checks that this source marker has at least one item with a short circuit.
     * @throws SyntaxException if check fails
     */
    public void checkForShortCircuit() throws SyntaxException
    {
        SourceItem sourceItem = getHeadSourceItem();
        while (sourceItem != null)
        {
            if (sourceItem.getInformation().startsWith("?"))
                return;
            sourceItem = sourceItem.getNext();
        }
        throw new SyntaxException(getHeadSourceItem().getInformation() + " has loop with no short circuit");
    }
    
    /**
     * compareTo - Order SourceMarker objects by qualified name
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(SourceMarker other)
    {
        int comparison = key - other.key;
        return comparison;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if (headSourceItem != null)
        	builder.append(headSourceItem.getInformation());
        else
        	return SourceTracker.INPUT;
        builder.append(' ')
               .append(" (")
               .append(getStartLine())
               .append(',')
               .append(getStartColumn())
               .append(") (")
		       .append(getLastLine())
		       .append(',')
		       .append(getLastColumn())
		       .append(')');
        return builder.toString();
    }

}
