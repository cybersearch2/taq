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
package star_person;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;

import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.query.QueryExecutionException;
import entities_axioms.StarPerson;
import entities_axioms.Zodiac;

public class ReadStarPersons {

	private static final String DESERIALIZATION_ERROR = "Error deserializing class %s";

	public interface RecordCallback {
		void onNextRecord(StarPerson starPerson);
	}
	
    /**
     * select all rows in the warehouses table
     * @throws IOException 
     */
    public void selectAll(ConnectionProfile profile, Map<String,Object> properties, RecordCallback callback) throws IOException {
        String sql = "SELECT `id`, `name`, `starsign`, `age`, `rating`, `timestamp` FROM `StarPerson`";
        try (
        	Connection conn = connect(profile, properties);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql)) {
            
        	//NumberFormat formatter = new DecimalFormat("#0.0");  
            // loop through the result set
            while (rs.next()) {
            	StarPerson starPerson = new StarPerson();
            	starPerson.setId(rs.getInt("id"));
            	starPerson.setName(rs.getString("name"));
            	starPerson.setStarsign(Zodiac.valueOf(rs.getString("starsign")));
            	starPerson.setAge(rs.getLong("age"));
            	starPerson.setRating(Double.longBitsToDouble(rs.getLong("rating")));
            	starPerson.setTimestamp((Instant) deserialize("timestamp", rs.getBinaryStream("timestamp")));
            	callback.onNextRecord(starPerson);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }    
    }

    public void dripTable(ConnectionProfile profile, Map<String,Object> properties)  throws IOException {
        String sql = "DROP TABLE IF EXISTS `StarPerson`";
        try (
            Connection conn = connect(profile, properties);
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

	private Object deserialize(String columnName, InputStream binaryStream) throws IOException {
        // Reading the object from a byte array
        ObjectInputStream in = new ObjectInputStream(binaryStream);
        // Method for deserialization of object
        Object object;
		try {
			object = in.readObject();
		} catch (ClassNotFoundException e) {
			throw new QueryExecutionException(String.format(DESERIALIZATION_ERROR, "for column" + columnName), e);
		}
        in.close();
		return object;
	}

}
