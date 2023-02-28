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
package app.functions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import app.AppCompletionHandler;

public class Types2Handler extends AppCompletionHandler {

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		assertThat(lineBuffer.isEmpty()).isFalse();
		List<String> truncated = new ArrayList<>();
		int index = lineBuffer.size() - 1;
		truncated.addAll(lineBuffer);
		truncated.remove(index);
		compareFile(truncated, "functions/types2.txt");
		if (!lineBuffer.get(index).startsWith("Timestamp") || 
			!lineBuffer.get(index).endsWith("(Type=String)")) {
        	lineBuffer.forEach(err -> System.err.println(err));
			assertThat(lineBuffer.get(index).startsWith("Timestamp"));
			assertThat(lineBuffer.get(index).endsWith("(Type=String)"));
		}
	}

}
