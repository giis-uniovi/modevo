package giis.modevo.consistency;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import giis.modevo.migration.script.ScriptException;
import giis.modevo.migration.script.execution.ConnectionData;
import giis.modevo.model.DocumentException;

public class OracleConnection {

	private static final String PROPERTIES = "src/test/resources/sqlconnection.properties";
	private Connection connect;

	public Connection getConnect() {
		return connect;
	}

	public void setConnect(Connection connect) {
		this.connect = connect;
	}

	public Connection connect(String dbName) {
		Properties properties = new ConnectionData().loadProperties(PROPERTIES);
		String ip = properties.getProperty("ipsql").trim();
		String port = properties.getProperty("portsql").trim();
		String username = properties.getProperty("usersql").trim();
		String password = properties.getProperty("passwordsql").trim();
		StringBuilder connection = new StringBuilder("jdbc:mysql://").append(ip).append(":").append(port)
				.append("/").append(dbName).append("?").append("user=").append(username).append("&password=")
				.append(password)
				.append("&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
		try {
			connect = DriverManager.getConnection(connection.toString());
		} catch (SQLException e) {
			throw new ScriptException(e);
		}
		return connect;
	}

	public void executeFileSql(String path) {
		String scriptFilePath = path;
		try (BufferedReader reader = new BufferedReader(new FileReader(scriptFilePath));
				Statement statement = connect.createStatement();) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				statement.addBatch(line);
			}
			statement.executeBatch();
		} catch (SQLException e) {
			throw new ScriptException(e);
		} catch (IOException e) {
			throw new DocumentException(e);
		} finally {
			closeConnection();
		}
	}

	public void closeConnection() {
		try {
			connect.close();
		} catch (SQLException e) {
			throw new ScriptException(e);
		}
	}
	
}
