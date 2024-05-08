package giis.modevo.migration.script.execution;

import java.net.InetSocketAddress;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.ResultSet;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CassandraConnection {

	private CqlSessionBuilder builder;
	private CqlSession session;
	
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
		builder = CqlSession.builder();
		builder.addContactPoint(new InetSocketAddress(ip, port));
		builder.withLocalDatacenter(dc.getDatacenter());
		session=builder.withAuthCredentials(username, password).build();
		log.info("Connection with database succesful");
	}

	public void close() {
		session.close();
		log.info("Connection with database closed");
	}
	public ResultSet executeStatement(String strLine) {
    	return this.session.execute(strLine);		
	}

}
