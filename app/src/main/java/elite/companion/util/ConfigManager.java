package elite.companion.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final ConfigManager INSTANCE = new ConfigManager();

    private ConfigManager() {
        // Private constructor to enforce singleton pattern
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public Map<String, String> readConfig(String filename) {
        Map<String, String> config = new HashMap<>();
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                log.error("Unable to find configuration file: {}", filename);
                return config;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    config.put(parts[0].trim(), parts[1].trim());
                }
            }
            reader.close();
        } catch (Exception e) {
            log.error("Error reading configuration file: {}", e.getMessage());
        }
        return config;
    }
}
