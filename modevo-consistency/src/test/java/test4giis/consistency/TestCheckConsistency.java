package test4giis.consistency;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import giis.modevo.consistency.OracleCSV;
import giis.modevo.consistency.MySQLAccess;
import giis.modevo.consistency.Oracle;
import giis.modevo.migration.script.MainScript;
import giis.modevo.migration.script.execution.CassandraConnection;
import giis.modevo.model.ModelObjects;
import test4giis.modevo.TestUtils;



public class TestCheckConsistency {
	private static final String PROPERTIES = "../modevo-script/src/test/resources/dbconnection.properties";
	private static CassandraConnection connection;

	@Rule public TestName name = new TestName();
	public static void setUp(){ //cleans the entire DB
		connection = new CassandraConnection(PROPERTIES);
		try {
			Files.createDirectories(Paths.get("dat/out"));
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}
	@Test
	public void testDummy() {
		assertTrue (true);
	}
	public void testCustomV1NewColumn() {
		testConsistency(name.getMethodName());
	}
	private void testConsistency (String nameTest) {
		OracleCSV oc = new OracleCSV();
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
			initDB (preparedStatementsTable);
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		testScript (name.getMethodName(), connection);
		try {
			compareCassandraSQL();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
   
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
    private void initDB(Map<String, PreparedStatement> preparedStatementsTable) throws Exception{
		Map<String, String> tableQuery = new HashMap<String, String>();
		java.sql.Connection connectionSQL = new MySQLAccess().connect("custom");
		Oracle oc = new Oracle();
		tableQuery.put("table2", "SELECT book.id as idbook FROM book ORDER BY idbook DESC;");
		tableQuery.put("table1", "SELECT author.id as idauthor, book.id as idbook, book.title as title FROM author Inner join authorbook ON author.id = authorbook.idauthor Inner join book ON book.id = authorbook.idbook;");
		Set<String> nameTables =  preparedStatementsTable.keySet();
		Iterator<String> iteratorNameTables = nameTables.iterator();
		while (iteratorNameTables.hasNext()) {
			String nameTable = iteratorNameTables.next();
			oc.sqlQueryMigrate("custom",nameTable, tableQuery.get(nameTable), connectionSQL, connection, preparedStatementsTable.get(nameTable));
		}
		connectionSQL.close();

	}
    private void compareCassandraSQL() throws Exception{
		Map<String, String> tableQuery = new HashMap<String, String>();
		MySQLAccess mysql = new MySQLAccess();
		Connection con = mysql.connect("custom");
		Oracle oc = new Oracle();
		tableQuery.put("table2", "SELECT distinct book.id as idbook, CASE WHEN authorbook.idbook IS NOT NULL THEN book.title ELSE NULL END AS title FROM book LEFT JOIN authorbook ON book.id = authorbook.idbook order by book.id DESC;");
		tableQuery.put("table1", "SELECT author.id as idauthor, book.id as idbook, book.title  FROM author Inner join authorbook ON author.id = authorbook.idauthor Inner join book ON book.id = authorbook.idbook;");
		oc.oracleCompare(tableQuery, name.getMethodName(), PROPERTIES, con);
		con.close();

	}
    private void testScript (String nameTest,  CassandraConnection c) {
		ModelObjects m = new TestUtils().executeTransformationsAndCompareOutput (nameTest);
		new MainScript().createScriptAndText(m, c, nameTest);
	}

    
}
