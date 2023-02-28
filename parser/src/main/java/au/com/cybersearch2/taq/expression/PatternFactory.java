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
package au.com.cybersearch2.taq.expression;

import au.com.cybersearch2.taq.interfaces.Operand;

/**
 * PatternOperand
 * Supplies PatternOperand instances to use in association with regular expressions 
 * @author Andrew Bowley
 * 31 Oct 2020
 */
public class PatternFactory implements Comparable<PatternFactory> {

	private String name;
	/** Optional flags to modify regular expression behavior */
	private int flags;
	/** Regular expression operand */
	private Operand regexOp;

	/**
	 * Construct PatternOperand object using literal or variable pattern
	 * must be embedded in the pattern. 
     * @param name Name
	 * @param regexOp Regular expression operand
	 * @param flags Optional flags such as 'case-insensitive'
	 */
     public PatternFactory(String name, Operand regexOp, int flags) {
    	 this.name = name;
    	 this.regexOp = regexOp;
    	 this.flags = flags;
     }

	public String getName() {
		return name;
	}

	public int getFlags() {
		return flags;
	}

	/**
	 * Returns pattern operand. This may need to be evaluated.
	 * @return
	 */
	Operand getPattenOperand() {
		Operand patternOp;
		if (regexOp.isEmpty())
			patternOp = new StringOperand(regexOp.getQualifiedName(), regexOp.getLeftOperand());
		else
			patternOp = regexOp;
		return patternOp;
	}
	
	@Override
	public int compareTo(PatternFactory other) {
		return name.compareTo(other.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PatternFactory)
			return name.equals(((PatternFactory)obj).name);
		return false;
	}

	@Override
	public String toString() {
		return name;
	}
}
