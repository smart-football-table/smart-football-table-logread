package sft.reader.log;

import static java.lang.Integer.parseInt;
import static java.lang.Math.pow;
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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sft.event.EventInTime;
import sft.reader.log.parser.LogEntryParsers;

public class LogReader {

	private static final int nanoDigitsUsed = 6;
	private static final int nanoMultiplier = (int) pow(10,
			String.valueOf(SECONDS.toNanos(1) - 1).length() - nanoDigitsUsed);
	private static final Pattern timestampPattern = compile("(\\d{2}):(\\d{2}):(\\d{2}).(\\d{" + nanoDigitsUsed + "})");

	public static List<EventInTime> read(InputStream is) throws IOException, ParseException {
		return read(new InputStreamReader(is));
	}

	public static List<EventInTime> read(Reader reader) throws IOException {
		try (BufferedReader br = new BufferedReader(reader)) {
			return br.lines().map(LogReader::makeEvent).filter(Optional::isPresent).map(Optional::get)
					.collect(toList());
		}
	}

	private static Optional<EventInTime> makeEvent(String line) {
		String[] split = line.trim().split("\\s");

		Matcher timestampMatcher = timestampPattern.matcher(split[0].trim());
		if (!timestampMatcher.find()) {
			throw new IllegalStateException("Cannot parse " + split[0].trim() + " as timestamp");
		}
		long nanos = nanos(timestampMatcher);
		String topic = split[1].trim();
		String message = split[2].trim();
		return LogEntryParsers.tryParse(topic, message).map(e -> new EventInTime(nanos, e));
	}

	private static long nanos(Matcher timestampMatcher) {
		return HOURS.toNanos(parseInt(timestampMatcher.group(1))) //
				+ MINUTES.toNanos(parseInt(timestampMatcher.group(2))) //
				+ SECONDS.toNanos(parseInt(timestampMatcher.group(3))) //
				+ parseInt(timestampMatcher.group(4)) * nanoMultiplier;
	}

}