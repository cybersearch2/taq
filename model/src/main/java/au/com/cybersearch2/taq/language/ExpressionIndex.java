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
 * List item reference by expression
 */
public class ExpressionIndex {

	/** The expression */
	private final IOperand expression;

	/**
	 * Construct ExpressionIndex object
	 * @param expression Expression to reference the list
	 */
	public ExpressionIndex(IOperand expression) {
		this.expression = expression;
	}

	public IOperand getExpression() {
		return expression;
	}

	@Override
	public String toString() {
		return "[" + expression.toString() + "]";
	}

}
