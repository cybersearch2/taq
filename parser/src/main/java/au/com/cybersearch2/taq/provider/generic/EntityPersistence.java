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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.ProviderException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.persistence.PersistenceException;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.ProviderManager;
import au.com.cybersearch2.taq.axiom.AxiomReflection;
import au.com.cybersearch2.taq.db.AxiomConverter;
import au.com.cybersearch2.taq.db.ColumnSetter;
import au.com.cybersearch2.taq.db.ConnectionProfile;
import au.com.cybersearch2.taq.db.DataSource;
import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.db.DbType;
import au.com.cybersearch2.taq.db.EntityClassLoader;
import au.com.cybersearch2.taq.db.MethodAnalyser;
import au.com.cybersearch2.taq.db.ObjectSelector;
import au.com.cybersearch2.taq.db.ProviderConnection;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.StringTerm;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.provider.GlobalFunctions;
import au.com.cybersearch2.taq.query.QueryExecutionException;

/**
 * EntityPersistence Receives and transmits axioms on a connection to an Sqlite
 * database
 * 
 * @author Andrew Bowley
 */
public class EntityPersistence extends DatabaseProvider<GenericEntityCollector<?>, GenericEntityEmitter<?>> {

	private class DatabaseAxiomListener implements LocaleAxiomListener {

		private final GenericEntityEmitter<?> genericEntityEmitter;

		public DatabaseAxiomListener(GenericEntityEmitter<?> genericEntityEmitter) {
			this.genericEntityEmitter = genericEntityEmitter;
		}

