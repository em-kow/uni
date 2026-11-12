package resort;

import events.SlopeRideStart;
import data.athletes.Athlete;
import events.Event;

public class Slope extends Connection{

    // Slope difficulty level.
    private int difficultyLevel;

    // Base surface smoothness of the slope.
    private double baseSurfaceSmoothness;

    // Current surface smoothness of the slope.
    private double currentSurfaceSmoothness;

    // Slope's resistance to wear.
    private double wearResistance;

    public Slope(
            int id,
            Node start,
            Node end,
            int travelTime,
            int difficultyLevel,
            double baseSurfaceSmoothness,
            double wearResistance
    ) {
        super(id, start, end, travelTime);

        this.difficultyLevel = difficultyLevel;
        this.baseSurfaceSmoothness = baseSurfaceSmoothness;
        this.wearResistance = wearResistance;

        // Substituting zero rides into the formula gives an initial surface
        // smoothness of 1.0.
        this.currentSurfaceSmoothness = 1.0;
    }

    @Override
    public void incrementRideCount(){
        super.incrementRideCount();

        // Update surface smoothness using the assignment's formula: current =
        // base + (1 - base) * resistance^rideCount. Its recurrence is: current
        // = base + (current - base) * resistance.
        currentSurfaceSmoothness = baseSurfaceSmoothness + 
        (currentSurfaceSmoothness - baseSurfaceSmoothness) * 
        wearResistance;
    }

    @Override
    public Event createEventAfterSelection(
            int time,
            Athlete athlete
    ) {
        return new SlopeRideStart(time, athlete, this);
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public double getCurrentSurfaceSmoothness() {
        return currentSurfaceSmoothness;
    }

    public double getBaseSurfaceSmoothness() {
        return baseSurfaceSmoothness;
    }

    public double getWearResistance() {
        return wearResistance;
    }
}
