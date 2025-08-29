package elite.companion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final ConfigManager INSTANCE = new ConfigManager();
    public static final int MAX_NUMBER_OF_CHARACTERS = 120;

    // Config file names
    public static final String USER_CONFIG_FILENAME = "player.conf";
    public static final String SYSTEM_CONFIG_FILENAME = "system.conf";

    // Values expected in config files
    // system.conf
    public static final String GROK_API_KEY = "grok_key";

    // player.conf
    public static final String PLAYER_MISSION_STATEMENT = "mission_statement";
    public static final String PLAYER_TITLE = "title";
    public static final String PLAYER_ALTERNATIVE_NAME = "alternative_name";

    // Default config templates
    private static final Map<String, String> DEFAULT_SYSTEM_CONFIG = new LinkedHashMap<>();
    private static final Map<String, String> DEFAULT_USER_CONFIG = new LinkedHashMap<>();

    static {
        // Initialize default system config
        DEFAULT_SYSTEM_CONFIG.put(GROK_API_KEY, "");

        // Initialize default user config
        DEFAULT_USER_CONFIG.put(PLAYER_MISSION_STATEMENT, "");
        DEFAULT_USER_CONFIG.put(PLAYER_TITLE, "");
        DEFAULT_USER_CONFIG.put(PLAYER_ALTERNATIVE_NAME, "");
    }

    private ConfigManager() {
        // Private constructor to enforce singleton pattern
        initializeConfigFiles();
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    private void initializeConfigFiles() {
        createDefaultConfigIfNotExists(SYSTEM_CONFIG_FILENAME, DEFAULT_SYSTEM_CONFIG);
        createDefaultConfigIfNotExists(USER_CONFIG_FILENAME, DEFAULT_USER_CONFIG);
    }

    private void createDefaultConfigIfNotExists(String filename, Map<String, String> defaultConfig) {
        try {
            // Check if file exists in resources
            InputStream input = getClass().getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                log.info("Configuration file {} not found, creating default", filename);
                writeConfigFile(filename, defaultConfig, true);
            } else {
                // File exists, ensure all expected keys are present
                Map<String, String> existingConfig = readConfig(filename);
                boolean updated = false;
                for (String key : defaultConfig.keySet()) {
                    if (!existingConfig.containsKey(key)) {
                        existingConfig.put(key, defaultConfig.get(key));
                        updated = true;
                    }
                }
                if (updated) {
                    log.info("Updating configuration file {} with missing keys", filename);
                    writeConfigFile(filename, existingConfig, false);
                }
                input.close();
            }
        } catch (Exception e) {
            log.error("Error initializing configuration file {}: {}", filename, e.getMessage());
        }
    }

    private Map<String, String> readConfig(String filename) {
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
                    String value = parts[1].trim();
                    if (filename.equals(USER_CONFIG_FILENAME)) {
                        value = value.substring(0, Math.min(value.length(), MAX_NUMBER_OF_CHARACTERS));
                    }
                    config.put(parts[0].trim(), value);
                }
            }
            reader.close();
        } catch (Exception e) {
            log.error("Error reading configuration file {}: {}", filename, e.getMessage());
        }
        return config;
    }

    public Map<String, String> readSystemConfig() {
        return readConfig(SYSTEM_CONFIG_FILENAME);
    }

    public Map<String, String> readUserConfig() {
        return readConfig(USER_CONFIG_FILENAME);
    }

    private void writeConfigFile(String filename, Map<String, String> config, boolean includeComments) {
        try {
            // For resources, we assume a writable directory (e.g., project root or config dir)
            File file = new File(filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            if (includeComments) {
                if (filename.equals(USER_CONFIG_FILENAME)) {
                    writer.write("## Provide brief description of your overall mission. Example:\n");
                    writer.write("# Note all entries will be trimmed to 120 characters. So be brief.\n");
                    writer.write("# Provides context for:\n");
                    writer.write("# mission_statement=We serve the Imperial fleet as explorers and bounty hunters\n\n");
                }
            }

            for (Map.Entry<String, String> entry : config.entrySet()) {
                writer.write(String.format("%s=%s\n", entry.getKey(), entry.getValue()));
                if (includeComments && filename.equals(USER_CONFIG_FILENAME)) {
                    if (entry.getKey().equals(PLAYER_MISSION_STATEMENT)) {
                        writer.write("\n# Use your guild title or any other name you want to be addressed by\n");
                        writer.write("# AI will randomly choose to address you by your name, highest military rank, corresponding honorific or title\n");
                    } else if (entry.getKey().equals(PLAYER_TITLE)) {
                        writer.write("\n# Use this to provide an alternative name. This is in case your in-game name is unpronounceable by the AI\n");
                        writer.write("# such as \"CMDR-PAPABARE123\"\n");
                    }
                }
            }

            writer.close();
            log.info("Successfully wrote configuration file: {}", filename);
        } catch (Exception e) {
            log.error("Error writing configuration file {}: {}", filename, e.getMessage());
        }
    }
}