package sft.event;

public class BallPosition extends Event {

	public final double x;
	public final double y;

	public BallPosition(long nanos, double x, double y) {
		super(nanos);
		this.x = x;
		this.y = y;
	}
}