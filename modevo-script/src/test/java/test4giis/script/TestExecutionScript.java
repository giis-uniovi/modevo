package test4giis.script;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import giis.modevo.migration.script.MainScript;
import giis.modevo.migration.script.execution.CassandraConnection;
import giis.modevo.model.ModelObjects;

import test4giis.modevo.TestUtils;


public class TestExecutionScript {
	private static final String PROPERTIES = "src/test/resources/dbconnection.properties";
	private static CassandraConnection connection;
	private static final String BMK_PATH = "dat/bmk/";
	private static final String OUTPUT_PATH = "dat/out/";
	private static final String OUTPUT_SCRIPT=  "script.cql";

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
	@AfterClass
	public static void tearDown(){
		connection.close();
	}
	@Before
	public void setUpTestCase() {
		String cql = "dat/inp/"+name.getMethodName()+"-initDB.cql";
		executeCQLFile(cql);
	}
	@Test
	public void testThingsBoardV11NewColumnNonKey() {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testThingsBoardV12NewColumnPK() {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testThingsBoardV13NewColumnKeyInTable()  {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testMindsV3SplitColumn () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testMindsV9NewTableMigrationFromOneTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testMindsV9NewColumnsPreviousVersion () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testMindsV10NewTableMigrationFromOneTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testMindsV25NewTableMigrationFromOneTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testMindsV27NewTableMigrationFromOneTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testWireV2NewTableMigrationFromOneTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testWireV8NewTableMigrationFromPreviousVersion () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testWireV91NewTableMigrationFromOneTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testWireV92NewTableMigrationFromSeveralTables () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV1NewColumn () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV2TwoNewColumnSourceTwoTables () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV3TwoNewColumnsUsingNMRelationship () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV4JoinTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV5JoinTableKeyEntityInTargetTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV6CopyTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV7SplitTable () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV8JoinColumn () {
		testScript(name.getMethodName(), connection);	
	}
	@Test
	public void testCustomV9RemovePK () {
		testScript(name.getMethodName(), connection);	
	}
	public static void executeCQLFile(String path){
	     try{
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
	            if (!(strLine.contains("--") || strLine.isBlank())){
	            	connection.executeStatement(strLine);
	            	connection.executeStatement (strLine);
	            }
            }
            //Close the input stream
            fstream.close();
	     }catch (Exception e){//Catch exception if any
	            connection.close();
	  			throw new RuntimeException ("Error executing CQL statement: "+e.getMessage());
	     } 
	}
	private void testScript (String nameTest,  CassandraConnection c) {
		ModelObjects m = new TestUtils().executeTransformationsAndCompareOutput (nameTest);
		createExecuteScript (nameTest, m, c);
	}
	private void createExecuteScript (String nameTest, ModelObjects m, CassandraConnection c) {
		String nameTestDash = nameTest +"-";//Added dash to separate nameTest and type of file in the name of the file
		String script = new MainScript().createScriptAndText(m, c, nameTest);
		if (script != null) {//until all scenarios have been covered
			String fullPathScript = outputScriptFile (script, nameTest);
			new TestUtils().AssertEqualFiles (BMK_PATH+nameTestDash+OUTPUT_SCRIPT, fullPathScript);	//Introduces the name of the test to the path	
		}
	}
	private String outputScriptFile (String script, String nameTest) {
		String path = OUTPUT_PATH+nameTest+"-script.cql";
		Path scriptPath = Paths.get(path);
		try {
			Files.write(scriptPath, script.getBytes());
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
		return path;
	}	
}
