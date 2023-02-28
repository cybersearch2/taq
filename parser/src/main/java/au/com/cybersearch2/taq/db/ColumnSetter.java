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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.query.QueryExecutionException;

/**
 * 
 * Translates an entity-object to values placed in a database row insertion statement
 * @param <T> Entity type
 * TODO - Support serialization
 */
public class ColumnSetter<T> extends MethodAnalyser<T> {

	private static final String SERIALIZATION_ERROR = "Error serializing column %s";

	public ColumnSetter(Class<T> entityClass) throws ExecutionException {
		super(MethodType.getter, entityClass);
	}
	
	/**
	 * Translate an entity object to a set of values placed in a prepared statement 
	 * @param instance Entity object created by default constructor
	 * @param pstmt Prepared statement
	 * @throws Throwable
	 */
	public void setFields(T instance, PreparedStatement pstmt) throws Throwable {
		int index = 1;
    	for (Map.Entry<String,MethodData> entry: getFieldMap().entrySet()) {
    		if (getIdFieldName().equals(entry.getKey()))
    			continue;
            Object value = entry.getValue().method.invoke(instance);
    		if (value == null)
    			pstmt.setNull(index++, getSqlType(entry.getValue().method.getReturnType()));
    		else
                setValue(pstmt, index++, value);
        }
	}

	/**
	 * Returns byte array containing serialization of given object
	 * @param columnName Name of column where object is persisted
	 * @param object Object to be serialized
	 * @return byte array
	 */
	public static byte[] toByteArry(String columnName, Object object) {
		try {
			return serialize(object);
		} catch (IOException e) {
			throw new QueryExecutionException(String.format(SERIALIZATION_ERROR, columnName), e);
		}
	}
	
	private void setValue(PreparedStatement pstmt, int column, Object value) throws SQLException {
		switch (value.getClass().getName()) {
		case "java.lang.String":
			pstmt.setString(column, value.toString()); break;
		case "boolean":
		case "java.lang.Boolean":
			pstmt.setBoolean(column, ((Boolean)value).booleanValue()); break;
		case "int":
		case "java.lang.Integer":
			pstmt.setInt(column, ((Integer)value).intValue()); break;
		case "long":
		case "java.lang.Long":
			pstmt.setLong(column, ((Long)value).longValue()); break;
		case "double":
		case "java.lang.Double":
			double doubleValue = ((Double)value).doubleValue();
			pstmt.setLong(column, Double.doubleToLongBits(doubleValue)); break;
		case "java.math.BigDecimal":
			pstmt.setBigDecimal(column, (BigDecimal)value); break;
		case "[B":
			pstmt.setBytes(column, (byte[])value); break;
		case "java.util.Date":
			pstmt.setDate(column, new java.sql.Date(((Date)value).getTime())); break;
		case "java.sql.Date":
		    pstmt.setDate(column, (java.sql.Date)value); break;
		default:
		    if (value.getClass().isEnum()) 
			    pstmt.setString(column, ((Enum<?>)value).name());
		    else if (MethodAnalyser.isSerializable(value.getClass()))  {
				try {
					pstmt.setBytes(column, serialize(value));
				} catch (IOException e) {
					throw new QueryExecutionException(String.format(SERIALIZATION_ERROR, "column " + column), e);
				}
			} else
				throw new QueryExecutionException(String.format("Type %s persistence not supported", value.getClass().getName()));
		}
	}

	private static byte[] serialize(Object object) throws IOException {
        // Save object in a byte array
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(file);
        // Method for serialization of object
        out.writeObject(object);
        out.close();
        return file.toByteArray();
	}

	private int getSqlType(Class<?> clazz) {
		switch (clazz.getName()) {
		case "java.lang.String":
			return Types.VARCHAR;
		case "boolean":
		case "java.lang.Boolean":
			return Types.BOOLEAN;
		case "int":
		case "java.lang.Integer":
			return Types.INTEGER;
		case "long":
		case "java.lang.Long":
			return Types.BIGINT;
		case "double":
		case "java.lang.Double":
			return Types.DOUBLE;
		case "java.math.BigDecimal":
		case "java.util.Date":
		case "java.sql.Date":
			return Types.NUMERIC;
		case "[B":
			return Types.BLOB;
		default:
			if (clazz.isEnum())
				return Types.VARCHAR;
			else if (MethodAnalyser.isSerializable(clazz))
				return Types.BLOB;
			else
			    throw new QueryExecutionException(String.format("Java type %s not supported", clazz.getName()));
		}
	}

 }
