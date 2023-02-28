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
package au.com.cybersearch2.taq.interfaces;

import java.util.List;


/**
 * DataCollector
 * Interface to persistence system used by axiom providers
 * @author Andrew Bowley
 * 10 Feb 2015
 */
public interface DataCollector  
{
	/**
	 * Returns list of objects from persistence system. 
	 * Note calling thread may be blocked waiting for results
	 * @return Object collection
	 */
	List<?> getData();
	/**
	 * Returns flag set true if a call to getData() may deliver more results.
	 * @return boolean
	 */
	boolean isMoreExpected();
}
