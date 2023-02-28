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
package au.com.cybersearch2.taq.db;

import java.util.concurrent.ExecutionException;

/**
 * Interface for translating a database row selected by primary key to an entity object
 * @param <E> Entity type
 */
public interface ObjectSelector<E> {

	/**
	 * Returns entity object translated from a dataabse row selected by primary key
	 * @param id Primary key (only int type supported)
	 * @return Entity object
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	E getObjectById(int id) throws InterruptedException, ExecutionException;
}
