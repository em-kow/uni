package util;

public class Time {

    private static final int START_HOUR = 9;

    private Time() {
        // This class contains only static methods.
    }

    // Convert HH:MM:SS to seconds since 9:00.
    public static int hmsToSeconds(String hms) {
        String[] parts = hms.split(":");

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        int secondsSinceMidnight = hours * 3600 + minutes * 60 + seconds;
        int startSeconds = START_HOUR * 3600;

        return secondsSinceMidnight - startSeconds;
    }

    // Convert seconds since the start to HH:MM:SS.
    public static String secondsToHms(int time) {
        int secondsSinceMidnight = START_HOUR * 3600 + time;

        int hours = secondsSinceMidnight / 3600;
        int minutes = (secondsSinceMidnight % 3600) / 60;
        int seconds = secondsSinceMidnight % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}