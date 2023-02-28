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
package au.com.cybersearch2.taq.list;

import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Specification to build a list appender
 */
public class AppenderSpec {

	private final QualifiedName listName;
	private final ListItemSpec[] indexDataArray;
	private final ListType listType;
	private final String operator; 
	private Operand expression;
	private ItemList<?> itemList;
	
    /**
     * Construct AppenderSpec object
     * @param listName Qualified name of list to append
     * @param indexDataArray List item reference
     * @param listType Distinguishes between basic and axiom type lists
     * @param operator Can be "+=" or "="
     * @param expression Left hand side of operation to be performed
     */
	public AppenderSpec(QualifiedName listName, 
			            ListItemSpec[] indexDataArray,
			            ListType listType,
			            String operator, 
			            Operand expression) {
		this.listName = listName;
		this.indexDataArray = indexDataArray;
		this.listType = listType;
		this.operator = operator;
		this.expression = expression;
	}


	public ItemList<?> getItemList() {
		return itemList;
	}


	public void setItemList(ItemList<?> itemList) {
		this.itemList = itemList;
	}


	public QualifiedName getListName() {
		return listName;
	}


	public ListItemSpec[] getIndexDataArray() {
		return indexDataArray;
	}

	public ListType getListType() {
		return listType;
	}


	public String getOperator() {
		return operator;
	}


	public Operand getExpression() {
		return expression;
	}

	public QualifiedName getDefaultVariableName() {
		return (new QualifiedName(listName.getName() + listName.incrementReferenceCount()));
	}

	public Operand clearExpression() {
		Operand temp = expression;
		expression = null;
		return temp;
	}
}
