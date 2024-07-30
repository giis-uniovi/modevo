package giis.modevo.consistency;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.datastax.oss.driver.api.core.cql.BoundStatementBuilder;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import giis.modevo.migration.script.ScriptException;
import giis.modevo.migration.script.execution.CassandraConnection;
import giis.visualassert.VisualAssert;

/**
 * Main class of the oracle that contains the methods that execute the database statements to obtain the projections from the 
 * SQL database and the data from each Cassandra table and the methods that verify data integrity in the Cassandra database
 */
public class Oracle {
	
	/**
	 * Executes a SQL query that is the projection of a Cassandra table
	 * @param pathSQL 
	 */
	public void sqlQuery(String tableName, String query, Connection con, String keyspaceCassandra, String pathSQL) {
		OracleCsv csv = new OracleCsv();
		try (Statement stmt = con.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			csv.convertToCsv(rs, tableName, pathSQL);
		} catch (SQLException e) {
			throw new ScriptException("Error executing SQL statement: " + e.getMessage());
		}
	}

	/**
	 * Method used to populate a Cassandra database with the data of its projection in the SQL database
	 */
	public void sqlQueryMigrate(String tableName, String query, Connection con,
			CassandraConnection connection, PreparedStatement ps) {
		try (Statement stmt = con.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			List<String> nameColumns = new ArrayList<>();
			Iterator<ColumnDefinition> iter = ps.getVariableDefinitions().iterator();
			Map<String, String> nameColumnsTypes = new HashMap<>();
			while (iter.hasNext()) {
				ColumnDefinition cd = iter.next();
				nameColumnAndType(cd, nameColumnsTypes, nameColumns, rsmd);
			}
			while (rs.next()) {
				BoundStatementBuilder boundStmtBuilder = processResultSetRow(nameColumns, nameColumnsTypes, rs, ps);
				connection.executeStatement(boundStmtBuilder.build());
			}
		} catch (SQLException e) {
			throw new ScriptException("Error executing SQL statement: " + e.getMessage());
		}
	}

	/**
	 * Replaces the value in a query with the data from a resultset
	 * @throws SQLException
	 */
	private BoundStatementBuilder processResultSetRow(List<String> nameColumns, Map<String, String> nameColumnsTypes,
			ResultSet rs, PreparedStatement ps) throws SQLException {
		Map<String, Integer> valuesInt = new HashMap<>();
		Map<String, String> valuesString = new HashMap<>();
		for (String nameColumn : nameColumns) {
			if (nameColumnsTypes.containsKey(nameColumn)) {
				if (nameColumnsTypes.get(nameColumn).equalsIgnoreCase("INT")) {
					int j = rs.getInt(nameColumn);
					valuesInt.put(nameColumn, j);
				} else {
					String obtainedString = rs.getString(nameColumn);
					valuesString.put(nameColumn, obtainedString);
				}
			}
		}
		List<Object> toBind = new ArrayList<>();
		List<Class<?>> classes = new ArrayList<>();
		for (String nameColumn : nameColumns) {
			if (nameColumnsTypes.containsKey(nameColumn)) {
				Integer valueInt = valuesInt.get(nameColumn);
				if (valueInt == null) {
					String valueString = valuesString.get(nameColumn);
					Class<?> clase = valueString.getClass();
					classes.add(clase);
					toBind.add(valueString);
				} else {
					toBind.add(valueInt.toString());
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
	 * Auxilary method of processResultSetRow to replace a single value depending on its type. 
	 */
	private BoundStatementBuilder determineDataType(Object value, BoundStatementBuilder boundStmtBuilder,
			int position) {
		if (value instanceof String valueString) {
			boundStmtBuilder = boundStmtBuilder.setString(position, valueString);
		} else if (value instanceof Integer valueInt) {
			boundStmtBuilder = boundStmtBuilder.setInt(position, valueInt);
		} else if (value instanceof Double valueDouble) {
			boundStmtBuilder = boundStmtBuilder.setDouble(position, valueDouble);
		} else if (value instanceof Long valueLong) {
			boundStmtBuilder = boundStmtBuilder.setLong(position, valueLong);
		} else if (value instanceof Boolean valueBool) {
			boundStmtBuilder = boundStmtBuilder.setBoolean(position, valueBool);
		} else {
			throw new IllegalArgumentException("Unsupported type: " + value.getClass());
		}
		return boundStmtBuilder;
	}

	/**
	 * Fills parameters nameColumns and nameColumnsTypes with the names of the columns of a table and its datatype
	 */
	private void nameColumnAndType(ColumnDefinition cd, Map<String, String> nameColumnsTypes, List<String> nameColumns, ResultSetMetaData rsmd)
			throws SQLException {
		String nameColumn = cd.getName().asCql(true);
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			if (nameColumn.contains(rsmd.getColumnName(i))) {
				nameColumnsTypes.put(nameColumn, rsmd.getColumnTypeName(i));
			}
		}
		nameColumns.add(nameColumn);
	}

}
