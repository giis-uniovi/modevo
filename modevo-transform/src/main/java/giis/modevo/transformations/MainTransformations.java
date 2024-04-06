package giis.modevo.transformations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IExtractor;
import org.eclipse.m2m.atl.core.IInjector;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.ModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFExtractor;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.eclipse.uml2.uml.resource.UMLResource;

import giis.modevo.model.ModelObjects;
import giis.modevo.model.datamigration.DataMigration;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schemaevolution.SchemaEvolution;
import lombok.extern.slf4j.Slf4j;

/**
 * Main class of MoDEvo where the ATL transformations are called and the models are loaded.
 * The process starts with the method createDataMigrationModel which receives as inputs the paths to the models for the Conceptual Model, Schema and the Schema Change.
 * This method calls the methods required to start the ATL transformation process to obtain the Data Migration model which specifies the migrations required to maintain the data
 * integrity for the given Schema Change. In the future, the process will continue by loading the models 
 */
@Slf4j
public class MainTransformations {
	
	private Properties propertiesATL; //Contains the properties required to load the metamodels and the ATL file that contains the transformations
	//Objects used to load the models into ATL
	protected IModel scModel;
	protected IModel cmModel;
	protected IModel evModel;
	protected IModel dataMigrationModel;
	private static final String XML_EXCEPTION = "Problem with document XML. Read";
	
