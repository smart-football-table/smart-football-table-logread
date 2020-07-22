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
import sft.event.TeamScore;
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
		int baseNanos = 123;
		BallPosition bp = new BallPosition(baseNanos, 0.2, 0.3);
		TeamScore ts = new TeamScore(5, 6);
		long sleepNanos = baseNanos + SECONDS.toNanos(5);
		EventInTime m1 = inTime(baseNanos, bp);
		EventInTime m2 = inTime(sleepNanos, ts);
		sut.replay(asList(m1, m2));

		mqttRule.client().assertReceived(message(m1), message(m2));
		InOrder orderVerifier = inOrder(sut);
		orderVerifier.verify(sut).publish(message(m1).getTopic(), message(m1).getPayload());
		// TODO any -> sleepNanos
//		orderVerifier.verify(sut).sleepNanos(sleepNanos);
		orderVerifier.verify(sut).sleepNanos(Mockito.any(long.class));
		orderVerifier.verify(sut).publish(message(m2).getTopic(), message(m2).getPayload());
	}

	private Message message(EventInTime eventInTime) {
		if (eventInTime.getEvent() instanceof BallPosition) {
			BallPosition position = (BallPosition) eventInTime.getEvent();
			return mqttMessage("ball/position/abs", eventInTime.nanos + "," + position.x + "," + position.y);
		}
		if (eventInTime.getEvent() instanceof TeamScore) {
			TeamScore teamScore = (TeamScore) eventInTime.getEvent();
			return mqttMessage("game/score/team/" + teamScore.team, String.valueOf(teamScore.score));
		}
		throw new IllegalStateException(String.valueOf(eventInTime));
	}

	private static EventInTime inTime(long nanos, Event event) {
		return new EventInTime(nanos, event);
	}

}
