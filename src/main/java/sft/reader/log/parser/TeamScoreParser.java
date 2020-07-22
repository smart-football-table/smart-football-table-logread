package sft.reader.log.parser;

import static java.lang.Integer.parseInt;

import sft.event.Event;
import sft.event.TeamScore;

public class TeamScoreParser implements LogEntryParser {

	@Override
	public boolean canParse(String topic) {
		return topic.startsWith("team/score/");
	}

	@Override
	public Event parse(String topic, String message) {
		int team = parseInt(topic.substring("team/score/".length()));
		int score = parseInt(message);
		return new TeamScore(team, score);
	}

}