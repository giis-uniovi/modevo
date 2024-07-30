package giis.modevo.consistency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import giis.modevo.migration.script.ScriptException;
import giis.modevo.migration.script.execution.CassandraConnection;
import giis.modevo.model.DocumentException;
import giis.visualassert.VisualAssert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OracleCsv {
	private static final String CSV_OPEN = "Error proccesing the CSV  file ";

	/**
	 * Creates a CSV file that contains the data that is stored in the table passed as first argument
	 * @param pathCassandra 
	 */
	public void csvCassandra(String nameTable, String keyspace, String properties, String pathCassandra) {
		CassandraConnection connection;
		connection = new CassandraConnection(properties);
		// ResultSet for cassandra, different than SQL ones
		ResultSet rs = connection.executeStatement("SELECT * FROM \"" + keyspace + "\"." + nameTable + ";");
		File file=new File(pathCassandra);
		File fileParent = new File(file.getParent());
		if (!fileParent.exists()) {
			fileParent.mkdirs();
		}
		List<String> rowsCassandra = new ArrayList<>();
		// Meta data of the columns of the table
		ColumnDefinitions meta = rs.getColumnDefinitions();
		int numberOfColumns = meta.size();
		int numberRows = rs.getAvailableWithoutFetching();
		String[][] results = new String[numberRows][numberOfColumns];
		boolean[] numeric = new boolean[numberOfColumns];
		int counterRows = 0;
		for (Row row : rs) {
			String rowString = rowProcessing(row, results, numeric, counterRows, numberOfColumns);
			rowsCassandra.add(rowString);
			counterRows++;
		}
		matrixSort(results, 0, rowsCassandra, 0, counterRows - 1, numeric); // orders results returned by cassandra
		try (FileWriter csvWriter = new FileWriter(file, false);) {
			for (String row : rowsCassandra) {
				csvWriter.write(row);
			}
		} catch (FileNotFoundException e) {
			throw new DocumentException("File not found " + e.getMessage());

		} catch (IOException e) {
			throw new DocumentException("Errror writing file");

		}
		connection.close();
	}

	/**
	 * Auxilary method of csvCassandra that processes each row and returns 
	 * its content in a string formatted as a CSV.
	 */
	private String rowProcessing(Row row, String[][] results, boolean[] numeric, int counterRows, int numberOfColumns) {
		StringBuilder rowString = new StringBuilder("\"");
		for (int i = 0; i < numberOfColumns; i++) {
			String result = row.getString(i);
			if (i==0) {
				rowString.append(row.getString(0).replaceAll("\"", "\\\"") + "\"");
			}
			else {
				rowString.append(",\"" + row.getString(i).replaceAll("\"", "\\\"") + "\"");
			}
			checkNumeric(numeric, i, result); //Although as string in the db, we check if the row is storing a numeric value
			results[counterRows][i] = result;
		}
		rowString.append("\n");
		return rowString.toString();
	}

	/**
	 * Sets value in array in position i as numeric or not numeric
	 */
	private void checkNumeric(boolean[] numeric, int i, String result) {
		if (isNumeric(result)) {
			numeric[i] = true;
		} else {
			numeric[i] = false;
		}
	}

	/**
	 * Sorts the results of a matrix.
	 */
	private String[][] matrixSort(String[][] matrixResultSet, int numberColumn, List<String> rowsCassandra,
			int beggining, int end, boolean[] numericArray) {

		// Return empty matrix if it is empty
		if (matrixResultSet.length == 0) {
			return new String[0][0];
		}
		// Check if column is numeric or not
		boolean numeric = numericArray[numberColumn];
		//If it is the first column of the table, it sorts the rows with a descending value
		if (numberColumn == 0) {
			sortColumn(0, matrixResultSet.length - 1, numberColumn, matrixResultSet, rowsCassandra, numeric);
			matrixResultSet = matrixSort(matrixResultSet, numberColumn + 1, rowsCassandra, 0,
					matrixResultSet.length - 1, numericArray);
			return matrixResultSet;
		} 
		/*
		 * For the rest of columns it sorts them when the values of the first column are the same. This also applies 
		 * for the following of columns
		 */	
		else {
			List<Integer> border = rangesEqual(beggining, end, numberColumn - 1, matrixResultSet,
					numericArray[numberColumn - 1]);
			for (int i = 0; i < border.size(); i = i + 2) {
				if (border.get(i).intValue() != border.get(i + 1).intValue()) { //If the values are not the same, it sorts them
					sortColumn(border.get(i), border.get(i + 1), numberColumn, matrixResultSet, rowsCassandra, numeric);
					matrixResultSet = matrixSort(matrixResultSet, numberColumn + 1, rowsCassandra, border.get(i),
							border.get(i + 1), numericArray);
				}
			}
			return matrixResultSet;
		}
	}

	/**
	 * Auxilary method of orderAlgorithm to sort a column in descending order
	 */
	private String[][] sortColumn (int beggining, int end, int numberColumn, String[][] matrixResultSet,
			List<String> rowsCassandra, boolean numeric) {
		if (beggining == end) {
			return matrixResultSet;
		}
		for (int i = beggining; i < matrixResultSet.length && i <= end; i++) {
			for (int j = i + 1; j < matrixResultSet.length && j <= end; j++) {
				sortIteration(numeric, matrixResultSet, rowsCassandra, i, j, numberColumn); //Sorts two rows
			}
		}
		return matrixResultSet;
	}

	/**
	 * Auxilary method of orderAlgorithm that sorts two rows
	 */
	private void sortIteration (boolean numeric, String[][] matrixResultSet, List<String> rowsCassandra, int row1, int row2,
			int numberColumn) {
		String current = matrixResultSet[row1][numberColumn];
		String parallel = matrixResultSet[row2][numberColumn];
		boolean sort = false;
		if (numeric) {
			Long numberCurrent = Long.valueOf(current);
			Long numberParallel = Long.valueOf(parallel);
			if (numberCurrent < numberParallel) {
				sort = true;
			}
		} else {
			if (current.compareTo(parallel) < 0) {
				sort = true;
			}
		}
		if (sort) { //Executes the change of values
			String[] temporal = matrixResultSet[row1];
			matrixResultSet[row1] = matrixResultSet[row2];
			matrixResultSet[row2] = temporal;
			String rowTemporal = rowsCassandra.get(row2);
			rowsCassandra.set(row2, rowsCassandra.get(row1));
			rowsCassandra.set(row1, rowTemporal);
		}
	}

	/**
	 * Returns the ranges of values which are the same
	 */
	public List<Integer> rangesEqual(int beggining, int end, int numberColumn, String[][] matrixResultSet,
			boolean numeric) {
		List<Integer> rangesEqualValues = new ArrayList<>();
		String numberRange = matrixResultSet[beggining][numberColumn];
		rangesEqualValues.add(beggining);
		if (beggining == end) {
			rangesEqualValues.add(end);
			return rangesEqualValues;
		}
		for (int i = beggining + 1; i <= end; i++) {
			if (numeric) {
				Long currentRange = Long.parseLong(numberRange);
				Long currentValue = Long.parseLong(matrixResultSet[i][numberColumn]);
				if (currentRange.longValue() != currentValue.longValue()) {
					numberRange = matrixResultSet[i][numberColumn];
					rangesEqualValues.add(i - 1);
					rangesEqualValues.add(i);
				}
			} else {
				String currentValue = matrixResultSet[i][numberColumn];
				if (numberRange.compareTo(currentValue) != 0) {
					numberRange = matrixResultSet[i][numberColumn];
					rangesEqualValues.add(i - 1);
					rangesEqualValues.add(i);
				}
			}

		}
		rangesEqualValues.add(end);
		return rangesEqualValues;
	}

	/**
	 * Compares two CSV files and returns true if they store the same data, false
	 * otherwise
	 */
	public boolean compareCSV(String pathSQL, String pathCQL, String nameTable) {
		String introduction = "Table " + nameTable + ": ";
		try (BufferedReader sql = new BufferedReader(new FileReader(pathSQL));
			BufferedReader cassandra = new BufferedReader(new FileReader(pathCQL));) {
			String lineSQL;
			String lineCassandra;
			while ((lineSQL = sql.readLine()) != null) {
				lineCassandra = cassandra.readLine();
				VisualAssert va = new VisualAssert();
				va.assertEquals(lineCassandra, lineSQL);
			}
			lineCassandra = cassandra.readLine();
			if (lineCassandra != null) {
				log.info(introduction + "Cassandra contains more lines than SQL.");
				return false;
			}
		} catch (IOException e) {
			throw new DocumentException(CSV_OPEN + e.getMessage());
		}
		return true;
	}

	/**
	 * Converts to a csv the result set of a SQL query
	 */
	public void convertToCsv(java.sql.ResultSet rs, String path) {
		File file = new File(path);
		File fileParent = new File(file.getParent());
		if (!fileParent.exists()) {
			fileParent.mkdirs();
		}
		List<String> extractionRS = new ArrayList<>();
		ResultSetMetaData meta;
		try {
			meta = rs.getMetaData();
			int numberOfColumns = meta.getColumnCount();
			while (rs.next()) {
				StringBuilder row = new StringBuilder();
				if (rs.getString(1) == null) {
					row.append("\"" + "" + "\"");
				} else {
					row.append("\"" + rs.getString(1).replace("\"", "\\\"") + "\"");
				}
				for (int i = 2; i < numberOfColumns + 1; i++) {
					if (rs.getMetaData().getColumnTypeName(i).equalsIgnoreCase("DECIMAL")) {
						row.append(",\"" + rs.getLong(i) + "\"");
					} else if (rs.getString(i) != null) {
						row.append(",\"" + rs.getString(i).replace("\"", "\\\"") + "\"");
					} else {
						row.append(",\"" + "" + "\"");
					}
				}
				row.append("\n");
				extractionRS.add(row.toString());
			}
		} catch (SQLException e) {
			throw new ScriptException("Error proccesing SQL statement " + e.getMessage());
		}
		FileWriter csvWriter;
		try {
			csvWriter = new FileWriter(file, false);
			for (String statement : extractionRS) {
				csvWriter.write(statement);
			}
			csvWriter.close();
		} catch (IOException e) {
			throw new DocumentException(CSV_OPEN + e.getMessage());
		}
	}

	/**
	 * Returns the names of the tables of a keyspace
	 */
	public Map<String, List<String>> namesTablesColumnsKeyspace(String keyspace, CassandraConnection connection) {
		Map<String, List<String>> tableColumns = new HashMap<>();
		String cqlNamesTables = "SELECT table_name FROM system_schema.tables WHERE keyspace_name = ?;";
		PreparedStatement tablesNames = connection.getSession().prepare(cqlNamesTables);
		BoundStatement bs = tablesNames.bind(keyspace);
		ResultSet results = connection.executeStatement(bs);
		Set<String> namesTablesSet = new HashSet<>();
		while (results.iterator().hasNext()) {
			String nameTable = results.iterator().next().getString("table_name");
			namesTablesSet.add(nameTable);
		}
		Iterator<String> iterateSetNamesTables = namesTablesSet.iterator();
		String cqlNameColumns = "SELECT column_name FROM system_schema.columns WHERE keyspace_name = ? and table_name = ?;";
		PreparedStatement columnNames = connection.getSession().prepare(cqlNameColumns);
		while (iterateSetNamesTables.hasNext()) {
			String nameTable = iterateSetNamesTables.next();
			List<String> nameColumns = new ArrayList<>();
			BoundStatement bsColumns = columnNames.bind(keyspace, nameTable);
			ResultSet resultNameColumns = connection.executeStatement(bsColumns);
			Iterator<Row> rowsNameColumns = resultNameColumns.iterator();
			while (rowsNameColumns.hasNext()) {
				String nameColumn = rowsNameColumns.next().getString("column_name");
				nameColumns.add(nameColumn);
			}
			tableColumns.put(nameTable, nameColumns);
		}
		return tableColumns;
	}

	/**
	 * Checks if a value is numeric
	 */
	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
