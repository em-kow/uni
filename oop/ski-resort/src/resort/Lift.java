package resort;

import java.util.ArrayDeque;
import java.util.Queue;

import athletes.Athlete;
import events.LiftQueueEntry;
import events.Event;

public class Lift extends Connection{
    
    // Time interval between boarding passenger groups.
    private int interval;

    // Maximum number of passengers in a group.
    private int capacity;

    // Maximum number of athletes waiting in the queue at the same time.
    private int maxQueueLength;

    // Sum of queue lengths over successive seconds.
    private int queueLengthSum;

    // Time of the most recent queue statistics update.
    private int lastQueueUpdateTime;

    // Total number of available seats across all lift departures.
    private int totalSeatCount;

    // Average queue length after finalizing the statistics.
    private double averageQueueLength;

    // Queue of athletes waiting for the lift.
    private Queue<Athlete> queue;

    public Lift(
            int id,
            Node start,
            Node end,
            int travelTime,
            int interval,
            int capacity
    ) {
        super(id, start, end, travelTime);

        this.interval = interval;
        this.capacity = capacity;

        this.maxQueueLength = 0;
        this.queueLengthSum = 0;
        this.lastQueueUpdateTime = 0;
        this.totalSeatCount = 0;
        this.averageQueueLength = 0.0;

        // Initially no athletes are waiting for the lift.
        this.queue = new ArrayDeque<>();
    }

    @Override
    public Event createEventAfterSelection(
            int time,
            Athlete athlete
    ) {
        return new LiftQueueEntry(time, athlete, this);
    }

    public void enqueue(Athlete athlete, int time) {
        updateQueueStatistics(time);

        queue.add(athlete);

        if(queue.size() > maxQueueLength) {
            maxQueueLength = queue.size();
        }
    }

    // Update queue statistics for the interval during which the queue had its
    // current length.
    private void updateQueueStatistics(int time) {
        int timeSinceLastUpdate = 
                time - lastQueueUpdateTime;

        queueLengthSum += queue.size() * timeSinceLastUpdate;

        lastQueueUpdateTime = time;
    }

    // Include the final time interval when calculating the average queue
    // length.
    public void finalizeQueueStatistics(int ridingEndTime) {
        updateQueueStatistics(ridingEndTime);

        averageQueueLength = (double) queueLengthSum / ridingEndTime;
    }

    public void recordDeparture() {
        totalSeatCount += capacity;
    }

    public int getWaitingCount() {
        return queue.size();
    }

    public int getCapacity() {
        return capacity;
    }

    public Athlete dequeue(int time) {
        updateQueueStatistics(time);

        return queue.remove();
    }
    
    public int getInterval() {
        return interval;
    }

    public int getMaxQueueLength() {
        return maxQueueLength;
    }

    public double getAverageQueueLength() {
        return averageQueueLength;
    }

    public double getOccupancyPercentage() {
        if(totalSeatCount == 0) {
            return 0.0;
        }

        return 100.0 * rideCount / totalSeatCount;
    }

    public int getTotalSeatCount() {
        return totalSeatCount;
    }
}
