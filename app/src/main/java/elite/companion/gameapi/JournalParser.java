package elite.companion.gameapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elite.companion.comms.voice.VoiceGenerator;
import elite.companion.gameapi.journal.EventRegistry;
import elite.companion.gameapi.journal.events.BaseEvent;
import elite.companion.util.EventBusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;

public class JournalParser {
    private static final Logger log = LoggerFactory.getLogger(JournalParser.class);
    private final Path journalDir = Paths.get(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous");

    public void startReading() throws IOException, InterruptedException {
        log.info("Starting Journal Parser");

        WatchService watchService = FileSystems.getDefault().newWatchService();
        journalDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

        Path currentFile = getLatestJournalFile();
        long lastPosition = 0;
        System.out.println("Monitoring " + currentFile.getFileName());

        while (true) {
            WatchKey key = watchService.take();
            Path latestFile = getLatestJournalFile();

            if (!latestFile.equals(currentFile)) {
                lastPosition = 0;
                currentFile = latestFile;
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

    private Path getLatestJournalFile() throws IOException {
        return Files.list(journalDir)
                .filter(p -> p.toString().endsWith(".log"))
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow(() -> new IOException("No journal files found in " + journalDir));
    }
}