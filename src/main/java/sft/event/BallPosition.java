package sft.event;

public class BallPosition implements Event {

	public final double x;
	public final double y;

	public BallPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

}