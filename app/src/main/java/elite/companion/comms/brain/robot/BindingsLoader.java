package elite.companion.comms.brain.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class BindingsLoader {
    private static final Logger log = LoggerFactory.getLogger(BindingsLoader.class);
    private final Path bindingsDir = Paths.get(System.getProperty("user.home"),
            "AppData", "Local", "Frontier Developments", "Elite Dangerous", "Options", "Bindings");

    public File getLatestBindsFile() throws Exception {
        Path latestFilePath = Files.list(bindingsDir)
                .filter(p -> p.toString().endsWith(".binds"))
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow(() -> new Exception("No .binds file found in " + bindingsDir));
        File latestFile = latestFilePath.toFile();
        log.info("Selected latest bindings file: {}", latestFile.getName());
        return latestFile;
    }
}