package sft.reader.mqtt;

import static java.lang.System.currentTimeMillis;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.Timeout.seconds;
import static reader.junit.rules.Message.mqttMessage;
import static reader.junit.rules.MqttRule.withLocalhostAndRandomPort;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import reader.junit.rules.MqttRule;
import sft.event.BallPosition;
import sft.event.Event;
import sft.event.TeamScore;

public class MqttClientAdapterTest {

	@Rule
	public Timeout timeout = seconds(30);

	@Rule
	public MqttRule mqttRule = withLocalhostAndRandomPort();

	@Test
	public void doesSubsribeAndReceive() throws Exception {
		List<Event> events = new ArrayList<>();
		try (MqttClientAdapter sut = new MqttClientAdapter(mqttRule.broker().host(), mqttRule.broker().port(),
				events::add)) {
			long timestamp = currentTimeMillis();
			double x = 0.12345;
			double y = 0.6789;
			int team = 1;
			int score = 2;
			mqttRule.client().publish(mqttMessage("ball/position/abs", timestamp + "," + x + "," + y));
			mqttRule.client().publish(mqttMessage("team/score/" + team, String.valueOf(score)));

			await().untilAsserted(() -> {
				assertThat(events.size(), is(2));
				BallPosition ballPosition = (BallPosition) events.get(0);
				assertThat(ballPosition.x, is(x));
				assertThat(ballPosition.y, is(y));

				TeamScore teamScored = (TeamScore) events.get(1);
				assertThat(teamScored.team, is(team));
				assertThat(teamScored.score, is(score));
			});

		}

	}

}
