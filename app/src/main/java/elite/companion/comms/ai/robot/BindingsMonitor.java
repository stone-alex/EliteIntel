package elite.companion.comms.ai.robot;

import elite.companion.comms.voice.VoiceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.util.Map;

public class BindingsMonitor {
    private static final Logger log = LoggerFactory.getLogger(BindingsMonitor.class);
    private final KeyBindingsParser parser;
    private final Path bindingsDir;
    private Map<String, KeyBindingsParser.KeyBinding> bindings;
    private File currentBindsFile;
    private WatchService watchService;

    public BindingsMonitor(KeyBindingsParser parser) {
        this.parser = parser;
        this.bindingsDir = Paths.get(System.getProperty("user.home"),
                "AppData", "Local", "Frontier Developments", "Elite Dangerous", "Options", "Bindings");
    }

    public void startMonitoring() throws Exception {
        currentBindsFile = new BindingsLoader().getLatestBindsFile();
        bindings = parser.parseBindings(currentBindsFile);
        VoiceGenerator.getInstance().speak("Key bindings are initialized.");
        log.info("Initial bindings loaded from: {}", currentBindsFile.getName());

        watchService = FileSystems.getDefault().newWatchService();
        bindingsDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        log.info("Started monitoring bindings directory: {}", bindingsDir);

        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                Path changed = (Path) event.context();
                if (changed.toString().endsWith(".binds")) {
                    File latestFile = new BindingsLoader().getLatestBindsFile();
                    if (!latestFile.equals(currentBindsFile) || Files.getLastModifiedTime(latestFile.toPath()).toMillis() > currentBindsFile.lastModified()) {
                        currentBindsFile = latestFile;
                        bindings = parser.parseBindings(currentBindsFile);
                        log.info("Reloaded bindings from: {}", currentBindsFile.getName());
                    }
                }
            }
            key.reset();
        }
    }

    public Map<String, KeyBindingsParser.KeyBinding> getBindings() {
        return bindings;
    }

    public void stopMonitoring() throws Exception {
        if (watchService != null) {
            watchService.close();
            log.info("Stopped monitoring bindings directory");
        }
    }
}