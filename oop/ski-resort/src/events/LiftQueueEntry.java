package events;
import resort.Lift;
import athletes.Athlete;
import simulation.Simulation;

public class LiftQueueEntry extends AthleteEvent {
    
    // Lift whose queue the athlete has joined.
    private Lift lift;

    public LiftQueueEntry(
            int time,
            Athlete athlete,
            Lift lift
    ) {
        super(time, athlete);

        this.lift = lift;
    }

    @Override
    protected void executeMainAction(Simulation simulation) {
        // Add the athlete to the lift's waiting queue.
        lift.enqueue(athlete, time);
    }

    @Override
    public String toString() {
        return "Athlete " + athlete.getId() +
            " joined the queue for lift " + lift.getId() + ".";
    }
}
