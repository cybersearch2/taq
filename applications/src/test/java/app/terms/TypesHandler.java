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
package app.terms;

import java.util.List;

import app.AppCompletionHandler;

public class TypesHandler extends AppCompletionHandler {

	String[] expected = new String[]{
		"Boolean=true(Boolean)",
		"String=penguins(String)",
		"Integer=12345(Long)",
		"Double=123400.0(Double)",
		"Decimal=1234.56(BigDecimal)",
		"Currency=12345.67(BigDecimal)"
	};
	
	@Override
	public void onAppComplete(List<String> lineBuffer) {
		compareArray(expected, lineBuffer);
	}

}
