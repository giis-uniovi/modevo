package test4giis.modevo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public class TestSchemaChanges {
	
	private static final String inputModelsFolderTemp = "target/input-models/";
	private static final String schema = "schema.xmi";
	private static final String cm = "CM.xmi";
	private static final String schemaChange = "schemaChange.xmi";
	 
	@Rule public TestName name = new TestName();
	
	/**
	 * Cleans the files used in a test case after its execution.
	 */
	@Before
	public void removeTempModelFolder() throws IOException  {
		Path schemaTempPath = Paths.get(inputModelsFolderTemp+schema);
		Path cmTempPath = Paths.get(inputModelsFolderTemp+cm);
		Path schemaChangeTempPath = Paths.get(inputModelsFolderTemp+schemaChange);
		Path tempFolder = Paths.get(inputModelsFolderTemp);
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
