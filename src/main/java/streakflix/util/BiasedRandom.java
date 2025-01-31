package streakflix.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class BiasedRandom {

    private Random random = new Random();

    public int getRandom() {
        Random random = new Random();
        int[] weights = new int[21];

        // Assign higher weight to 0
        weights[0] = 30; // Heavy bias for 0
        for (int i = 1; i < weights.length; i++) {
            weights[i] = 1; // Lower weight for other numbers
        }

        int randomNumber = getBiasedRandomNumber(weights, random);
        System.out.println("Biased Random Number: " + randomNumber);

        return randomNumber;
    }

    public static int getBiasedRandomNumber(int[] weights, Random random) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        int randomIndex = random.nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomIndex < cumulativeWeight) {
                return i;
            }
        }
        return -1; // Should never reach here
    }

    public boolean biasedFalse() {
        return random.nextInt(100) > 65;
    }

    public int getOTTRandomPlatform(int exist){
        int r = random.nextInt(4) + 1;
        while(r == exist && exist != 0){
            r = random.nextInt(4) + 1;
        }
        return r;
    }

    public int getRandomNumber(int range){
        return random.nextInt(range) + 1;
    }


}