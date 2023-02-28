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

public class CalculateSquareMiles3Handler extends AppCompletionHandler {

	String[] expected = new String[] { 
			"Locale = australia",
			"country_area(country=Australia, surface_area=7741220.0, units=km2)",
			"country_area(country=United States, surface_area=9831510.0, units=km2)",
			"Locale = usa",
			"us_surface_area_query",
			"country_area(country=Australia, surface_area=2988885.042, units=mi2)",
			"country_area(country=United States, surface_area=3795946.011, units=mi2)",
			"Locale = global",
			"country_area(country=Australia, surface_area=7741220.0, units=km2)",
			"country_area(country=United States, surface_area=9831510.0, units=km2)"
		};

		@Override
		public void onAppComplete(List<String> lineBuffer) {
			compareArray(expected, lineBuffer);
		}

}
