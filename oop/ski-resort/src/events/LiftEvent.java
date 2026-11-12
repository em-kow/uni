package events;

import resort.Lift;

public abstract class LiftEvent extends Event {

    // Lift associated with this event.
    protected Lift lift;

    public LiftEvent(int time, Lift lift) {
        super(time);

        this.lift = lift;
    }
}
