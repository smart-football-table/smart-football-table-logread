package reader.log;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import event.Event;
import event.BallPosition;
import event.TeamScored;

public class LogReader {

	private static interface LogEntryParser {
		boolean canParse(String topic);

		Event parse(long nanos, String topic, String message);
	}

	private static class GameScoreTeamParser implements LogReader.LogEntryParser {

		@Override
		public boolean canParse(String topic) {
			return topic.startsWith("game/score/team/");
		}

		@Override
		public Event parse(long nanos, String topic, String message) {
			int team = parseInt(topic.substring("game/score/team/".length()));
			int score = parseInt(message);
			return new TeamScored(nanos, team, score);
		}

	}

	private static class BallPositionParser implements LogReader.LogEntryParser {

		private static final Pattern xPattern = doubleValueFromJsonPattern("x");
		private static final Pattern yPattern = doubleValueFromJsonPattern("y");

		@Override
		public boolean canParse(String topic) {
			return topic.equals("ball/position");
		}

		@Override
		public Event parse(long nanos, String topic, String message) {
			Matcher xMatcher = xPattern.matcher(message);
			Matcher yMatcher = yPattern.matcher(message);
			if (!xMatcher.find() || !yMatcher.find()) {
				throw new IllegalStateException("Cannot parse " + message + " as position");
			}
			return new BallPosition(nanos, parseDouble(xMatcher.group(1)), parseDouble(yMatcher.group(1)));
		}

	}

	private static Pattern doubleValueFromJsonPattern(String var) {
		return compile("\"" + var + "\":([\\d\\.]+)[,}]");
	}

	private static final int nanoDigitsUsed = 6;
	private static final int nanoMultiplier = (int) pow(10,
			String.valueOf(SECONDS.toNanos(1) - 1).length() - nanoDigitsUsed);
	private static final Pattern timestampPattern = compile("(\\d{2}):(\\d{2}):(\\d{2}).(\\d{" + nanoDigitsUsed + "})");
	private static final List<LogReader.LogEntryParser> parsers = asList(new BallPositionParser(),
			new GameScoreTeamParser());

	public static List<Event> read(InputStream is) throws IOException, ParseException {
		return read(new InputStreamReader(is));
	}

	public static List<Event> read(Reader reader) throws IOException {
		try (BufferedReader br = new BufferedReader(reader)) {
			return br.lines().map(LogReader::makeEvent).filter(e -> e != Event.NULL).collect(toList());
		}
	}

	private static Event makeEvent(String line) {
		String[] split = line.trim().split("\\s");

		Matcher timestampMatcher = timestampPattern.matcher(split[0].trim());
		if (!timestampMatcher.find()) {
			throw new IllegalStateException("Cannot parse " + split[0].trim() + " as timestamp");
		}
		long nanos = nanos(timestampMatcher);
		String topic = split[1].trim();
		String message = split[2].trim();
		return parsers.stream().filter(p -> p.canParse(topic)).findFirst().map(p -> p.parse(nanos, topic, message))
				.orElse(Event.NULL);
	}

	private static long nanos(Matcher timestampMatcher) {
		return HOURS.toNanos(parseInt(timestampMatcher.group(1))) //
				+ MINUTES.toNanos(parseInt(timestampMatcher.group(2))) //
				+ SECONDS.toNanos(parseInt(timestampMatcher.group(3))) //
				+ parseInt(timestampMatcher.group(4)) * nanoMultiplier;
	}
}