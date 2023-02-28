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
package app.queries;

import java.util.List;

import app.AppCompletionHandler;

public class CustomerChargeHandler extends AppCompletionHandler {

	String[] expected = new String[]{
		    "freight(charge=23.99, city=Athens)",
		    "customer_freight(name=Acropolis Construction, city=Athens, charge=23.99)",
		    "freight(charge=13.99, city=Sparta)",
		    "customer_freight(name=Marathon Marble, city=Sparta, charge=13.99)",
		    "freight(charge=13.99, city=Sparta)",
		    "customer_freight(name=Agora Imports, city=Sparta, charge=13.99)",
		    "freight(charge=17.99, city=Milos)",
		    "customer_freight(name=Spiros Theodolites, city=Milos, charge=17.99)"
		};
	
	@Override
	public void onAppComplete(List<String> lineBuffer) {
		compareArray(expected, lineBuffer);
	}

}
