package sft.reader.log.parser;

import sft.event.Event;

public interface LogEntryParser {
	boolean canParse(String topic);

	Event parse(long nanos, String topic, String message);
}