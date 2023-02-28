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

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Foundation for database queries 
 * @param <E> Entity type
 */
public abstract class QueryBase<E> {

    /** The entity class */
    private final Class<E> entityClass;
    /** Holds connection information to allow re-establishment following a disconnection */
	private ProviderConnection conn;
    /** Maximum number of objects to return from a single query */
	private int maxResults;
    /** The start position of the first result, numbered from 0 */
	private int startPosition;

	/**
	 * Construct QueryBase object
	 * @param entityClass Entity class
	 */
	public QueryBase(Class<E> entityClass) {
        this.entityClass = entityClass;
	}

	/**
	 * Handle completion of query
	 * @param success Flag set true if query completed sucessfully
	 */
	abstract public void onPostExecute(boolean success);
	
	/**
	 * Handle failure during transaction causing roll back
	 * @param rollbackException Exception thrown during transaction
	 */
	abstract public void onRollback(Throwable rollbackException) ;
	
    public Class<E> getEntityClass() {
		return entityClass;
	}

	/**
	 * Returns name of table persisting objects of the set entity class
	 * @return name of table
	 */
	public String getTableName() {
		return MethodAnalyser.getTableName(entityClass);
	}

	public void setConnection(ProviderConnection conn) {
		this.conn = conn;
	}

	/**
	 * Returns new Statement object bound to current connection
	 * @return Statement object
	 * @throws SQLException
	 */
	public Statement createStatement() throws SQLException {
		if (conn == null)
			throw new IllegalStateException("Connection not set");
		return conn.getConnection().createStatement();
	}
	
	/**
	 * Returns limit set on number of results a query will produce
	 * @return Limit number or 0 if no limit
	 */
	public int getMaxResults() 
	{
		return maxResults;
	}

	/**
	 * Set limit on number of results a query will produce
	 * @param maxResults int greater than 0
	 */
	public void setMaxResults(int maxResults) 
	{
		this.maxResults = maxResults;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

}
