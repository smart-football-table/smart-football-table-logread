package reader.log;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.rules.Timeout.seconds;
import static reader.junit.rules.Message.mqttMessage;
import static reader.junit.rules.MqttRule.withLocalhostAndRandomPort;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import reader.junit.rules.Message;
import reader.junit.rules.MqttRule;
import sft.event.BallPosition;
import sft.event.Event;
import sft.event.TeamScored;
import sft.event.TimestampedEvent;

public class MqttReplayTest {

	@Rule
	public Timeout timeout = seconds(30);

	@Rule
	public MqttRule mqttRule = withLocalhostAndRandomPort();

	private MqttReplay sut;

	@Before
	public void setup() throws MqttSecurityException, MqttException {
		sut = new MqttReplay(mqttRule.broker().host(), mqttRule.broker().port()) {
			@Override
			protected void sleepNanos(long nanos) {
				// TODO assert sleep time
//				super.sleepNanos(nanos);
			}
		};
	}

	@After
	public void tearDown() throws IOException {
		sut.close();
	}

	@Test
	public void canReplayLog() throws MqttSecurityException, MqttException, InterruptedException {
		BallPosition bp = new BallPosition(0.2, 0.3);
		TeamScored ts = new TeamScored(5, 6);
		int baseNanos = 123;
		sut.replay(asList(timestamped(baseNanos, bp), timestamped(baseNanos + SECONDS.toNanos(5), ts)));
		mqttRule.client().assertReceived(message(bp), message(ts));
	}

	private Message message(TeamScored teamScored) {
		return mqttMessage("game/score/team/" + teamScored.team, String.valueOf(teamScored.score));
	}

	private Message message(BallPosition position) {
		return mqttMessage("ball/position", "{\"x\": " + position.x + ", \"y\": " + position.y + "}");
	}

	private static TimestampedEvent timestamped(long nanos, Event event) {
		return new TimestampedEvent(nanos, event);
	}

}
