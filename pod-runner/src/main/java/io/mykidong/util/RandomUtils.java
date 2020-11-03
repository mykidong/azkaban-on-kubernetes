package io.mykidong.util;

import java.util.Random;

public class RandomUtils {

    public static String getRandomNumber(int max, int count) {
        Random random = new Random();
        String randomString = "";
        for(int i = 0; i < count; i++) {
            int value = random.nextInt(max);
            randomString += String.valueOf(value);
        }
        return randomString;
    }
}
