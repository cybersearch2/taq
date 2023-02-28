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

public class Lists2Handler extends AppCompletionHandler {

	String[] expected = new String[] { 
	    "London lists",
	    "",
	    "List: fruit",
	    "0=strawberry",
	    "1=cherry",
	    "2=peach",
	    "size=3",
	    "List: dice",
	    "0=2",
	    "1=5",
	    "2=1",
	    "size=3",
	    "List: dimensions",
	    "0=12.54",
	    "1=6.98",
	    "2=9.12",
	    "size=3",
	    "List: roaches",
	    "7,372,036,854,775,530",
	    "size=1",
	    "List: movies",
	    "movie_1=greatest(The Godfather)",
	    "movie_2=greatest(The Shawshank Redemption)",
	    "movie_3=greatest(Schindler's List)",
	    "size=3",
	    "List: flags",
	    "0=true",
	    "1=false",
	    "size=2",
	    "List: stars",
	    "0=Sirius",
	    "1=Canopus",
	    "2=Rigil Kentaurus",
	    "size=3",
	    "",
	    "New York lists",
	    "",
	    "List: fruit",
	    "0=apple",
	    "1=pear",
	    "2=orange",
	    "size=3",
	    "List: dice",
	    "0=6",
	    "1=6",
	    "2=6",
	    "size=3",
	    "List: dimensions",
	    "0=16.84",
	    "1=9.08",
	    "2=11.77",
	    "size=3",
	    "List: roaches",
	    "35,223,372,036,854,775,691",
	    "size=1",
	    "List: movies",
	    "movie_1=greatest(Star Wars)",
	    "movie_2=greatest(Gone With The Wind)",
	    "movie_3=greatest(Spider Man)",
	    "size=3",
	    "List: flags",
	    "0=false",
	    "1=true",
	    "size=2",
	    "List: stars",
	    "0=Polarus",
	    "1=Betelgeuse",
	    "2=Vega",
	    "size=3"
	};

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		compareArray(expected, lineBuffer);
	}
}
