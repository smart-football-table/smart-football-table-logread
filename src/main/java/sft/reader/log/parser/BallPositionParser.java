package sft.reader.log.parser;

import static java.lang.Double.parseDouble;

import sft.event.BallPosition;
import sft.event.Event;

public class BallPositionParser implements LogEntryParser {

	@Override
	public boolean canParse(String topic) {
		return topic.equals("ball/position/abs");
	}

	@Override
	public Event parse(String topic, String message) {
		String[] values = message.split("\\,");
		if (values.length != 3) {
			throw new IllegalStateException("Cannot parse " + message + " as position (" + values.length + " != 3)");
		}
		return new BallPosition(Long.parseLong(values[0]), parseDouble(values[1]), parseDouble(values[2]));
	}

}