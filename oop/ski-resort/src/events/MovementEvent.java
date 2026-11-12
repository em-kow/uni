package events;

import resort.Connection;
import athletes.Athlete;
import simulation.Simulation;

// An event after which the athlete chooses their next move.

public abstract class MovementEvent extends AthleteEvent {
    
    public MovementEvent(int time, Athlete athlete) {
        super(time, athlete);
    }

    // After this event, the athlete is at a node and chooses their next move.
    @Override
    protected void executeAfterPrinting(Simulation simulation) {
        // The athlete cannot make another choice after the cutoff.
        if(!simulation.isBeforeRidingEnd(getTime())) {
            return;
        }

        Connection connection = athlete.chooseNextConnection();

        simulation.addEvent(
                connection.createEventAfterSelection(getTime(), athlete)
        );
    }
}
