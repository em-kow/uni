package data;

import java.util.List;

import resort.Slope;
import resort.Node;
import resort.Lift;
import athletes.Athlete;

public class SimulationData {

    // All nodes in the resort.
    private List<Node> nodes;

    // All lifts in the resort.
    private List<Lift> lifts;

    // All slopes in the resort.
    private List<Slope> slopes;

    // All athletes.
    private List<Athlete> athletes;

    public SimulationData(
        List<Node> nodes,
        List<Lift> lifts,
        List<Slope> slopes,
        List<Athlete> athletes
    ) {
        this.nodes = nodes;
        this.lifts = lifts;
        this.slopes = slopes;
        this.athletes = athletes;
    }

    public List<Lift> getLifts() {
        return lifts;
    }

    public List<Slope> getSlopes() {
        return slopes;
    }

    public List<Athlete> getAthletes() {
        return athletes;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
