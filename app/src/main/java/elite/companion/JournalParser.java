package elite.companion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import elite.companion.events.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class JournalParser {
    private static final Logger log = LoggerFactory.getLogger(JournalParser.class);
    private final Gson gson = new GsonBuilder().setLenient().create(); // Handles malformed JSON


    //TODO: Refactor this to be settable via config file or user interface.
    private final Path journalDir = Paths.get(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous");

    public void startReading() throws IOException, InterruptedException {
        log.info("Starting Journal Parser");
        WatchService watchService = FileSystems.getDefault().newWatchService();
        journalDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

        Path currentFile = getLatestJournalFile();
        long lastPosition = 0;

        while (true) {
            WatchKey key = watchService.take(); // Blocks until file changes
            Path latestFile = getLatestJournalFile();

            if (!latestFile.equals(currentFile)) {
                lastPosition = 0; // Reset position on new file
                currentFile = latestFile;
            }

            // Use UTF-8 explicitly; Elite Dangerous journals are UTF-8 (often with BOM)
            try (BufferedReader reader = Files.newBufferedReader(currentFile, StandardCharsets.UTF_8)) {
                reader.skip(lastPosition);
                String line;
                boolean isFirstLineAfterSeek = (lastPosition == 0);
                while ((line = reader.readLine()) != null) {
                    // Strip UTF-8 BOM if present on the first line
                    if (isFirstLineAfterSeek) {
                        if (line.length() > 0 && line.charAt(0) == '\uFEFF') {
                            line = line.substring(1);
                        }
                        isFirstLineAfterSeek = false;
                    }

                    if (line.isBlank()) {
                        // Advance position and skip empty/whitespace-only lines
                        lastPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                        continue;
                    }

                    try {
                        JsonElement element = JsonParser.parseString(line);
                        if (!element.isJsonObject()) {
                            // Not an object (could be a primitive/array); skip safely
                            lastPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                            continue;
                        }

                        JsonObject event = element.getAsJsonObject();
                        if (event.has("event")) {
                            String eventType = event.get("event").getAsString();
                            log.info("Processing Journal Event: {} {}", eventType, event.toString());
                            switch (eventType) {
                                case "LoadGame":
                                    EventBusManager.publish(gson.fromJson(event, LoadGameEvent.class));
                                    break;
                                case "Commander":
                                    EventBusManager.publish(gson.fromJson(event, CommanderEvent.class));
                                    break;
                                case "MiningRefined":
                                    EventBusManager.publish(gson.fromJson(event, MiningRefinedEvent.class));
                                    break;
                                case "ProspectedAsteroid":
                                    EventBusManager.publish(gson.fromJson(event, ProspectedAsteroidEvent.class));
                                    break;
                                case "LaunchDrone":
                                    EventBusManager.publish(gson.fromJson(event, LaunchDroneEvent.class));
                                    break;
                                case "CarrierStats":
                                    EventBusManager.publish(gson.fromJson(event, CarrierStatsEvent.class));

                                default:
                                    // Ignore noise (e.g., "Music", "ReceiveText")
                                    break;
                            }
                        }
                    } catch (JsonSyntaxException jse) {
                        // Malformed or partial JSON line; skip and continue watching
                        System.err.println("Skipping malformed journal line: " + jse.getMessage());
                    }

                    // Account for bytes read + newline in UTF-8
                    lastPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                }
            } catch (IOException e) {
                log.error("Error reading journal: {}", e.getMessage());
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