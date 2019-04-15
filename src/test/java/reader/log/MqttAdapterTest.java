package reader.log;

import static java.lang.System.nanoTime;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.Timeout.seconds;
import static reader.junit.rules.Message.mqttMessage;
import static reader.junit.rules.MqttRule.withLocalhostAndRandomPort;
import static sft.reader.log.parser.LogEntryParsers.tryParse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import reader.junit.rules.Broker;
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
		List<Event> events = new ArrayList<>();
		new MqttClientAdapter(mqttRule.broker().host(), mqttRule.broker().port(), events::add);

		double x = 0.12345;
		double y = 0.6789;
		int team = 1;
		int score = 2;
		mqttRule.client().publish(mqttMessage("ball/position", "{\"x\":" + x + ",\"y\":" + y + "}"));
		mqttRule.client().publish(mqttMessage("game/score/team/" + team, String.valueOf(score)));

		TimeUnit.MILLISECONDS.sleep(100);

		assertThat(events.size(), is(2));

		BallPosition ballPosition = (BallPosition) events.get(0);
		assertThat(ballPosition.x, is(x));
		assertThat(ballPosition.y, is(y));

		TeamScored teamScored = (TeamScored) events.get(1);
		assertThat(teamScored.team, is(team));
		assertThat(teamScored.score, is(score));
	}

}
