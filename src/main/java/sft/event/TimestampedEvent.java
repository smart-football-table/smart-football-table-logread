package sft.event;

public class TimestampedEvent {

	public static final TimestampedEvent NULL = new TimestampedEvent(-1, null);

	public final long nanos;

	private final Event event;

	public TimestampedEvent(long nanos, Event event) {
		this.nanos = nanos;
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}

}
