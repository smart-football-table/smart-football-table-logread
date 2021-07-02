package reader.junit.rules;

import static io.moquette.BrokerConstants.HOST_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PORT_PROPERTY_NAME;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;

public class Broker implements Closeable {

	private final String host;
	private final int port;
	private Server server;

	public Broker(String host, int port) throws IOException {
		this.port = port;
		this.host = host;
		this.server = newMqttServer(host, port);
	}

	private Server newMqttServer(String host, int port) throws IOException {
		Server server = new Server();
		server.startServer(config(host, port));
		return server;
	}

	private IConfig config(String host, int port) {
		Properties properties = new Properties();
		properties.setProperty(HOST_PROPERTY_NAME, host);
		properties.setProperty(PORT_PROPERTY_NAME, String.valueOf(port));
		return new MemoryConfig(properties);
	}

	public String host() {
		return host;
	}

	public int port() {
		return port;
	}

	@Override
	public void close() {
		server.stopServer();
	}

}