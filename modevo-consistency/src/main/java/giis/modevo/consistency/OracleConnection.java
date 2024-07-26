package giis.modevo.consistency;

import java.io.BufferedReader;
import java.io.FileInputStream;
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
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
public class OracleConnection {
	private static final String PROBLEM_SQL = "Problem executing SQL statement";
	private static final String CLOSING_SQL = "Error closing SQL connection";
	private static final String PROPERTIES = "src/test/resources/sqlconnection.properties";
	private static final String PROBLEM_FILE = "Problem processing SQL file";
	private Connection connect;

	public Connection getConnect() {
		return connect;
	}

	public void setConnect(Connection connect) {
		this.connect = connect;
	}

	public Connection connect(String nameDB) {
		Properties properties = new ConnectionData().loadProperties(PROPERTIES);
		String ip = properties.getProperty("ipsql").trim();
		String port = properties.getProperty("portsql").trim();
		String username = properties.getProperty("usersql").trim();
		String password = properties.getProperty("passwordsql").trim();
		try {
			StringBuilder connection = new StringBuilder("jdbc:mysql://").append(ip).append(":").append(port)
					.append("/").append(nameDB).append("?").append("user=").append(username).append("&password=")
					.append(password)
					.append("&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
			connect = DriverManager.getConnection(connection.toString());
		} catch (SQLException e) {
			throw new ScriptException(PROBLEM_SQL + e);
		}
		return connect;
	}

	public void executeFileSQL(String path) {
		String scriptFilePath = path;
		try (BufferedReader reader = new BufferedReader(new FileReader(scriptFilePath));
				Statement statement = connect.createStatement();) {
			// create statement object
			// initialize file reader
			String line = null;
			// read script line by line
			while ((line = reader.readLine()) != null) {
				// execute query
				statement.addBatch(line);
			}
			statement.executeBatch();
			closeConnection();
		} catch (SQLException e) {
			closeConnection();
			throw new ScriptException(PROBLEM_SQL);
		} catch (IOException e) {
			closeConnection();
			throw new DocumentException(PROBLEM_FILE);
		}
	}

	public void closeConnection() {
		try {
			connect.close();
		} catch (SQLException e) {
			throw new ScriptException(CLOSING_SQL);
		}
	}
	
}
