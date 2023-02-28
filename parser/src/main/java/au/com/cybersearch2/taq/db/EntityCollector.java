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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.interfaces.DataCollector;
import au.com.cybersearch2.taq.query.QueryExecutionException;

/**
 * EntityCollector
 * 
 * Base class for executing database queries to supply axiom providers.
 * Abstract method internalGetData() must be implemented using the chosen 
 * persistence technology to perform queries.
 * @param <E> Entity type
 * @author Andrew Bowley
 */
public abstract class EntityCollector<E> extends QueryBase<E> implements DataCollector
{
	public interface QueryRunner<E> {
		
		/**
		 * Returns query result as an entity object list
		 * @return a list of objects of type E
		 */
		List<E> getResultList() throws InterruptedException, ExecutionException;
    }
	
    /** flag set true if a call to getData() may deliver more results */
    protected boolean moreExpected;

    /** Optional post-processing of collected entity objects */
    private BatchCollector<E,?> batchCollector;

    /**
     * Construct an EntityCollector object for a specific type
     * @param entityClass Class of entity to be collected
     */
    public EntityCollector(Class<E> entityClass)
    {
        super(entityClass);
    }

    /**
     * Returns text to identify this collector in messages 
     * @return description
     */
    abstract public String getDescription();

    /**
     * Returns object which translates a database row selected by primary key to an entity object
     * @return ObjectSelector object
     * @throws ExecutionException
     */
    abstract public ObjectSelector<E> getObjectSelector() throws ExecutionException;
    
    /**
     * Returns query result as an entity object list
     * @return a list of objects of type E
     * @throws InterruptedException
     * @throws ExecutionException
     */
    abstract protected List<E> internalGetData() throws InterruptedException, ExecutionException;

	/**
	 * Set batch collector
	 * @param batchCollector BatchCollector object
	 */
	public void setBatchCollector(BatchCollector<E, ?> batchCollector) {
		this.batchCollector = batchCollector;
	}

	@Override
	public List<?> getData() {
		List<E> data = null;
		try 
		{
			data = internalGetData();
		} 
		catch (InterruptedException e) 
		{
			return Collections.emptyList();
		} catch (ExecutionException e) {
			throw new QueryExecutionException(getDescription() + " failed", e.getCause());
		}
		if (data == null)
			throw new QueryExecutionException(getDescription() + " failed");
		if (batchCollector != null)
			return batchCollector.processBatch(data);
		return data;
	}

	@Override
	public boolean isMoreExpected() {
		return moreExpected;
	}

	@Override
	public void onPostExecute(boolean success) 
	{
        if (!success)
        {
        	moreExpected = false;
        }
	}

	@Override
	public void onRollback(Throwable rollbackException) 
	{
    	moreExpected = false;
	}

	/**
	 * Employ given query runner to return next list of entity objects.
	 * @param queryRunner Query runner
	 * @return list of objects of type E
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	protected List<E> doQuery(QueryRunner<E> queryRunner) throws InterruptedException, ExecutionException 
	{
        List<E> resultList = queryRunner.getResultList();
        if (getMaxResults() > 0)
        {   // Advance start position or 
        	// clear "moreExpected" flag if no more results avaliable
        	if (resultList.size() > 0)
        	{
        		int startPosition = getStartPosition();
        		startPosition += resultList.size();
        		setStartPosition(startPosition);
        		moreExpected = true;
        	}
        	else
        		moreExpected = false;
        }
        return resultList;
	}


}
