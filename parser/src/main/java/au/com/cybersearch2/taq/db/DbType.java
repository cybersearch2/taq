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

import java.sql.Connection;
import java.util.Properties;

import au.com.cybersearch2.taq.query.QueryExecutionException;

/**
 * Distinguishing characteristics of a database JDBC driver
 */
public interface DbType {

	/**
	 * Returns first 2 segments of a JDBC URL
	 * @return JDBC prefix
	 */
	String getJdbcPrefix();

	/**
	 * Returns JDBC URL for a database in memory
	 * @return JDBC URL
	 */
	String getJdbcInMemory();

	/**
	 * Returns mapping of a Java type to a database type in text format
	 * @param fieldClass Class of field to be persisted
	 * @param entityClass Entity class
	 * @return Database type
	 * @throws QueryExecutionException
	 */
	String getType(Class<?> fieldClass, Class<?> entityClass) throws QueryExecutionException;

	/**
	 * Append text to complete the primary key SQL statement
	 * @param sql String builder to append
	 * @param isAutoIncrement Flag set true if key auto increments
	 */
	void appendIdentity(StringBuilder sql, boolean isAutoIncrement);

	/**
	 * Returns a database connection for given profile and properties
	 * @param profile Connection profile
	 * @param properties Connection properties
	 * @return Connection object
	 */
	Connection getConnection(ConnectionProfile profile, Properties properties);
}