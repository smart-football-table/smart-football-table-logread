package reader.log;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import sft.event.BallPosition;
import sft.event.TeamScored;
import sft.event.TimestampedEvent;
import sft.reader.log.LogReader;

public class LogReaderTest {

	@Test
	public void canParse() throws IOException, ParseException {
		List<String> lines = new ArrayList<>();

		int team = 1;
		int score = 2;
		lines.add(makeGameScoreTeam("14:13:00.590269", team, score));

		double x = 0.7209876543209877;
		double y = 0.20246913580246914;
		lines.add(makeBallPosition("14:13:00.817681", x, y));

		StringReader stringReader = new StringReader(lines.stream().collect(joining("\n")));
		List<TimestampedEvent> events = LogReader.read(stringReader);
		assertThat(events.size(), is(score));

		TeamScored event0 = (TeamScored) events.get(0).getEvent();
		assertThat(events.get(0).nanos, is(51180590269000L));
		assertThat(event0.team, is(team));
		assertThat(event0.score, is(score));

		BallPosition event1 = (BallPosition) events.get(1).getEvent();
		assertThat(events.get(1).nanos, is(51180817681000L));
		assertThat(event1.x, is(x));
		assertThat(event1.y, is(y));
	}

	private static String makeGameScoreTeam(String timestamp, int team, int score) {
		return timestamp + " " + "game/score/team/" + team + " " + score;
	}

	private String makeBallPosition(String timestamp, double x, double y) {
		return timestamp + " " + "ball/position {\"x\":" + x + ",\"y\":" + y + "}";
	}

}
