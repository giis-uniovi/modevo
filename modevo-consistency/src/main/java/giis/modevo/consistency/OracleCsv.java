package giis.modevo.consistency;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import giis.modevo.migration.script.ScriptException;
import giis.modevo.migration.script.execution.CassandraConnection;
import giis.modevo.model.DocumentException;

public class OracleCsv {

	/**
	 * Creates a CSV file that contains the data that is stored in the table passed as first argument
	 */
	public void csvCassandra(String tableName, String keyspace, String properties, String cassandraPath) {
		CassandraConnection connection;
		connection = new CassandraConnection(properties);
		// ResultSet for cassandra, different than SQL ones
		ResultSet rs = connection.executeStatement("SELECT * FROM \"" + keyspace + "\"." + tableName + ";");
		File file=new File(cassandraPath);
		File fileParent = new File(file.getParent());
		if (!fileParent.exists()) {
			fileParent.mkdirs();
		}
		List<String> cassandraRows = new ArrayList<>();
		// Meta data of the columns of the table
		ColumnDefinitions meta = rs.getColumnDefinitions();
		int numberOfColumns = meta.size();
		int numberOfRows = rs.getAvailableWithoutFetching();
		String[][] results = new String[numberOfRows][numberOfColumns];
		boolean[] numeric = new boolean[numberOfColumns];
		int rowCounter = 0;
		for (Row row : rs) {
			String rowString = rowProcessing(row, results, numeric, rowCounter, numberOfColumns);
			cassandraRows.add(rowString);
			rowCounter++;
		}
		sortMatrix(results, 0, cassandraRows, 0, rowCounter - 1, numeric); // orders results returned by cassandra
		try (FileWriter csvWriter = new FileWriter(file, false);) {
			for (String row : cassandraRows) {
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
	private String rowProcessing(Row row, String[][] results, boolean[] numeric, int rowNumber, int columnNumber) {
		StringBuilder stringRow = new StringBuilder("\"");
		for (int i = 0; i < columnNumber; i++) {
			String result = row.getString(i);
			if (i==0) {
				stringRow.append(row.getString(0).replaceAll("\"", "\\\"") + "\"");
			}
			else {
				stringRow.append(",\"" + row.getString(i).replaceAll("\"", "\\\"") + "\"");
			}
			numericCheck(numeric, i, result); //Although as string in the db, we check if the row is storing a numeric value
			results[rowNumber][i] = result;
		}
		stringRow.append("\n");
		return stringRow.toString();
	}

	/**
	 * Sets value in array in position i as numeric or not numeric
	 */
	private void numericCheck(boolean[] numeric, int i, String result) {
		if (isNumeric(result)) {
			numeric[i] = true;
		} else {
			numeric[i] = false;
		}
	}

	/**
	 * Sorts the results of a matrix.
	 */
	private String[][] sortMatrix(String[][] matrixResultSet, int columnNumber, List<String> cassandraRows,
			int beggining, int end, boolean[] numericArray) {

		// Return empty matrix if it is empty
		if (matrixResultSet.length == 0) {
			return new String[0][0];
		}
		// Check if column is numeric or not
		boolean numeric = numericArray[columnNumber];
		//If it is the first column of the table, it sorts the rows with a descending value
		if (columnNumber == 0) {
			sortColumn(0, matrixResultSet.length - 1, columnNumber, matrixResultSet, cassandraRows, numeric);
			matrixResultSet = sortMatrix(matrixResultSet, columnNumber + 1, cassandraRows, 0,
					matrixResultSet.length - 1, numericArray);
			return matrixResultSet;
		} 

		 //For the rest of columns it sorts them when the values of the first column are the same. This also applies 
		 //for the following of columns
		 
		else {
			List<Integer> border = equalRanges(beggining, end, columnNumber - 1, matrixResultSet,
					numericArray[columnNumber - 1]);
			for (int i = 0; i < border.size(); i = i + 2) {
				if (border.get(i).intValue() != border.get(i + 1).intValue()) { //If the values are not the same, it sorts them
					sortColumn(border.get(i), border.get(i + 1), columnNumber, matrixResultSet, cassandraRows, numeric);
					matrixResultSet = sortMatrix(matrixResultSet, columnNumber + 1, cassandraRows, border.get(i),
							border.get(i + 1), numericArray);
				}
			}
			return matrixResultSet;
		}
	}

	/**
	 * Auxilary method of orderAlgorithm to sort a column in descending order
	 */
	private String[][] sortColumn (int beginning, int end, int numberColumn, String[][] matrixResultSet,
			List<String> rowsCassandra, boolean numeric) {
		if (beginning == end) {
			return matrixResultSet;
		}
		for (int i = beginning; i < matrixResultSet.length && i <= end; i++) {
			for (int j = i + 1; j < matrixResultSet.length && j <= end; j++) {
				sortIteration(numeric, matrixResultSet, rowsCassandra, i, j, numberColumn); //Sorts two rows
			}
		}
		return matrixResultSet;
	}

	/**
	 * Auxilary method of orderAlgorithm that sorts two rows in descending order
	 */
	private void sortIteration (boolean numeric, String[][] matrixResultSet, List<String> rowsCassandra, int row1, int row2,
			int numberColumn) {
		String current = matrixResultSet[row1][numberColumn];
		String next = matrixResultSet[row2][numberColumn];
		boolean sort = false;
		if (numeric) {
			Long currentNumber = Long.valueOf(current);
			Long nextNumber = Long.valueOf(next);
			if (currentNumber < nextNumber) {
				sort = true;
			}
		} else {
			if (current.compareTo(next) < 0) {
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
	public List<Integer> equalRanges(int start, int end, int columnNumber, String[][] matrixResultSet,
			boolean numeric) {
		List<Integer> equalRangeValues = new ArrayList<>();
		String numberRange = matrixResultSet[start][columnNumber];
		equalRangeValues.add(start);
		if (start == end) {
			equalRangeValues.add(end);
			return equalRangeValues;
		}
		for (int i = start + 1; i <= end; i++) {
			if (numeric) {
				Long currentRange = Long.parseLong(numberRange);
				Long currentValue = Long.parseLong(matrixResultSet[i][columnNumber]);
				if (currentRange.longValue() != currentValue.longValue()) {
					numberRange = matrixResultSet[i][columnNumber];
					equalRangeValues.add(i - 1);
					equalRangeValues.add(i);
				}
			} else {
				String currentValue = matrixResultSet[i][columnNumber];
				if (numberRange.compareTo(currentValue) != 0) {
					numberRange = matrixResultSet[i][columnNumber];
					equalRangeValues.add(i - 1);
					equalRangeValues.add(i);
				}
			}

		}
		equalRangeValues.add(end);
		return equalRangeValues;
	}

	/**
	 * Converts to a csv the result set of a SQL query
	 */
	public void convertToCsv(java.sql.ResultSet rs, String path) {
		File file = new File(path);
		File parentFile = new File(file.getParent());
		if (!parentFile.exists()) {
			parentFile.mkdirs();
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
			throw new DocumentException(e.getMessage());
		}
	}

	/**
	 * Returns the names of the tables of a keyspace
	 */
	public Map<String, List<String>> namesTablesColumnsKeyspace(String keyspace, CassandraConnection connection) {
		Map<String, List<String>> tableColumns = new HashMap<>();
		String cqlNamesTables = "SELECT table_name FROM system_schema.tables WHERE keyspace_name = ?;";
		PreparedStatement tableNames = connection.getSession().prepare(cqlNamesTables);
		BoundStatement bs = tableNames.bind(keyspace);
		ResultSet results = connection.executeStatement(bs);
		List<String> tableNameList = new ArrayList<>();
		while (results.iterator().hasNext()) {
			String tableName = results.iterator().next().getString("table_name");
			tableNameList.add(tableName);
		}
		String cqlColumnNames = "SELECT column_name FROM system_schema.columns WHERE keyspace_name = ? and table_name = ?;";
		PreparedStatement columnNamesPreparedStatement = connection.getSession().prepare(cqlColumnNames);
		for (String tableName: tableNameList) {
			List<String> columnNamesList = new ArrayList<>();
			BoundStatement bsColumns = columnNamesPreparedStatement.bind(keyspace, tableName);
			ResultSet resultColumnNames = connection.executeStatement(bsColumns);
			Iterator<Row> rowColumnNames = resultColumnNames.iterator();
			while (rowColumnNames.hasNext()) {
				String columnName = rowColumnNames.next().getString("column_name");
				columnNamesList.add(columnName);
			}
			tableColumns.put(tableName, columnNamesList);
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
