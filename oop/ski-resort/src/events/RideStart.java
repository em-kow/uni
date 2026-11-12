package events;

import athletes.Athlete;
import simulation.Simulation;

// An event that starts a slope or lift ride.
public abstract class RideStart extends AthleteEvent {

    public RideStart(int time, Athlete athlete) {
        super(time, athlete);
    }

    @Override
    protected void executeMainAction(Simulation simulation) {
        recordRideStart();

        // Every ride start must schedule its corresponding ride end.
        scheduleRideEnd(simulation);
    }

    protected abstract void recordRideStart();

    protected abstract void scheduleRideEnd(Simulation simulation);
}