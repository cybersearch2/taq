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
package au.com.cybersearch2.taq.language;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties to initialize a scope or resource
 */
public class InitialProperties {

	private final Map<String, Object> properties;
	
	public InitialProperties() {
		properties = new HashMap<>();
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * Process InitializerDeclaration production for setting a single property value
	 * @param qname Qualified name of property value
	 * @param value Property value
	 * @return source text
	 */
	public String initializerDeclaration(QualifiedName qname, Object value) {
	     String name = qname.toString();
	     properties.put(name, value);
	     return name + "=" + value.toString();
	}


}
