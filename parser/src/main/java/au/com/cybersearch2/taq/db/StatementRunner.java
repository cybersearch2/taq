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
 * Interface for inserting a database row mapped to an entity object
 * @param <E> Entity type
 */
public interface StatementRunner<E> {

	/**
	 * Inserts a database row mapped to givenn entity object
	 * @param entity Entity object
	 * @return Id of new row
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	int insertEntity(E entity) throws InterruptedException, ExecutionException;

}
