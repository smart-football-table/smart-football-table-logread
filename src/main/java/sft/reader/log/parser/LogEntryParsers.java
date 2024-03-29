package sft.reader.log.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Optional;

import sft.event.Event;

public final class LogEntryParsers {

	private static final List<LogEntryParser> parsers = unmodifiableList(
			asList(new BallPositionParser(), new TeamScoreParser()));

	private LogEntryParsers() {
		super();
	}

	public static Optional<Event> tryParse(String topic, String message) {
		return parsers.stream().filter(p -> p.canParse(topic)).findFirst().map(p -> p.parse(topic, message));
	}

}