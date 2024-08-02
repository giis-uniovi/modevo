package giis.modevo.consistency;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import giis.modevo.migration.script.ScriptException;
import giis.modevo.migration.script.execution.CassandraConnection;

/**
 * Main class of the oracle that contains the methods that execute the database statements to obtain the projections from the 
 * SQL database and the data from each Cassandra table and the methods that verify data integrity in the Cassandra database
 */
public class Oracle {
	
	/**
	 * Executes a SQL query that is the projection of a Cassandra table and creates a CSV with the retrieved data
	 */
	public void sqlQuery(String query, Connection con, String pathSQL) {
		OracleCsv csv = new OracleCsv();
		try (Statement stmt = con.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			csv.convertToCsv(rs, pathSQL);
		} catch (SQLException e) {
			throw new ScriptException("Error executing SQL statement: " + e.getMessage());
		}
	}

	/**
	 * Method used to populate a Cassandra database with the data of its projection in the SQL database
	 */
	public void sqlQueryMigrate(String query, Connection con, CassandraConnection connection, PreparedStatement ps) {
		try (Statement stmt = con.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			List<String> columnNames = new ArrayList<>();
			Map<String, String> columnNameTypeMap = new HashMap<>();
			for (ColumnDefinition cd : ps.getVariableDefinitions()) {
				columnAndTypeNames(cd, columnNameTypeMap, columnNames, rsmd);
			}
			while (rs.next()) {
				BoundStatementBuilder boundStmtBuilder = processResultSetRow(columnNames, columnNameTypeMap, rs, ps);
				connection.executeStatement(boundStmtBuilder.build());
			}
		} catch (SQLException e) {
			throw new ScriptException("Error executing SQL statement: " + e.getMessage());
		}
	}

	/**
	 * Replaces the value in a query with the data from a resultset
	 */
	private BoundStatementBuilder processResultSetRow(List<String> columnNames, Map<String, String> columnTypes,
			ResultSet rs, PreparedStatement ps) throws SQLException {
		Map<String, Integer> intValues = new HashMap<>();
		Map<String, String> stringValues = new HashMap<>();
		for (String columnName : columnNames) {
			if (columnTypes.containsKey(columnName)) {
				if (columnTypes.get(columnName).equalsIgnoreCase("INT")) {
					int j = rs.getInt(columnName);
					intValues.put(columnName, j);
				} else {
					String obtainedString = rs.getString(columnName);
					stringValues.put(columnName, obtainedString);
				}
			}
		}
		List<Object> toBind = new ArrayList<>();
		List<Class<?>> classes = new ArrayList<>();
		for (String columnName : columnNames) {
			if (columnTypes.containsKey(columnName)) {
				Integer intValue = intValues.get(columnName);
				if (intValue == null) {
					String stringValue = stringValues.get(columnName);
					Class<?> clase = stringValue.getClass();
					classes.add(clase);
					toBind.add(stringValue);
				} else {
					toBind.add(intValue.toString());
				}
			}
		}
		BoundStatementBuilder boundStmtBuilder = ps.boundStatementBuilder();
		for (int i = 0; i < toBind.size(); i++) {
			Object value = toBind.get(i);
			boundStmtBuilder = determineDataType(value, boundStmtBuilder, i);
		}
		return boundStmtBuilder;
	}

	/**
	 * Auxiliary method of processResultSetRow to replace a single value depending on its type. 
	 */
	private BoundStatementBuilder determineDataType(Object value, BoundStatementBuilder boundStmtBuilder,
			int position) {
		if (value instanceof String stringValue) {
			boundStmtBuilder = boundStmtBuilder.setString(position, stringValue);
		} else if (value instanceof Integer intValue) {
			boundStmtBuilder = boundStmtBuilder.setInt(position, intValue);
		} else if (value instanceof Double doubleValue) {
			boundStmtBuilder = boundStmtBuilder.setDouble(position, doubleValue);
		} else if (value instanceof Long longValue) {
			boundStmtBuilder = boundStmtBuilder.setLong(position, longValue);
		} else if (value instanceof Boolean boolValue) {
			boundStmtBuilder = boundStmtBuilder.setBoolean(position, boolValue);
		} else {
			throw new IllegalArgumentException("Unsupported type: " + value.getClass());
		}
		return boundStmtBuilder;
	}

	/**
	 * Fills parameters columnNames and columnsTypes with the names of the columns of a table and its datatype
	 */
	private void columnAndTypeNames(ColumnDefinition cd, Map<String, String> columnsTypes, List<String> columnNames, ResultSetMetaData rsmd)
			throws SQLException {
		String columnName = cd.getName().asCql(true);
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			if (columnName.contains(rsmd.getColumnName(i))) {
				columnsTypes.put(columnName, rsmd.getColumnTypeName(i));
			}
		}
		columnNames.add(columnName);
	}

}
