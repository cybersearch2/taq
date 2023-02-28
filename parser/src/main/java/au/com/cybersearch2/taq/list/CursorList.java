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
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Encapsulates a cursor and it's associated list
 */
public interface CursorList {

	final static String IS_PART = "is_%s_%s";
	
    boolean isFact();
 
    QualifiedName getCursorQname();

	QualifiedName getQualifiedListName();

	Cursor getCursor();

	int getIndex();
	
	ItemList<?> getItemList();
	
    static QualifiedName getPartName(String part, Cursor cursor) // fact
    {
    	QualifiedName cursorQname = cursor.getCursorQname();
        return new QualifiedName(String.format(IS_PART, cursorQname.getName() , part), cursorQname);
    }
 
}
