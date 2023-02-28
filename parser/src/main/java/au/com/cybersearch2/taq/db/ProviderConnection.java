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
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Holds connection information to allow re-establishment following a disconnection
  */
public class ProviderConnection {

	/** Connection information */
	private final ConnectionProfile profile;
	/** Optional connection properties */
	private Properties properties;
    /** Database connection - initially null until {@link #open(Map)} is called */
    private Connection conn;
	
    /**
     * Construct ProviderConnection object
     * @param profile Connection information
     * @param propertiesMap Maps property key to object containing value
     */
	public ProviderConnection(ConnectionProfile profile, Map<String, Object> properties) {
		this.profile = new ConnectionProfile(profile);
    	this.properties = new Properties(properties.size());
    	properties.forEach((key,value) -> this.properties.put(key, value.toString()));
	}

	public boolean isConnectionOpen() throws SQLException {
		return (conn != null) && !conn.isClosed();
	}
	
	public Connection getConnection() throws SQLException {
		synchronized(this) {
			if (!isConnectionOpen())
		       conn = profile.getDbType().getConnection(profile, properties);
		}
		return conn;
	}
	
    public synchronized void close() throws SQLException
    {
    	if ((conn != null))
			try {
				if (!conn.isClosed())
				    conn.close();
			} finally {
				// Use null to indicate database is closed
				conn = null;
			}
    }
}
