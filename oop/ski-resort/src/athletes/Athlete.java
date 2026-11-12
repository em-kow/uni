package athletes;

import java.util.List;
import java.util.ArrayList;

import util.RandomSource;
import resort.Connection;
import resort.Slope;
import resort.Node;
import resort.Lift;

public abstract class Athlete {

    // Athlete ID.
    private int id;

    // Athlete's skill level.
    private int skillLevel;

    // Probability that the athlete chooses a slope or lift at random.
    protected double spontaneity;

    // Weight assigned to matching the slope's difficulty to the athlete's
    // skill.
    private double difficultyWeight;

    // Weight assigned to the slope's surface smoothness.
    private double surfaceWeight;

    // Rate at which the athlete becomes bored with slopes.
    private double boredomFactor;

    // Weight assigned to boredom when calculating attractiveness.
    private double boredomWeight;

    // Node where the athlete starts the day.
    private Node startNode;

    // Node where the athlete is currently located.
    protected Node currentNode;

    // Whether the athlete is tracked.
    private boolean tracked;

    // Start time in seconds since 9:00.
    private int startTime;

    // Slope information from the athlete's perspective.
    private SlopeLog slopeLog;

    // Used for the athlete's random decisions.
    protected RandomSource randomSource;

    // Ride history of a tracked athlete.
    private List<Connection> rideHistory;

    public Athlete(
        int id,
        int skillLevel,
        double spontaneity,
        double boredomFactor,
        boolean tracked,
        double difficultyWeight,
        double surfaceWeight,
        double boredomWeight,
        Node startNode,
        int startTime,
        RandomSource randomSource
    ) {
        this.id = id;
        this.skillLevel = skillLevel;
        this.spontaneity = spontaneity;
        this.boredomFactor = boredomFactor;
        this.tracked = tracked;
        this.difficultyWeight = difficultyWeight;
        this.surfaceWeight = surfaceWeight;
        this.boredomWeight = boredomWeight;
        this.startNode = startNode;
        this.startTime = startTime;

        this.randomSource = randomSource;

        this.slopeLog = new SlopeLog(boredomFactor);
        this.rideHistory = new ArrayList<>();
    }

    // Choose the athlete's next connection: a lift or a slope.
    public abstract Connection chooseNextConnection();

    // Choose a slope or lift at random.
    protected Connection chooseSpontaneously() {
        // Consider all connections leaving the athlete's current node.
        List<Slope> slopes = currentNode.getSlopes();
        List<Lift> lifts = currentNode.getLifts();

        // Choose from all outgoing connections.
        int optionCount = slopes.size() + lifts.size();
        int index = randomSource.nextIndex(optionCount);

        // Handle selection of a slope.
        if(index < slopes.size()) {
            // The selected slope.
            return slopes.get(index);
        }
        // Handle selection of a lift.
        else {
            // The selected lift.
            return lifts.get(index - slopes.size());
        }
    }

    // Calculate the given slope's attractiveness to this athlete.
    protected double calculateAttractiveness(Slope slope) {
        double difficultyMatch = calculateDifficultyMatch(slope);
        double boredom = slopeLog.getBoredom(slope);

        return surfaceWeight * slope.getCurrentSurfaceSmoothness() 
            + difficultyWeight * difficultyMatch
            + boredomWeight * (1.0  - boredom);
    }

    // Calculate how well the given slope's difficulty matches this athlete's
    // skill.
    private double calculateDifficultyMatch(Slope slope) {
        int athleteSkillLevel = skillLevel;
        int slopeDifficulty = slope.getDifficultyLevel();

        if(slopeDifficulty >= athleteSkillLevel + 5) {
            return 0.0;
        }

        if(slopeDifficulty >= athleteSkillLevel) {
            return 1.0 - (slopeDifficulty - athleteSkillLevel) / 5.0;
        }

        return Math.max(
            0.2,
            1.0 - (athleteSkillLevel - slopeDifficulty) / 7.0
        );
    }

    // Record the ride if the athlete is tracked.
    public void recordRide(Connection connection) {
        if(!tracked) {
            return;
        }

        rideHistory.add(connection);
    }

    public List<Connection> getRideHistory() {
        return rideHistory;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setCurrentNode(Node node) {
        currentNode = node;
    }

    public boolean isTracked() {
        return tracked;
    }

    public int getId() {
        return id;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public double getSpontaneity() {
        return spontaneity;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public double getDifficultyWeight() {
        return difficultyWeight;
    }

    public double getSurfaceWeight() {
        return surfaceWeight;
    }

    // Number of times the athlete has skied down this slope.
    public int getSlopeRunCount(Slope slope) {
        return slopeLog.getRunCount(slope);
    }

    public void recordRun(Slope slope) {
        slopeLog.recordRun(slope);
    }

    public int getStartTime() {
        return startTime;
    }
}
