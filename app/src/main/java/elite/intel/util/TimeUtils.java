package elite.intel.util;

public class TimeUtils {
    private static final int SECONDS_PER_MINUTE = 60;

    /**
     * Converts a time duration in seconds (as a double) to a human-readable string
     * in the format "N minutes remaining".
     *
     * @param seconds the time duration in seconds
     * @return a string in the format "N minutes remaining"
     */
    public static String secondsToMinutesRemainingString(Double seconds) {
        if (seconds == null) {
            return "";
        }
        int minutes = (int) (seconds / SECONDS_PER_MINUTE);
        return minutes + " minutes remaining";
    }
}
