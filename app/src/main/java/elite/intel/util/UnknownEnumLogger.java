package elite.intel.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/*
 * Logs unknown enum values (e.g., mission types or targets) to a file, with deduplication to avoid spam.
 */
public class UnknownEnumLogger {

    private static final Logger logger = LogManager.getLogger(UnknownEnumLogger.class);
    private static final Set<String> alreadyLogged = Collections.synchronizedSet(new HashSet<>());

    /**
     * Writes a line to the log file if the value has not been logged before.
     *
     * @param category either "MISSION_TYPE" or "MISSION_TARGET"
     * @param rawValue the exact string that could not be mapped
     */
    public static void log(String category, String rawValue) {
        if (rawValue != null) {
            // Build a deterministic key for deduplication
            String key = category + "|" + rawValue;

            // Fast‑path check – if we already logged it, skip I/O
            if (!alreadyLogged.add(key)) {
                return;
            }

            String timestamp = ISO_INSTANT.format(Instant.now());
            logger.info("{}  {}  {}", timestamp, category, rawValue);
        }
    }
}
