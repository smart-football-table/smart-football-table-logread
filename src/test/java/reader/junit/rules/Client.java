package reader.junit.rules;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public abstract class Client implements Closeable {

	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final MqttClient client;

	public Client(Broker broker) throws MqttSecurityException, MqttException {
		client = newMqttClient(broker.host(), broker.port(), "randomClientId-" + System.currentTimeMillis());
		subsribe("#", messages::offer);
	}

	private MqttClient newMqttClient(String host, int port, String id) throws MqttException, MqttSecurityException {
		MqttClient client = new MqttClient("tcp://" + host + ":" + port, id, new MemoryPersistence());
		client.connect(mqttConnectOptions());
		return client;
	}

	private MqttConnectOptions mqttConnectOptions() {
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setAutomaticReconnect(true);
		return mqttConnectOptions;
	}

	private void subsribe(String topic, Consumer<Message> consumer) {
		try {
			client.subscribe(topic, (t, m) -> consumer.accept(new Message(t, new String(m.getPayload()))));
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (client.isConnected()) {
				client.disconnect();
			}
			client.close();
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}

	public BlockingQueue<Message> getMessages() {
		return messages;
	}

	public abstract void assertReceived(Message... messages);

}