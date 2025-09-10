package elite.companion.ai;

import elite.companion.session.SystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The ConfigManager class is a singleton responsible for managing
 * configuration files and default values for both system and user-specific configurations.
 * It provides features to read, write, and initialize configuration values dynamically.
 * <p>
 * System Configuration:
 * - Stores settings like API keys (e.g., AI API, TTS API, STT API).
 * <p>
 * User Configuration:
 * - Maintains user-specific details such as mission statement, title, and alternative name.
 * <p>
 * Key responsibilities:
 * - Ensures initialization of config files with default values if they do not exist.
 * - Reads configuration from files or classpath resources, with support for comments and trimming user inputs.
 * - Updates missing keys in configuration files with defaults during initialization.
 * - Provides utility methods to retrieve specific configuration keys for system or user use.
 * <p>
 * Thread Safety:
 * - The singleton pattern used in this class ensures that only one instance of ConfigManager
 * is created and accessed during the lifecycle of the application.
 * <p>
 * Logging:
 * - The class uses SLF4J for logging activities and errors during operations like reading or
 * writing configuration files.
 */
public class ConfigManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final ConfigManager INSTANCE = new ConfigManager();
    private final String APP_DIR;
    public static final String USER_CONFIG_FILENAME = "player.conf";
    public static final String SYSTEM_CONFIG_FILENAME = "system.conf";
    private final int MAX_NUMBER_OF_CHARACTERS = 120;

    // Config keys
    public static final String AI_API_KEY = "ai_api_key";
    public static final String TTS_API_KEY = "tts_api_key"; // New key for Google API
    public static final String STT_API_KEY = "stt_api_key";
    public static final String PLAYER_MISSION_STATEMENT = "mission_statement";
    public static final String PLAYER_TITLE = "title";
    public static final String PLAYER_ALTERNATIVE_NAME = "alternative_name";
    public static final String PLAYER_EDSM_KEY = "edsm_key";
    public static final String JOURNAL_DIR = "journal_dir";
    public static final String BINDINGS_DIR = "bindings_dir";

    private final Map<String, String> DEFAULT_SYSTEM_CONFIG = new LinkedHashMap<>();
    private final Map<String, String> DEFAULT_USER_CONFIG = new LinkedHashMap<>();

    private ConfigManager() {
        // Initialize default configs
        DEFAULT_SYSTEM_CONFIG.put(AI_API_KEY, "");
        DEFAULT_SYSTEM_CONFIG.put(TTS_API_KEY, "");
        DEFAULT_SYSTEM_CONFIG.put(STT_API_KEY, "");
        DEFAULT_USER_CONFIG.put(PLAYER_MISSION_STATEMENT, "");
        DEFAULT_USER_CONFIG.put(PLAYER_TITLE, "");
        DEFAULT_USER_CONFIG.put(PLAYER_ALTERNATIVE_NAME, "");
        DEFAULT_USER_CONFIG.put(JOURNAL_DIR, "");
        DEFAULT_USER_CONFIG.put(BINDINGS_DIR, "");

        // Initialize APP_DIR
        String appDir = "";
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

        initializeConfigFiles();
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    private void initializeConfigFiles() {
        createDefaultConfigIfNotExists(SYSTEM_CONFIG_FILENAME, DEFAULT_SYSTEM_CONFIG);
        createDefaultConfigIfNotExists(USER_CONFIG_FILENAME, DEFAULT_USER_CONFIG);
    }

    public Path getJournalPath() {
        String customJournalDir = ConfigManager.getInstance().getPlayerKey(ConfigManager.JOURNAL_DIR);
        if (customJournalDir == null || customJournalDir.isBlank()) {
            return Paths.get(System.getProperty("user.home"), "Saved Games", "Frontier Developments", "Elite Dangerous");
        } else {
            return Paths.get(customJournalDir);
        }
    }

    public Path getBindingsPath() {
        String customBindingsDir = ConfigManager.getInstance().getPlayerKey(ConfigManager.BINDINGS_DIR);
        if (customBindingsDir == null || customBindingsDir.isBlank()) {
            return Paths.get(System.getProperty("user.home"), "AppData", "Local", "Frontier Developments", "Elite Dangerous", "Options", "Bindings");
        } else {
            return Paths.get(customBindingsDir);
        }
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

    public String getSystemKey(String keyType) {
        SystemSession systemSession = SystemSession.getInstance();
        String key = String.valueOf(systemSession.get(keyType));
        if (key == null || key.isEmpty() || key.equals("null")) {
            String value = readConfig(SYSTEM_CONFIG_FILENAME).get(keyType);
            if (value != null || !value.isEmpty()) {
                systemSession.put(keyType, value);
                return value;
            } else {
                throw new IllegalStateException(String.format("No value found for system key %s", keyType));
            }
        } else {
            return key;
        }
    }


    public String getPlayerKey(String keyType) {
        SystemSession systemSession = SystemSession.getInstance();
        String key = String.valueOf(systemSession.get(keyType));
        if (key == null || key.isEmpty() || key.equals("null")) {
            String value = readConfig(USER_CONFIG_FILENAME).get(keyType);
            if (value != null || !value.isEmpty()) {
                systemSession.put(keyType, value);
                return value;
            } else {
                throw new IllegalStateException(String.format("No value found for user key %s", keyType));
            }
        } else {
            return key;
        }
    }

    public Map<String, String> readSystemConfig() {
        return readConfig(SYSTEM_CONFIG_FILENAME);
    }

    public Map<String, String> readUserConfig() {
        return readConfig(USER_CONFIG_FILENAME);
    }

    public void writeConfigFile(String filename, Map<String, String> config, boolean includeComments) {
        if (filename == null) {
            log.error("Filename is null, cannot write config file");
            return;
        }
        File file = new File(APP_DIR + filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (includeComments) {
                if (filename.equals(SYSTEM_CONFIG_FILENAME)) {
                    writer.write("## System configuration for API keys\n");
                    writer.write("# ai_api_key: API key for LLM\n");
                    writer.write("# tts_api_key: API key for TTS\n");
                    writer.write("# stt_api_key: API key for STT\n\n");
                } else if (filename.equals(USER_CONFIG_FILENAME)) {
                    writer.write("## Provide brief description of your overall mission. Example:\n");
                    writer.write("# Note all entries will be trimmed to 120 characters. So be brief.\n");
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
                    } else if (entry.getKey().equals(JOURNAL_DIR)) {
                        writer.write("\n# Custom directory path for Elite Dangerous journal files (leave blank for default)\n");
                    } else if (entry.getKey().equals(BINDINGS_DIR)) {
                        writer.write("\n# Custom directory path for Elite Dangerous key bindings files (leave blank for default)\n");
                    }
                }
            }
            log.info("Successfully wrote configuration file: {}", file.getAbsolutePath());
        } catch (Exception e) {
            log.error("Error writing configuration file {}: {}", file.getAbsolutePath(), e.getMessage());
        }
    }
}