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
package app.entities_axioms;

import java.util.List;

import app.AppCompletionHandler;

public class HighCitiesSorted3Handler extends AppCompletionHandler {

	String[] expected = new String[] { 
			"Exported cities:",
			"",
			"1	denver,5280",
			"2	flagstaff,6970",
			"3	addis ababa,8000",
			"4	leadville,10200"
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			compareArray(expected, lineBuffer);
		}

}
