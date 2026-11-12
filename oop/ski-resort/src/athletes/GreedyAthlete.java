package athletes;

import java.util.List;

import util.RandomSource;
import resort.Slope;
import resort.Node;
import resort.PathFinder;

public class GreedyAthlete extends PlanningAthlete {

    public GreedyAthlete(
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
        super(
            id, 
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
        double bestAttractiveness = -1.0;

        for(Slope slope: allSlopes) {
            double attractiveness = calculateAttractiveness(slope);

            if(bestAttractiveness < attractiveness) {
                bestAttractiveness = attractiveness;
                bestSlope = slope;
            }
        }

        return bestSlope;
    }
}
