package elite.intel.ai.hands;

import elite.intel.session.PlayerSession;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * The BindingsLoader class is responsible for loading binding files from the bindings directory,
 * specifically identifying and retrieving the most recently modified ".binds" file.
 * This is useful for applications or systems that leverage the binding configurations from
 * the Elite Dangerous game by Frontier Developments.
 */
public class BindingsLoader {
    private static final Logger log = LogManager.getLogger(BindingsLoader.class);

    public File getLatestBindsFile() throws Exception {
        Path bindingsDir = PlayerSession.getInstance().getBindingsDir();
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