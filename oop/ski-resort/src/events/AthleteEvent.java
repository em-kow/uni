package events;

import athletes.Athlete;
import simulation.Simulation;

// An event concerning an athlete.
public abstract class AthleteEvent extends Event {
    
    // Athlete associated with this event.
    protected Athlete athlete;

    public AthleteEvent(int time, Athlete athlete) {
        super(time);

        this.athlete = athlete;
    }

    @Override
    public final void execute(Simulation simulation) {
        // First execute the main action of the athlete's event.
        executeMainAction(simulation);

        // Print event information if the athlete is tracked.
        if (athlete.isTracked()) {
            simulation.print(getTime(), toString());
        }

        // Execute any action that follows printing.
        executeAfterPrinting(simulation);
    }

    // The main action depends on the concrete subclass.
    protected abstract void executeMainAction(Simulation simulation);

    // Usually nothing happens after printing, but subclasses may override this.
    protected void executeAfterPrinting(Simulation simulation) {

    }
}
