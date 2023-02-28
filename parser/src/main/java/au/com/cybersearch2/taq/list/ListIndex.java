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

/**
 * References an array list item or term of an axiom list item
 */
public class ListIndex {

	private final int position;

	protected int index;

	public ListIndex(int index) {
		this.index = index;
		position = -1;
	}

	public ListIndex(int index, int position) {
		this.index = index;
		this.position = position;
	}

	public int getIndex() {
		return index;
	}

	public void incrementIndex() {
		index++;
	}

	public int getPosition() {
		return position;
	}

}
