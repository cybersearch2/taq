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
package app.puzzles;

import java.util.List;

import app.AppCompletionHandler;

public class TowersOfHanoiHandler extends AppCompletionHandler {

	String[] expected = new String[] { 
			" n=1",
			"Move disk 1 from rod A to rod C",
			"",
			" n=2",
			"Move disk 1 from rod A to rod B",
			"Move disk 2 from rod A to rod C",
			"Move disk 1 from rod B to rod C",
			"",
			" n=3",
			"Move disk 1 from rod A to rod C",
			"Move disk 2 from rod A to rod B",
			"Move disk 1 from rod C to rod B",
			"Move disk 3 from rod A to rod C",
			"Move disk 1 from rod B to rod A",
			"Move disk 2 from rod B to rod C",
			"Move disk 1 from rod A to rod C"
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			compareArray(expected, lineBuffer);
		}

}
