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

import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.db.ColumnSetter;
import au.com.cybersearch2.taq.db.EntityEmitter;
import au.com.cybersearch2.taq.db.StatementRunner;

/**
 * GenericEntityEmitter
 * Extends EntityEmitter to suit Sqlite database
 *
 * @param <E> Entity type
 */
public class GenericEntityEmitter<E> extends EntityEmitter<E> {

	/** Translates an entity-object to values placed in a database row insertion statement */
    private ColumnSetter<E> columns;
    /** Inserts a database row mapped to an entity object */
    private StatementRunner<E> statementRunner;

    /**
     * Construct GenericEntityEmitter object
     * @param entityClass Entity class
     */
	public GenericEntityEmitter(Class<E> entityClass) {
		super(entityClass);
	}

	public ColumnSetter<E> getColumnSetter() throws ExecutionException {
		if (columns == null)
	    	columns = new ColumnSetter<E>((Class<E>) getEntityClass());
		return columns;
	}

	@Override
	public StatementRunner<E> getStatementRunner() throws ExecutionException {
		if (statementRunner == null)
		    statementRunner = new GenericStatementRunner<>(this, getColumnSetter());
		return statementRunner;
	}
	
	@Override
	public void onPostExecute(boolean success) 
	{
	}

	@Override
	public void onRollback(Throwable rollbackException) 
	{
	}

	@Override
	public String getDescription() {
		return String.format("Performs Sqlite database inserts for %s table", getEntityClass().getSimpleName());
	}


	@Override
	public void internalEmitData(E entity) throws InterruptedException, ExecutionException {
		getStatementRunner().insertEntity(entity);
	}


}
