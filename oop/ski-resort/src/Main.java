import data.SimulationData;
import data.DataReader;
import simulation.Simulation;

public class Main {

    public static void main(String[] args) {
        try {
            DataReader reader = new DataReader();
            
            SimulationData data = reader.read();

            Simulation simulation = new Simulation(data);
            simulation.run();
            simulation.printStatistics();
        }
        catch(Exception | AssertionError exception) {
            System.err.println(
                "A program error occurred."
            );
            exception.printStackTrace(System.err);
        }
    }
}