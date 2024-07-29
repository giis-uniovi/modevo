package giis.modevo.migration.script.execution;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import giis.modevo.migration.script.ScriptException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
@Getter @Setter @Slf4j
public class ConnectionData {

	private String user;
	private String password;
	private String ip;
	private int port;
	private String datacenter;
	private String rack;
	public ConnectionData() {
	}
	public ConnectionData(String propertiesPath) {
		Properties properties = loadProperties(propertiesPath);
		ip=properties.getProperty("ip").trim();
		String portExtracted = properties.getProperty("port").trim();
		user = properties.getProperty("user").trim();
		password = properties.getProperty("password").trim();
		datacenter = properties.getProperty("datacenter").trim();
		rack = properties.getProperty("rack").trim();
		try {
			port = Integer.valueOf(portExtracted);
	    } catch(NumberFormatException e) { //If no port is assigned, the default 9042 is assigned
	    	port = 9042;
			log.info("Using default port %d", port);
	    }
	}
	public Properties loadProperties(String propertiesPath) {
		Properties properties = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(propertiesPath);
			properties.load(in);
			in.close();
		} catch (IOException e) {
			throw new ScriptException ("Error opening properties file");
		}
		return properties;
	}
}
