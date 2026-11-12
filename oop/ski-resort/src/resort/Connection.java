package resort;

import data.athletes.Athlete;
import events.Event;


public abstract class Connection {

    // Connection ID.
    protected int id;

    // Node where the connection starts.
    protected Node start;

    // Node where the connection ends.
    protected Node end;

    // Travel time in seconds.
    protected int travelTime;

    // Number of rides made along this connection.
    protected int rideCount;

    public Connection(
            int id,
            Node start, 
            Node end, 
            int travelTime
    ) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.travelTime = travelTime;

        // Initially no rides have been made along this connection.
        this.rideCount = 0;
    }

    // Create an event after the athlete chooses the next connection.
    public abstract Event createEventAfterSelection(
            int time,
            Athlete athlete
    );

    public int getId() {
        return id;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public void incrementRideCount() {
        rideCount++;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public int getRideCount() {
        return rideCount;
    }
}
