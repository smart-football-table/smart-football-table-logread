package sft.event;

public class TeamScored implements Event {

	public final int team;
	public final int score;

	public TeamScored(int team, int score) {
		this.team = team;
		this.score = score;
	}

}