package data;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import util.Time;
import util.RandomSource;
import resort.Slope;
import resort.Node;
import resort.Lift;
import athletes.Athlete;
import athletes.CollectorAthlete;
import athletes.LocalAthlete;
import athletes.GreedyAthlete;

import java.util.Locale;

public class DataReader {

    // Scanner that reads input line by line.
    private Scanner inputScanner;

    // Random source used by the program.
    private RandomSource randomSource;

    public DataReader() {
        this.inputScanner = new Scanner(System.in);

        this.randomSource = new RandomSource();
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
        List<Node> nodes = readNodes();
        List<Lift> lifts = readLifts(nodes);
        List<Slope> slopes = readSlopes(nodes);
        List<Athlete> athletes = readAthletes(nodes, slopes);

        // Return an object containing all simulation data.
        return new SimulationData(nodes, lifts, slopes, athletes);
    }

    private List<Node> readNodes() {
        // The first number specifies how many nodes to read.
        int nodeCount = Integer.parseInt(readNonEmptyLine());

        // Create a list for all nodes.
        List<Node> nodes = new ArrayList<>();

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
            nodes.add(new Node(i, elevation, x, y, accessible));

            lineScanner.close();
        }

        return nodes;
    }

    private List<Lift> readLifts(List<Node> nodes) {
        // The first number specifies how many lifts to read.
        int liftCount = Integer.parseInt(readNonEmptyLine());

        List<Lift> lifts = new ArrayList<>();

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

            Node start = nodes.get(startId);
            Node end = nodes.get(endId);

            // Lifts are read in order, so i is the lift ID.
            Lift lift = new Lift(
                i,
                start, 
                end, 
                travelTime, 
                interval, 
                capacity
            );
            
            lifts.add(lift);
            
            // Register this lift at its starting node.
            start.addLift(lift);

            lineScanner.close();

        }

        return lifts;
    }

    private List<Slope> readSlopes(List<Node> nodes) {
        // The first number specifies how many slopes to read.
        int slopeCount = Integer.parseInt(readNonEmptyLine());

        List<Slope> slopes = new ArrayList<>();

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

            Node start = nodes.get(startId);
            Node end = nodes.get(endId);

            // Slopes are read in order, so i is the slope ID.
            Slope slope = new Slope(
                i,
                start,
                end,
                travelTime,
                difficultyLevel,
                baseSurfaceSmoothness,
                wearResistance
            );
            slopes.add(slope);

            // Register this slope at its starting node.
            start.addSlope(slope);

            lineScanner.close();
        }

        return slopes;
    }

    private List<Athlete> readAthletes(
        List<Node> nodes, 
        List<Slope> slopes
    ) {
        // The first number specifies how many athlete groups to read.
        int groupCount = Integer.parseInt(readNonEmptyLine());

        List<Athlete> athletes = new ArrayList<>();

        for (int i = 0; i < groupCount; i++) {
            // Scanner for the first line describing an athlete group.
            Scanner firstLine = createLineScanner(readNonEmptyLine());

            // First line: group size, skill level, spontaneity, boredom factor,
            // athlete type, and "s" if the group should be tracked.
            int groupSize = firstLine.nextInt();
            int skillLevel = firstLine.nextInt();
            double spontaneity = firstLine.nextDouble();
            double boredomFactor = firstLine.nextDouble();

            String athleteType = firstLine.next();

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

            // Second line: weights for difficulty matching, surface smoothness,
            // and boredom.
            double difficultyWeight = secondLine.nextDouble();
            double surfaceWeight = secondLine.nextDouble();
            double boredomWeight = secondLine.nextDouble();

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

            Node startNode = nodes.get(startNodeId);

            // Create the specified number of athletes using the group
            // description.
            for (int j = 0; j < groupSize; j++) {

                // Calculate the start time using the interval.
                int departureTime = firstStartTime 
                                    + j * startInterval;

                // Athletes are created in order, so the current count is the
                // new athlete's ID.
                int athleteId = athletes.size();

                athletes.add(createAthlete(
                        athleteType,
                        athleteId,
                        skillLevel,
                        spontaneity,
                        boredomFactor,
                        trackedGroup,
                        difficultyWeight,
                        surfaceWeight,
                        boredomWeight,
                        startNode,
                        departureTime,
                        slopes
                ));
            }
        }

        return athletes;
    }

    // Create the appropriate athlete subclass based on its type.
    private Athlete createAthlete(
        String athleteType,
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
        List<Slope> slopes
    ) {
        if(athleteType.equals("L")) {
            return new LocalAthlete(
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

        if(athleteType.equals("Z")) {
            return new GreedyAthlete(
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
                slopes
            );
        }

        if(athleteType.equals("K")) {
            return new CollectorAthlete(
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
                slopes
            );
        }
        
        // Throw an exception if the athlete type is not L, Z, or K.
        throw new IllegalArgumentException(
            "Unknown athlete type " + athleteType
        );
    }
}
