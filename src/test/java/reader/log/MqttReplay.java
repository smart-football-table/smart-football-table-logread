package reader.log;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import sft.event.BallPosition;
import sft.event.Event;
import sft.event.TeamScored;
import sft.event.TimestampedEvent;

public class MqttReplay implements Closeable {

	private final MqttClient client;

	public MqttReplay(String host, int port) throws MqttSecurityException, MqttException {
		this.client = newMqttClient(host, port, "logreplay-" + System.currentTimeMillis());
	}

	public void replay(List<TimestampedEvent> events)
			throws MqttSecurityException, MqttException, InterruptedException {
		TimestampedEvent prev = null;
		for (TimestampedEvent timestampedEvent : events) {
			Event event = timestampedEvent.getEvent();
			if (prev != null) {
				sleepNanos(timestampedEvent.nanos - prev.nanos);
			}

			if (event instanceof BallPosition) {
				BallPosition position = (BallPosition) event;
				publish(client, "ball/position", "{\"x\": " + position.x + ", \"y\": " + position.y + "}");
			} else if (event instanceof TeamScored) {
				TeamScored teamScored = (TeamScored) event;
				publish(client, "game/score/team/" + teamScored.team, String.valueOf(teamScored.score));
			}
			prev = timestampedEvent;
		}
	}

	protected void sleepNanos(long nanos) throws InterruptedException {
		TimeUnit.NANOSECONDS.sleep(nanos);
	}

	private MqttClient newMqttClient(final String host, final int port, final String id)
			throws MqttException, MqttSecurityException {
		MqttClient client = new MqttClient("tcp://" + host + ":" + port, id, new MemoryPersistence());
		client.connect(mqttConnectOptions());
		return client;
	}

	private MqttConnectOptions mqttConnectOptions() {
		MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setAutomaticReconnect(true);
		return mqttConnectOptions;
	}

	private void publish(MqttClient client, String topic, String payload)
			throws MqttException, MqttPersistenceException {
		client.publish(topic, payload.getBytes(), 0, false);
	}

	@Override
	public void close() throws IOException {
		try {
			client.disconnect();
			client.close();
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}

}
