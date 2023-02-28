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
package app.basic_lists;

import java.util.List;

import app.AppCompletionHandler;

public class ListsHandler extends AppCompletionHandler {

	String[] expected = new String[] { 
	   "",
	    "List: fruit",
	    "apple",
	    "pear",
	    "orange",
	    "size=3",
	    "",
	    "List: dice",
	    "2",
	    "5",
	    "1",
	    "size=3",
	    "",
	    "List: dimensions",
	    "12.54",
	    "6.98",
	    "9.12",
	    "size=3",
	    "",
	    "List: huges",
	    "9223372036854775808",
	    "-9223372036854775808",
	    "size=2",
	    "",
	    "List: flags",
	    "true",
	    "false",
	    "size=2",
	    "",
	    "List: stars",
	    "Sirius",
	    "Canopus",
	    "Rigil Kentaurus",
	    "size=3",
	    "",
	    "List: movies",
	    "movie_1=greatest(The Godfather, Francis Ford Coppola)",
	    "movie_2=greatest(The Shawshank Redemption, Frank Darabont)",
	    "movie_3=greatest(Schindler's List, Steven Spielberg)",
	    "size=3"
	};

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		compareArray(expected, lineBuffer);
	}

}
