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
import java.util.Map;
import java.util.TreeMap;

import agriculture.AgriAreaPercent;
import agriculture.Country;
import agriculture.YearPercent;
import au.com.cybersearch2.taq.db.ConnectionProfile;

public class ReadAgriData2 {

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
    	Map<Integer,Country> countries = getCountries(profile);
    	int count = 0;
        String sql = "SELECT `id`, `country_id`, `decade`, `percent` FROM `YearPercent`";
        try (
        	Connection conn = connect(profile);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql)) {
            
        	int countryId = -1;
        	int id = 0;
        	int record = 0;
        	AgriAreaPercent agriAreaPercent = new AgriAreaPercent();
            // loop through the result set
            while (rs.next()) {
            	YearPercent yearPercent = new YearPercent();
            	yearPercent.setCountryId(rs.getInt("country_id"));
            	yearPercent.setDecade(rs.getString("decade"));
            	yearPercent.setPercent(rs.getLong("percent"));
            	if (countryId != yearPercent.getCountryId()) {
            		Country country = countries.get(yearPercent.getCountryId());
            		agriAreaPercent.setCountry(country.getCountry());
            		agriAreaPercent.setSurfaceAreaKm2(Double.longBitsToDouble(country.getSurfaceAreaKm2()));
            		agriAreaPercent.setId(++id);
            		countryId = yearPercent.getCountryId();
            	}
            	switch (yearPercent.getDecade()) {
            	case "y1970": 
            		agriAreaPercent.setY1970(Double.longBitsToDouble(yearPercent.getPercent())); break;
               	case "y1980": 
            		agriAreaPercent.setY1980(Double.longBitsToDouble(yearPercent.getPercent())); break;
               	case "y1990": 
            		agriAreaPercent.setY1990(Double.longBitsToDouble(yearPercent.getPercent())); break;
               	case "y2000": 
            		agriAreaPercent.setY2000(Double.longBitsToDouble(yearPercent.getPercent())); break;
               	case "y2010": 
            		agriAreaPercent.setY2010(Double.longBitsToDouble(yearPercent.getPercent())); break;
            	default:
            	}
            	if (++record == 5) {
            		++count;
            		record = 0;
                	callback.onNextRecord(agriAreaPercent);
            	}
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
   
   private Map<Integer,Country> getCountries(ConnectionProfile profile) {
	   Map<Integer,Country> countryMap = new TreeMap<>();
       String sql = "SELECT `id`, `country`, `surface_area_km2` FROM `Country`";
       try (
            Connection conn = connect(profile);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
            	Country country = new Country();
            	country.setId(rs.getInt("id"));
            	country.setCountry(rs.getString("country"));
            	country.setSurfaceAreaKm2(rs.getLong("surface_area_km2"));
            	countryMap.put(country.getId(), country);
            }
       } catch (SQLException e) {
           System.out.println(e.getMessage());
       }    
	   return countryMap;
   }
}
