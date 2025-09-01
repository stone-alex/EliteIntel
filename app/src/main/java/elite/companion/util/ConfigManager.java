package elite.companion.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final ConfigManager INSTANCE = new ConfigManager();
    private final String APP_DIR;
    private final String USER_CONFIG_FILENAME = "player.conf";
    private final String SYSTEM_CONFIG_FILENAME = "system.conf";
    private final int MAX_NUMBER_OF_CHARACTERS = 120;

    // Config keys (public for external use)
    public static final String GROK_API_KEY = "grok_key";
    public static final String EDSM_KEY = "edsm_key";
    public static final String PLAYER_MISSION_STATEMENT = "mission_statement";
    public static final String PLAYER_TITLE = "title";
    public static final String PLAYER_ALTERNATIVE_NAME = "alternative_name";

    // Default config templates
    private final Map<String, String> DEFAULT_SYSTEM_CONFIG = new LinkedHashMap<>();
    private final Map<String, String> DEFAULT_USER_CONFIG = new LinkedHashMap<>();

    private ConfigManager() {
        // Initialize default configs
        DEFAULT_SYSTEM_CONFIG.put(GROK_API_KEY, "");
        DEFAULT_USER_CONFIG.put(PLAYER_MISSION_STATEMENT, "");
        DEFAULT_USER_CONFIG.put(PLAYER_TITLE, "");
        DEFAULT_USER_CONFIG.put(PLAYER_ALTERNATIVE_NAME, "");
        DEFAULT_USER_CONFIG.put(EDSM_KEY, "");

        // Initialize APP_DIR
        String appDir = ""; // Default to empty string for development
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
        APP_DIR = appDir;

        // Initialize config files after APP_DIR is set
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
        if (filename == null) {
            log.error("Filename is null, cannot create or update config file");
            return;
        }
        File file = new File(APP_DIR + filename);
        if (!file.exists()) {
            log.info("Configuration file {} not found, creating default in {}", filename, file.getAbsolutePath());
            if (defaultConfig != null) {
                writeConfigFile(filename, defaultConfig, true);
            } else {
                log.error("Default config for {} is null, cannot create file", filename);
            }
        } else {
            // File exists, ensure all expected keys are present
            Map<String, String> existingConfig = readConfig(filename);
            boolean updated = false;
            if (defaultConfig != null) {
                for (String key : defaultConfig.keySet()) {
                    if (!existingConfig.containsKey(key)) {
                        existingConfig.put(key, defaultConfig.get(key));
                        updated = true;
                    }
                }
                if (updated) {
                    log.info("Updating configuration file {} with missing keys in {}", filename, file.getAbsolutePath());
                    writeConfigFile(filename, existingConfig, false);
                }
            } else {
                log.error("Default config for {} is null, cannot update missing keys", filename);
            }
        }
    }

    private Map<String, String> readConfig(String filename) {
        if (filename == null) {
            log.error("Filename is null, cannot read config");
            return new HashMap<>();
        }
        Map<String, String> config = new HashMap<>();
        File file = new File(APP_DIR + filename);

        // Try reading from app directory first
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
                log.debug("Successfully read config file: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("Error reading configuration file {}: {}", file.getAbsolutePath(), e.getMessage());
            }
        } else {
            // Fallback to resources
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (input == null) {
                    log.warn("Configuration file {} not found in resources", filename);
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
                log.debug("Successfully read config file from resources: {}", filename);
            } catch (Exception e) {
                log.error("Error reading resource configuration file {}: {}", filename, e.getMessage());
            }
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
        if (filename == null) {
            log.error("Filename is null, cannot write config file");
            return;
        }
        File file = new File(APP_DIR + filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (includeComments) {
                if (filename.equals(USER_CONFIG_FILENAME)) {
                    writer.write("## Provide brief description of your overall mission. Example:\n");
                    writer.write("# Note all entries will be trimmed to 120 characters. So be brief.\n");
                    writer.write("# Provides context for:\n");
                    writer.write("# mission_statement=We serve the Imperial fleet as explorers and bounty hunters\n\n");
                    writer.write("# edsm_key=your_edsm_key\n\n");
                    writer.write("# edsm_key=your_edsm_key\n\n");
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
            log.info("Successfully wrote configuration file: {}", file.getAbsolutePath());
        } catch (Exception e) {
            log.error("Error writing configuration file {}: {}", file.getAbsolutePath(), e.getMessage());
        }
    }
}