package athletes;

import java.util.List;

import util.RandomSource;
import resort.Slope;
import resort.Node;
import resort.PathFinder;

public class CollectorAthlete extends PlanningAthlete {
    
    public CollectorAthlete(
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
            randomSource, 
            allSlopes
        );
    }

    @Override
    protected Slope chooseTargetSlope(
        PathFinder pathFinder
    ) {
        Slope bestSlope = null;

        // Parameters of the best slope found so far.
        int bestRunCount = Integer.MAX_VALUE;
        int bestDistance = Integer.MAX_VALUE;
        double bestAttractiveness = -1.0;

        for(Slope slope: allSlopes) {
            int runCount = getSlopeRunCount(slope);
            int distance = pathFinder.getDistanceTo(
                slope.getStart()
            );
            double attractiveness = calculateAttractiveness(slope);

            if(isBetter(
                runCount,
                distance,
                attractiveness,
                bestRunCount,
                bestDistance,
                bestAttractiveness
            )) {
                bestSlope = slope;
                bestRunCount = runCount;
                bestDistance = distance;
                bestAttractiveness = attractiveness;
            }
        }
        
        return bestSlope;
    }

    // Check whether this slope is better than the current best according to the
    // collector's criteria.
    private boolean isBetter(
        int runCount,
        int distance,
        double attractiveness,
        int bestRunCount,
        int bestDistance,
        double bestAttractiveness
    ) {
        if(runCount < bestRunCount) {
            return true;
        }

        if(runCount > bestRunCount) {
            return false;
        }

        if(distance < bestDistance) {
            return true;
        }

        if(distance > bestDistance) {
            return false;
        }

        return attractiveness > bestAttractiveness;
    }
}
