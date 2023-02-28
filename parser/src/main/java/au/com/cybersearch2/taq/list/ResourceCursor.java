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

import java.util.Iterator;

import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.provider.ResourceMonitor.EventHandler;

/**
 * Cursor linked to a resource provider
 */
public class ResourceCursor extends Cursor implements CursorList {

	/** 
	 * Handler for resource open and close events.
	 * This is needed to keep the cursor in sync with the resource satatus.
	 */
	private class CursorEventHandler implements EventHandler {

		@Override
		public void onOpen() {
			if (getDirection() == Direction.forward) {
				// Start to navigate resource items by iteration 
				iterator = resourceList.iterator();
				if (iterator.hasNext()) {
					if (!resourceList.isEmpty())
						resourceList.clear();
					resourceList.append(iterator.next());
					iteration = 1;
				}
			}
		}

		@Override
		public void onClose() {
			isFact = false;
			isActive = false;
			iterator = null;
			iteration = 0;
		}
		
	}
	
	/** ArrayList which is loaded with the resource content if non-iterative operation requested */
	private final ResourceList resourceList;
    /** Qualified name of list */
	private final  QualifiedName qname;

	/** Provider iterator returned from provider on going active */
	private Iterator<Axiom> iterator;
	/** Iteration count incremented until provider iterator hasNext() returns false */
	private int iteration;

	/**
	 * Construct ResourceCursor object
	 * @param cursorQname Qualified name of Variable - list name with "_length" appended 
     * @param resourceList ArrayList which is loaded with the resource content if non-iterative operation requested
	 * @param isReverse Flag set true if initial direction is reverse
	 */
	public ResourceCursor(QualifiedName cursorQname, ResourceList resourceList, boolean isReverse) {
		super(cursorQname, null, isReverse);
		this.resourceList = resourceList;
		qname = new QualifiedName(resourceList.getName());
		isActive = true;
		resourceList.addHandler(new CursorEventHandler());
	}

	public void evaluate() {
	}
	
	public void backup() {
		iteration = 1;
	}

    /**
     * getQualifiedListName
     * @see au.com.cybersearch2.taq.interfaces.ListItemSpec#getQualifiedListName()
     */
    @Override
    public QualifiedName getQualifiedListName()
    {
        return qname; 
    }

	@Override
	public Cursor getCursor() {
		return this;
	}

	@Override
	public ItemList<?> getItemList() {
		return resourceList;
	}

	@Override
    public void setIndex(int index, ItemList<?> itemList)
    {  
    	if ((index == 1) && (iterator != null)) {
    		 // Post increment with iterator available from open event
    		 // Do not change cursor index so only first item in list is accessed
			if (iterator.hasNext()) {
				if (iteration > 0)
					resourceList.assignItem(0, iterator.next());
			} else {
				isFact = false;
				itemList.clear();
			}
    	} else
    	    super.setIndex(index, itemList);
    }
}
