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
package au.com.cybersearch2.taq.provider.generic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.db.ColumnSetter;
import au.com.cybersearch2.taq.db.MethodAnalyser;
import au.com.cybersearch2.taq.db.StatementBase;
import au.com.cybersearch2.taq.db.StatementRunner;
import au.com.cybersearch2.taq.db.MethodAnalyser.MethodData;
import au.com.cybersearch2.taq.db.MethodAnalyser.MethodType;

/**
 * Inserts a database row mapped to an entity object
 * @param <E> Entity type
 */
public class GenericStatementRunner<E> implements StatementRunner<E> {

	/** Foundation for database prepared statements */
	private final StatementBase<E> statementBase;
	/** Translates an entity-object to values placed in a database row insertion statement */
    private final ColumnSetter<E> columns;
    /** Flag set true if entity has an int primary key */
    private final boolean hasIntId;
    /** Reflection method to set entity primary key */
    private Method setIdMethod;

    /**
     * Construct GenericStatementRunner object
     * @param statementBase Foundation for database prepared statements
     * @param columns Translates an entity-object to values placed in a database row insertion statement
     */
	public GenericStatementRunner(StatementBase<E> statementBase, ColumnSetter<E> columns) {
		this.statementBase = statementBase;
		this.columns = columns;
     	MethodData idData = columns.getFieldMap().get(columns.getIdFieldName());
     	if (idData != null) 
     		hasIntId = idData.method.getReturnType() == int.class;
     	else
     		hasIntId = false;

	}

	@Override
	public int insertEntity(E entity) throws InterruptedException, ExecutionException {
		int rowId = 0;
		try {
			/*
            try ( PreparedStatement stmt = statementBase.prepareStatement("BEGIN") ) {
		 	    stmt.executeUpdate();
            } catch (SQLException e) {
            	statementBase.onRollback(e);
				throw new ExecutionException("Error executing BEGIN", e);
            } */
			StringBuilder sql = new StringBuilder("INSERT INTO ");
			sql.append(statementBase.getTableName()).append('(');
			StringBuilder fieldNames = new StringBuilder();
			StringBuilder ehs = new StringBuilder();
			columns.getFieldMap().forEach((fieldName,methodData) -> {
				if (!columns.getIdFieldName().equals(fieldName)) {
					if (fieldNames.length() == 0) {
						fieldNames.append(methodData.term);
						ehs.append('?');
					} else {
						fieldNames.append(',').append(methodData.term);
						ehs.append(",?");
					}
				}
			});
			sql.append(fieldNames.toString()).append(") VALUES(").append(ehs).append(')');
            try ( PreparedStatement stmt = statementBase.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS) ) {
		 	    columns.setFields(entity, stmt);
		 	    stmt.executeUpdate();
                try (
                        ResultSet rs = stmt.getGeneratedKeys() ;
                )
                {
                    while (rs.next())
                    {
                    	rowId = rs.getInt(1);
                 		if (hasIntId) 
                 			setEntityId(entity, rowId);
                        //System.out.println( "generated key: " + rowId );
                    }
                }

            
            } catch (SQLException e) {
            	statementBase.onRollback(e);
				throw new ExecutionException(String.format("Error executing prepared statement %s", sql.toString()), e);
            }
            /*
             try ( PreparedStatement stmt = statementBase.prepareStatement("COMMIT") ) {
		 	    stmt.executeUpdate();
             } catch (SQLException e) {
            	statementBase.onRollback(e);
				throw new ExecutionException("Error executing COMMIT", e);
             } */
             statementBase.onPostExecute(true);
        } catch (Throwable throwable) {
        	statementBase.onRollback(throwable);
			throw new ExecutionException(String.format("Error insrting data for entity %s", statementBase.getEntityClass().getName()), throwable);
		}
		return rowId;
	}

	/**
	 * Sets primary key value of given entity object
	 * @param entity Entity object to set
	 * @param rowId Id of row 
	 * @throws ExecutionException
	 */
	private void setEntityId(E entity, int rowId) throws ExecutionException {
		if (setIdMethod == null) {
			MethodAnalyser<E> methodAnalyser = new MethodAnalyser<>(MethodType.setter, statementBase.getEntityClass());
			MethodData idData = methodAnalyser.getFieldMap().get(methodAnalyser.getIdFieldName());
			setIdMethod = idData.method;
		}
		try {
			setIdMethod.invoke(entity, rowId);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
         	statementBase.onRollback(e);
				throw new ExecutionException("Error setting primary key", e);
		}
	}

}
