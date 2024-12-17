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
	@Test
	public void testCustomV8JoinColumn() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, book.title as title, book.subtitle AS subtitle FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook, CONCAT(book.title, book.subtitle) AS completetitle, book.subtitle AS subtitle, book.title as title FROM  author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}
	@Test
	public void testCustomV9RemovePK() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("table1beforechange", "SELECT author.id AS idauthor, book.id AS idbook, book.title as title FROM author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook;");
		projectionAfterEvo.put("table1", "SELECT author.id AS idauthor, book.id AS idbook FROM  author INNER JOIN authorbook ON author.id = authorbook.idauthor INNER JOIN book ON book.id = authorbook.idbook ORDER BY author.id DESC, book.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "custom", projectionBeforeEvo);
	}

	@Test
	public void testMindsV10NewTableMigrationFromOneTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("user_hashtags", "SELECT id AS hashtag, iduser AS user_guid FROM hashtag;");
		projectionAfterEvo.put("user_hashtags", "SELECT iduser AS user_guid, id AS hashtag FROM hashtag ORDER BY user_guid DESC;");
		projectionAfterEvo.put("hidden_hashtags", "SELECT id AS hashtag, iduser AS admin_guid FROM hashtag ORDER BY hashtag DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "minds", projectionBeforeEvo);
	}
	
	@Test
	public void testMindsV27NewTableMigrationFromOneTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("withholdings", "SELECT address AS wallet_address, iduser AS user_guid FROM onchain;");
		projectionAfterEvo.put("withholdings", "SELECT DISTINCT iduser AS user_guid, address AS wallet_address FROM onchain ORDER BY user_guid DESC;");
		projectionAfterEvo.put("wire_support_tiers", "SELECT address, iduser AS user_guid FROM onchain ORDER BY address DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "minds", projectionBeforeEvo);
	}
	@Test
	public void testMindsV9NewTableMigrationFromOneTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("user", "SELECT id AS 'key' FROM user;");
		projectionAfterEvo.put("user", "SELECT id AS 'key' FROM user ORDER BY user.id DESC;");
		projectionAfterEvo.put("sendwyre_accounts", "SELECT id AS user_guid FROM user ORDER BY user.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "minds", projectionBeforeEvo);
	}
	@Test
	public void testWireV2NewTableMigrationFromOneTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("user_team", "SELECT user.id AS user, user.teamid AS team FROM user;");
		projectionAfterEvo.put("user_team", "SELECT user.id AS user, user.teamid AS team FROM user ORDER BY user.id DESC;");
		projectionAfterEvo.put("billing_team_member", "SELECT user.id AS user, user.teamid AS team FROM user ORDER BY user.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "wire", projectionBeforeEvo);
	}
	@Test
	public void testWireV8NewTableMigrationFromPreviousVersion() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("scim_user", "SELECT scim.id AS id FROM scim;");
		projectionAfterEvo.put("scim_user", "SELECT scim.id AS id FROM scim ORDER BY scim.id DESC;");
		projectionAfterEvo.put("scim_user_times", "SELECT scim.id AS id FROM scim ORDER BY scim.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "wire", projectionBeforeEvo);
	}
	@Test
	public void testWireV91NewTableMigrationFromOneTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("user_team", "SELECT user.id AS user, user.teamid AS team FROM user;");
		projectionAfterEvo.put("user_team", "SELECT user.id AS user, user.teamid AS team FROM user ORDER BY user.id DESC;");
		projectionAfterEvo.put("users_pending_activation", "SELECT user.id AS user FROM user ORDER BY user.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "wire", projectionBeforeEvo);
	}
	@Test
	public void testWireV92NewTableMigrationFromSeveralTables() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("scim_external_ids", "SELECT user.id AS user, user.external AS external FROM user;");
		projectionAfterEvo.put("scim_external_ids", "SELECT user.id AS user, user.external AS external FROM user ORDER BY user.id DESC;");
		projectionBeforeEvo.put("user_team", "SELECT user.id AS user, user.teamid AS team FROM user;");
		projectionAfterEvo.put("user_team", "SELECT user.id AS user, user.teamid AS team FROM user ORDER BY user.id DESC;");
		projectionAfterEvo.put("scim_external", "SELECT user.id AS user, user.external AS external, user.teamid AS team FROM user ORDER BY user.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "wire", projectionBeforeEvo);
	}
	@Test
	public void testThingsBoardV11NewColumnNonKey() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("device", "SELECT device.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.dev_name as name, device.dev_type as type FROM device INNER JOIN customer ON customer.iddevice = device.id INNER JOIN tenant ON tenant.iddevice = device.id;");
		projectionAfterEvo.put("device", "SELECT device.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.dev_name as name, device.dev_type as type FROM device INNER JOIN customer ON customer.iddevice = device.id INNER JOIN tenant ON tenant.iddevice = device.id ORDER BY device.id DESC, tenant_id DESC, customer_ID DESC;");
		projectionBeforeEvo.put("entity_view", "SELECT entity.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, entity.name as name, entity.type as type FROM device INNER JOIN customer ON customer.iddevice = device.id INNER JOIN tenant ON tenant.iddevice = device.id INNER JOIN entity ON (customer.identity = entity.id AND tenant.identity = entity.id);");
		projectionAfterEvo.put("entity_view", "SELECT entity.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.dev_type as dev_type, entity.name as name, entity.type as type FROM device INNER JOIN customer ON customer.iddevice = device.id INNER JOIN tenant ON tenant.iddevice = device.id INNER JOIN entity ON (customer.identity = entity.id AND tenant.identity = entity.id) ORDER BY entity.id DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "thingsboard2", projectionBeforeEvo);
	}
	@Test
	public void testThingsBoardV12NewColumnPK() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("device", "SELECT device.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.dev_name as name, device.dev_type as type FROM device INNER JOIN customer ON customer.cus_id = device.idcustomer INNER JOIN tenant ON tenant.ten_id = device.idtenant;");
		projectionAfterEvo.put("device", "SELECT device.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.dev_name as name, device.dev_type as type FROM device INNER JOIN customer ON customer.cus_id = device.idcustomer INNER JOIN tenant ON tenant.ten_id = device.idtenant ORDER BY device.id DESC, tenant_id DESC, customer_ID DESC;");
		projectionBeforeEvo.put("entity_view2_old", "SELECT entity.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, entity.name as name, entity.type as type FROM device INNER JOIN customer ON customer.cus_id = device.idcustomer INNER JOIN tenant ON tenant.ten_id = device.idtenant INNER JOIN entity ON (customer.identity = entity.id AND tenant.identity = entity.id);");
		projectionAfterEvo.put("entity_view2", "SELECT entity.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.dev_type as dev_type, entity.name as name, entity.type as type FROM device INNER JOIN customer ON customer.cus_id = device.idcustomer INNER JOIN tenant ON tenant.ten_id = device.idtenant INNER JOIN entity ON (customer.identity = entity.id AND tenant.identity = entity.id) ORDER BY id DESC, tenant_id DESC, customer_id DESC, dev_type DESC;");
		testConsistency(name.getMethodName(), projectionAfterEvo, "thingsboard", projectionBeforeEvo);
	}
	@Test
	public void testThingsBoardV13NewColumnKeyInTable() throws IOException {
		Map<String, String> projectionAfterEvo = new HashMap<String, String>();
		Map<String, String> projectionBeforeEvo = new HashMap<String, String>();
		projectionBeforeEvo.put("device", "SELECT device.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.dev_name as name, device.dev_type as type FROM device INNER JOIN customer ON customer.cus_id = device.idcustomer INNER JOIN tenant ON tenant.ten_id = device.idtenant;");
		projectionAfterEvo.put("device", "SELECT device.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.dev_name as name, device.dev_type as type FROM device INNER JOIN customer ON customer.cus_id = device.idcustomer INNER JOIN tenant ON tenant.ten_id = device.idtenant ORDER BY device.id DESC, tenant_id DESC, customer_ID DESC;");
		projectionBeforeEvo.put("entity_view_devpk", "SELECT entity.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.id as device_id, entity.name as name, entity.type as type FROM device INNER JOIN customer ON customer.cus_id = device.idcustomer INNER JOIN tenant ON tenant.ten_id = device.idtenant INNER JOIN entity ON (customer.identity = entity.id AND tenant.identity = entity.id);");
		projectionAfterEvo.put("entity_view_devpk", "SELECT entity.id as id, tenant.ten_id as tenant_id, customer.cus_id as customer_id, device.id as device_id, device.dev_type as dev_type, entity.name as name, entity.type as type FROM device INNER JOIN customer ON customer.cus_id = device.idcustomer INNER JOIN tenant ON tenant.ten_id = device.idtenant INNER JOIN entity ON (customer.identity = entity.id AND tenant.identity = entity.id) ORDER BY id DESC, tenant_id DESC, customer_id DESC, device_id DESC, dev_type DESC;");
	
		testConsistency(name.getMethodName(), projectionAfterEvo, "thingsboard", projectionBeforeEvo);
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
		resetDatabaseCassandraUnitTesting (testName);
		OracleCsv oc = new OracleCsv();
		Map<String, List<String>> tableColumnsMap = oc.namesTablesColumnsKeyspace(testName, connection, tableProjection); //Map of the names of tables and its columns
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
	 * Empties the database of data
	 */
	private void resetDatabaseCassandraUnitTesting(String keyspace) {
		String cql = "SELECT table_name FROM system_schema.tables WHERE keyspace_name = '"+keyspace+"';";
		com.datastax.oss.driver.api.core.cql.ResultSet results = connection.executeStatement(cql);
		for (com.datastax.oss.driver.api.core.cql.Row row : results) {
			String nameTable = row.getString(0);
			String delete = "";
			delete = "TRUNCATE \""+keyspace+"\"."+nameTable+";";
			connection.executeStatement(delete);	
		}		
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
