package sft.reader.log.parser;

import static java.lang.Integer.parseInt;

import sft.event.Event;
import sft.event.TeamScored;

public class GameScoreTeamParser implements LogEntryParser {

	@Override
	public boolean canParse(String topic) {
		return topic.startsWith("game/score/team/");
	}

	@Override
	public Event parse(String topic, String message) {
		int team = parseInt(topic.substring("game/score/team/".length()));
		int score = parseInt(message);
		return new TeamScored(team, score);
	}

}