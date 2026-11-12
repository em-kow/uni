package events;
import resort.Lift;
import athletes.Athlete;
import simulation.Simulation;

public class LiftDeparture extends LiftEvent {
    
    public LiftDeparture(int time, Lift lift) {
        super(time, lift);
    }

    @Override
    public void execute(Simulation simulation) {
        // Board no more athletes than the lift's capacity or the number
        // waiting.
        int boardingCount = Math.min(
            lift.getWaitingCount(),
            lift.getCapacity()
        );

        for(int i = 0; i < boardingCount; i++) {
            // Remove the first athlete from the queue.
            Athlete athlete = lift.dequeue(time);

            // Schedule the start of this athlete's lift ride.
            simulation.addEvent(new LiftRideStart(
                time,
                athlete,
                lift
            ));

        }

        // Schedule this lift's next departure.
        simulation.addEvent(new LiftDeparture(
            time + lift.getInterval(),
            lift
        ));

        lift.recordDeparture();
    }
}
