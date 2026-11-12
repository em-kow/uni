package events;
import athletes.Athlete;
import simulation.Simulation;

public class SkiDayStart extends MovementEvent {
    
    public SkiDayStart(int time, Athlete athlete) {
        super(time, athlete);
    }

    @Override
    protected void executeMainAction(Simulation simulation) {
        // The athlete begins at their starting node.
        athlete.setCurrentNode(athlete.getStartNode());
    }

    @Override
    public String toString() {
        return "Athlete " + athlete.getId() +
            " started the skiing day.";
    }
}
