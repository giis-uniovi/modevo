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
	private static final String PROPERTIES = "modevo.properties";
	private static CassandraConnection connection;
	private static final String bmkPath = "dat/bmk/";
	private static final String outputPath = "dat/out/";
	private static final String outputScript=  "script.cql";

	@Rule public TestName name = new TestName();
	@BeforeClass
	public static void setUp(){ //cleans the entire DB
		connection = new CassandraConnection(PROPERTIES);
		try {
			Files.createDirectories(Paths.get("/dat/out"));
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
	public void testCustomV1NewColumn () {
		testScript(name.getMethodName(), connection);	
	}
	private static void executeCQLFile(String path){
	     try{
	            // Open the file that is the first 
	            // command line parameter
	            FileInputStream fstream = new FileInputStream(path);
	            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	            String strLine;
	            //Read File Line By Line
	            while ((strLine = br.readLine()) != null)   {
		            if (!(strLine.contains("--") || strLine.isBlank())){
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
			new TestUtils().AssertEqualFiles (bmkPath+nameTestDash+outputScript, fullPathScript);	//Introduces the name of the test to the path	
		}
	}
	private String outputScriptFile (String script, String nameTest) {
		String path = outputPath+nameTest+"-script.cql";
		Path scriptPath = Paths.get(path);
		try {
			Files.write(scriptPath, script.getBytes());
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
		return path;
	}	
}
