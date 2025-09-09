package elite.companion.util;

import elite.companion.ai.ears.google.GoogleSTTImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * The DaftSecretarySanitizer class is a singleton utility for sanitizing
 * transcripts and correcting common speech-to-text (STT) transcription mistakes.
 * It processes and replaces misheard words from STT results using a correction
 * dictionary, which is either loaded from or created in the application directory.
 * <p>
 * This class also provides helper functions such as word capitalization.
 */
public class DaftSecretarySanitizer {

    private static final DaftSecretarySanitizer INSTANCE = new DaftSecretarySanitizer();
    private final Logger log = LoggerFactory.getLogger(DaftSecretarySanitizer.class);
    private final Map<String, String> GOOGLE_CORRECTIONS = loadCorrections();

    private DaftSecretarySanitizer() {
        // Private constructor for singleton
    }

    public static DaftSecretarySanitizer getInstance() {
        return INSTANCE;
    }

    private String determineAppDir() {
        String appDir = "dictionary/";
        try {
            URI jarUri = GoogleSTTImpl.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(jarUri);
            if (jarFile.getPath().endsWith(".jar")) {
                String parentDir = jarFile.getParent();
                if (parentDir != null) {
                    appDir = parentDir + File.separator + "dictionary/";
                    log.debug("Running from JAR, set APP_DIR to: {}", appDir);
                } else {
                    log.warn("JAR parent directory is null, using default APP_DIR: {}", appDir);
                }
            } else {
                log.debug("Not running from JAR, using default APP_DIR for classpath resources: {}", appDir);
            }
        } catch (Exception e) {
            log.warn("Could not determine JAR location, using default APP_DIR: {}. Error: {}", appDir, e.getMessage());
        }
        return appDir;
    }


    // Ensure corrections file exists, create with defaults if not

    private File ensureCorrectionsFileExists() {
        String APP_DIR = determineAppDir();
        String CORRECTIONS_FILE = "stt-correction-dictionary.txt";
        File file = new File(APP_DIR, CORRECTIONS_FILE);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                log.error("Failed to create session directory: {}", parentDir.getPath());
                return null;
            }
        }
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("###########################################################################\n");
                writer.write("# STT is a deff and dumb secretary. We have to correct it's mistakes.\n");
                writer.write("# List of words or phrases to be corrected\n");
                writer.write("# Add your own, if the Speech To Text can't understand you well.\n");
                writer.write("# Surround the key and value with quotes\n");
                writer.write("# Speech to Text corrections (format: \"misheard\"=\"corrected\")\n");
                writer.write("# Example: \"treat you\"=tritium\n");

                log.info("Created default corrections file: {}", file.getAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to create default corrections file: {}. Error: {}", file.getPath(), e.getMessage());
            }
        }
        log.info("Corrections file: {}", file.getAbsolutePath());
        return file;
    }


    private Map<String, String> loadCorrections() {
        Map<String, String> map = new HashMap<>();
        File file = ensureCorrectionsFileExists();
        if (file != null && file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            map.put(parts[0].trim().toLowerCase().replaceAll("\"", ""), parts[1].trim().toLowerCase().replaceAll("\"", ""));
                        }
                    }
                }
                log.info("Loaded {} STT corrections from {}", map.size(), file.getAbsolutePath());
            } catch (IOException e) {
                log.warn("Failed to load STT corrections from {}: {}", file.getPath(), e.getMessage());
            }
        }
        return map;
    }

    public String correctMistakes(String transcript) {
        loadCorrections();
        String sanitized = transcript.toLowerCase();
        for (Map.Entry<String, String> entry : GOOGLE_CORRECTIONS.entrySet()) {
            sanitized = sanitized.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue());
        }
        return sanitized.trim();
    }


    /**
     * Converts all characters to lower case and capitalizes the first character of each word in the string
     *
     * @param input String to process
     * @return Processed string with capitalized words or null if input is null or empty
     */
    public String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return null;

        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1));
            }
            if (i < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }
}