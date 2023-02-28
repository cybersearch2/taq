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
package agriculture.provider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import agriculture.Agri20Year;
import au.com.cybersearch2.taq.db.ConnectionProfile;

public class ReadAgriSolution {

	public interface RecordCallback {
		void onNextRecord(Agri20Year ari20Year);
	}
	
    /**
     * select all rows in the Agri20Year table
     * @param profile Connection profile
     * @param callback Record callback
     * @return number of records retrieved
     */
    public int selectAll(ConnectionProfile profile, RecordCallback callback) {
    	int count = 0;
        String sql = "SELECT `id`, `country`, `surface_area`FROM `Agri20Year`";
        try (
        	Connection conn = connect(profile);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql)) {
            
             // loop through the result set
            while (rs.next()) {
            	Agri20Year agri20Year = new Agri20Year();
            	agri20Year.setId(rs.getInt("id"));
            	agri20Year.setCountry(rs.getString("country"));
            	agri20Year.setSurfaceArea(Double.longBitsToDouble(rs.getLong("surface_area")));
            	callback.onNextRecord(agri20Year);
            	++count;
            }
         } catch (SQLException e) {
            System.out.println(e.getMessage());
        }    
        return count;
    }

    public void dripTable(ConnectionProfile profile)  throws IOException {
        String sql = "DROP TABLE IF EXISTS `Agri20Year`";
        try (
            Connection conn = connect(profile);
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
   public Connection connect(ConnectionProfile profile) throws SQLException {
       Connection conn = null;
       // db parameters
       String url = profile.getDbType().getJdbcPrefix() + profile.getDatabasePath();
       // create a connection to the database
       conn = DriverManager.getConnection(url);
       
       //System.out.println("Connection to SQLite has been established.");
           
       return conn;
   }
}
