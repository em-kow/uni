# Ski Resort Simulation

A ski resort simulation written in Java 21. The program models skiers moving between slopes and ski lifts, choosing routes and waiting in lift queues. Each skier follows one of three strategies, taking into account factors such as slope difficulty, surface conditions and previous rides.

The simulation is event-driven. Instead of updating every skier at fixed time intervals, it processes scheduled events in chronological order.

## Resort model

The resort is represented as a directed graph. Nodes correspond to locations, while connections represent either ski slopes or lifts.

Both types of connections have a starting node, an ending node, a travel time and a ride counter. They also have their own properties:

- **Slopes** have a difficulty level, surface smoothness and wear resistance. Their surface conditions gradually deteriorate as more skiers use them.
- **Lifts** have a departure interval and passenger capacity. Skiers wait in FIFO queues and board at scheduled departure times, up to the lift's capacity.

## Skier behaviour

Each skier has a skill level, individual preferences and a history of visited slopes. The attractiveness of a slope depends on three factors: how well its difficulty matches the skier's skill, its current surface smoothness and the skier's boredom with that slope.

The implementation includes three types of skiers:

- **LocalAthlete** chooses the most attractive slope available directly from the current location or reachable by taking one lift.
- **GreedyAthlete** looks for the most attractive slope across the entire resort and plans a route to it.
- **CollectorAthlete** prefers slopes they have visited the fewest times. When several slopes qualify, the skier considers their distance and attractiveness.

Skiers can also make random decisions, controlled by an individual spontaneity parameter.

For the two planning strategies, `PathFinder` uses BFS to find shortest paths through the resort graph. Path length is measured in the number of connections rather than travel time. The resulting route is stored and followed until the skier needs to make another decision.

## Event scheduling

The simulation uses a `PriorityQueue` to process events in chronological order. Events with the same timestamp are executed in the order in which they were added.

The main events include starting a skiing day, entering a lift queue, lift departures, and starting or completing a ride.

For example, when a skier chooses a lift, they enter its waiting queue. At the next departure, the lift boards up to its capacity and schedules ride completion events for its passengers. Once a skier reaches the destination node, they can choose another connection.

The skiing day starts at 9:00. New rides can begin before 15:00, while the simulation continues processing eligible events until 16:00.

## Statistics

The program prints statistics after the simulation, including:

- Number of rides and final surface smoothness for each slope.
- Maximum and average queue lengths for each lift.
- Number of passengers transported and lift seat occupancy.

It also supports tracking selected skiers and printing their activities with timestamps.