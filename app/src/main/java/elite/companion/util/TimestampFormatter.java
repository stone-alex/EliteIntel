package elite.companion.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampFormatter {
    private static final DateTimeFormatter HUMAN_READABLE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d 'at' HH:mm");

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