		@Override
		public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) {
				try {
					getWorkerService().submitWork(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							genericEntityEmitter.onNextAxiom(qname, axiom, locale);
							return null;
						}
					}, Void.class);
				} catch (InterruptedException e) {

				} catch (ExecutionException e) {
					throw new QueryExecutionException(String.format("Error emitting axiom '%s'", axiom.toString()), e);
				}
			return true;
		}
	}

	private static final String STATEMENT_FAILED_MESSAGE = "Error executing statement %s";

	/** Logger */
	private static final Logger logger = LogManager.getLogger(EntityPersistence.class);

	/** Configuration needed to open a database connection */
	private final ConnectionProfile connectionProfile;

	/** Maps Entity Collectors to axiom source names */
	private Map<String, GenericEntityCollector<?>> collectorMap;
	/** Maps Entity Emitters to axiom source names */
	private Map<String, GenericEntityEmitter<?>> emitterMap;
	/** Maps Axiom Listeners to axiom keys */
	private Map<String, LocaleAxiomListener> axiomListenerMap;
	/** Database connection - initially null until {@link #open(Map)} is called */
	private ProviderConnection conn;
	/** Dedicated entity class loader */
	private EntityClassLoader entityClassLoader;

	/**
	 * Construct EntityPersistence object
	 * 
	 * @param connectionProfile configuration needed to open a database connection
	 */
	public EntityPersistence(ConnectionProfile connectionProfile) {
		super();
		this.connectionProfile = new ConnectionProfile(connectionProfile.getName(), connectionProfile.getDbType(),
				connectionProfile.getDatabasePath());
		collectorMap = Collections.emptyMap();
		emitterMap = Collections.emptyMap();
	}

	@Override
	public boolean hasCollector(String axiomName) {
		return collectorMap.containsKey(axiomName);
	}

	@Override
	public boolean hasEmitter(String axiomName) {
		return emitterMap.containsKey(axiomName);
	}

	@Override
	public String getDatabasePath() {
		return connectionProfile.getDatabasePath();
	}

	/**
	 * Associate Entity Class with axiom name
	 * 
	 * @param axiomName   Axiom name
	 * @param entityClass Entity class
	 */
	@Override
	public <T> void addCollectorEntity(String axiomName, Class<T> entityClass) {
		addCollector(axiomName, new GenericEntityCollector<T>(entityClass));
	}

	/**
	 * Associate Entity Collector with axiom name
	 * 
	 * @param axiomName              Axiom name
	 * @param genericEntityCollector JpaEntityCollector object
	 */
	@Override
	public void addCollector(String axiomName, GenericEntityCollector<?> genericEntityCollector) {
		if (collectorMap.size() == 0)
			collectorMap = new HashMap<>();
		collectorMap.put(axiomName, genericEntityCollector);
	}

	/**
	 * Associate Entity Class with axiom name
	 * 
	 * @param axiomName   Axiom name
	 * @param entityClass Entity class
	 */
	@Override
	public <T> GenericEntityEmitter<T> addEmitterEntity(String axiomName, Class<T> entityClass) {
		GenericEntityEmitter<T> genericEntityEmitter = new GenericEntityEmitter<T>(entityClass);
		addEmitter(axiomName, genericEntityEmitter);
		return genericEntityEmitter;
	}

	/**
	 * Associate Entity Collector with axiom name
	 * 
	 * @param axiomName            Axiom name
	 * @param genericEntityEmitter JpaEntityCollector object
	 */
	@Override
	public void addEmitter(String axiomName, GenericEntityEmitter<?> genericEntityEmitter) {
		if (emitterMap.size() == 0)
			emitterMap = new HashMap<>();
		emitterMap.put(axiomName, genericEntityEmitter);
	}

	@Override
	public void addCollectorEntity(String axiomName, String entityClassName) {
		Class<?> clazz = getEntityClassLoader().loadClass(entityClassName);
		String entityName = getEntityName(clazz);
		if ((entityName == null) || entityName.isEmpty()) {
			if (!GenericEntityCollector.class.isAssignableFrom(clazz))
				throw new ProviderException(String.format(
						"Class %s is not an Entity nor a sub class of GenericEntityCollector", entityClassName));
			ParameterizedType superClass = (ParameterizedType) clazz.getGenericSuperclass();
			Type[] typeParameter = superClass.getActualTypeArguments();
			entityName = getEntityName((Class<?>) typeParameter[0]);
			if ((entityName == null) || entityName.isEmpty())
				throw new ProviderException(
						String.format("Class %s is not an Entity", typeParameter[0].getClass().getName()));
			try {
				@SuppressWarnings("rawtypes")
				GenericEntityCollector genericEntityCollector = (GenericEntityCollector) clazz.getDeclaredConstructor()
						.newInstance();
				addCollector(axiomName, genericEntityCollector);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new ProviderException(String.format(ProviderManager.CLASS_LOAD_ERROR, entityClassName), e);
			}
		} else
			addCollectorEntity(axiomName, clazz);
	}

	@Override
	public GenericEntityEmitter<?> addEmitterEntity(String axiomName, String entityClassName) {
		return addEmitterEntity(axiomName, getEntityClassLoader().loadClass(entityClassName));
	}

	/**
	 * Drop all tables that can be created by this provider and recreate them
	 */
	@Override
	public void dropAllTables() {
		if (conn != null) {
			for (GenericEntityEmitter<?> emitter : emitterMap.values()) {
				String tableName = emitter.getTableName();
				if (tableExists(tableName)) {
					deleteTable(tableName);
					createTable(tableName, emitter);
				}
			}
		}
	}

	/**
	 * Returns object which translates a database row selected by primary key to an
	 * entity object
	 * 
	 * @param name Axiom name of collector which provides the selector
	 * @return ObjectSelector object
	 */
	@Override
	public ObjectSelector<?> getObjectSelector(String name) {
		GenericEntityCollector<?> genericEntityCollector = getDataSourcePart(name);
		try {
			return genericEntityCollector.getObjectSelector();
		} catch (ExecutionException e) {
			throw new ProviderException("Error creating object selector", e);
		}
	}

	/**
	 * Returns Axiom Provider identity
	 */
	@Override
	public String getName() {
		return connectionProfile.getName();
	}

	@Override
	public ConnectionProfile getConnectionProfile() {
		return new ConnectionProfile(connectionProfile);
	}

	/**
	 * Open Axiom Provider
	 */
	@Override
	public void open() {
		if (conn == null) {
			try {
				Map<String, Object> properties = getConnectionProperties();
				if (!properties.isEmpty()) {
					Map<String, Object> sessionProps = new HashMap<>();
					sessionProps.putAll(properties);
					connectionProfile.setCredentials(sessionProps);
					conn = new ProviderConnection(connectionProfile, sessionProps);
				} else {
					connectionProfile.setCredentials(properties);
					conn = new ProviderConnection(connectionProfile, properties);
				}
				conn.getConnection();
				createAllTables();
				collectorMap.values().forEach(collector -> collector.setConnection(conn));
				emitterMap.values().forEach(emitter -> emitter.setConnection(conn));
				super.onOpen();
			} catch (SQLException e) {
				throw new ProviderException(
						String.format("Error opening database '%s'", this.connectionProfile.getDatabasePath()), e);
			}
		}
	}

	@Override
	public void close() {
		if ((conn != null))
			try {
				conn.close();
				super.onClose();
			} catch (SQLException e) {
				logger.error(e, "Error closing database connection");
			}
	}

	@Override
	public boolean isEmpty() {
		return (conn == null) && (collectorMap.size() > 0);
	}

	@Override
	public Iterator<Axiom> iterator(AxiomArchetype archetype) {
		return new DataSource(getDataSourcePart(archetype.getName()), new AxiomConverter(getAxiomReflection(archetype)))
				.iterator(null);
	}

	@Override
	public boolean setProperty(String key, Object value) {
		return getEntityClassLoader().setProperty(key, value);
	}

	@Override
	public void logToConsole() {
		chainAxiomListener(new LocaleAxiomListener() {

			GlobalFunctions globalFunctions = new GlobalFunctions();

			@Override
			public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) {
				globalFunctions.print(new StringTerm(axiom.toString()));
				return true;
			}
		});
	}

	@Override
	public LocaleAxiomListener getAxiomListener(String name) {
		if (!emitterMap.isEmpty()) {
			GenericEntityEmitter<?> genericEntityEmitter = emitterMap.get(name);
			if (genericEntityEmitter != null) {
				if (axiomListenerMap == null) {
					axiomListenerMap = new HashMap<>();
				}
				return axiomListenerMap.computeIfAbsent(name, key -> {
					// Parameter 'key' not used
					LocaleAxiomListener newAxiomListener = new DatabaseAxiomListener(genericEntityEmitter);
					if (isListenerChainEmpty()) {
						return newAxiomListener;
					} else {
						super.chainAxiomListener(newAxiomListener);
						return super.listenerChainInstance();
					}
				});
			}
		}
		throw new QueryExecutionException(String.format("Enitter named %s not found", name));
	}

	@Override
	public boolean chainAxiomListener(LocaleAxiomListener axiomListener) {
		return super.chainAxiomListener(axiomListener);
	}

	protected AxiomReflection getAxiomReflection(AxiomArchetype archetype) {
		GenericEntityCollector<?> genericEntityCollector = getDataSourcePart(archetype.getName());
		Class<?> entityClass = genericEntityCollector.getEntityClass();
		return new AxiomReflection(archetype, MethodAnalyser.getNameMap(entityClass));
	}

	@Override
	public void emit(Axiom axiom, Locale locale) {
		getAxiomListener(axiom.getName()).onNextAxiom(axiom.getArchetype().getQualifiedName(), axiom, locale);
	}

	private EntityClassLoader getEntityClassLoader() {
		if (entityClassLoader == null)
			entityClassLoader = new EntityClassLoader();
		return entityClassLoader;
	}

	private GenericEntityCollector<?> getDataSourcePart(String name) {
		GenericEntityCollector<?> genericEntityCollector = collectorMap.get(name);
		if (genericEntityCollector == null)
			throw new QueryExecutionException(String.format("Entity collector not found for %s axiom", name));
		return genericEntityCollector;
	}

	private boolean tableExists(String tableName) {
		try (Statement stmt = conn.getConnection().createStatement();
				ResultSet rs = conn.getConnection().getMetaData().getTables(null, null, tableName.toUpperCase(),
						null)) {
			return rs.next();
		} catch (SQLException e) {
			throw new QueryExecutionException("Error getting table metadata", e);
		}
	}

	private void createTable(String tableName, GenericEntityEmitter<?> emitter) {
		ColumnSetter<?> columnSetter = null;
		try {
			columnSetter = emitter.getColumnSetter();
		} catch (ExecutionException e) {
			throw new QueryExecutionException(String.format("Error creating table %s", tableName), e);
		}
		DbType dbType = connectionProfile.getDbType();
		StringBuilder sql = new StringBuilder();
		ColumnSetter<?> columns = columnSetter;
		columns.getFieldMap().forEach((fieldName, methodData) -> {
			boolean isPrimaryKey = fieldName.equals(columns.getIdFieldName());
			Class<?> clazz = columns.getType(fieldName);
			String typeName = clazz.getName();
			String type = dbType.getType(clazz, emitter.getEntityClass());
			if (sql.length() == 0)
				sql.append("CREATE TABLE ").append(tableName).append('(').append(methodData.term).append(' ')
						.append(type);
			else
				sql.append(',').append(methodData.term).append(' ').append(type);
			if (isPrimaryKey) {
				dbType.appendIdentity(sql, (typeName.equals("int")) || (typeName.equals("java,lang.Integer")));
			}
		});
		String idFieldName = columns.getIdFieldName();
		if (!columns.getFieldMap().containsKey(idFieldName)) {
			Field field = null;
			try {
				field = emitter.getEntityClass().getDeclaredField(idFieldName);
			} catch (NoSuchFieldException e) { // This will not happen
				throw new IllegalStateException(e);
			}
			Class<?> clazz = field.getType();
			String typeName = clazz.getName();
			String type = dbType.getType(clazz, emitter.getEntityClass());
			sql.append(',').append(idFieldName).append(' ').append(type);
			dbType.appendIdentity(sql, (typeName.equals("int")) || (typeName.equals("java,lang.Integer")));
		}
		sql.append(')');
		begin();
		try (Statement stmt = conn.getConnection().createStatement()) {
			stmt.execute(sql.toString());
		} catch (SQLException e) {
			throw new QueryExecutionException(String.format(STATEMENT_FAILED_MESSAGE, sql.toString()), e);
		}
		commit();
	}

	private void createAllTables() {
		for (GenericEntityEmitter<?> emitter : emitterMap.values()) {
			String tableName = emitter.getTableName();
			if (!tableExists(tableName)) {
				createTable(tableName, emitter);
			}
		}
		for (GenericEntityCollector<?> collector : collectorMap.values()) {
			String tableName = collector.getTableName();
			if (!tableExists(tableName)) {
				GenericEntityEmitter<?> genericEntityEmitter = new GenericEntityEmitter<>(collector.getEntityClass());
				genericEntityEmitter.setConnection(conn);
				createTable(tableName, genericEntityEmitter);
			}
		}
	}

	private void deleteTable(String tableName) {
		begin();
		String sql = String.format("DROP TABLE %s", tableName);
		try (Statement stmt = conn.getConnection().createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			throw new QueryExecutionException(String.format(STATEMENT_FAILED_MESSAGE, sql), e);
		}
		commit();
	}

	private void begin() {
		String sql = "BEGIN";
		try (Statement stmt = conn.getConnection().createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			throw new QueryExecutionException(String.format(STATEMENT_FAILED_MESSAGE, sql), e);
		}
	}

	private void commit() {
		String sql = "COMMIT";
		try (Statement stmt = conn.getConnection().createStatement()) {
			stmt.execute(sql);
		} catch (SQLException e) {
			throw new QueryExecutionException(String.format(STATEMENT_FAILED_MESSAGE, sql), e);
		}
	}

	/**
	 * Utility method to return unitName of class with PersistenceUnit annotation
	 * 
	 * @param clazz Class which is expect to have PersistenceUnit annotation
	 * @return unitName
	 * @throws PersistenceException if unitName not specified or empty
	 */
	public static String getEntityName(Class<?> clazz) {
		String entityName = null;
		for (Annotation annotation : clazz.getAnnotations()) {
			Class<?> annotationClass = annotation.annotationType();
			if (annotationClass.getName().equals("javax.persistence.Entity")) {
				entityName = MethodAnalyser.getStringByInvocation(annotation, "name");
				break;
			}
		}
		return entityName;
	}

}
