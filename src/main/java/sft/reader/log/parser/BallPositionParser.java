package sft.reader.log.parser;

import static java.lang.Double.parseDouble;
import static java.util.regex.Pattern.compile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sft.event.BallPosition;
import sft.event.Event;

public class BallPositionParser implements LogEntryParser {

	private static final Pattern xPattern = doubleValueFromJsonPattern("x");
	private static final Pattern yPattern = doubleValueFromJsonPattern("y");

	@Override
	public boolean canParse(String topic) {
		return topic.equals("ball/position");
	}

	@Override
	public Event parse(String topic, String message) {
		Matcher xMatcher = xPattern.matcher(message);
		Matcher yMatcher = yPattern.matcher(message);
		if (!xMatcher.find() || !yMatcher.find()) {
			throw new IllegalStateException("Cannot parse " + message + " as position");
		}
		return new BallPosition(parseDouble(xMatcher.group(1)), parseDouble(yMatcher.group(1)));
	}

	private static Pattern doubleValueFromJsonPattern(String var) {
		return compile("\"" + var + "\":([\\d\\.]+)[,}]");
	}

}