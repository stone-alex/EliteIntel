package elite.intel.ai.hands;

import elite.intel.ai.brain.handlers.commands.Commands;
import elite.intel.ai.mouth.subscribers.events.AiVoxResponseEvent;
import elite.intel.gameapi.EventBusManager;
import elite.intel.session.PlayerSession;
import elite.intel.ui.event.AppLogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The BindingsMonitor class is responsible for monitoring changes to
 * key bindings files in the specified directory and updating the internal
 * bindings map accordingly. It continuously monitors the target directory
 * for file events and processes changes to ensure the bindings remain up to date.
 * <p>
 * This class relies on the {@link KeyBindingsParser} to parse the contents of key
 * bindings files and determine the mapping of actions to key bindings.
 * <p>
 * Features:
 * - Monitors a directory for changes to "*.binds" files.
 * - Automatically reloads and parses bindings when a new or modified file is detected.
 * - Provides access to the current bindings map.
 * <p>
 * Thread Safety:
 * - This class uses synchronization to ensure thread-safe access to start and stop
 * monitoring operations.
 * <p>
 * Logging:
 * - Uses SLF4J for logging to provide information on status, errors, and events during monitoring.
 * <p>
 * Exceptions:
 * - Captures and logs IOExceptions, InterruptedExceptions, and other unexpected errors
 * during the monitoring process.
 */
public class BindingsMonitor {
    private static final Logger log = LogManager.getLogger(BindingsMonitor.class);
    private static volatile BindingsMonitor instance;
    private final KeyBindingsParser parser;
    private Path bindingsDir;
    private Map<String, KeyBindingsParser.KeyBinding> bindings;
    private File currentBindsFile;
    private Thread processingThread;
    private volatile boolean running;

    private BindingsMonitor() {
        this.parser = KeyBindingsParser.getInstance();

    }

    public static BindingsMonitor getInstance() {
        if (instance == null) {
            synchronized (BindingsMonitor.class) {
                if (instance == null) {
                    instance = new BindingsMonitor();
                }
            }
        }
        return instance;
    }

    public synchronized void startMonitoring() throws IOException {
        this.bindingsDir = PlayerSession.getInstance().getBindingsDir();
        if (processingThread != null && processingThread.isAlive()) {
            log.warn("BindingsMonitor is already running");
            return;
        }
        running = true;
        processingThread = new Thread(this::monitorBindings, "BindingsMonitorThread");
        processingThread.start();
        log.info("BindingsMonitor started");
    }

    public synchronized void stopMonitoring() {
        if (processingThread == null || !processingThread.isAlive()) {
            log.warn("BindingsMonitor is not running");
            return;
        }
        running = false;
        processingThread.interrupt();
        try {
            processingThread.join(5000); // Wait up to 5 seconds for clean shutdown
            log.info("BindingsMonitor stopped");
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for BindingsMonitor to stop", e);
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        processingThread = null;
    }

    private void monitorBindings() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            bindingsDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
            log.info("Monitoring key bindings in directory: {}", bindingsDir);

            // Initial parse of bindings
            parseAndUpdateBindings();

            while (running) {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) {
                    if (Thread.currentThread().isInterrupted() || !running) {
                        log.info("Shutting down BindingsMonitor due to interruption or stop signal");
                        return;
                    }
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path changed = (Path) event.context();
                        if (changed.toString().endsWith(".binds")) {
                            File latestFile = new BindingsLoader().getLatestBindsFile();
                            if (!latestFile.equals(currentBindsFile) ||
                                    Files.getLastModifiedTime(latestFile.toPath()).toMillis() > currentBindsFile.lastModified()) {
                                currentBindsFile = latestFile;
                                parseAndUpdateBindings();
                                log.info("Reloaded bindings from: {}", currentBindsFile.getName());
                            }
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    log.error("Watch key no longer valid; directory may be inaccessible");
                    EventBusManager.publish(new AiVoxResponseEvent("Error: Key bindings directory inaccessible"));
                    break;
                }
            }
            checkBindings();
        } catch (IOException e) {
            log.error("IOException in BindingsMonitor", e);
            EventBusManager.publish(new AiVoxResponseEvent("Please check the bindings directory"));
        } catch (InterruptedException e) {
            log.info("BindingsMonitor interrupted, shutting down");
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            log.error("Unexpected error in BindingsMonitor", e);
            EventBusManager.publish(new AiVoxResponseEvent("Please check the bindings directory"));
        }
    }

    private void parseAndUpdateBindings() {
        try {
            currentBindsFile = new BindingsLoader().getLatestBindsFile();
            bindings = parser.parseBindings(currentBindsFile);
            EventBusManager.publish(new AppLogEvent("SYSTEM: Key bindings updated from file " + currentBindsFile.getAbsolutePath()));
            log.info("Key bindings updated from: {}", currentBindsFile.getName());
        } catch (Exception e) {
            log.error("Failed to parse key bindings from: {}", currentBindsFile != null ? currentBindsFile.getName() : "null", e);
            EventBusManager.publish(new AiVoxResponseEvent("Failed to update key bindings. Plese check the bindings directory. "));
        }
    }

    public Map<String, KeyBindingsParser.KeyBinding> getBindings() {
        return bindings;
    }

    public void checkBindings() {

        Commands[] commands = Commands.values();
        for (Commands c : commands) {
            if (getBindings().get(c.getBinding()) != null) {
                EventBusManager.publish(new AppLogEvent("No Binding found for " + c.getBinding()));
            }
        }
    }
}