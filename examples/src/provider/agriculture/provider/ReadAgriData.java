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

import agriculture.AgriAreaPercent;
import au.com.cybersearch2.taq.db.ConnectionProfile;

public class ReadAgriData {

	public interface RecordCallback {
		void onNextRecord(AgriAreaPercent agriAreaPercent);
	}
	
    /**
     * select all rows in the AgriAreaPercent table
     * @param profile Connection profile
     * @param callback Record callback
     * @return number of records retrieved
     */
    public int selectAll(ConnectionProfile profile, RecordCallback callback) {
    	int count = 0;
        String sql = "SELECT `id`, `country`, `surface_area_km2`, `Y1970`, `Y1980`, `Y1990`,`Y2000`, `Y2010` FROM `AgriAreaPercent`";
        try (
        	Connection conn = connect(profile);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql)) {
            
            // loop through the result set
            while (rs.next()) {
            	AgriAreaPercent agriAreaPercent = new AgriAreaPercent();
            	agriAreaPercent.setId(rs.getInt("id"));
            	agriAreaPercent.setCountry(rs.getString("country"));
            	agriAreaPercent.setSurfaceAreaKm2(Double.longBitsToDouble(rs.getLong("surface_area_km2")));
            	agriAreaPercent.setY1970(Double.longBitsToDouble(rs.getLong("y1970")));
            	agriAreaPercent.setY1980(Double.longBitsToDouble(rs.getLong("y1980")));
           	    agriAreaPercent.setY1990(Double.longBitsToDouble(rs.getLong("y1990")));
           	    agriAreaPercent.setY2000(Double.longBitsToDouble(rs.getLong("y2000")));
             	agriAreaPercent.setY2010(Double.longBitsToDouble(rs.getLong("y2010")));
            	callback.onNextRecord(agriAreaPercent);
            	++count;
           }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }    
        return count;
    }

    public void dripTable(ConnectionProfile profile)  throws IOException {
        String sql = "DROP TABLE IF EXISTS `AgriAreaPercent`";
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
