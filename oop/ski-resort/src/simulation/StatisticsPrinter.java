package simulation;

import java.util.List;

import data.SimulationData;
import resort.Slope;
import resort.Lift;

public class StatisticsPrinter {

    // The constructor is private because this class contains only static
    // methods.
    private StatisticsPrinter() {

    }

    // Print statistics for slopes and lifts.
    public static void print(SimulationData data) {
        printSlopeStatistics(data);

        printLiftStatistics(data);
    }

    private static void printSlopeStatistics(SimulationData data) {
        List<Slope> slopes = data.getSlopes();

        System.out.println("");
        System.out.println("Slope statistics:");
        System.out.println("");

        for (Slope slope: slopes) {

            System.out.println("Slope " + slope.getId() + ":");

            System.out.println("  ride count: " +
                slope.getRideCount()
            );

            System.out.printf("  final surface smoothness: %.2f%n",
                slope.getCurrentSurfaceSmoothness()
            );

            System.out.println("");
        }

        System.out.println("");
    }

    private static void printLiftStatistics(SimulationData data) {
        List<Lift> lifts = data.getLifts();

        System.out.println("Lift statistics:");

        for (Lift lift: lifts) {
            System.out.println("");

            System.out.println("Lift " + lift.getId() + ":");

            System.out.println("  maximum queue length: " + 
                lift.getMaxQueueLength()
            );

            System.out.printf("  average queue length: %.2f%n",
                lift.getAverageQueueLength()
            );

            System.out.println("  total passengers transported: " + 
                lift.getRideCount()
            );

            System.out.printf("  seat occupancy: %.2f%%%n",
                lift.getOccupancyPercentage()
            );
        }
    }
}