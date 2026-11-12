package athletes;

import java.util.HashMap;
import java.util.Map;

import resort.Slope;

// Store information about slopes from one athlete's perspective.

public class SlopeLog {
    
    // Athlete's boredom factor.
    private double boredomFactor;

    // Total number of runs made by the athlete across all slopes.
    private int totalRunCount;

    // 
    private Map<Slope, SlopeData> slopeData;

    public SlopeLog(double boredomFactor) {
        this.boredomFactor = boredomFactor;

        this.totalRunCount = 0;

        this.slopeData = new HashMap<>();
    }

    // The athlete's current boredom with this slope.
    public double getBoredom(Slope slope) {
        SlopeData data = slopeData.get(slope);

        // The athlete has not yet skied down this slope.
        if(data == null) {
            return 0.0;
        }

        // Count the runs since boredom with this slope was last updated.
        int runsSinceLastUpdate = totalRunCount - 
                data.lastUpdateRunNumber;
        
        double currentBoredom = data.boredom * 
            Math.pow(1.0 - boredomFactor, runsSinceLastUpdate);

        // Update the stored data.
        data.boredom = currentBoredom;
        data.lastUpdateRunNumber = totalRunCount;

        return currentBoredom;
    }

    // Number of times the athlete has skied down this slope.
    public int getRunCount(Slope slope) {
        SlopeData data = slopeData.get(slope);

        if(data == null) {
            return 0;
        }

        return data.runCount;
    }

    // Record a run on the given slope.
    public void recordRun(Slope slope) {
        double currentBoredom = getBoredom(slope);

        totalRunCount++;

        double newBoredom = boredomFactor + 
            (1.0 - boredomFactor) * currentBoredom;

        SlopeData data = slopeData.get(slope);

        // First run on this slope.
        if(data == null) {
            data = new SlopeData();
            slopeData.put(slope, data);
        }

        data.boredom = newBoredom;
        data.lastUpdateRunNumber = totalRunCount;
        data.incrementRunCount();
    }

    // Helper class storing one athlete's data about a single slope.
    private static class SlopeData {
        // Boredom with this slope.
        private double boredom;

        // Run number at the last update of this slope's data.
        private int lastUpdateRunNumber;

        // Number of times the athlete has skied down this slope.
        private int runCount;

        private SlopeData() {
            this.boredom = 0.0;
            this.lastUpdateRunNumber = 0;
            this.runCount = 0;
        }

        private void incrementRunCount() {
            runCount++;
        }
    }
}
