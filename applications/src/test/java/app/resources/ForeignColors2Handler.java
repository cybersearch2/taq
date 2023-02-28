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
package app.resources;

import java.util.List;

import app.AppCompletionHandler;

public class ForeignColors2Handler extends AppCompletionHandler {

	String[] expected = new String[] { 
			"colors(aqua=Wasser, black=schwarz, blue=blau, white=weiß)",
			"colors(aqua=bleu vert, black=noir, blue=bleu, white=blanc)",
			"color_query(name=bleu vert, red=0, green=255, blue=255)",
			"color_query(name=noir, red=0, green=0, blue=0)",
			"color_query(name=blanc, red=255, green=255, blue=255)",
			"color_query(name=bleu, red=0, green=0, blue=255)",
			"color_query(name=Wasser, red=0, green=255, blue=255)",
			"color_query(name=schwarz, red=0, green=0, blue=0)",
			"color_query(name=weiß, red=255, green=255, blue=255)",
			"color_query(name=blau, red=0, green=0, blue=255)"
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			compareArray(expected, lineBuffer);
		}

}
