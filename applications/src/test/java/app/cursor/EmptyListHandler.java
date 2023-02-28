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
package app.cursor;

import java.util.List;

import app.AppCompletionHandler;

public class EmptyListHandler extends AppCompletionHandler {

	String[] expected = new String[] { 
			"empty_list(value=NaN, backward=-1, inc=NaN, dec=NaN, index=-1, isFact=false)"
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			compareArray(expected, lineBuffer);
		}

}
