package test4giis.modevo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import giis.modevo.model.ModelObjects;
import giis.modevo.transformations.MainTransformations;

public class TestUtils {
	private static final String inputModelsFolderTemp = "target/input-models/";
	private static final String inputPath = "../modevo-transform/dat/inp/";
	private static final String outputPath = "../modevo-transform/dat/out/";
	private static final String bmkPath = "../modevo-transform/dat/bmk/";
	private static final String schema = "schema.xmi";
	private static final String cm = "CM.xmi";
	private static final String schemaChange = "schemaChange.xmi";
	private static final String output=  "dataMigration.xmi";
	
	protected void testModels (String nameTest) {
		String nameTestDash = nameTest +"-";//Added dash to separate nameTest and type of file in the name of the file
		String outputTest = outputPath+nameTestDash+output;
		executeTransformationsAndCompareOutput (nameTest);
		AssertEqualFiles (bmkPath+nameTestDash+output, outputTest);	//Introduces the name of the test to the path	
	}
	
	/**
	 * Prepares the inputs for the model transformation and checks if the output is the expected one.
	 */
	public ModelObjects executeTransformationsAndCompareOutput(String nameTest) {
		String nameTestDash = nameTest +"-";//Added dash to separate nameTest and type of file in the name of the file
		String outputTest = outputPath+nameTestDash+output;
		Pattern pattern = Pattern.compile("(?<=test).*(?=V[0-9])");
		Matcher matcher = pattern.matcher(nameTest);
		matcher.find();
		String caseStudy = matcher.group();
		copyInputs (inputPath+nameTestDash+schema, inputPath+caseStudy+cm, inputPath+nameTestDash+schemaChange);
		MainTransformations main = new MainTransformations();
		ModelObjects models = main.createDataMigrationModelAndScript(inputModelsFolderTemp+schema, inputModelsFolderTemp+cm, inputModelsFolderTemp+schemaChange, outputTest);
		return models;	
	}

	/**
	 * Method that copies the input models used in a test case in a temporary folder. This is done because for yet unknown reasons the input models are sometimes modified after
	 * executing a transformation. Until this gets solved the test cases will use this method to always have functional input models. This method shall be deleted when the aforementioned
	 * issue is solved. The parameters are the paths to the three input models (conceptual model, schema and schema evolution)
	 */
	private void copyInputs(String schemaInp, String cmInp, String schemaEvolutionInp)  {
		Path schemaInpPath = Paths.get(schemaInp);
		Path cmInpPath = Paths.get(cmInp);
		Path schemaChangeInpPath = Paths.get(schemaEvolutionInp);
		Path tempFolder = Paths.get(inputModelsFolderTemp);
		Path schemaTempPath = Paths.get(inputModelsFolderTemp+schema);
		Path cmTempPath = Paths.get(inputModelsFolderTemp+cm);
		Path schemaChangeTempPath = Paths.get(inputModelsFolderTemp+schemaChange);
		try {
			Files.createDirectories(tempFolder); //Only creates it if it does not exists. Should this be done in a BeforeClass?
			Files.copy(schemaInpPath, schemaTempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			Files.copy(cmInpPath, cmTempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			Files.copy(schemaChangeInpPath, schemaChangeTempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}
	
	/**
	 * Compares the content of the files whose paths are specified in the method's parameter. 
	 */
	public void AssertEqualFiles (String outputBMK, String outputTest) {
		Path outputTestPath = Paths.get(outputTest);
		Path outputBMKPath = Paths.get(outputBMK);
		try {
			 String outputText = new String(Files.readAllBytes(outputTestPath), StandardCharsets.UTF_8);
			 String bmkText = new String(Files.readAllBytes(outputBMKPath), StandardCharsets.UTF_8);
			 assertEquals (bmkText, outputText);
		} catch (IOException e) {
	    	throw new RuntimeException (e);
		}
	}
}
