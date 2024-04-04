package giis.modevo.transformations;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;


/**
 * Class where methods that can be used alongside the application are placed.
 */
public class UtilMoDEvo {

	/**
	 * Loads the properties of a given file in a Properties object
	 */
	public Properties loadProperties(String path) {
		Properties properties = new Properties();
		try (FileInputStream in = new FileInputStream(path)){
			properties.load(in);
		} catch (IOException e) {
			throw new DocumentReadException ("Error opening properties file");
		}
		return properties;
	}
	
}