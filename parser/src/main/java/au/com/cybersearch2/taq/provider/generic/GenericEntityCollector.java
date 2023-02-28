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
package au.com.cybersearch2.taq.provider.generic;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.db.EntityCollector;
import au.com.cybersearch2.taq.db.ObjectSelector;

/**
 * GenericEntityCollector
 * Extends EntityColletor to suit Sqlite database
 *
 * @param <Entity> Entity type
 */
public class GenericEntityCollector<Entity> extends EntityCollector<Entity> {

    private List<Entity> resultList;
	
	/**
	 * Construct GenericEntityCollector object. Call setConnection() before use.
	 * @param entityClass Entity class
	 */
	public GenericEntityCollector(Class<Entity> entityClass) {
		super(entityClass);
	}

	public List<Entity> getResultList() {
		return resultList != null ? resultList : Collections.emptyList();
	}

	@Override
	public ObjectSelector<Entity> getObjectSelector() throws ExecutionException {
		return new GenericQueryRunner<>(this);
	}
	
	@Override
	public String getDescription() {
		return String.format("Queries Sqlite database for %s table rows", getEntityClass().getSimpleName());
	}

	@Override
	protected List<Entity> internalGetData() throws InterruptedException, ExecutionException {
		GenericQueryRunner<Entity> queryRunner = new GenericQueryRunner<>(this);
		resultList = doQuery(queryRunner);
		return resultList;
	}


	
}
