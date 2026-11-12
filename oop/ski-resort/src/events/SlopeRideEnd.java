package events;

import resort.Slope;
import athletes.Athlete;

public class SlopeRideEnd extends RideEnd {

    public SlopeRideEnd(
            int time,
            Athlete athlete,
            Slope slope
    ) {
        super(time, athlete, slope);
    }

    @Override
    public String toString() {
        return "Athlete " + athlete.getId() +
            " finished a run on slope " + connection.getId() + ".";

    }
}
