package athletes;

import java.util.List;

import util.RandomSource;
import resort.Connection;
import resort.Slope;
import resort.Node;
import resort.Lift;

public class LocalAthlete extends Athlete {

    public LocalAthlete(
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
            randomSource
        );
    }

    @Override
    public Connection chooseNextConnection() {
        // Check whether the athlete makes a spontaneous choice.
        if(randomSource.occurs(spontaneity)) {
            return chooseSpontaneously();
        }
        // Otherwise, choose the best available move.
        else {
            return chooseByAttractiveness();
        }
    }

    // Choose a slope or lift based on attractiveness.
    private Connection chooseByAttractiveness() {
        // Consider all connections leaving the athlete's current node.
        List<Slope> slopes = currentNode.getSlopes();
        List<Lift> lifts = currentNode.getLifts();

        // The best slope considered so far and its attractiveness. Start at -1,
        // which is below every possible nonnegative attractiveness value.
        Slope bestSlope = null;
        double bestAttractiveness = -1.0;

        // Lift leading to the best slope if it is not directly accessible from
        // this node. Otherwise, leave this null.
        Lift bestLift = null;

        // Consider slopes directly leaving the current node.
        for(Slope slope: slopes) {
            double attractiveness = calculateAttractiveness(slope);

            // Update the best choice if this slope is more attractive.
            if(bestSlope == null || attractiveness > bestAttractiveness) {
                bestSlope = slope;
                bestAttractiveness = attractiveness;
            }
        }

        // Consider each outgoing lift and all slopes leaving its destination
        // node.
        for(Lift lift: lifts) {
            Node endStation = lift.getEnd();
            List<Slope> slopesFromEnd = endStation.getSlopes();

            for(Slope slope: slopesFromEnd) {
                double attractiveness = calculateAttractiveness(slope);

                // Update the best choice if this slope is more attractive.
                if(bestSlope == null 
                            || attractiveness > bestAttractiveness) {
                    bestSlope = slope;
                    bestAttractiveness = attractiveness;

                    // This slope is accessible only after taking this lift.
                    bestLift = lift;
                }
            }
        }

        // If no slope was found, the input guarantees an outgoing lift, so
        // choose any lift.
        if(bestSlope == null) {
            return lifts.get(0);
        }

        // If the best slope leaves the current node directly, start skiing down
        // it.
        if(bestLift == null) {
            return bestSlope;
        }
        // Otherwise, join the queue for the lift leading to the best slope.
        else {
            return bestLift;
        }
    }
}
