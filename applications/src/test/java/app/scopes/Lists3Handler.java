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

public class Lists3Handler extends AppCompletionHandler {

	String[] expected = new String[] { 
	    "London lists",
	    "",
	    "List: roaches",
	    "7,372,036,854,775,530",
	    "size=1",
	    "List: movies",
	    "movie_1=greatest(The Godfather)",
	    "movie_2=greatest(The Shawshank Redemption)",
	    "movie_3=greatest(Schindler's List)",
	    "size=3",
	    "List: fruit",
	    "strawberry",
	    "cherry",
	    "peach",
	    "size=3",
	    "List: dice",
	    "2",
	    "5",
	    "1",
	    "size=3",
	    "List: dimensions",
	    "12.54",
	    "6.98",
	    "9.12",
	    "size=3",
	    "List: flags",
	    "true",
	    "false",
	    "size=2",
	    "List: stars",
	    "Sirius",
	    "Canopus",
	    "Rigil Kentaurus",
	    "size=3",
	    "",
	    "New York lists",
	    "",
	    "List: roaches",
	    "35,223,372,036,854,775,691",
	    "size=1",
	    "List: movies",
	    "movie_1=greatest(Star Wars)",
	    "movie_2=greatest(Gone With The Wind)",
	    "movie_3=greatest(Spider Man)",
	    "size=3",
	    "List: fruit",
	    "apple",
	    "pear",
	    "orange",
	    "size=3",
	    "List: dice",
	    "6",
	    "6",
	    "6",
	    "size=3",
	    "List: dimensions",
	    "16.84",
	    "9.08",
	    "11.77",
	    "size=3",
	    "List: flags",
	    "false",
	    "true",
	    "size=2",
	    "List: stars",
	    "Polarus",
	    "Betelgeuse",
	    "Vega",
	    "size=3"
	};

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		compareArray(expected, lineBuffer);
	}
}
