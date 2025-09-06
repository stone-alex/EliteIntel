package elite.companion.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The TimestampFormatter class provides utility methods for formatting timestamps.
 * It converts ISO 8601 formatted timestamp strings into a more user-friendly, human-readable format.
 */
public class TimestampFormatter {
    private static final DateTimeFormatter HUMAN_READABLE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d 'at' HH:mm");

    /**
     * Formats an ISO 8601 timestamp string to a human-readable format.
     *
     * @param isoTimestamp the ISO 8601 formatted timestamp to be parsed and formatted
     * @param useLocalTime a flag indicating whether to convert the timestamp to local time for formatting
     * @return a formatted timestamp string in a human-readable format or "unknown" if an error occurs
     */
    public static String formatTimestamp(String isoTimestamp, boolean useLocalTime) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(isoTimestamp, DateTimeFormatter.ISO_DATE_TIME);
            if (useLocalTime) {
                zdt = zdt.withZoneSameInstant(ZoneId.of("America/Los_Angeles")); // PDT
            }
            return zdt.format(HUMAN_READABLE_FORMATTER);
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }
}