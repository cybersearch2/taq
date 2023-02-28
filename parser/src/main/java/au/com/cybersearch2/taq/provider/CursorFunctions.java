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
package au.com.cybersearch2.taq.provider;

import au.com.cybersearch2.taq.expression.CursorSentinelOperand;

/**
 * Cursor functions available by object interface.
 * index() returns current cursor index
 *
 */
public class CursorFunctions {

	public final CursorSentinelOperand cursorOperand;

	/**
	 * Construct CursorFunctions object
	 * @param cursorOperand Cursor sentinal operand
	 */
	public CursorFunctions(CursorSentinelOperand cursorOperand) {
		this.cursorOperand = cursorOperand;
	}

	public long index() {
		return cursorOperand.getIndex();
	}
}
