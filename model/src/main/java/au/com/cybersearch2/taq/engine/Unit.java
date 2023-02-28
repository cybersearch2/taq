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

import au.com.cybersearch2.taq.language.TaqLiteral;

/**
 * Information extracted from a parser input token required for debugging.
 * @author Andrew Bowley
 *
 */
public class Unit {

	/** The line number of the first character of this Token */
	private final int beginLine;
	/** The column number of the first character of this Token */
	private final int beginColumn;
	/** The token identity translated to a enumeration value from original parser constant */
	private final TaqLiteral kind;

  /**
   * Construct Unit object
   * @param beginLine The line number of the first character of this Token
   * @param beginColumn The column number of the first character of this Token
   * @param kind The token identity
   */
	public Unit(int beginLine, int beginColumn, TaqLiteral kind) {
		super();
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
		this.kind = kind;
	}

	public int getBeginLine() {
		return beginLine;
	}

	public int getBeginColumn() {
		return beginColumn;
	}

	public TaqLiteral getKind() {
		return kind;
	}
}
