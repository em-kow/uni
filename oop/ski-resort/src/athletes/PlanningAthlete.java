package athletes;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import util.RandomSource;
import resort.Connection;
import resort.Slope;
import resort.Node;
import resort.PathFinder;

// Base class for athletes who plan a route to a slope.

public abstract class PlanningAthlete extends Athlete {
    
    // All slopes in the resort.
    protected List<Slope> allSlopes;

    // Planned sequence of connections the athlete intends to take.
    private Queue<Connection> plan;

    public PlanningAthlete(
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
        RandomSource randomSource,
        List<Slope> allSlopes
    ) {
        super(id, 
            skillLevel, 
            spontaneity, 
            boredomFactor, 
            tracked, 
            difficultyWeight, 
            surfaceWeight, 
            boredomWeight, 
            startNode, 
            startTime, 
            randomSource
        );

        this.allSlopes = allSlopes;
        this.plan = new ArrayDeque<>();
    }

    @Override
    public Connection chooseNextConnection() {
        // If the athlete has a plan, take its next step.
        if(!plan.isEmpty()) {
            return plan.remove();
        }

        // Otherwise, first check whether to choose the next connection at
        // random.
        if(randomSource.occurs(spontaneity)) {
            return chooseSpontaneously();
        }
        
        // If not, choose a target slope and find a shortest route to reach it.
        PathFinder pathFinder = 
                        new PathFinder(currentNode);
        
        // First use BFS to compute distances to the other nodes.
        pathFinder.bfs();

        // Choose the target slope.
        Slope targetSlope = chooseTargetSlope(pathFinder);

        // Add the new route to the target slope to the plan.
        plan.addAll(pathFinder.reconstructPlanToSlope(targetSlope));

        // Take the first connection in the new plan.
        return plan.remove();
    }

    protected abstract Slope chooseTargetSlope(
        PathFinder pathFinder
    );
}
