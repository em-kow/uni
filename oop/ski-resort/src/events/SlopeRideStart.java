package events;

import resort.Slope;
import athletes.Athlete;
import simulation.Simulation;

public class SlopeRideStart extends RideStart {

    // Slope on which the athlete is starting a ride.
    private Slope slope;

    public SlopeRideStart(
            int time,
            Athlete athlete,
            Slope slope
    ) {
        super(time, athlete);

        this.slope = slope;
    }

    @Override
    protected void recordRideStart() {
        // Increment the global ride count for this slope.
        slope.incrementRideCount();

        // Let the athlete record this run individually.
        athlete.recordRun(slope);

        // Record the ride if the athlete is tracked.
        athlete.recordRide(slope);
    }

    @Override
    protected void scheduleRideEnd(Simulation simulation) {
        simulation.addEvent(new SlopeRideEnd(
                time + slope.getTravelTime(),
                athlete,
                slope
        ));
    }

    @Override
    public String toString() {
        return "Athlete " + athlete.getId()
                + " started a run on slope " + slope.getId() + ".";
    }
}