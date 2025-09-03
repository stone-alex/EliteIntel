package elite.companion.gameapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.gameapi.journal.EventRegistry;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.ui.event.AppLogEvent;
import elite.companion.util.EventBusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class JournalParser implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(JournalParser.class);
    private final Path journalDir = Paths.get(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous");

    private Thread processingThread;
    private volatile boolean isRunning;

    public synchronized void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("JournalParser is already running");
            return;
        }
        isRunning = true;
        processingThread = new Thread(this, "JournalParserThread");
        processingThread.start();
        log.info("JournalParser started");
    }

    public synchronized void stop() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("JournalParser is not running");
            return;
        }
        isRunning = false;
        processingThread.interrupt();
        try {
            processingThread.join(5000); // Wait up to 5 seconds for clean shutdown
            log.info("JournalParser stopped");
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for JournalParser to stop", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        processingThread = null;
    }

    @Override
    public void run() {
        try {
            startReading();
        } catch (IOException e) {
            log.error("IOException in JournalParser", e);
            EventBusManager.publish(new AppLogEvent("JournalParser failed: " + e.getMessage()));
        } catch (InterruptedException e) {
            log.info("JournalParser interrupted, shutting down");
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            log.error("Unexpected error in JournalParser", e);
            EventBusManager.publish(new AppLogEvent("Unexpected error in JournalParser: " + e.getMessage()));
        }
    }

    private void startReading() throws IOException, InterruptedException {
        log.info("Starting Journal Parser");

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            journalDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

            Path currentFile = getLatestJournalFile();
            long lastPosition = 0;
            log.info("Monitoring {}", currentFile.getFileName());

            while (isRunning) {
                // Wait for file events with a timeout to check isRunning and interruptions
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) {
                    if (Thread.currentThread().isInterrupted() || !isRunning) {
                        log.info("Shutting down JournalParser due to interruption or stop signal");
                        return;
                    }
                    continue;
                }

                Path latestFile = getLatestJournalFile();
                if (!latestFile.equals(currentFile)) {
                    lastPosition = 0;
                    currentFile = latestFile;
                    log.info("Switched to new journal file: {}", currentFile.getFileName());
                }

                try (BufferedReader reader = Files.newBufferedReader(currentFile, StandardCharsets.UTF_8)) {
                    reader.skip(lastPosition);
                    String line;
                    boolean isFirstLineAfterSeek = (lastPosition == 0);

                    while ((line = reader.readLine()) != null) {
                        if (isFirstLineAfterSeek) {
                            if (line.length() > 0 && line.charAt(0) == '\uFEFF') {
                                line = line.substring(1);
                            }
                            isFirstLineAfterSeek = false;
                        }

                        if (line.isBlank()) {
                            lastPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                            continue;
                        }

                        try {
                            String sanitizedLine = line.replaceAll("[\\p{Cntrl}\\p{Cc}\\p{Cf}]", "").trim();
                            JsonElement element = JsonParser.parseString(sanitizedLine);
                            if (!element.isJsonObject()) {
                                lastPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                                continue;
                            }

                            JsonObject eventJson = element.getAsJsonObject();
                            if (eventJson.has("event")) {
                                String eventType = eventJson.get("event").getAsString();
                                BaseEvent event = EventRegistry.createEvent(eventType, eventJson);
                                if (event != null) {
                                    EventBusManager.publish(event);
                                    EventBusManager.publish(new AppLogEvent("Processing Event: " + eventType));
                                    log.info("Processing Journal Event: {} {}", eventType, event.toJsonObject());
                                } else {
                                    log.warn("Skipping event: {}", eventType);
                                }
                            }
                        } catch (Exception e) {
                            log.error("Failed to parse journal entry: {}", line, e);
                        }

                        lastPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                    }
                } catch (IOException e) {
                    EventBusManager.publish(new VoiceProcessEvent("Error reading journal: " + e.getMessage()));
                    log.error("Error reading journal: {}", e.getMessage(), e);
                }

                key.reset();
            }
        }
    }

    private Path getLatestJournalFile() throws IOException {
        return Files.list(journalDir)
                .filter(p -> p.toString().endsWith(".log"))
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow(() -> new IOException("No journal files found in " + journalDir));
    }
}