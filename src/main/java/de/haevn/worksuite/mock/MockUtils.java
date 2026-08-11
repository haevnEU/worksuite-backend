package de.haevn.worksuite.mock;

import java.security.SecureRandom;
import java.time.Year;
import java.util.Random;

public class MockUtils {
    private static final Random RANDOM = new SecureRandom();

    public static String createRandomName() {
        return "Name" + ((int) (Math.random() * 10000));
    }

    public static String createRandomMail() {
        return "mail" + ((int) (Math.random() * 10000)) + "@example.com";
    }

    public static String createRandomPhoneNumber() {
        return "0" + ((int) (Math.random() * 1000000000));
    }

    public static String createMelo() {
        long millis = System.currentTimeMillis();
        long nanoPart = (System.nanoTime() / 1_000L) % 1_000_000L;
        long randomPart = RANDOM.nextLong(1_000_000_000_000L);
        return String.format("DE%013d%06d%012d", millis, nanoPart, randomPart);
    }

    public static String nextMeterNumber() {
        long timePart = (System.currentTimeMillis() / 1000L) % 100_000L;
        int randomPart = RANDOM.nextInt(100_000);
        return String.format("1ITR%05d%05d", timePart, randomPart);
    }

    public static String getYearFromThis(final int offset) {
        return String.valueOf(Year.now().getValue() + offset);
    }

    public static String createRandomValue() {
        return String.valueOf(RANDOM.nextInt(10000));
    }

    public static String createRandomDate() {
        int year = 2020 + RANDOM.nextInt(10);
        int month = 1 + RANDOM.nextInt(12);
        int day = 1 + RANDOM.nextInt(28);
        return String.format("%02d.%02d.%04d", year, month, day);
    }
}
