package events;

import resort.Lift;
import athletes.Athlete;
import simulation.Simulation;

public class LiftRideStart extends RideStart {

    // Lift on which the athlete is starting a ride.
    private Lift lift;

    public LiftRideStart(
            int time,
            Athlete athlete,
            Lift lift
    ) {
        super(time, athlete);

        this.lift = lift;
    }

    @Override
    protected void recordRideStart() {
        // Increment the global ride count for this lift.
        lift.incrementRideCount();

        // Record the ride if the athlete is tracked.
        athlete.recordRide(lift);
    }

    @Override
    protected void scheduleRideEnd(Simulation simulation) {
        simulation.addEvent(new LiftRideEnd(
                time + lift.getTravelTime(),
                athlete,
                lift
        ));
    }

    @Override
    public String toString() {
        return "Athlete " + athlete.getId()
                + " started a ride on lift " + lift.getId() + ".";
    }
}