package giis.modevo.migration.script.execution;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CassandraConnection {

	private Cluster cluster;
	private Session session;
	public CassandraConnection(String propertiesPath) {
		connect(propertiesPath);
	}

	/**
	 * Establish the connection to a Cassandra database
	 */
	public void connect(String propertiesPath) {
		ConnectionData dc = new ConnectionData(propertiesPath);
		String ip=dc.getIp();
		int port = dc.getPort();
		String username = dc.getUser();
		String password = dc.getPassword();
		cluster = Cluster.builder().withPort(port).addContactPoint(ip).withCredentials(username, password).build();
		session=cluster.newSession();
		log.info("Connection with database succesful");
	}

	public void close() {
		cluster.close();
		log.info("Connection with database closed");
	}
	public ResultSet executeStatement(String strLine) {
    	return this.session.execute(strLine);		
	}

}
