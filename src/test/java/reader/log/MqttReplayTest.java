package reader.log;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.rules.Timeout.seconds;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
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
import org.mockito.InOrder;
import org.mockito.Mockito;

import reader.junit.rules.Message;
import reader.junit.rules.MqttRule;
import sft.event.BallPosition;
import sft.event.Event;
import sft.event.TeamScored;
import sft.event.EventInTime;

public class MqttReplayTest {

	@Rule
	public Timeout timeout = seconds(30);

	@Rule
	public MqttRule mqttRule = withLocalhostAndRandomPort();

	private MqttReplay sut;

	@Before
	public void setup() throws MqttSecurityException, MqttException {
		sut = spy(new MqttReplay(mqttRule.broker().host(), mqttRule.broker().port()));
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
		long sleepNanos = baseNanos + SECONDS.toNanos(5);
		sut.replay(asList(inTime(baseNanos, bp), inTime(sleepNanos, ts)));

		mqttRule.client().assertReceived(message(bp), message(ts));
		InOrder orderVerifier = inOrder(sut);
		orderVerifier.verify(sut).publish(message(bp).getTopic(), message(bp).getPayload());
		// TODO any -> sleepNanos
//		orderVerifier.verify(sut).sleepNanos(sleepNanos);
		orderVerifier.verify(sut).sleepNanos(Mockito.any(long.class));
		orderVerifier.verify(sut).publish(message(ts).getTopic(), message(ts).getPayload());
	}

	private Message message(TeamScored teamScored) {
		return mqttMessage("game/score/team/" + teamScored.team, String.valueOf(teamScored.score));
	}

	private Message message(BallPosition position) {
		return mqttMessage("ball/position", "{\"x\": " + position.x + ", \"y\": " + position.y + "}");
	}

	private static EventInTime inTime(long nanos, Event event) {
		return new EventInTime(nanos, event);
	}

}
