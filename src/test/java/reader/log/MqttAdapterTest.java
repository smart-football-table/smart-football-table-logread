package reader.log;

import static java.lang.System.nanoTime;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.Timeout.seconds;
import static reader.junit.rules.MqttRule.withLocalhostAndRandomPort;
import static sft.reader.log.parser.LogEntryParsers.tryParse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import reader.junit.rules.MqttRule;
import sft.event.BallPosition;
import sft.event.Event;
import sft.event.TeamScored;

public class MqttAdapterTest {

	@Rule
	public Timeout timeout = seconds(30);

	@Rule
	public MqttRule mqttRule = withLocalhostAndRandomPort();

	@Test
	public void doesSubsribeAndReceive() throws Exception {
		MqttClient client = newMqttClient(mqttRule.broker().host(), mqttRule.broker().port(),
				"mqttadapter-" + System.currentTimeMillis());

		List<Event> events = new ArrayList<>();
		subscribe(client, events::add);

		double x = 0.12345;
		double y = 0.6789;
		int team = 1;
		int score = 2;
		mqttRule.client().publish("ball/position", "{\"x\":" + x + ",\"y\":" + y + "}");
		mqttRule.client().publish("game/score/team/" + team, String.valueOf(score));

		TimeUnit.MILLISECONDS.sleep(100);

		assertThat(events.size(), is(2));

		BallPosition ballPosition = (BallPosition) events.get(0);
		assertThat(ballPosition.x, is(x));
		assertThat(ballPosition.y, is(y));

		TeamScored teamScored = (TeamScored) events.get(1);
		assertThat(teamScored.team, is(team));
		assertThat(teamScored.score, is(score));
	}

	private void subscribe(MqttClient client, Consumer<Event> consumer) throws MqttException {
		client.subscribe("#", (t, m) -> {
			tryParse(nanoTime(), t, new String(m.getPayload())).ifPresent(consumer);
		});
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

}
