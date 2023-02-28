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

public class CitiesHandler extends AppCompletionHandler {

	String[] expected = new String[] { 
			"Imported cities:",
			"",
			"1	bilene,1718",
			"2	addis ababa,8000",
			"3	denver,5280",
			"4	flagstaff,6970",
			"5	jacksonville,8",
			"6	leadville,10200",
			"7	madrid,1305",
			"8	richmond,19",
			"9	spokane,1909",
			"10	wichita,1305",
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			compareArray(expected, lineBuffer);
		}

}
