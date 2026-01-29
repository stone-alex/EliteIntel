package elite.intel.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

/*
* TODO: AI Generated Slop
*
*/
public class UnknownEnumLogger {
    private static final String LOG_FILE_NAME = "unknown-missions.log";
    private static final Logger LOGGER = Logger.getLogger(UnknownEnumLogger.class.getName());

    // In‑memory deduplication for the current JVM run
    private static final Set<String> alreadyLogged = Collections.synchronizedSet(new HashSet<>());

    static {
        try {
            // Ensure the log directory exists (relative to the working dir)
            Path logPath = Paths.get(LOG_FILE_NAME).toAbsolutePath();
            Files.createDirectories(logPath.getParent());

            // Configure a simple file handler (append = true, limit = 0 = no rotation)
            Handler fileHandler = new FileHandler(logPath.toString(), true);
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    // We ignore the default LogRecord formatting – we build our own line.
                    return lr.getMessage() + System.lineSeparator();
                }
            });
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false); // suppress console spam
        } catch (IOException e) {
            // If we cannot open the file we fall back to console logging – never crash the app.
            LOGGER.log(Level.WARNING, "Unable to initialise UnknownValueLogger file handler", e);
        }
    }

    /**
     * Writes a line to the log file if the value has not been logged before.
     *
     * @param category  either "MISSION_TYPE" or "MISSION_TARGET"
     * @param rawValue  the exact string that could not be mapped
     */
    public static void log(String category, String rawValue) {
        if (rawValue == null) {
            return; // nothing to log
        }

        // Build a deterministic key for deduplication
        String key = category + "|" + rawValue;

        // Fast‑path check – if we already logged it, skip I/O
        if (!alreadyLogged.add(key)) {
            return;
        }

        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String line = String.format("%s  %s  %s", timestamp, category, rawValue);
        LOGGER.info(line); // the SimpleFormatter will output exactly the line we built
    }

    // Private constructor – utility class
    private UnknownEnumLogger() {}
}
