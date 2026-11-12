package events;

import simulation.Simulation;

public abstract class Event {
    
    // Event time in seconds since 9:00.
    protected int time;

    public Event(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    // Execute the event.
    public abstract void execute(Simulation simulation);

    // Add the event only if its time is within the allowed interval. The
    // default cutoff is the end of riding, but some subclasses override this
    // method.
    public void tryAddToSimulation(Simulation simulation) {
        if(simulation.isBeforeRidingEnd(time)) {
            simulation.enqueueEvent(this);
        }
    }
}
