package reader.junit.rules;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.rules.ExternalResource;

public class MqttRule extends ExternalResource {

	private final String host;
	private final int port;

	private Broker broker;
	private Client client;

	public static MqttRule withLocalhostAndRandomPort() {
		try {
			return withHostAndPort("localhost", NetUtils.randomPort());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static MqttRule withHostAndPort(String host, int port) {
		return new MqttRule(host, port);
	}

	private MqttRule(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	protected void before() throws Throwable {
		this.broker = new Broker(host, port);
		this.client = new Client(broker) {
			@Override
			public void assertReceived(Message... expected) {
				await().untilAsserted(() -> {
					assertThat(new ArrayList<>(getMessages()), is(Arrays.asList(expected)));
				});
			}
		};
		super.before();
	}

	@Override
	protected void after() {
		super.after();
		close(client);
		close(broker);
	}

	private void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Broker broker() {
		return broker;
	}

	public Client client() {
		return client;
	}

}
