package elite.companion.comms.ai.robot;

import elite.companion.gameapi.VoiceProcessEvent;
import elite.companion.util.EventBusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BindingsMonitor {
    private static final Logger log = LoggerFactory.getLogger(BindingsMonitor.class);
    private final KeyBindingsParser parser;
    private final Path bindingsDir;
    private Map<String, KeyBindingsParser.KeyBinding> bindings;
    private File currentBindsFile;
    private Thread processingThread;
    private volatile boolean running;

    public BindingsMonitor(KeyBindingsParser parser) {
        this.parser = parser;
        this.bindingsDir = Paths.get(System.getProperty("user.home"),
                "AppData", "Local", "Frontier Developments", "Elite Dangerous", "Options", "Bindings");
    }

    public synchronized void startMonitoring() throws IOException {
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
                    EventBusManager.publish(new VoiceProcessEvent("Error: Key bindings directory inaccessible"));
                    break;
                }
            }
        } catch (IOException e) {
            log.error("IOException in BindingsMonitor", e);
            EventBusManager.publish(new VoiceProcessEvent("Error in BindingsMonitor: " + e.getMessage()));
        } catch (InterruptedException e) {
            log.info("BindingsMonitor interrupted, shutting down");
            Thread.currentThread().interrupt(); // Restore interrupted status
        } catch (Exception e) {
            log.error("Unexpected error in BindingsMonitor", e);
            EventBusManager.publish(new VoiceProcessEvent("Unexpected error in BindingsMonitor: " + e.getMessage()));
        }
    }

    private void parseAndUpdateBindings() {
        try {
            currentBindsFile = new BindingsLoader().getLatestBindsFile();
            bindings = parser.parseBindings(currentBindsFile);
            log.info("Key bindings updated from: {}", currentBindsFile.getName());
        } catch (Exception e) {
            log.error("Failed to parse key bindings from: {}", currentBindsFile != null ? currentBindsFile.getName() : "null", e);
            EventBusManager.publish(new VoiceProcessEvent("Failed to update key bindings: " + e.getMessage()));
        }
    }

    public Map<String, KeyBindingsParser.KeyBinding> getBindings() {
        return bindings;
    }
}