	public MainTransformations() {
		propertiesATL = new Properties();
		try (InputStream stream = getFileURL("MoDEvo.properties").openStream()){
			propertiesATL.load(stream);
		} catch (IOException e) {
			throw new DocumentReadException ("Problem reading MoDEvo.properties file. Read "+e);
		}
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, new UMLResourceFactoryImpl());
	}
	
	/**
	 * Initiates the ATL process to execute the transformation rules required to produce the Data Migration
	 * output model from the input models (Conceptual Model, Schema and Schema evolution) 
	 */
	public ModelObjects createDataMigrationModelAndScript(String schemaModelPath, String conceptualModelPath, String schemaEvolutionModelPath, String dataMigrationModelPath){
		this.loadModels(schemaModelPath, conceptualModelPath, schemaEvolutionModelPath);
		this.launchTransformations(new NullProgressMonitor());
		this.saveModels(dataMigrationModelPath);
		Schema sc = new Schema ();
		SchemaEvolution se = new SchemaEvolution();
		DataMigration dm = new DataMigration ();
		sc = sc.loadSchemaIntoApp(schemaModelPath);
		se = se.readSchemaEvolutionModel(schemaEvolutionModelPath);
		dm = dm.readDataMigrationModel(dataMigrationModelPath, se, sc);
		return new ModelObjects(sc, se, dm);
	}
	/**
	 * Load the input and input/output metamodels into IModel variables of the class.
	 */
	private void loadModels(String lmModelPath, String cmModelPath, String chModelPath) {
	 	this.scModel =  injectModel ( "Schema", lmModelPath);
	 	this.evModel = injectModel ("SchemaEvolution", chModelPath);
	 	this.cmModel = injectModel ( "ConceptualModel", cmModelPath);
	 	this.dataMigrationModel = injectModel ( "DataMigration", null);
	}
	
	private IModel injectModel(String nameMetamodel, String pathModel) {
		IReferenceModel metamodel = null;
		ModelFactory factory = new EMFModelFactory();
		IInjector injector = new EMFInjector();
		try {
			metamodel = factory.newReferenceModel();
			injector.inject(metamodel, getMetamodelUri(nameMetamodel));
			IModel factoryNewModel = factory.newModel(metamodel);
			if (pathModel != null) {
				injector.inject(factoryNewModel, pathModel);
			}
			return factoryNewModel;
		} 
		catch (ATLCoreException e) {
			throw new ATLException (XML_EXCEPTION + e);
		}
	}

	public void saveModels(String outModelPath) {
		IExtractor extractor = new EMFExtractor();
		try {
			extractor.extract(dataMigrationModel, outModelPath);
		} catch (ATLCoreException e) {
			throw new ATLException ("Problem with the ATL file" + outModelPath +"Read the message" + e);
		}
	}

	/**
	 * Class that triggers the ATL transformations using the information loaded into the 
	 * IModels in previous methods
	 */
	public void launchTransformations(IProgressMonitor monitor)  {
		ILauncher launcher = new EMFVMLauncher();
		HashMap<String,Object> launcherOptions = new HashMap<>();
		launcher.initialize(launcherOptions);
		launcher.addInModel(scModel, "LM", "Schema");
		launcher.addInModel(cmModel, "CM", "ConceptualModel");
		launcher.addInModel(evModel, "CH", "SchemaEvolution");
		launcher.addOutModel(dataMigrationModel, "OUT", "DataMigration");
		try {
			
			/*
			 * Note about order of libraries: Due to the way of how libraries are handled by the launcher, 
			 * the helpers and lazy migrations of a library can only be accessed from libraries that
			 * are in the HashMap that internally stores the libraries in a later position. 
			 * The order of the HashMap is determined by the name given in the method "addLibrary". 
			 * The order is established by comparing the string lexicographically if there is only
			 * one character (Unicode code).
			 */
			launcher.addLibrary("a", getLibraryAsStream("ConceptualModelAttribute"));
			launcher.addLibrary("b", getLibraryAsStream("ConceptualModelEntity"));
			launcher.addLibrary("c", getLibraryAsStream("SchemaEvolutionColumn"));
			launcher.addLibrary("d", getLibraryAsStream("SchemaEvolutionTable"));
			launcher.addLibrary("e", getLibraryAsStream("SchemaColumn"));
			launcher.addLibrary("f", getLibraryAsStream("SchemaTable"));
			launcher.addLibrary("g", getLibraryAsStream("NewColumn"));
			launcher.addLibrary("h", getLibraryAsStream("NewTable"));
			launcher.addLibrary("i", getLibraryAsStream("JoinTable"));
			launcher.addLibrary("j", getLibraryAsStream("SplitColumn"));
			launcher.addLibrary("k", getLibraryAsStream("SplitTable"));
			launcher.addLibrary("l", getLibraryAsStream("CopyTable"));
			launcher.addLibrary("m", getLibraryAsStream("JoinColumn"));
			launcher.addLibrary("n", getLibraryAsStream("RemovePK"));

		} catch (IOException e) {
			throw new ATLException ("A module of ATL could not be found. See detail: "+e);
		}
		launcher.launch("run", monitor, launcherOptions, (Object[]) getModulesList());
		log.info ("Transformations executed");
	}
	
	protected InputStream[] getModulesList() {
		InputStream[] modules = null;
		String modulesList = propertiesATL.getProperty("MoDEvo.modules");
		if (modulesList != null) {
			String[] moduleNames = modulesList.split(",");
			modules = new InputStream[moduleNames.length];
			for (int i = 0; i < moduleNames.length; i++) {
				String asmModulePath = new Path(moduleNames[i].trim()).removeFileExtension().addFileExtension("asm").toString();
				try {
					modules[i] = getFileURL(asmModulePath).openStream();
				} catch (IOException e) {
					throw new ATLException ("There was some error reading the ATL compiled file 'asm'. For specific information read "+e);
				}
			}
		}
		return modules;
	}
	
	/**
	 * Returns the URI of the given metamodel, parameterized from the property file.
	 */
	protected String getMetamodelUri(String metamodelName) {
		return propertiesATL.getProperty("MoDEvo.metamodels." + metamodelName);
	}
	
	/**
	 * Returns the file name of the given library, parameterized from the property file.
	 */
	protected InputStream getLibraryAsStream(String libraryName) throws IOException {
		return getFileURL(propertiesATL.getProperty("MoDEvo.libraries." + libraryName)).openStream();
	}
	
	/**
	 * Returns the options map, parameterized from the property file.
	 * 
	 */
	protected Map<String, Object> getOptions() {
		Map<String, Object> options = new HashMap<>();
		for (Entry<Object, Object> entry : propertiesATL.entrySet()) {
			if (entry.getKey().toString().startsWith("MoDEvo.options.")) {
				options.put(entry.getKey().toString().replaceFirst("MoDEvo.options.", ""), 
				entry.getValue().toString());
			}
		}
		return options;
	}
	
	protected URL getFileURL(String fileName) {
		String pathATL = "src/main/java/giis/modevo/transformations/atl/";
		try {
			return new File(pathATL+fileName).toURI().toURL();
		} catch (MalformedURLException e) {
			throw new DocumentReadException ("The URL contains errors. See: " + e);
		}
	}
	
}