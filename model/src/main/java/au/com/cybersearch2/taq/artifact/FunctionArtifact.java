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
package au.com.cybersearch2.taq.artifact;

import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Call to perform an operation and possibly return a result
 */
public interface FunctionArtifact {

	/**
	 * Returns function qualified name
	 * @return QualifiedName object
	 */
	QualifiedName getName();
	
	/**
	 * Returns receiver template bound to this function
	 * @return ITemplate or null if none
	 */
	ITemplate getReceiver();
	
	/**
	 * Future feature - Set single quoted string parameter
	 * @param quote String readable by Java  Scanner
	 */
	void setQuote(String quote);
	
	/**
	 * Set receiver template bound to this function
	 * @param receiver Template
	 */
	void setReceiver(ITemplate receiver);
	
	/**
	 * Set template which evaluates the function's parameters
	 * @param parametersTemplate Template
	 */
	void setParametersTemplate(ITemplate parametersTemplate);
	
}
