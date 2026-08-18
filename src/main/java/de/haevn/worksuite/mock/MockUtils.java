package de.haevn.worksuite.mock;

import java.security.SecureRandom;
import java.time.Year;
import java.util.List;
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


    public static String createRandomWord(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = (char) ('a' + RANDOM.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
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

    public static String getRandomDate() {
        int year = 2020 + RANDOM.nextInt(10);
        int month = 1 + RANDOM.nextInt(12);
        int day = 1 + RANDOM.nextInt(28);
        return String.format("%02d.%02d.%04d", day, month, year);
    }

    public static String getObis() {
        List<String> obisList = List.of("");
        return obisList.get((int) (Math.random() * obisList.size()));
    }

    public static String createRandomValue() {
        return String.valueOf(RANDOM.nextInt(10000));
    }

    public static String createRandomValue(int max) {
        return String.valueOf(RANDOM.nextInt(max));
    }

    public static String getGLN() {
        final List<String> glnList = List.of("9904431000004");
        return glnList.get((int) (Math.random() * glnList.size()));
    }

    public static String getTarifID() {
        List<String> tarifIDs = List.of("1", "2", "7");
        return tarifIDs.get((int) (Math.random() * tarifIDs.size()));
    }



    public static String createRandomDate() {
        int year = 2020 + RANDOM.nextInt(10);
        int month = 1 + RANDOM.nextInt(12);
        int day = 1 + RANDOM.nextInt(28);
        return String.format("%02d.%02d.%04d", year, month, day);
    }


    public static String getDeviceType() {
        List<String> deviceTypes =
            List.of("1-Tarif-Drehstromzähler", "1-Tarif elektr. Drehstromzähler", "1-Tarif-Messwandler-Drehstromzähler",
                "1-Tarif-Wechselstromzähler", "1-Tarif-Drehstrom-Zweirichtungszähler",
                "1-Tarif elektronischer Messwandler-Drehstromzähler", "Doppeltarif-Drehstromzähler",
                "elektronischer Doppeltarif-Drehstromzähler", "Doppeltarif-Messwandler-Drehstromzähler",
                "elektronischer Doppeltarif-Messwandler-Drehstromzähler", "Doppeltarif-Wechselstromzähler",
                "Zweirichtungszähler", "Lieferzähler");
        return deviceTypes.get((int) (Math.random() * deviceTypes.size()));
    }

    public static String getAffixation() {
        List<String> affixations =
            List.of("Stecktechnik", "Dreipunktzähler", "Wandler", "Sym2", "Prepay", "Hutschiene");
        return affixations.get((int) (Math.random() * affixations.size()));
    }

    public static String getConversion() {
        List<String> conversions = List.of("Direktmessung", "Niederspannung");
        return conversions.get((int) (Math.random() * conversions.size()));
    }


    public static String getSparte() {
        List<String> sparten = List.of("Strom", "Gas", "Wasser", "Wärme");
        return sparten.get((int) (Math.random() * sparten.size()));
    }

    public static String createRandomDateTime() {
        int year = 2020 + RANDOM.nextInt(10);
        int month = 1 + RANDOM.nextInt(12);
        int day = 1 + RANDOM.nextInt(28);
        int hour = RANDOM.nextInt(24);
        int minute = RANDOM.nextInt(60);
        return String.format("%02d.%02d.%04d %02d:%02d", day, month, year, hour, minute);
    }


    public static class Address {
        public static String STREET = "itestWeg";
        public static String HOUSE_NUMBER = "18";
        public static String HOUSE_NUMBER_ADDITION = "a";
        public static String ZIP_CODE = "12345";
        public static String CITY = "itestStadt";
    }
}
