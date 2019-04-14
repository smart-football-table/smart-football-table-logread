package sft.event;

public class Event {

	public static final Event NULL = new Event(-1);

	public final long nanos;

	public Event(long nanos) {
		this.nanos = nanos;
	}
}