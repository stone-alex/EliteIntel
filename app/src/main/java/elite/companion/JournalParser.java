package elite.companion;

import com.google.gson.*;
import elite.companion.comms.VoiceGenerator;
import elite.companion.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public class JournalParser {
    private static final Logger log = LoggerFactory.getLogger(JournalParser.class);
    public static final int THRESHOLD = 10; //Seconds
    public static final int THRESHOLD_LONG = 60; //Seconds
    private final Gson gson = new GsonBuilder().setLenient().create();

    private final Path journalDir = Paths.get(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous");

    public void startReading() throws IOException, InterruptedException {
        log.info("Starting Journal Parser");

        WatchService watchService = FileSystems.getDefault().newWatchService();
        journalDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

        Path currentFile = getLatestJournalFile();
        long lastPosition = 0;
        VoiceGenerator.getInstance().speak("Monitoring Journal");
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
                        JsonElement element = JsonParser.parseString(line);
                        if (!element.isJsonObject()) {
                            lastPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                            continue;
                        }

                        JsonObject event = element.getAsJsonObject();
                        if (event.has("event")) {
                            String eventType = event.get("event").getAsString();
                            String eventTimestamp = event.get("timestamp").getAsString();
                            log.info("Processing Journal Event: {} {}", eventType, event.toString());
                            switch (eventType) {
                                case "LoadGame":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, LoadGameEvent.class));
                                    break;
                                case "Commander":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, CommanderEvent.class));
                                    break;
                                case "MiningRefined":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, MiningRefinedEvent.class));
                                    break;
                                case "ProspectedAsteroid":
                                    if (isRecent(eventTimestamp, THRESHOLD_LONG)) EventBusManager.publish(gson.fromJson(event, ProspectedAsteroidEvent.class));
                                    break;
                                case "LaunchDrone":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, LaunchDroneEvent.class));
                                    break;
                                case "CarrierStats":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, CarrierStatsEvent.class));
                                    break;
                                case "Powerplay":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, PowerplayEvent.class));
                                    break;
                                case "Statistics":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, StatisticsEvent.class));
                                    break;
                                case "ReceiveText":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, ReceiveTextEvent.class));
                                    break;
                                case "FSSSignalDiscovered":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, FSSSignalDiscoveredEvent.class));
                                    break;
                                case "FSDJump":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, FSDJumpEvent.class));
                                    break;
                                case "Touchdown":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, TouchdownEvent.class));
                                    break;
                                case "Liftoff":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, LiftoffEvent.class));
                                    break;
                                case "CarrierJumpRequest":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, CarrierJumpRequestEvent.class));
                                    break;
                                case "FSDTarget":
                                    /*if (isRecent(eventTimestamp, THRESHOLD)) */
                                    EventBusManager.publish(gson.fromJson(event, FSDTargetEvent.class));
                                    break;
                                case "Scan":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, ScanEvent.class));
                                    break;
                                case "ShipTargeted":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, ShipTargetedEvent.class));
                                    break;
                                case "Loadout":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, LoadoutEvent.class));
                                    break;
                                case "SwitchSuitLoadout":
                                    if (isRecent(eventTimestamp, THRESHOLD)) EventBusManager.publish(gson.fromJson(event, SwitchSuitLoadoutEvent.class));
                                    break;

                                default:
                                    break;
                            }
                        }
                    } catch (JsonSyntaxException jse) {
                        System.err.println("Skipping malformed journal line: " + jse.getMessage());
                    }

                    lastPosition += line.getBytes(StandardCharsets.UTF_8).length + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
                }
            } catch (IOException e) {
                VoiceGenerator.getInstance().speak("Error reading journal: {}", e.getMessage());
                log.error("Error reading journal: {}", e.getMessage());
            }

            key.reset();
        }
    }

    private boolean isRecent(String timestamp, long secondsThreshold) {
        try {
            Instant eventTime = Instant.parse(timestamp);
            Instant now = Instant.now();
            return !eventTime.isBefore(now.minus(secondsThreshold, ChronoUnit.SECONDS));
        } catch (Exception e) {
            log.warn("Invalid timestamp format: {}", timestamp);
            return false;
        }
    }

    private Path getLatestJournalFile() throws IOException {
        return Files.list(journalDir)
                .filter(p -> p.toString().endsWith(".log"))
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow(() -> new IOException("No journal files found in " + journalDir));
    }
}