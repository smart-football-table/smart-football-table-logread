package reader.log;

import static java.lang.System.nanoTime;
import static sft.reader.log.parser.LogEntryParsers.tryParse;

import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import sft.event.Event;

public class MqttClientAdapter {

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
		client.subscribe(topic, (t, m) -> tryParse(nanoTime(), t, new String(m.getPayload())).ifPresent(consumer));
	}

}
