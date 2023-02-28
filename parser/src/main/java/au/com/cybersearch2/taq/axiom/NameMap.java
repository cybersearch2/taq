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
package au.com.cybersearch2.taq.axiom;

/**
 * NameMap
 * Maps a term name to a term position and Java field name
 * @author Andrew Bowley
 * 19 Mar 2015
 */
public class NameMap implements Comparable<NameMap> 
{
	protected String termName;
	protected int position;
	protected String fieldName;
	
	/**
	 *  Construct a NameMap object
	 *  @param termName Term name
	 *  @param fieldName Field name - can be overridden
	 */
	public NameMap(String termName, String fieldName) 
	{
		this.termName = termName;
		this.fieldName = fieldName;
	}

	/**
	 *  Copy constructor
	 *  @param nameMap Object to copy
	 */
	public NameMap(NameMap nameMap) 
	{
		termName = nameMap.termName;
		fieldName = nameMap.fieldName;
	}
	
	/**
	 * @return the termName
	 */
	public String getTermName() 
	{
		return termName;
	}

	/**
	 * @param termName the termName to set
	 */
	public void setTermName(String termName) 
	{
		this.termName = termName;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() 
	{
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) 
	{
		this.fieldName = fieldName;
	}

    /**
     * @return the position
     */
    public int getPosition()
    {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position)
    {
        this.position = position;
    }

	@Override
	public int compareTo(NameMap other) {
		return fieldName.compareTo(other.fieldName);
	}

	@Override
	public int hashCode() {
		return fieldName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NameMap))
			return false;
		return fieldName.equals(((NameMap)obj).fieldName);
	}

}
