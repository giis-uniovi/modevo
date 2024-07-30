package test4giis.consistency;

import static org.junit.Assert.assertTrue;

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

import org.junit.Before;
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
	private void testConsistency (String nameTest, Map<String, String> tableQueryCompare, String keyspace, Map<String, String> tableQueryInit) {
		OracleCsv oc = new OracleCsv();
		Map<String, List<String>> tableColumns = oc.namesTablesColumnsKeyspace(nameTest, connection);
		Map <String, PreparedStatement> preparedStatementsTable = new HashMap <>();
		Set<String> namesTables = tableColumns.keySet();
		Iterator<String> iteratorNameTables = namesTables.iterator();
		while (iteratorNameTables.hasNext()) {
			String nameTable = iteratorNameTables.next();
			PreparedStatement ps = buildInsert (nameTest, nameTable, tableColumns.get(nameTable));
			preparedStatementsTable.put(nameTable, ps);
		}
		try {
			initDB (preparedStatementsTable, keyspace, tableQueryInit);
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		testScript (name.getMethodName(), connection);
		try {
			compareCassandraSQL(tableQueryCompare, keyspace);
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
   
	/**
	 * Method to build a PreparedStatement to insert data to all columns of a Cassandra table
	 */
	private PreparedStatement buildInsert (String keyspace, String nameTable, List<String> columns) {
    	StringBuilder insert = new StringBuilder ("INSERT INTO ");
    	String a = "\""+keyspace+"\"."+nameTable;
    	System.out.print(a);
    	insert.append(a).append(" (");
    	int numberColumns = columns.size();
    	StringBuilder placeholders = new StringBuilder();
    	for (int i=0; i<numberColumns;i++) {
    		insert.append(columns.get(i));
    		if (i+1 == numberColumns) {
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
     * @param keyspace 
     * @param tableQuery 
     */
    private void initDB(Map<String, PreparedStatement> preparedStatementsTable, String keyspace, Map<String, String> tableQuery) throws Exception{
		java.sql.Connection connectionSQL = new OracleConnection().connect(keyspace);
		Oracle oc = new Oracle();
		Set<String> nameTables =  preparedStatementsTable.keySet();
		Iterator<String> iteratorNameTables = nameTables.iterator();
		while (iteratorNameTables.hasNext()) {
			String nameTable = iteratorNameTables.next();
			oc.sqlQueryMigrate(nameTable, tableQuery.get(nameTable), connectionSQL, connection, preparedStatementsTable.get(nameTable));
		}
		connectionSQL.close();

	}
    /**
     * Method to compare the data stored in the Cassandra database and the SQL database by comparing
     * each table of the Cassandra database with its projection from the SQL database.
     * @param tableQuery 
     * @param keyspace 
     */
    private void compareCassandraSQL(Map<String, String> tableQuery, String keyspace) throws Exception{
		OracleConnection mysql = new OracleConnection();
		Connection con = mysql.connect(keyspace);
		String path = "dat/out/" + keyspace + "/";
		OracleCsv csv = new OracleCsv();
		Iterator<Entry<String, String>> iteratorTableQuery = tableQuery.entrySet().iterator();
		while (iteratorTableQuery.hasNext()) {
			Entry<String, String> tableQueryIteration = iteratorTableQuery.next();
			String table= tableQueryIteration.getKey();
			String query = tableQueryIteration.getValue();
			String pathSQL = path + table + "SQL.csv";
			String pathCassandra = path + table + "CQL.csv";
			new Oracle().sqlQuery(table, query, con, name.getMethodName(), pathSQL);
			csv.csvCassandra(table, name.getMethodName(), PROPERTIES, pathCassandra);
			csv.compareCSV(pathSQL, pathCassandra, table);
		}
		try {
			con.close();
		} catch (SQLException e1) {
			throw new ScriptException(e1);
		}
		con.close();
	}
    /**
     * Call the transform and script module for the migration determined by MoDEvo
     */
    private void testScript (String nameTest,  CassandraConnection c) {
		ModelObjects m = new TestUtils().executeTransformationsAndCompareOutput (nameTest);
		new MainScript().createScriptAndText(m, c, nameTest);
	}
}
