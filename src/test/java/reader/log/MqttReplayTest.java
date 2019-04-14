package reader.log;

import static java.util.Arrays.asList;
import static org.junit.rules.Timeout.seconds;
import static reader.junit.rules.MqttRule.withLocalhostAndRandomPort;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import reader.junit.rules.Broker;
import reader.junit.rules.Message;
import reader.junit.rules.MqttRule;
import sft.event.BallPosition;
import sft.event.Event;
import sft.event.TeamScored;

public class MqttReplayTest {

	@Rule
	public Timeout timeout = seconds(30);

	@Rule
	public MqttRule mqttRule = withLocalhostAndRandomPort();

	@Test
	public void canReplayLog() throws MqttSecurityException, MqttException, InterruptedException {
		replay(asList(new BallPosition(1, 0.2, 0.3), new TeamScored(4, 5, 6)));
		mqttRule.client().assertReceived(new Message("ball/position", "{\"x\": 0.2, \"y\": 0.3}"),
				new Message("game/score/team/5", "6"));
	}

	private void replay(List<Event> events) throws MqttSecurityException, MqttException {
		Broker broker = mqttRule.broker();
		MqttClient client = newMqttClient(broker.host(), broker.port(), "logreplay-" + System.currentTimeMillis());
		for (Event event : events) {
			// TODO check timestamps and sleep
			if (event instanceof BallPosition) {
				BallPosition position = (BallPosition) event;
				publish(client, "ball/position", "{\"x\": " + position.x + ", \"y\": " + position.y + "}");
			} else if (event instanceof TeamScored) {
				TeamScored teamScored = (TeamScored) event;
				publish(client, "game/score/team/" + teamScored.team, String.valueOf(teamScored.score));
			}
		}
	}

	private void publish(MqttClient client, String topic, String payload)
			throws MqttException, MqttPersistenceException {
		client.publish(topic, payload.getBytes(), 0, false);
	}

	private MqttClient newMqttClient(final String host, final int port, final String id)
			throws MqttException, MqttSecurityException {
		final MqttClient client = new MqttClient("tcp://" + host + ":" + port, id, new MemoryPersistence());
		client.connect(mqttConnectOptions());
		return client;
	}

	private MqttConnectOptions mqttConnectOptions() {
		final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
		mqttConnectOptions.setAutomaticReconnect(true);
		return mqttConnectOptions;
	}

}
