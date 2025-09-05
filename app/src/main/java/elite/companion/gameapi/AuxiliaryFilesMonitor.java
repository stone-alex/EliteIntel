package elite.companion.gameapi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import elite.companion.gameapi.gamestate.events.GameEvents;
import elite.companion.ui.event.AppLogEvent;
import elite.companion.util.EventBusManager;
import elite.companion.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Monitors auxiliary JSON files in the Elite Dangerous saved games directory.
 * These files contain event-like JSON data (e.g., Market.json with "event":"Market").
 * The monitor reads them initially, deserializes them into specific Event DTOs,
 * and publishes the DTOs to the event bus. It then watches for modifications and
 * re-publishes updated DTOs.
 * <p>
 * This runs in a separate thread to avoid blocking the main application.
 */
public class AuxiliaryFilesMonitor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(AuxiliaryFilesMonitor.class);
    private static final Gson GSON = GsonFactory.getGson();

    // List of files to monitor
    private static final List<String> MONITORED_FILES = Arrays.asList(
            "Cargo.json",
            "ModulesInfo.json",
            "Status.json",
            "Backpack.json",
            "NavRoute.json",
            "FCMaterials.json",
            "Outfitting.json",
            "Shipyard.json",
            "ShipLocker.json",
            "Market.json"
    );

    // Map file names to their corresponding event classes
    private static final Map<String, Class<?>> FILE_TO_EVENT_CLASS = new HashMap<>();

    static {
        FILE_TO_EVENT_CLASS.put("Cargo.json", GameEvents.CargoEvent.class);
        FILE_TO_EVENT_CLASS.put("ModulesInfo.json", GameEvents.ModulesInfoEvent.class);
        FILE_TO_EVENT_CLASS.put("Status.json", GameEvents.StatusEvent.class);
        FILE_TO_EVENT_CLASS.put("Backpack.json", GameEvents.BackpackEvent.class);
        FILE_TO_EVENT_CLASS.put("NavRoute.json", GameEvents.NavRouteEvent.class);
        FILE_TO_EVENT_CLASS.put("FCMaterials.json", GameEvents.FCMaterialsEvent.class);
        FILE_TO_EVENT_CLASS.put("Outfitting.json", GameEvents.OutfittingEvent.class);
        FILE_TO_EVENT_CLASS.put("Shipyard.json", GameEvents.ShipyardEvent.class);
        FILE_TO_EVENT_CLASS.put("ShipLocker.json", GameEvents.ShipLockerEvent.class);
        FILE_TO_EVENT_CLASS.put("Market.json", GameEvents.MarketEvent.class);
    }

    private final Path directory;
    private final Set<String> monitoredFileSet = new HashSet<>(MONITORED_FILES);
    private Thread processingThread;
    private volatile boolean isRunning;

    public AuxiliaryFilesMonitor() {
        this.directory = Paths.get(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous");
    }

    public synchronized void start() {
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("AuxiliaryFilesMonitor is already running");
            return;
        }
        isRunning = true;
        processingThread = new Thread(this, "AuxiliaryFilesMonitorThread");
        processingThread.start();
        log.info("AuxiliaryFilesMonitor started");
    }

    public synchronized void stop() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("AuxiliaryFilesMonitor is not running");
            return;
        }
        isRunning = false;
        processingThread.interrupt();
        try {
            processingThread.join(5000); // Wait up to 5 seconds for clean shutdown
            log.info("AuxiliaryFilesMonitor stopped");
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for AuxiliaryFilesMonitor to stop", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        processingThread = null;
    }

    @Override
    public void run() {
        try {
            readAndPublishInitialFiles();
            monitorFiles();
        } catch (IOException e) {
            log.error("IOException in AuxiliaryFilesMonitor", e);
            EventBusManager.publish(new AppLogEvent("AuxiliaryFilesMonitor failed: " + e.getMessage()));
        } catch (InterruptedException e) {
            log.info("AuxiliaryFilesMonitor interrupted, shutting down");
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            log.error("Unexpected error in AuxiliaryFilesMonitor", e);
            EventBusManager.publish(new AppLogEvent("Unexpected error in AuxiliaryFilesMonitor: " + e.getMessage()));
        }
    }

    private void monitorFiles() throws IOException, InterruptedException {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("Auxiliary files monitor started, watching directory: {}", directory);

            while (isRunning) {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) {
                    if (Thread.currentThread().isInterrupted() || !isRunning) {
                        log.info("Shutting down AuxiliaryFilesMonitor due to interruption or stop signal");
                        return;
                    }
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = (Path) event.context();
                        String fileName = filePath.toString();
                        if (monitoredFileSet.contains(fileName)) {
                            Path fullPath = directory.resolve(fileName);
                            Object eventObject = readAndParseFile(fullPath, fileName);
                            if (eventObject != null) {
                                EventBusManager.publish(eventObject);
                                log.info("Published update for file: {}", fileName);
                            }
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    log.error("Watch key no longer valid; directory may be inaccessible");
                    break;
                }
            }
        }
    }

    /**
     * Reads and publishes all monitored files that exist at startup.
     */
    private void readAndPublishInitialFiles() {
        for (String fileName : MONITORED_FILES) {
            Path filePath = directory.resolve(fileName);
            if (Files.exists(filePath)) {
                Object eventObject = readAndParseFile(filePath, fileName);
                if (eventObject != null) {
                    EventBusManager.publish(eventObject);
                    log.info("Published initial event for file: {}", fileName);
                }
            }
        }
    }

    /**
     * Reads the file, parses it as a JsonObject, and deserializes it into the appropriate Event DTO.
     * Returns null if there's an error reading or parsing.
     */
    private Object readAndParseFile(Path filePath, String fileName) {
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);
            if (jsonObject != null) {
                Class<?> eventClass = FILE_TO_EVENT_CLASS.get(fileName);
                if (eventClass != null) {
                    return GSON.fromJson(jsonObject, eventClass);
                } else {
                    log.warn("No event class mapped for file: {}", fileName);
                }
            }
        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
        } catch (JsonParseException e) {
            log.error("Failed to parse JSON in file: {}", filePath, e);
        }
        return null;
    }
}