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

import au.com.cybersearch2.taq.artifact.NameIndex;

/**
* List index containing either an ExpressionIndex or NameIndex object
*
*/
public class DualIndex {

private final NameIndex nameIndex;
private final ExpressionIndex expressionIndex;

/**
 * Construct ParserListIndex object to contain a NameIndex object
 * @param nameIndex Name index
 */
public DualIndex(NameIndex nameIndex) {
	this.nameIndex = nameIndex;
	expressionIndex = null;
}

/**
 * Construct ParserListIndex object to contain a ExpressionIndex object
 * @param expressionIndex Expression index
 */
public DualIndex(ExpressionIndex expressionIndex) {
	this.expressionIndex = expressionIndex;
	nameIndex = null;
}

public boolean hasName() {
	return nameIndex != null;
}

public boolean hasExpression() {
	return expressionIndex != null;
}

public String getName() {
	return hasName() ? nameIndex.getName() : null;
}

public IOperand getExpression() {
	return hasExpression() ? expressionIndex.getExpression() : null;
}

@Override
public String toString() {
	return hasName() ? nameIndex.toString() : expressionIndex.toString();
}

}
