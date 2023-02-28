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
 * Contains one or two DaulIndex objects
 */
public class ListReference {

	/** First order list item reference */
	private final DualIndex listItemSpec1;
	/** Optional second order list item reference */
	private final DualIndex listItemSpec2;
	
	
	public ListReference(DualIndex listItemSpec1) {
		this.listItemSpec1 = listItemSpec1;
		this.listItemSpec2 = null;
	}

	public ListReference(DualIndex listItemSpec1, DualIndex listItemSpec2) {
		this.listItemSpec1 = listItemSpec1;
		this.listItemSpec2 = listItemSpec2;
	}

	public int dimension() {
		return listItemSpec2 == null ? 1 : 2;
	}
	
	public DualIndex getListItemSpec1() {
		return listItemSpec1;
	}

	public DualIndex getListItemSpec2() {
		return listItemSpec2;
	}

}
