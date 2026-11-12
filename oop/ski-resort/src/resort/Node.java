package resort;

import java.util.ArrayList;
import java.util.List;

public class Node {
    
    // Node ID.
    private int id;

    // Node elevation.
    private int elevation;

    // Node coordinates.
    private int x;
    private int y;

    // Whether the node is accessible.
    private boolean accessible;

    // Slopes and lifts leaving this node.
    private List<Slope> slopes;
    private List<Lift> lifts;

    public Node(int id, int elevation, int x, int y, boolean accessible) {
        this.id = id;
        this.elevation = elevation;
        this.x = x;
        this.y = y;
        this.accessible = accessible;

        this.slopes = new ArrayList<>();
        this.lifts = new ArrayList<>();
    }

    public void addSlope(Slope slope) {
        slopes.add(slope);
    }

    public void addLift(Lift lift) {
        lifts.add(lift);
    }

    public List<Slope> getSlopes() {
        return slopes;
    }

    public List<Lift> getLifts() {
        return lifts;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isAccessible() {
        return accessible;
    }
}
