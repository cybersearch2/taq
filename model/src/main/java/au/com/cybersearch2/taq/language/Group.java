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

import java.util.ArrayList;
import java.util.List;

/**
 * A sequence of Operands which are assigned values when the owning Operand is evaluated.
 */
public class Group {

	/** Operand list. Position is significant. */
    private List<? super IOperand> groupList;

	/**
	 * Construct a Group object
	 */
	public Group() 
	{
		groupList = new ArrayList<>();
	}

	/**
	 * Adds operand to group
	 * @param operand Operand to add
	 */
	public void addGroup(IOperand operand)
	{
		groupList.add(operand);
	}

	/**
	 * Returns list of operands
	 * @return Operand List
	 */
	public List<? super IOperand> getGroupList()
	{
		return groupList;
	}

}
