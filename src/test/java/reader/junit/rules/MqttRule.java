package reader.junit.rules;

import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.stream.IntStream.range;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

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
				assertThat(poll(getMessages(), expected.length), is(expected));
			}

			private Message[] poll(BlockingQueue<Message> queue, int size) {
				return range(0, size).mapToObj(i -> poll(queue)).toArray(Message[]::new);
			}

			private Message poll(BlockingQueue<Message> queue) {
				try {
					Message poll;
					do {
						poll = queue.poll(MAX_VALUE, DAYS);
					} while (poll == null);
					return poll;
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
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
