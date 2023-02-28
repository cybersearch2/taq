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
package app.scopes;

import java.util.List;

import app.AppCompletionHandler;

public class ForeignColorsHandler extends AppCompletionHandler {

	String[] expected = new String[] { 
			"color(shade=Wasser, red=0, green=255, blue=255)",
			"color(shade=schwarz, red=0, green=0, blue=0)",
			"color(shade=weiß, red=255, green=255, blue=255)",
			"color(shade=blau, red=0, green=0, blue=255)",
			"color(shade=bleu vert, red=0, green=255, blue=255)",
			"color(shade=noir, red=0, green=0, blue=0)",
			"color(shade=blanc, red=255, green=255, blue=255)",
			"color(shade=bleu, red=0, green=0, blue=255)"
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			compareArray(expected, lineBuffer);
		}

}
