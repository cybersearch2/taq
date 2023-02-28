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

import java.util.concurrent.ConcurrentSkipListMap;

import au.com.cybersearch2.taq.language.OperatorEnum;

/**
 * OperatorMap Converts between operator as a String and as an enum. Also
 * supports operator as a Char for single character operators
 * 
 * @author Andrew Bowley 30 Nov 2014
 */
public class OperatorMap {
	/** Maps Char to enum */
	static class OperatorChar {
		char operator;
		OperatorEnum operatorEnum;

		public OperatorChar(char operator, OperatorEnum operatorEnum) {
			this.operator = operator;
			this.operatorEnum = operatorEnum;
		}
	}

	/** Maps String to enum */
	static class OperatorString {
		String operator;
		OperatorEnum operatorEnum;

		public OperatorString(String operator, OperatorEnum operatorEnum) {
			this.operator = operator;
			this.operatorEnum = operatorEnum;
		}

		public OperatorString(OperatorChar operatorChar) {
			this.operator = Character.toString(operatorChar.operator);
			this.operatorEnum = operatorChar.operatorEnum;
		}
	}

	/** Initialization Char data */
	protected OperatorChar[] operatorChars = new OperatorChar[] { 
			new OperatorChar('=', OperatorEnum.ASSIGN),
			new OperatorChar('<', OperatorEnum.LT), new OperatorChar('>', OperatorEnum.GT),
			new OperatorChar('!', OperatorEnum.NOT), new OperatorChar('~', OperatorEnum.TILDE),
			new OperatorChar('+', OperatorEnum.PLUS), new OperatorChar('-', OperatorEnum.MINUS),
			new OperatorChar('*', OperatorEnum.STAR), new OperatorChar('/', OperatorEnum.SLASH),
			new OperatorChar('&', OperatorEnum.BIT_AND), new OperatorChar('|', OperatorEnum.BIT_OR),
			new OperatorChar('^', OperatorEnum.XOR), new OperatorChar('%', OperatorEnum.REM),
			new OperatorChar(',', OperatorEnum.COMMA), new OperatorChar('?', OperatorEnum.HOOK),
			new OperatorChar(':', OperatorEnum.COLON) };

	/** Initialization String data */
	protected OperatorString[] operatorStrings = new OperatorString[] { 
			new OperatorString("==", OperatorEnum.EQ),
			new OperatorString("<=", OperatorEnum.LE), new OperatorString(">=", OperatorEnum.GE),
			new OperatorString("!=", OperatorEnum.NE), new OperatorString("||", OperatorEnum.SC_OR),
			new OperatorString("&&", OperatorEnum.SC_AND), new OperatorString("++", OperatorEnum.INCR),
			new OperatorString("--", OperatorEnum.DECR), new OperatorString("<<", OperatorEnum.LSHIFT),
			new OperatorString(">>", OperatorEnum.RSIGNEDSHIFT), new OperatorString(">>>", OperatorEnum.RUNSIGNEDSHIFT),
			new OperatorString("+=", OperatorEnum.PLUSASSIGN), new OperatorString("-=", OperatorEnum.MINUSASSIGN),
			new OperatorString("*=", OperatorEnum.STARASSIGN), new OperatorString("/=", OperatorEnum.SLASHASSIGN),
			new OperatorString("&=", OperatorEnum.ANDASSIGN), new OperatorString("|=", OperatorEnum.ORASSIGN),
			new OperatorString("^=", OperatorEnum.XORASSIGN), new OperatorString("%=", OperatorEnum.REMASSIGN),
			new OperatorString("<<", OperatorEnum.LSHIFTASSIGN),
			new OperatorString(">>", OperatorEnum.RSIGNEDSHIFTASSIGN),
			new OperatorString(">>>", OperatorEnum.RUNSIGNEDSHIFTASSIGN) };

	/** Skip list map for good operator look up performance */
	protected ConcurrentSkipListMap<String, OperatorEnum> operatorMap;

	/**
	 * Construct an OperatorMap object
	 */
	public OperatorMap() {
		operatorMap = new ConcurrentSkipListMap<>();
		for (OperatorString operatorString : operatorStrings) {
			operatorMap.put(operatorString.operator, operatorString.operatorEnum);
		}

		for (OperatorChar operatorChar : operatorChars) {
			operatorMap.put(Character.toString(operatorChar.operator), operatorChar.operatorEnum);
		}
	}

	/**
	 * Returns Operator as enum
	 * 
	 * @param operator String
	 * @return OperatorEnum
	 */
	public OperatorEnum get(String operator) {
		if (operator == null)
			throw new IllegalArgumentException("Parameter \"operator\" is null");
		OperatorEnum operatorEnum = operatorMap.get(operator);
		if (operatorEnum == null)
			return OperatorEnum.UNKOWN;
		return operatorEnum;
	}

	/**
	 * Returns Operator as enum
	 * 
	 * @param operator char
	 * @return OperatorEnum
	 */
	public OperatorEnum get(char operator) {
		OperatorEnum operatorEnum = operatorMap.get(Character.toString(operator));
		if (operatorEnum == null)
			return OperatorEnum.UNKOWN;
		return operatorEnum;
	}

	/**
	 * Returns operator enum corresponding to specified text
	 * 
	 * @param operator Operator
	 * @return OperatorEnum
	 */
	public OperatorEnum convertOperator(String operator) {
		return operatorMap.get(operator);
	}

	/**
	 * Returns operator enum corresponding to specified char
	 * 
	 * @param operatorCharacter char
	 * @return OperatorEnum
	 */
	public OperatorEnum convertOperatorChar(char operatorCharacter) {
		return operatorMap.get(Character.toString(operatorCharacter));
	}

	/**
	 * Returns String value of Operator enum
	 * 
	 * @param operatorEnum OperatorEnum
	 * @return String
	 */
	public String toString(OperatorEnum operatorEnum) {
		for (OperatorString operatorString : operatorStrings) {
			if (operatorEnum == operatorString.operatorEnum)
				return operatorString.operator;
		}

		for (OperatorChar operatorChar : operatorChars) {
			if (operatorEnum == operatorChar.operatorEnum)
				return Character.toString(operatorChar.operator);
		}
		return "?";
	}

}
