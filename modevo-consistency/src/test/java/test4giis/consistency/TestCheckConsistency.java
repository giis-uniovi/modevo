package test4giis.consistency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
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
import giis.modevo.migration.script.execution.CassandraConnection;
import giis.modevo.model.ModelObjects;
import giis.visualassert.VisualAssert;
import test4giis.modevo.TestUtils;

public class TestCheckConsistency {
	private static final String PROPERTIES = "../modevo-script/src/test/resources/dbconnection.properties";
	private static CassandraConnection connection;

	@Rule public TestName name = new TestName();
	
	@BeforeClass
	public static void setUp() throws IOException{ //cleans the entire DB
		connection = new CassandraConnection(PROPERTIES);
		Files.createDirectories(Paths.get("dat/out"));
	}
	
	/**
	 * First the Cassandra database is populated with data from the SQL database to guarantee that it has data integrity before the migration.
	 * These data is retrieved through the queries that are in projectionAfterEvo map.
	 * After evolving the Cassandra databases and migrating data with MoDEvo, it compares the data stored in both databases.
	 * For each Cassandra table a projection of the table from the SQL table is obtained by executing the query defined in  projectionAfterEvo.
	 * 
	 */
	@Test
	public void testCustomV1NewColumn() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionAfterEvo.put("table2", "SELECT distinct book.id AS idbook, CASE WHEN authorbook.idbook IS NOT NULL THEN book.title ELSE NULL END AS title FROM book LEFT JOIN authorbook ON book.id = authorbook.idbook ORDER BY book.id DESC;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		projectionBeforeEvo.put("table2", "SELECT book.id AS idbook FROM book ORDER BY idbook DESC;");
		projectionBeforeEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title AS title FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	@Test
	public void testCustomV2TwoNewColumnSourceTwoTables() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title AS title FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		projectionBeforeEvo.put("table2", "SELECT book.id AS idbook FROM book ORDER BY idbook DESC;");
		projectionAfterEvo.put("table2", "SELECT DISTINCT book.id AS idbook, CASE WHEN authorbook.idbook IS NOT NULL THEN book.publisher ELSE NULL END AS publisher, CASE WHEN authorbook.idbook IS NOT NULL THEN book.title ELSE NULL END AS title FROM book LEFT JOIN authorbook ON book.id = authorbook.idbook ORDER BY idbook DESC;");
		projectionBeforeEvo.put("table3", "SELECT book.id AS idbook,author.id AS idauthor,  book.publisher AS publisher FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table3", "SELECT book.id AS idbook,author.id AS idauthor,   book.publisher AS publisher FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY book.id DESC, author.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	@Test
	public void testCustomV3TwoNewColumnsUsingNMRelationship() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title AS title FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		projectionBeforeEvo.put("table2", "SELECT author.id AS idauthor FROM author;");
		projectionAfterEvo.put("table2New", "SELECT author.id AS idauthor, CASE WHEN authorbook.idbook IS NOT NULL THEN book.id ELSE NULL END AS idbook, CASE WHEN authorbook.idbook IS NOT NULL THEN book.title ELSE NULL END AS title FROM author LEFT JOIN authorbook ON author.id = authorbook.idauthor LEFT JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	@Test
	public void testCustomV4JoinTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title AS title FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		projectionBeforeEvo.put("table2", "SELECT author.id AS idauthor FROM author;");
		projectionAfterEvo.put("table2New", "SELECT author.id AS idauthor, CASE WHEN authorbook.idbook IS NOT NULL THEN book.id ELSE NULL END AS idbook, CASE WHEN authorbook.idbook IS NOT NULL THEN book.title ELSE NULL END AS title FROM author LEFT JOIN authorbook ON author.id = authorbook.idauthor LEFT JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	@Test
	public void testCustomV5JoinTableKeyEntityInTargetTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title AS title FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		projectionBeforeEvo.put("table2", "SELECT author.id AS idauthor, book.id AS idbook FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table2", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	@Test
	public void testCustomV6CopyTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title AS title FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		projectionAfterEvo.put("table1copied", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	@Test
	public void testCustomV7SplitTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title AS title FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title  FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		projectionAfterEvo.put("table1copied1", "SELECT DISTINCT book.id AS idbook, book.title  FROM book INNER JOIN authorbook ON book.id = authorbook.idbook INNER JOIN author ON author.id = authorbook.idauthor ORDER BY book.id DESC;");
		projectionAfterEvo.put("table1copied2", "SELECT DISTINCT author.id AS idauthor FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	/**
	 * Generic method for the verification of the data integrity in the database after performing the migrations determined by MoDEvo.
	 * First, it initializes the Cassandra database with data obtained from a SQL database that maintains data integrity. 
	 * Second, it calls the other modules of MoDEvo to determine the migrations and execute them.
	 * Third, it checks that data integrity is maintained by using the oracle that compares the data stored in the SQL database and the Cassandra database
	 */
	private void testConsistency (String testName, Map<String, String> tableQueryCompare, String keyspace, Map<String, String> tableQueryInit) throws IOException {
		setUpCassandraDatabase(testName, keyspace, tableQueryInit);
		testScript (name.getMethodName(), connection);
		compareCassandraSql(tableQueryCompare, keyspace);
	}
   
	/**
	 * Sets up the Cassandra by populating it with all the data required from the SQL database using the queries specified in tableProjection
	 */
	private void setUpCassandraDatabase(String testName, String keyspace, Map<String, String> tableProjection) {
		OracleCsv oc = new OracleCsv();
		Map<String, List<String>> tableColumnsMap = oc.namesTablesColumnsKeyspace(testName, connection); //Map of the names of tables and its columns
		Map <String, PreparedStatement> preparedStatementsTable = new HashMap <>();
		Set <Entry <String, List <String>>> entries= tableColumnsMap.entrySet();
		for (Entry<String, List<String>> tableColumns: entries) {
			String tableName = tableColumns.getKey();
			List<String> columnNames = tableColumns.getValue();
			PreparedStatement ps = buildInsert (testName, tableName, columnNames); //Generic preparedStatement for the table
			preparedStatementsTable.put(tableName, ps);
		}	
		migrateSqlToCassandra (preparedStatementsTable, keyspace, tableProjection); //Migrates data from the SQL database to each Cassandra table
	}
	
	/**
	 * Builds a PreparedStatement to insert data to all columns of a Cassandra table
	 */
	private PreparedStatement buildInsert (String keyspace, String tableName, List<String> columns) {
    	StringBuilder insert = new StringBuilder ("INSERT INTO ");
    	String a = "\""+keyspace+"\"."+tableName;
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
    	return connection.getSession().prepare(insert.toString());
    }
	
    /**
     * Starts the migration of data from the SQL database to the Cassandra database to populate it before using MoDEvo
     */
    private void migrateSqlToCassandra(Map<String, PreparedStatement> tablePreparedStatement, String keyspace, Map<String, String> tableQuery){
		OracleConnection oracleConnection = new OracleConnection();
    	java.sql.Connection connectionSql = oracleConnection.connect(keyspace);
		Set<Entry<String, PreparedStatement>> entryTablePreparedStatement =tablePreparedStatement.entrySet();
		for (Entry<String, PreparedStatement> preparedStatementTable:entryTablePreparedStatement) {
			String tableName = preparedStatementTable.getKey();
			if (tableQuery.containsKey(tableName)) {
				PreparedStatement preparedStatement = preparedStatementTable.getValue();
				String query = tableQuery.get(tableName);
				new Oracle().sqlQueryMigrate(query, connectionSql, connection, preparedStatement);
			}
		}
		oracleConnection.closeConnection();
	}
    /**
     * Calls the transform and script module for the migration determined by MoDEvo
     */
    private void testScript (String testName,  CassandraConnection c) {
		ModelObjects m = new TestUtils().executeTransformationsAndCompareOutput (testName);
		new MainScript().createScriptAndText(m, c, testName);
	}
    
    /**
     * Compares the data stored in the Cassandra database and the SQL database by comparing
     * each table of the Cassandra database with its projection from the SQL database.
     */
    private void compareCassandraSql(Map<String, String> tableQuery, String keyspace) throws IOException {
		OracleConnection mysql = new OracleConnection();
		Connection con = mysql.connect(keyspace);
		String path = "dat/out/" + keyspace + "/";
		OracleCsv csv = new OracleCsv();
		for (Entry<String, String> tableQueryIteration:tableQuery.entrySet()) {
			String table= tableQueryIteration.getKey();
			String query = tableQueryIteration.getValue();
			String sqlPath = path + table + "SQL.csv";
			String cassandraPath = path + table + "CQL.csv";
			new Oracle().sqlQuery(query, con, sqlPath); //Creates a CSV with the projection data
			csv.csvCassandra(table, name.getMethodName(), PROPERTIES, cassandraPath);
			//Retrieves the data from the csv files to the variables sql and cassandra
			String sql = Files.readString(Paths.get(sqlPath)); 
			String cassandra = Files.readString(Paths.get(cassandraPath));
			VisualAssert va = new VisualAssert();
			va.assertEquals(sql, cassandra); //compares the data stored in both files
		}
		mysql.closeConnection();
	}
}
