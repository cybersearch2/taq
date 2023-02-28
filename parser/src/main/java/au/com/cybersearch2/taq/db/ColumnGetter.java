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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.query.QueryExecutionException;

/**
 * 
 * Translates a database row to an entity-object
 * @param <T> Entity type
 * TODO - Support serialization
 */
public class ColumnGetter<T> extends MethodAnalyser<T>{

	private static final String DESERIALIZATION_ERROR = "Error deserializing class %s";

	/**
	 * Construct ColumnGetter object
	 * @param entityClass Entity class
	 * @throws ExecutionException
	 */
	public ColumnGetter(Class<T> entityClass) throws ExecutionException {
		super(MethodType.setter, entityClass);
	}

	/**
	 * Set given entity object by translating result set 
	 * @param rs Result set
	 * @param instance Entity object created by default constructor
	 * @throws Throwable
	 */
	public void setColumns(ResultSet rs, T instance) throws Throwable {
    	for (Map.Entry<String,MethodData> entry: getFieldMap().entrySet()) {
    		String termName = entry.getValue().term;
            Object value = getValue(termName, entry.getKey(), rs);
            entry.getValue().method.invoke(instance, value);
        }
	}

	/**
	 * Returns enum member identified by name
	 * @param clazz Enum class
	 * @param unknownEnumName Name
	 * @return enum
	 */
	public static Enum<?> findMatchingEnumVal(Class<?> clazz, String unknownEnumName) {
		for (Enum<?> enumVal : (Enum<?>[]) clazz.getEnumConstants())
			if (enumVal.name().equalsIgnoreCase(unknownEnumName)) {
				return enumVal;
		}
		throw new IllegalArgumentException(String.format("Enum class %s member %s not found", clazz.getName(), unknownEnumName));
	}

	/** 
	 * Finds the value of the given enumeration by name, case-insensitive. 
	 * Throws an IllegalArgumentException if no match is found.  
	 **/
	public static <T extends Enum<T>> T valueOfIgnoreCase(
	        Class<T> enumeration, String name) {

	    for (T enumValue : enumeration.getEnumConstants()) {
	        if (enumValue.name().equalsIgnoreCase(name)) {
	            return enumValue;
	        }
	    }

	    throw new IllegalArgumentException(String.format(
	        "There is no value with name '%s' in Enum %s",
	        name, enumeration.getName()
	    ));
	}

	/**
	 * Returns object deserialized from content of given byte array
	 * @param columnName Name of column where object is persisted
	 * @param data Byte array 
	 * @return Object
	 */
	public static Object fromByteArray(String columnName, byte[] data) {
		try {
			return deserialize(columnName, new ByteArrayInputStream(data));
		} catch (IOException e) {
			throw new QueryExecutionException(String.format(DESERIALIZATION_ERROR, "for column" + columnName), e);
		}
	}
	
	private Object getValue(String termName, String columnName, ResultSet rs) throws SQLException {
		Object value = null;
		int column = rs.findColumn(termName);
		Class<?> clazz = getType(columnName);
		switch (clazz.getName()) {
		case "java.lang.String":
			value = rs.getString(column); break;
		case "boolean":
		case "java.lang.Boolean":
			value = rs.getBoolean(column); break;
		case "int":
		case "java.lang.Integer":
			value = rs.getInt(column); break;
		case "long":
		case "java.lang.Long":
			value = rs.getLong(column); break;
		case "double":
		case "java.lang.Double":
			value = Double.longBitsToDouble(rs.getLong(column)); break;
		case "java.math.BigDecimal":
			value = rs.getBigDecimal(column); break;
		case "[B":
			value = rs.getBytes(column); break;
		case "java.util.Date":
			value = new Date(rs.getDate(column).getTime()); break;
		case "java.sql.Date":
		    value = rs.getDate(column); break;
		default:
			if (clazz.isEnum()) {
				String name = rs.getString(column);
				if (name == null || name.isEmpty()) 
					throw new QueryExecutionException(String.format("Enum class %s member name is empty", clazz.getName()));
				value = findMatchingEnumVal(clazz, name);
			} else if (MethodAnalyser.isSerializable(clazz)) {
				try {
					value = deserialize(columnName, rs.getBinaryStream(column));
				} catch (IOException e) {
					throw new QueryExecutionException(String.format(DESERIALIZATION_ERROR, "for column" + columnName), e);
				}
			}
           	throw new UnsupportedOperationException(String.format("Type '%s' not supported", getType(columnName)));
        }
        if (value == null)
        	value = new Null();
		return value;
	}

	private static Object deserialize(String columnName, InputStream binaryStream) throws IOException {
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
