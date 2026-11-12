package data;

import java.util.Scanner;
import model.Athlete;
import model.Slope;
import model.Node;
import model.Lift;
import util.Time;

import java.util.Locale;

public class DataReader {

    // Scanner that reads input line by line.
    private Scanner inputScanner;

    public DataReader() {
        this.inputScanner = new Scanner(System.in);
    }

    private String readNonEmptyLine() {
        // trim() removes leading and trailing whitespace.
        String line = inputScanner.nextLine().trim();

        while (line.isEmpty()) {
            line = inputScanner.nextLine().trim();
        }

        return line;
    }

    // Create a scanner for a single line.
    private Scanner createLineScanner(String line) {
        Scanner lineScanner = new Scanner(line);

        // Floating-point numbers use a decimal point.
        lineScanner.useLocale(Locale.ENGLISH);

        return lineScanner;
    }

    // Read all input sections.
    public SimulationData read() {
        Node[] nodes = readNodes();
        Lift[] lifts = readLifts(nodes);
        Slope[] slopes = readSlopes(nodes);
        Athlete[] athletes = readAthletes(nodes);

        // Return an object containing all simulation data.
        return new SimulationData(nodes, lifts, slopes, athletes);
    }

    private Node[] readNodes() {
        // The first number specifies how many nodes to read.
        int nodeCount = Integer.parseInt(readNonEmptyLine());

        // Allocate an array for all nodes.
        Node[] nodes = new Node[nodeCount];

        for(int i = 0; i < nodeCount; i++) {
            // Scanner for the description of one node.
            Scanner lineScanner = createLineScanner(readNonEmptyLine());

            // Node description: elevation, coordinates, and "s" if accessible.
            int elevation = lineScanner.nextInt();

            int x = lineScanner.nextInt();
            int y = lineScanner.nextInt();

            // A trailing "s" marks an accessible node.
            boolean accessible = false;

            if (lineScanner.hasNext()) {
                String marker = lineScanner.next();

                if(marker.equals("s")) {
                    accessible = true;
                }
            }

            // Nodes are read in order, so i is the node ID.
            nodes[i] = new Node(i, elevation, x, y, accessible);

            lineScanner.close();
        }

        return nodes;
    }

    private Lift[] readLifts(Node[] nodes) {
        // The first number specifies how many lifts to read.
        int liftCount = Integer.parseInt(readNonEmptyLine());

        Lift[] lifts = new Lift[liftCount];

        for(int i = 0; i < liftCount; i ++) {
            // Scanner for the description of one lift.
            Scanner lineScanner = createLineScanner(readNonEmptyLine());

            // Lift description: start ID, end ID, departure interval, capacity,
            // and travel time.
            int startId = lineScanner.nextInt();
            int endId = lineScanner.nextInt();
            int interval = lineScanner.nextInt();
            int capacity = lineScanner.nextInt();
            int travelTime = lineScanner.nextInt();

            Node start = nodes[startId];
            Node end = nodes[endId];

            // Lifts are read in order, so i is the lift ID.
            lifts[i] = new Lift(
                i,
                start, 
                end, 
                travelTime, 
                interval, 
                capacity);
            
            // Register this lift at its starting node.
            start.addLift(lifts[i]);

            lineScanner.close();

        }

        return lifts;
    }

    private Slope[] readSlopes(Node[] nodes) {
        // The first number specifies how many slopes to read.
        int slopeCount = Integer.parseInt(readNonEmptyLine());

        Slope[] slopes = new Slope[slopeCount];

        for (int i = 0; i < slopeCount; i++) {
            // Scanner for the description of one slope.
            Scanner lineScanner = createLineScanner(readNonEmptyLine());

            // Slope description: start ID, end ID, difficulty level, travel
            // time, base surface smoothness, and wear resistance.
            int startId = lineScanner.nextInt();
            int endId = lineScanner.nextInt();
            int difficultyLevel = lineScanner.nextInt();
            int travelTime = lineScanner.nextInt();
            double baseSurfaceSmoothness = lineScanner.nextDouble();
            double wearResistance = lineScanner.nextDouble();

            Node start = nodes[startId];
            Node end = nodes[endId];

            // Slopes are read in order, so i is the slope ID.
            slopes[i] = new Slope(
                    i,
                    start,
                    end,
                    travelTime,
                    difficultyLevel,
                    baseSurfaceSmoothness,
                    wearResistance
            );

            // Register this slope at its starting node.
            start.addSlope(slopes[i]);

            lineScanner.close();
        }

        return slopes;
    }

