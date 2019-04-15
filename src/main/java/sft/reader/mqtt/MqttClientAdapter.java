package sft.reader.mqtt;

import static sft.reader.log.parser.LogEntryParsers.tryParse;

import java.io.Closeable;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import sft.event.Event;

public class MqttClientAdapter implements Closeable {

	private final MqttClient client;

	public MqttClientAdapter(String host, int port, Consumer<Event> consumer) throws MqttException {
		this.client = new MqttClient("tcp://" + host + ":" + port, "mqttadapter-" + System.currentTimeMillis(),
				new MemoryPersistence());
		this.client.connect(mqttConnectOptions());
		subscribe(this.client, "#", consumer);
	}

	private MqttConnectOptions mqttConnectOptions() {
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setAutomaticReconnect(true);
		return mqttConnectOptions;
	}

	private void subscribe(MqttClient client, String topic, Consumer<Event> consumer) throws MqttException {
		client.subscribe(topic, (t, m) -> tryParse(t, new String(m.getPayload())).ifPresent(consumer));
	}

	@Override
	public void close() {
		try {
			client.disconnect();
			client.close();
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

}
