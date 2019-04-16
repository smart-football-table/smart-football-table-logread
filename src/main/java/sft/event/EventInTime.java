package sft.event;

public class EventInTime {

	public static final EventInTime NULL = new EventInTime(-1, null);

	public final long nanos;

	private final Event event;

	public EventInTime(long nanos, Event event) {
		this.nanos = nanos;
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}

}
