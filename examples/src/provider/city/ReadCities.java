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
package city;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import au.com.cybersearch2.taq.db.ConnectionProfile;

public class ReadCities {

	public static class Record {
	
		public Record(int id, String name, int altitude) {
			this.id = id;
			this.name = name;
			this.altitude = altitude;
		}
		
		public Record(ResultSet rs) throws SQLException {
			id = rs.getInt("id"); 
            name = rs.getString("name");
            altitude = rs.getInt("altitude");
		}
		
		public int id;
		public String name;
		public int altitude;
	}
	
	public interface RecordCallback {
		void onNextRecord(Record record);
	}
	
    /**
     * select all rows in the warehouses table
     */
    public void selectAll(ConnectionProfile profile, Map<String,Object> propertiesMap, RecordCallback callback){
        String sql = "SELECT `id`, `name`, `altitude` FROM `City`";
        try (
        	Connection conn = connect(profile, propertiesMap);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql)) {
            
            // loop through the result set
            while (rs.next()) {
            	callback.onNextRecord(new Record(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }    
    }
    
    public void dropTable(ConnectionProfile profile, Map<String,Object> propertiesMap)  throws IOException {
        String sql = "DROP TABLE IF EXISTS `City`";
        try (
            Connection conn = connect(profile, propertiesMap);
            Statement stmt  = conn.createStatement()) {
        	stmt.executeUpdate(sql);
        	stmt.close();
        	conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }    
    
    /**
    * Connect to a sample database
    */
    public Connection connect(ConnectionProfile profile, Map<String,Object> propertiesMap) throws SQLException {
    	   Properties properties = new Properties(propertiesMap.size());
    	   propertiesMap.forEach((key,value) -> properties.put(key, value.toString()));
        return profile.getDbType().getConnection(profile, properties);
   }
}
