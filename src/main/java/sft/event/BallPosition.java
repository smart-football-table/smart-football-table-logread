package sft.event;

public class BallPosition implements Event {

	public final long timestamp;
	public final double x;
	public final double y;

	public BallPosition(long timestamp, double x, double y) {
		this.timestamp = timestamp;
		this.x = x;
		this.y = y;
	}

}