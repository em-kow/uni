package simulation;

import java.util.PriorityQueue;
import java.util.List;

import data.SimulationData;
import athletes.Athlete;
import util.Time;
import resort.Lift;
import events.LiftDeparture;
import events.SkiDayStart;
import events.Event;

public class Simulation {
    
    // No new rides start from 15:00 onwards. Time is measured in seconds since
    // 9:00, so this is 6 * 60 * 60 seconds.
    private static final int RIDING_END = 6 * 60 * 60;

    // The simulation and lift operations end at 16:00. Time is measured in
    // seconds since 9:00, so this is 7 * 60 * 60 seconds.
    private static final int SIMULATION_END = 7 * 60 * 60;

    private SimulationData data;

    // Queue of scheduled events with their insertion order. Compare events by
    // time first, then by insertion order to break ties.
    private PriorityQueue<EventQueueEntry> eventQueue;

    // Insertion order of the next event added to the queue.
    private int nextInsertionOrder;

    public Simulation(SimulationData data) {
        this.data = data;

        this.eventQueue = new PriorityQueue<>();
        this.nextInsertionOrder = 0;

        // Initialize a new simulation with its starting events.
        addInitialEvents();
    }

    // Add each athlete's ski day start and the first departure of every lift.
    private void addInitialEvents() {
        addSkiDayStarts();

        addFirstLiftDepartures();
    }

    private void addSkiDayStarts() {
        List<Athlete> athletes = data.getAthletes();

        for (Athlete athlete: athletes) {
            addEvent(new SkiDayStart(
                    athlete.getStartTime(),
                    athlete
            ));
        }
    }

    private void addFirstLiftDepartures() {
        List<Lift> lifts = data.getLifts();

        for (Lift lift: lifts) {
            // The first lift departures occur at the start of the simulation.
            addEvent(new LiftDeparture(
                    0,
                    lift
            ));
        }
    }

    public void enqueueEvent(Event event) {
        eventQueue.add(new EventQueueEntry(
            event, nextInsertionOrder
        ));

        nextInsertionOrder++;
    }

    // Add an event to the simulation.
    public void addEvent(Event event) {
        // Let the event decide whether it can enter the queue, since different
        // event types have different scheduling cutoffs.
        event.tryAddToSimulation(this);

    }

    // Check whether the time is before the cutoff for starting rides.
    public boolean isBeforeRidingEnd(int time) {
        return time < RIDING_END;
    }

    // Check whether the time is within lift operating hours and the simulation
    // is still running.
    public boolean isBeforeSimulationEnd(int time) {
        return time < SIMULATION_END;
    }

    public void run() {
        // Process events until the queue is empty.
        while(!eventQueue.isEmpty()) {
            // Remove and execute the first event in the queue.
            Event event = eventQueue.remove().event;
            event.execute(this);
        }

        finalizeStatistics();
    }

    // Include the final interval needed to calculate each lift's average queue
    // length.
    private void finalizeStatistics() {
        for(Lift lift: data.getLifts()) {
            lift.finalizeQueueStatistics(RIDING_END);
        }
    }

    // Print an event message with its timestamp.
    public void print(int time, String message) {
        System.out.println(Time.secondsToHms(time) + ": " + message);
    }

    // Use the statistics printer to report how many rides were made on each
    // slope and lift.
    public void printStatistics() {
        StatisticsPrinter.print(data);
    }

    // Helper class storing an event and its insertion order in the event queue.
    private static class EventQueueEntry implements 
                                Comparable<EventQueueEntry>{
        private Event event;
        private int insertionOrder;

        private EventQueueEntry(Event event, int insertionOrder) {
            this.event = event;
            this.insertionOrder = insertionOrder;
        }

        @Override
        public int compareTo(EventQueueEntry other) {
            int timeComparison = Integer.compare(
                this.event.getTime(), other.event.getTime()
            );

            if(timeComparison != 0) {
                return timeComparison;
            }

            return Integer.compare(this.insertionOrder, other.insertionOrder);

        }
    }
}