    private Athlete[] readAthletes(Node[] nodes) {
        // The first number specifies how many athlete groups to read.
        int groupCount = Integer.parseInt(readNonEmptyLine());

        // The total number of athletes is not yet known. Start with a temporary
        // capacity and grow the array when needed.
        Athlete[] athletes = new Athlete[128];
        int athleteCount = 0;

        for (int i = 0; i < groupCount; i++) {
            // Scanner for the first line describing an athlete group.
            Scanner firstLine = createLineScanner(readNonEmptyLine());

            // First line: group size, skill level, spontaneity, and "s" if the
            // group should be tracked.
            int groupSize = firstLine.nextInt();
            int skillLevel = firstLine.nextInt();
            double spontaneity = firstLine.nextDouble();

            boolean trackedGroup = false;

            // A trailing "s" marks a tracked group.
            if (firstLine.hasNext()) {
                String marker = firstLine.next();

                if (marker.equals("s")) {
                    trackedGroup = true;
                }
            }

            firstLine.close();

            // Scanner for the second line describing an athlete group.
            Scanner secondLine = createLineScanner(readNonEmptyLine());

            // Second line: weights for difficulty matching and surface
            // smoothness.
            double difficultyWeight = secondLine.nextDouble();
            double surfaceWeight = secondLine.nextDouble();

            secondLine.close();

            // Scanner for the third line describing an athlete group.
            Scanner thirdLine = createLineScanner(readNonEmptyLine());

            // Third line: starting node ID, the first athlete's start time, and
            // the interval between starts for groups with more than one
            // athlete.
            int startNodeId = thirdLine.nextInt();
            String firstStartClockTime = thirdLine.next();

            // Convert the first athlete's start time to seconds since 9:00.
            int firstStartTime = 
                    Time.hmsToSeconds(firstStartClockTime);

            // The start interval is supplied only for groups with more than one
            // athlete. Use zero for a single athlete to simplify the
            // calculation.
            int startInterval = 0;

            if (thirdLine.hasNextInt()) {
                startInterval = thirdLine.nextInt();
            }

            thirdLine.close();

            Node startNode = nodes[startNodeId];

            // Create the specified number of athletes using the group
            // description.
            for (int j = 0; j < groupSize; j++) {
                // Grow the athlete array if it is full.
                if (athleteCount == athletes.length) {
                    // Double the array capacity to avoid frequent resizing
                    // without wasting too much memory.
                    Athlete[] expanded = new Athlete[athletes.length * 2];

                    for (int k = 0; k < athletes.length; k++) {
                        expanded[k] = athletes[k];
                    }

                    athletes = expanded;

                }

                // Calculate the start time using the interval.
                int departureTime = firstStartTime 
                                    + j * startInterval;

                // Athletes are created in order, so the current count is the
                // new athlete's ID.
                athletes[athleteCount] = new Athlete(
                        athleteCount,
                        skillLevel,
                        spontaneity,
                        trackedGroup,
                        difficultyWeight,
                        surfaceWeight,
                        startNode,
                        departureTime
                );

                athleteCount++;
            }
        }

        // Return an array of the exact size, without unused entries.
        Athlete[] result = new Athlete[athleteCount];

        for (int i = 0; i < athleteCount; i++) {
            result[i] = athletes[i];
        }

        return result;
    }
}
