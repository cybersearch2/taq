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

/**
 * SourceItem
 * Marks the portion in a source document containing an item of
 * a language unit with a list structure eg. term of template
 * @author Andrew Bowley
 */
public class SourceItem
{
    /** Begin line in source document */
    private final int beginLine;
    /** Column in first line */
    private final int beginColumn;
    /** Line in source document */
    private int endLine;
    /** Column in last line */
    private int endColumn;
    /** Information available from parser eg. Operand.toString() */
    private String information;
    /** Next item to form chain */
    private SourceItem next;
    
    /**
     * Construct SourceItem object
     * @param token Parser token
     * @param extent Extent
     * @param information Information
     */
    public SourceItem(Unit token, Extent extent, String information)
    {
        this.beginLine = token.getBeginLine();
        this.beginColumn = token.getBeginColumn();
        this.information = information;
        setExtent(extent);
    }

    /**
     * Set end line and column from given extent
     * @param extent Extent
     */
    public void setExtent(Extent extent)
    {
        setEndLine(extent.getEndLine());
        setEndColumn(extent.getEndColumn());
    }
 
    /**
     * Append to source item given extent and information to append
     * @param extent Extent
     * @param information Information
     */
    public void append(Extent extent, String... information)
    {
    	if ((information == null) || (information.length == 0))
    		return;
    	setExtent(extent);
        for (String text: information)
        	this.information += text;
    }

    /**
     * Returns line where item starts
     * @return line number
     */
    public int getBeginLine() {
		return beginLine;
	}

    /**
     * Returns column where item starts
     * @return column number
     */
	public int getBeginColumn() {
		return beginColumn;
	}

    /**
     * Returns line where item ends
     * @return line number
     */
    public int getEndLine()
    {
        return endLine;
    }

    /**
     * Set line where item ends
     * @param endLine the endLine to set
     */
    public void setEndLine(int endLine)
    {
        this.endLine = endLine;
    }

    /**
     * Returns column where item ends
     * @return column number
     */
    public int getEndColumn()
    {
        return endColumn;
    }

    /**
     * Set column where item ends
     * @param endColumn the endColumn to set
     */
    public void setEndColumn(int endColumn)
    {
        this.endColumn = endColumn;
    }

    /**
     * Set item information
     * @param information Information
     */
    public void setInformation(String information)
    {
        this.information = information;
    }

    /**
     * Returns the item information
     * @return information
     */
    public String getInformation()
    {
        return information;
    }

    /**
     * Returns next source item or null if none
     * @return SourceItem object
     */
    public SourceItem getNext()
    {
        return next;
    }

    /**
     * Set next source item
     * @param next Source item
     */
    public void setNext(SourceItem next)
    {
        this.next = next;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(information);
        builder.append(" (")
                .append(beginLine)
                .append(',')
                .append(beginColumn)
                .append(')')
                .append(" (")
                .append(endLine)
                .append(',')
                .append(endColumn)
                .append(')');
        return builder.toString();
    }

}
