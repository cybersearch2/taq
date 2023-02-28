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
package au.com.cybersearch2.taq.db.sqlite;

import java.security.ProviderException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.DbType;
import au.com.cybersearch2.taq.db.MethodAnalyser;
import au.com.cybersearch2.taq.query.QueryExecutionException;

/**
 * Characteristics of Sqlite
 */
public class Sqlite implements DbType {

	/**
	 * Returns first 2 segments of a JDBC URL
	 * @return JDBC prefix
	 */
	@Override
	public String getJdbcPrefix() {
		return "jdbc:sqlite:";
	}

	/**
	 * Returns JDBC URL for a database in memory
	 * @return JDBC URL
	 */
	@Override
	public String getJdbcInMemory() {
		return "jdbc:sqlite:memory";
	}

	/**
	 * Returns mapping of a Java type to a database type in text format
	 * @param fieldClass Class of field to be persisted
	 * @param entityClass Entity class
	 * @return Database type
	 * @throws QueryExecutionException
	 */
	@Override
	public String getType(Class<?> fieldClass, Class<?> entityClass) throws QueryExecutionException {
		String type = null;
		String typeName = fieldClass.getName();
		switch (typeName) {
		case "java.lang.String":
			type = "TEXT"; break;
		case "boolean":
		case "java.lang.Boolean":
			type = "BOOLEAN"; break;
		case "int":
		case "java.lang.Integer":
			type = "INTEGER"; break;
		case "long":
		case "java.lang.Long":
			type = "BIGINT"; break;
		case "double":
		case "java.lang.Double":
			type = "DOUBLE"; break;
		case "java.math.BigDecimal":
			type = "NUMERIC"; break;
		case "[B":
			type = "BLOB"; break;
		case "java.util.Date":
		case "java.sql.Date":
		    type = "DATA"; break;
		default:
			if (fieldClass.isEnum())
				type = "TEXT";
			else if (MethodAnalyser.isSerializable(fieldClass))
				type = "BLOB";
		}       
		if (type == null)
			throw new QueryExecutionException(String.format("Java type %s not supported", typeName));
		return type;
	}
	
	@Override
	public void appendIdentity(StringBuilder sql, boolean isAutoIncrement) {
		sql.append(" PRIMARY KEY");;
		if (isAutoIncrement)
			sql.append(" AUTOINCREMENT");
	}

	@Override
	public Connection getConnection(ConnectionProfile profile, Properties properties) {
		try {
			if (!profile.getUser().isEmpty()) {
				properties.setProperty("user", profile.getUser());
				properties.setProperty("paswword", profile.getPassword());
			}
			return DriverManager.getConnection(profile.getDatabasePath(), properties);
		} catch (SQLException e) {
			throw new ProviderException("Error getting database connection", e);
		}
	}
}
