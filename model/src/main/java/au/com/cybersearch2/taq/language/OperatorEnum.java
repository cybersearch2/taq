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

/**
 * Operator
 * @author Andrew Bowley
 *
 * @since 03/10/2010
 */
public enum OperatorEnum
{
    ASSIGN("="), // 
    LT("<"), // 
    GT(">"), // 
    NOT("!"), // 
    TILDE("~"), // 
    HOOK("?"), // 
    COLON(":"), // 
    EQ("=="), // 
    LE("<="), // 
    GE(">="), // 
    NE("!="), // 
    SC_OR("||"), // 
    SC_AND("&&"), // 
    INCR("++"), // 
    DECR("--"), // 
    PLUS("+"), // 
    MINUS("-"), // 
    STAR("*"), // 
    SLASH("/"), // 
    BIT_AND("&"), // 
    BIT_OR("|"), // 
    XOR("^"), // 
    REM("%"), // 
    LSHIFT("<<"), // 
    RSIGNEDSHIFT(">>"), // 
    RUNSIGNEDSHIFT(">>>"), // 
    PLUSASSIGN("+="), // 
    MINUSASSIGN("-="), // 
    STARASSIGN("*="), // 
    SLASHASSIGN("/="), // "/="
    ANDASSIGN("&="), // 
    ORASSIGN("|="), // 
    XORASSIGN("^="), // 
    REMASSIGN("%="), // 
    LSHIFTASSIGN("<<="), // 
    RSIGNEDSHIFTASSIGN(">>="), // 
    RUNSIGNEDSHIFTASSIGN(">>>="), // 
    ELLIPSIS("..."), // ,
    COMMA(","),
    UNKOWN(" ");

	private OperatorEnum(String symbol) {
		this.symbol = symbol;
	}
	
	private String symbol;
	
	public String getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}

}

