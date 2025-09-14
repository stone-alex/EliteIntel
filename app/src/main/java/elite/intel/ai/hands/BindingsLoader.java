package elite.intel.ai.hands;

import elite.intel.ai.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * The BindingsLoader class is responsible for loading binding files from the bindings directory,
 * specifically identifying and retrieving the most recently modified ".binds" file.
 * This is useful for applications or systems that leverage the binding configurations from
 * the Elite Dangerous game by Frontier Developments.
 */
public class BindingsLoader {
    private static final Logger log = LoggerFactory.getLogger(BindingsLoader.class);

    public File getLatestBindsFile() throws Exception {
        Path bindingsDir = ConfigManager.getInstance().getBindingsPath();
        Path latestFilePath = Files.list(bindingsDir)
                .filter(p -> p.toString().endsWith(".binds"))
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElseThrow(() -> new Exception("No .binds file found in " + bindingsDir)
                );

        File latestFile = latestFilePath.toFile();
        log.info("Selected latest bindings file: {}", latestFile.getName());
        return latestFile;
    }
}