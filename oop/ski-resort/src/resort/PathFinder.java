package resort;

import java.util.Set;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;

// Find shortest paths from a given node.

public class PathFinder {
    
    // Starting node for shortest-path searches.
    private Node start;

    // Nodes visited during BFS.
    private Set<Node> visited;

    // Distance from the starting node, measured in connections.
    private Map<Node, Integer> distance;

    // Connection used to reach each node for the first time.
    private Map<Node, Connection> previousConnection;

    public PathFinder(Node start) {
        this.start = start;

        this.visited = new HashSet<>();
        this.distance = new HashMap<>();
        this.previousConnection = new HashMap<>();
    }

    // Search the graph using BFS.
    public void bfs() {
        Queue<Node> queue = new ArrayDeque<>();

        visited.add(start);
        distance.put(start, 0);
        queue.add(start);

        while(!queue.isEmpty()) {
            Node current = queue.remove();

            for(Slope slope: current.getSlopes()) {
                tryVisit(current, slope, queue);
            }

            for(Lift lift: current.getLifts()) {
                tryVisit(current, lift, queue);
            }
        }
    }

    // Visit an unvisited node during BFS.
    private void tryVisit(
        Node current,
        Connection connection,
        Queue<Node> queue
    ) {
        // The next node is the connection's destination.
        Node nextNode = connection.getEnd();

        // Skip nodes that have already been visited.
        if(visited.contains(nextNode)) {
            return;
        }

        visited.add(nextNode);

        // The next node's distance is one greater than the current node's
        // distance.
        distance.put(nextNode, distance.get(current) + 1);

        // Remember the connection used to reach the next node for the first
        // time.
        previousConnection.put(nextNode, connection);

        // Enqueue the next node for processing.
        queue.add(nextNode);
    }

    // Reconstruct a shortest path from the starting node to the given node.
    public List<Connection> reconstructPathTo(Node target) {
        List<Connection> path = new ArrayList<>();

        Node current = target;

        while(current != start) {
            Connection connection = previousConnection.get(current);

            path.add(connection);

            // Move back to the node where this connection starts.
            current = connection.getStart();
        }

        Collections.reverse(path);

        return path;
    }

    // Build a plan to reach the slope's starting node and then ski down it.
    public List<Connection> reconstructPlanToSlope(Slope slope) {
        List<Connection> plan = reconstructPathTo(slope.getStart());

        plan.add(slope);

        return plan;
    }

    public int getDistanceTo(Node node) {
        return distance.get(node);
    }
}
