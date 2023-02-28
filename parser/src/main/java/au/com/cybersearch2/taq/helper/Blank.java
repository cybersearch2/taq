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
package au.com.cybersearch2.taq.helper;

/**
 * Blank
 * Class to represent empty value that is replaceable by unification
 * @author Andrew Bowley
 * 1 Sep 2015
 */
public class Blank implements Comparable<Blank>
{
    public static final String BLANK = "";
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Blank o)
    {
        // All blank objects are equal
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof Blank)
        	return true;
        if (other != null)
        	return other.toString().equals(BLANK);
        return false;
    }

    @Override
	public int hashCode() {
		return 0;
	}

	/**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() 
    {
        return BLANK;
    }

}
