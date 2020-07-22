package sft.event;

public class TeamScore implements Event {

	public final int team;
	public final int score;

	public TeamScore(int team, int score) {
		this.team = team;
		this.score = score;
	}

}