package sft.event;

public class TeamScored extends Event {

	public final int team;
	public final int score;

	public TeamScored(long nanos, int team, int score) {
		super(nanos);
		this.team = team;
		this.score = score;
	}

}