package events;

import resort.Connection;
import athletes.Athlete;
import simulation.Simulation;

// An event that ends a lift or slope ride.
public abstract class RideEnd extends MovementEvent {
    
    // Connection used by the athlete: a slope or a lift.
    protected Connection connection;

    public RideEnd(
            int time,
            Athlete athlete,
            Connection connection
    ) {
        super(time, athlete);
        
        this.connection = connection;
    }

    // Ride-end events may be scheduled until the simulation ends.
    @Override
    public void tryAddToSimulation(Simulation simulation) {
        if(simulation.isBeforeSimulationEnd(time)) {
            simulation.enqueueEvent(this);
        }
    }

    @Override
    protected void executeMainAction(Simulation simulation) {
        // After the ride, the athlete is at the connection's destination node.
        athlete.setCurrentNode(connection.getEnd());
    }
}
