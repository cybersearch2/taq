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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

/**
 * Foundation for database prepared statements
 * @param <E> Entity type
 */
public abstract class StatementBase<E> {

    /** The entity class */
    private final Class<E> entityClass;
    /** Holds connection information to allow re-establishment following a disconnection */
	private ProviderConnection conn;

	/**
	 * Construct StatementBase object
	 * @param entityClass Entity class
	 */
	public StatementBase(Class<E> entityClass) {
        this.entityClass = entityClass;
	}

    /**
     * Persists given entity object
     * @param entity Entity object
     * @throws InterruptedException
     * @throws ExecutionException
     */
    abstract public void internalEmitData(E entity) throws InterruptedException, ExecutionException;

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
	 * Returns PreparedStatement object bound to current connection
	 * @param sql SQL statement to execute
	 * @return PreparedStatement object
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.getConnection().prepareStatement(sql);
	}

	/**
	 * Returns PreparedStatement object bound to current connection with auto generate keys enabled
	 * @param sql SQL statement to execute
	 * @param autoGeneratedKeys Constant to enable auto generate keys - Statement.RETURN_GENERATED_KEYS
	 * @return PreparedStatement object
	 * @throws SQLException
	 */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
        throws SQLException {
   		return conn.getConnection().prepareStatement(sql, autoGeneratedKeys);
   	}

	/**
	 * Returns Statement object bound to current connection
	 * @return Statement object
	 * @throws SQLException
	 */
	public Statement createStatement() throws SQLException {
		return conn.getConnection().createStatement();
	}
	
}
