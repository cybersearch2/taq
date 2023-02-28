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
package app.axioms;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import app.AppCompletionHandler;

public class GamingHandler extends AppCompletionHandler {

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		if (lineBuffer.size() >= 3) {
			Iterator<String> iterator = lineBuffer.iterator();
			for (int i = 0; i < 3; ++i) {
				String fruit = iterator.next();
				boolean ok =
						fruit.contains("apple") &&
						fruit.contains("banana") &&
						fruit.contains("lemon") &&
						fruit.contains("orange");
				if (!ok) {
		           	lineBuffer.forEach(err -> System.err.println(err));
		            assertTrue(fruit.contains("apple"));
		            assertTrue(fruit.contains("banana"));
		            assertTrue(fruit.contains("lemon"));
		            assertTrue(fruit.contains("orange"));
		            return;
				}
			}
		} else {
           	lineBuffer.forEach(err -> System.err.println(err));
           	assertTrue(lineBuffer.size() >= 3);
		}

	}

}
