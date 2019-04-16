package sft.event;

public class EventInTime {

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
