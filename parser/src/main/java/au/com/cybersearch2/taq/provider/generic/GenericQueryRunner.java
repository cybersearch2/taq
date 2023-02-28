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

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.db.ColumnGetter;
import au.com.cybersearch2.taq.db.ObjectSelector;
import au.com.cybersearch2.taq.db.QueryBase;
import au.com.cybersearch2.taq.db.EntityCollector.QueryRunner;

/**
 * Executes a query to select rows of an entity database table
 * @param <E> Entity type
 */
public class GenericQueryRunner<E> implements QueryRunner<E>, ObjectSelector<E> {

	/** Foundation for database queries */
	private final QueryBase<E> queryBase;
    private ColumnGetter<E> columns;

    /**
     * Construct GenericQueryRunner object
     * @param queryBase Foundation for database queries
     * @throws ExecutionException
     */
	public GenericQueryRunner(QueryBase<E> queryBase) throws ExecutionException {
		this.queryBase = queryBase;
    	columns = new ColumnGetter<E>((Class<E>) queryBase.getEntityClass());
    }
	
	@Override
	public E getObjectById(int id) throws InterruptedException, ExecutionException {
		List<E> resultList = getSelection(id);
		if (resultList.isEmpty())
			throw new ExecutionException(String.format("Entity object with id %d not found", id), null);
		return resultList.get(0);
	}
	
	@Override
	public List<E> getResultList() throws InterruptedException, ExecutionException {
		return getSelection(-1);
	}

	/**
	 * Returns query result as an entity object list
	 * @param id Primary key for selection of a specific object or -1 if selecting multiple objects
	 * @return a list of objects of type E
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private  List<E> getSelection(int id) throws InterruptedException, ExecutionException {
		List<E> resultList = new ArrayList<>();
		try {
			// Build select statement 
			StringBuilder sql = new StringBuilder();
			columns.getFieldMap().forEach((fieldName,methodData) -> {
				if (sql.length() == 0)
					sql.append("SELECT ").append(methodData.term);
				else
					sql.append(',').append(methodData.term);
			});
			sql.append(" FROM ").append(queryBase.getTableName());
			if (id > 0)
				sql.append(" WHERE ").append(columns.getIdFieldName()).append("=").append(Integer.toString(id));
			int maxResults = queryBase.getMaxResults();
			int startPosition = queryBase.getStartPosition();
			if ((maxResults > 0) && (id < 1)) { // Paging required
				sql.append(' ');
				Long offset = null;
				if (startPosition > 0)
					offset = Long.valueOf(startPosition);
				appendLimitValue(sql, (long)maxResults, offset);
			}
			String query = sql.toString();
			//System.out.println(query);
			// Use reflection to create and populate entity object
			Constructor<E> constructor = (Constructor<E>) queryBase.getEntityClass().getConstructor((Class<?>[])null);
            try (Statement stmt = queryBase.createStatement();
                   ResultSet rs = stmt.executeQuery(query)) {
                // loop through the result set
                while (rs.next()) {
                	E item = constructor.newInstance();
                	columns.setColumns(rs, item);
                	resultList.add(item);
                }
            } catch (SQLException e) {
            	queryBase.onRollback(e);
				throw new ExecutionException(String.format("Error executing query %s", query), e);
            }
            queryBase.onPostExecute(true);
        } catch (Throwable throwable) {
        	queryBase.onRollback(throwable);
			throw new ExecutionException(
				String.format("Error retrieving data for entity %s", queryBase.getEntityClass().getName()), throwable);
		}
		return resultList;
	}
	
	
	public void appendLimitValue(StringBuilder sb, long limit, Long offset) {
		sb.append("LIMIT ");
		if (offset != null) {
			sb.append(offset).append(',');
		}
		sb.append(limit).append(' ');
	}
}
