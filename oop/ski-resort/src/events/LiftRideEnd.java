package events;

import resort.Lift;
import athletes.Athlete;

public class LiftRideEnd extends RideEnd {

    public LiftRideEnd(
            int time,
            Athlete athlete,
            Lift lift
    ) {
        super(time, athlete, lift);
    }

    @Override
    public String toString() {
        return "Athlete " + athlete.getId() +
            " finished a ride on lift " + connection.getId() + ".";

    }
}
