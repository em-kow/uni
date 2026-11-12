package util;

import java.util.Random;

public class RandomSource {
    
    private Random random;

    public RandomSource() {
        this.random = new Random();
    }

    // Check whether an event with the given probability occurs.
    public boolean occurs(double probability) {
        return random.nextDouble() < probability;
    }

    // Choose an index from [0, bound).
    public int nextIndex(int bound) {
        return random.nextInt(bound);
    }
}
