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

import java.util.Map;
import java.util.Objects;

/**
 * Configuration needed to open a database connection
 */
public class ConnectionProfile implements Comparable<ConnectionProfile> {

	/** Resource name */
	private String name;
	private String databasePath;
	private String user;
	private String password;
	/**  Distinguishing characteristics of a database JDBC driver */
	private DbType dbType;

	/**
	 * Construct a ConnectionProfile object
	 * @param name Resource name
	 * @param dbType  Distinguishing characteristics of a database JDBC driver
	 * @param databasePath Database path
	 */
	public ConnectionProfile(String name, DbType dbType, String databasePath) {
		this.name = name;
		this.dbType = dbType;
		this.databasePath = databasePath;
		user = "";
		password = "";
				
	}

	/**
	 * ConnectionProfile copy constructor
	 * @param profile Connection profile to copy from
	 */
	public ConnectionProfile(ConnectionProfile profile) {
		name = profile.getName();
		dbType = profile.getDbType();
		databasePath = profile.getDatabasePath();
		user = profile.getUser();
		password = profile.getPassword();
	}
	
	public String getName() {
		return name;
	}

	public String getDatabasePath() {
		return databasePath;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public DbType getDbType() {
		return dbType;
	}

	/**
	 * Extract user and password values from properties, if present
	 * @param properties Connection properties
	 */
	public void setCredentials(Map<String, Object> properties) {
       String user = getStringProperty("user", properties);
       if (user != null) {
    	   setUser(user);
    	   properties.remove("user");
       }
       String password = getStringProperty("password", properties);
       if (password != null) {
    	   setPassword(password);
    	   properties.remove("password");
       }
	}
	
	@Override
	public int compareTo(ConnectionProfile other) {
		if (!other.name.equals(name))
		    return name.compareTo(other.name);
		else if (!other.dbType.getClass().getSimpleName().equals(dbType.getClass().getSimpleName()))
			return other.dbType.getClass().getSimpleName().compareTo(dbType.getClass().getSimpleName());
		else
		    return other.getDatabasePath().compareTo(databasePath);

	}

	@Override
	public int hashCode() {
		return Objects.hash(name, databasePath, dbType.getClass().getSimpleName());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ConnectionProfile))
			return false;
		ConnectionProfile other = (ConnectionProfile)obj;
		return other.name.equals(name) &&
               other.databasePath.equals(databasePath) && 
               other.dbType.getClass().getSimpleName().equals(dbType.getClass().getSimpleName());
	}

	@Override
	public String toString() {
		return dbType.getClass().getSimpleName() + " " + name + " @ " + databasePath;
	}
	
	private String getStringProperty(String key, Map<String, Object> properties) {
		Object object = properties.get(key);
		return object == null ? null : object.toString();
	}
}
