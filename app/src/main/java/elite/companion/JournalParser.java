package elite.companion;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import elite.companion.model.MiningRefinedDTO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;


public class JournalParser {
    private final Gson gson = new GsonBuilder().setLenient().create(); // Handles malformed JSON
    private final EventBus bus;
    private final Path journalDir = Paths.get(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous");

    public JournalParser(EventBus bus) {
        this.bus = bus;
    }

    public void startReading() throws IOException, InterruptedException {
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

            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile.toFile()))) {
                reader.skip(lastPosition);
                String line;
                while ((line = reader.readLine()) != null) {
                    JsonObject event = gson.fromJson(line, JsonObject.class);
                    if (event != null && event.has("event")) {
                        String eventType = event.get("event").getAsString();
                        switch (eventType) {
                            case "MiningRefined":
                                bus.post(gson.fromJson(event, MiningRefinedDTO.class));
                                break;
/*
                            case "ProspectedAsteroid":
                                bus.post(gson.fromJson(event, ProspectedAsteroidDTO.class));
                                break;
                            case "LaunchDrone":
                                bus.post(gson.fromJson(event, LaunchDroneDTO.class));
                                break;
*/
                            // Add more cases (e.g., "Scan" for combat) as needed
                            default:
                                // Ignore noise (e.g., "Music", "ReceiveText")
                                break;
                        }
                    }
                    lastPosition += line.length() + 1; // Account for newline
                }
            } catch (IOException e) {
                System.err.println("Error reading journal: " + e.getMessage());
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