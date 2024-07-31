package test4giis.consistency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import giis.modevo.consistency.OracleCsv;
import giis.modevo.consistency.OracleConnection;
import giis.modevo.consistency.Oracle;
import giis.modevo.migration.script.MainScript;
import giis.modevo.migration.script.ScriptException;
import giis.modevo.migration.script.execution.CassandraConnection;
import giis.modevo.model.ModelObjects;
import test4giis.modevo.TestUtils;
import test4giis.script.TestExecutionScript;

public class TestCheckConsistency {
	private static final String PROPERTIES = "../modevo-script/src/test/resources/dbconnection.properties";
	private static CassandraConnection connection;

	@Rule public TestName name = new TestName();
	
	@BeforeClass
	public static void setUp(){ //cleans the entire DB
		connection = new CassandraConnection(PROPERTIES);
		try {
			Files.createDirectories(Paths.get("dat/out"));
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}
	
	/**
	 * First the Cassandra database is populated with data from the SQL database to guarantee that it has data integrity before the migration.
	 * These data is retrieved through the queries that are in projectionAfterEvo map.
	 * After evolving the Cassandra databases and migrating data with MoDEvo, it compares the data stored in both databases.
	 * For each Cassandra table a projection of the table from the SQL table is obtained by executing the query defined in  projectionAfterEvo.
	 * 
	 */
	@Test
	public void testCustomV1NewColumn() {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionAfterEvo.put("table2", "SELECT distinct book.id as idbook, CASE WHEN authorbook.idbook IS NOT NULL THEN book.title ELSE NULL END AS title FROM book LEFT JOIN authorbook ON book.id = authorbook.idbook order by book.id DESC;");
		projectionAfterEvo.put("table1", "SELECT author.id as idauthor, book.id as idbook, book.title  FROM author Inner join authorbook ON author.id = authorbook.idauthor Inner join book ON book.id = authorbook.idbook order by author.id DESC, book.id DESC;");
		projectionBeforeEvo.put("table2", "SELECT book.id as idbook FROM book ORDER BY idbook DESC;");
		projectionBeforeEvo.put("table1", "SELECT author.id as idauthor, book.id as idbook, book.title as title FROM author Inner join authorbook ON author.id = authorbook.idauthor Inner join book ON book.id = authorbook.idbook;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	
	/**
	 * Generic method for the verification of the data integrity in the database after performing the migrations determined by MoDEvo.
	 * First, it initializes the Cassandra database with data obtained from a SQL database that maintains data integrity. 
	 * Second, it calls the other modules of MoDEvo to determine the migrations and execute them.
	 * Third, it checks that data integrity is maintained by using the oracle that compares the data stored in the SQL database and the Cassandra database
	 */
	private void testConsistency (String testName, Map<String, String> tableQueryCompare, String keyspace, Map<String, String> tableQueryInit) {
		setUpCassandraDatabase(testName, keyspace, tableQueryInit);
		testScript (name.getMethodName(), connection);
		compareCassandraSQL(tableQueryCompare, keyspace);
	}
   
	/**
	 * Sets up the Cassandra by populating it with all the data required from the SQL database using the queries specified in tableProjection
	 */
	private void setUpCassandraDatabase(String testName, String keyspace, Map<String, String> tableProjection) {
		OracleCsv oc = new OracleCsv();
		Map<String, List<String>> tableColumnsMap = oc.namesTablesColumnsKeyspace(testName, connection); //Map of the names of tables and its columns
		Map <String, PreparedStatement> preparedStatementsTable = new HashMap <>();
		Set <Entry <String, List <String>>> entries= tableColumnsMap.entrySet();
		Iterator<Entry<String, List<String>>> iteratorTableNames = entries.iterator();
		while (iteratorTableNames.hasNext()) { //Iterates over all tables of the database to populate them
			Entry<String, List<String>> tableColumns = iteratorTableNames.next();
			String tableName = tableColumns.getKey();
			List<String> columnNames = tableColumns.getValue();
			PreparedStatement ps = buildInsert (testName, tableName, columnNames); //Generic preparedStatement for the table
			preparedStatementsTable.put(tableName, ps);
		}
		migrateSqlToCassandra (preparedStatementsTable, keyspace, tableProjection); //Migrates data from the SQL database to each Cassandra table
	}
	
	/**
	 * Method to build a PreparedStatement to insert data to all columns of a Cassandra table
	 */
	private PreparedStatement buildInsert (String keyspace, String tableName, List<String> columns) {
    	StringBuilder insert = new StringBuilder ("INSERT INTO ");
    	String a = "\""+keyspace+"\"."+tableName;
    	System.out.print(a);
    	insert.append(a).append(" (");
    	int columnNumber = columns.size();
    	StringBuilder placeholders = new StringBuilder();
    	for (int i=0; i<columnNumber;i++) {
    		insert.append(columns.get(i));
    		if (i+1 == columnNumber) {
    			insert.append(")");
    			placeholders.append("?)");
    		}
    		else {
    			insert.append(", ");
    			placeholders.append("?, ");

    		}
    	}
    	insert.append(" VALUES (").append(placeholders).append(";");
    	System.out.print(insert.toString());
    	return connection.getSession().prepare(insert.toString());
    }
	
    /**
     * Method to start the migration of data from the SQL database to the Cassandra database to populate it before using MoDEvo
     */
    private void migrateSqlToCassandra(Map<String, PreparedStatement> preparedStatementsTable, String keyspace, Map<String, String> tableQuery){
		java.sql.Connection connectionSQL = new OracleConnection().connect(keyspace);
		Set<String> tablesNames =  preparedStatementsTable.keySet();
		Iterator<String> iteratorTablesNames = tablesNames.iterator();
		while (iteratorTablesNames.hasNext()) {
			String tableName = iteratorTablesNames.next();
			new Oracle().sqlQueryMigrate(tableQuery.get(tableName), connectionSQL, connection, preparedStatementsTable.get(tableName));
		}
		try {
			connectionSQL.close();
		} catch (SQLException e) {
			throw new ScriptException(e);
		}
	}
    /**
     * Call the transform and script module for the migration determined by MoDEvo
     */
    private void testScript (String testName,  CassandraConnection c) {
		ModelObjects m = new TestUtils().executeTransformationsAndCompareOutput (testName);
		new MainScript().createScriptAndText(m, c, testName);
	}
    
    /**
     * Method to compare the data stored in the Cassandra database and the SQL database by comparing
     * each table of the Cassandra database with its projection from the SQL database.
     */
    private void compareCassandraSQL(Map<String, String> tableQuery, String keyspace) {
		OracleConnection mysql = new OracleConnection();
		Connection con = mysql.connect(keyspace);
		String path = "dat/out/" + keyspace + "/";
		OracleCsv csv = new OracleCsv();
		Iterator<Entry<String, String>> iteratorTableQuery = tableQuery.entrySet().iterator();
		while (iteratorTableQuery.hasNext()) {
			Entry<String, String> tableQueryIteration = iteratorTableQuery.next();
			String table= tableQueryIteration.getKey();
			String query = tableQueryIteration.getValue();
			String sqlPath = path + table + "SQL.csv";
			String cassandraPath = path + table + "CQL.csv";
			new Oracle().sqlQuery(query, con, sqlPath);
			csv.csvCassandra(table, name.getMethodName(), PROPERTIES, cassandraPath);
			csv.compareCSV(sqlPath, cassandraPath, table);
		}
		try {
			con.close();
		} catch (SQLException e1) {
			throw new ScriptException(e1);
		}
	}
}
