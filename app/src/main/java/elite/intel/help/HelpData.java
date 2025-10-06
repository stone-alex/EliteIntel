package elite.intel.help;

import elite.intel.ai.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class HelpData {

    private static final Logger log = LogManager.getLogger(HelpData.class);
    private static HelpData instance;
    private final String DIR_NAME;
    public static final String HELP_FILE_NAME = "help.conf";
    private Map<String, String> map = new HashMap<>();
    public static synchronized HelpData getInstance() {
        if (instance == null) {
            instance = new HelpData();
        }
        return instance;
    }


    private HelpData() {
        String appDir = "help/";
        try {
            URI jarUri = ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(jarUri);
            if (jarFile.getPath().endsWith(".jar")) {
                String parentDir = jarFile.getParent();
                if (parentDir != null) {
                    appDir = parentDir + File.separator;
                    log.debug("Running from JAR, set APP_DIR to: {}", appDir);
                } else {
                    log.warn("JAR parent directory is null, using empty APP_DIR: {}", appDir);
                }
            } else {
                log.debug("Not running from JAR, using empty APP_DIR for classpath resources");
            }
        } catch (Exception e) {
            log.warn("Could not determine JAR location, using empty APP_DIR: {}. Error: {}", appDir, e.getMessage());
        }
        DIR_NAME = appDir;
        loadHelpFile();
    }


    public String getHelp(String topic) {
        return map.get(topic);
    }

    public List<String> getTopics() {
        return new ArrayList<>(map.keySet());
    }

    private void loadHelpFile() {
        Path path = Paths.get(DIR_NAME);
        File file = path.resolve(HELP_FILE_NAME).toFile();

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String value = parts[1].trim();
                        map.put(parts[0].replace("\"", "").trim(), value.replace("\"", "").trim());
                    }
                }
                log.debug("Successfully read map file: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("Error reading help file {}: {}", file.getAbsolutePath(), e.getMessage());
            }
        }
    }
}
