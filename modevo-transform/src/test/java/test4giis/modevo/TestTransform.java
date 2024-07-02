package test4giis.modevo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public class TestTransform {
	
	private static final String INPUT_MODELS_FOLDER_TEMP = "target/input-models/";
	private static final String SCHEMA = "schema.xmi";
	private static final String CM = "CM.xmi";
	private static final String SCHEMA_CHANGE = "schemaChange.xmi";
	 
	@Rule public TestName name = new TestName();
	
	/**
	 * Cleans the files used in a test case after its execution.
	 */
	@Before
	public void removeTempModelFolder() throws IOException  {
		Path schemaTempPath = Paths.get(INPUT_MODELS_FOLDER_TEMP+SCHEMA);
		Path cmTempPath = Paths.get(INPUT_MODELS_FOLDER_TEMP+CM);
		Path schemaChangeTempPath = Paths.get(INPUT_MODELS_FOLDER_TEMP+SCHEMA_CHANGE);
		Path tempFolder = Paths.get(INPUT_MODELS_FOLDER_TEMP);
		Files.deleteIfExists (schemaTempPath);
		Files.deleteIfExists (cmTempPath);
		Files.deleteIfExists (schemaChangeTempPath);
		Files.deleteIfExists(tempFolder);
	}
	
	@Test
	public void testThingsBoardV11NewColumnNonKey() {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testThingsBoardV12NewColumnPK() {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testThingsBoardV13NewColumnKeyInTable()  {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testMindsV3SplitColumn () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testMindsV9NewTableMigrationFromOneTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testMindsV9NewColumnsPreviousVersion () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testMindsV10NewTableMigrationFromOneTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testMindsV25NewTableMigrationFromOneTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testMindsV27NewTableMigrationFromOneTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testWireV2NewTableMigrationFromOneTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testWireV8NewTableMigrationFromPreviousVersion () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testWireV91NewTableMigrationFromOneTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testWireV92NewTableMigrationFromSeveralTables () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV1NewColumn () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV2TwoNewColumnSourceTwoTables () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV3TwoNewColumnsUsingNMRelationship () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV4JoinTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV5JoinTableKeyEntityInTargetTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV6CopyTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV7SplitTable () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV8JoinColumn () {
		new TestUtils().testModels(name.getMethodName());	
	}
	@Test
	public void testCustomV9RemovePK () {
		new TestUtils().testModels(name.getMethodName());	
	}
	
}
