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
package entities_axioms;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * City
 * @author Andrew Bowley
 * 8 Feb 2015
 */
@Entity(name="City")
public class City 
{

	/**
     * City default constructor required
	 */
	public City() 
	{
	}

	/** We use this field-name so we can query for posts with a certain id */
	public final static String ID_FIELD_NAME = "id";

	/** This id is generated by the database and set on the object when it is passed to the create method */
    @Id @GeneratedValue
 	int id;

	@Column(name="name")
	String name;
	
	@Column(name="altitude")
	long altitude;

	/**
	 * Create City object
	 * @param name City name
	 * @param altitude Height in feet
	 */
	public City(String name, int altitude) 
	{
		this.name = name;
		this.altitude = altitude;
	}

	@Override
	public String toString() 
	{
		return "city(name = " + name + ", altitude = " + altitude +")";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getAltitude() {
		return altitude;
	}

	public void setAltitude(long altitude) {
		this.altitude = altitude;
	}
	
	
}
