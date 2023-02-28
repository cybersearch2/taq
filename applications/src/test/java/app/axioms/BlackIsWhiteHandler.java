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
package app.axioms;

import java.util.List;

import app.AppCompletionHandler;

public class BlackIsWhiteHandler extends AppCompletionHandler {

	String[] expected = new String[] { 
		"color_query(name=\"white\", red=255, green=255, blue=255)",
		"dyna_query(dyna_list(name=white, red=255, green=255, blue=255))",
		"list_query(axiom_list(name=\"white\", red=255, green=255, blue=255))"
	};

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		compareArray(expected, lineBuffer);
	}

}