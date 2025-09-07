package elite.companion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for transforming and sanitizing strings.
 */
public class StringSanitizer {
    private static final Logger log = LoggerFactory.getLogger(StringSanitizer.class);

    /**
     * Converts all characters to lower case and capitalizes the first character of each word in the string
     *
     * @param input String to process
     * @return Processed string with capitalized words or null if input is null or empty
     */
    public static String capitalizeWords(String input) {
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


    public static String sanitizeGoogleMistakes(String voiceCommand) {
        if (voiceCommand == null || voiceCommand.isEmpty()) return null;

        String command = voiceCommand.toLowerCase().trim();

        String[] misheardPhrases = {
                "treat you", "trees you", "3 tube", "hydrogen 3", "hydrogen three", "32",
                "carrier fuel", "carrier juice", "carrot juice", "treatyou", "treesyou",
        };
        for (String phrase : misheardPhrases) {
            if (command.contains(phrase)) {
                command = command.replaceAll("(?i)" + phrase.replace(" ", "\\s+"), "tritium");
                log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
            }
        }


        if (command.contains("southwest")) {
            command = command.replace("southwest", "set voice to");
            command = command.replace("southwest", "set voice to");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if (command.contains("atlanta")) {
            command = command.replace("atlanta", "lana");
            command = command.replace("Atlanta", "lana");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if (command.contains("supercross")) {
            command = command.replace("supercross", "supercruise");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if (command.contains("hannah")) {
            command = command.replace("Hannah", "Anna");
            command = command.replace("hannah", "Anna");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if (command.contains("streaming mode of")) {
            command = command.replace("streaming mode of", "streaming mode off");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if (command.contains("bunch of tl")) {
            command = command.replace("bunch of tl", "toggle ftl");
        }

        if (command.contains("bunch of cl")) {
            command = command.replace("bunch of cl", "toggle ftl");
        }

        if (command.contains("bunch of tail")) {
            command = command.replace("bunch of tail", "toggle ftl");
        }

        if (command.contains("bunch it")) {
            command = command.replace("bunch it", "toggle ftl");
        }

        if (command.contains("display enough panel")) {
            command = command.replace("display enough panel", "display navigation panel");
        }

        if (command.contains("display nav panel")) {
            command = command.replace("display nav panel", "display navigation panel");
        }

        if (command.contains("play the navigation panel")) {
            command = command.replace("play the navigation panel", "display navigation panel");
        }

        if (command.contains("display hard")) {
            command = command.replace("display hard", "display hud");
        }

        if (command.contains("display hot")) {
            command = command.replace("display hot", "display hud");
        }

        if (command.contains("exit to head")) {
            command = command.replace("exit to head", "display hud");
        }

        if (command.contains("exit to hard")) {
            command = command.replace("exit to hard", "display hud");
        }

        if (command.contains("despite galaxy map")) {
            command = command.replace("despite galaxy map", "display galaxy map");
        }

        if (command.contains("and their super cruise")) {
            command = command.replace("and their super cruise", "enter supercruise");
        }

        return command.toLowerCase();
    }
}



/*
* FUTURE IMPL. Read from file:
*
* // Determine session/ directory relative to JAR or project root
    private static String determineAppDir() {
        String appDir = "session/";
        try {
            URI jarUri = GoogleSTTImpl.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File jarFile = new File(jarUri);
            if (jarFile.getPath().endsWith(".jar")) {
                String parentDir = jarFile.getParent();
                if (parentDir != null) {
                    appDir = parentDir + File.separator + "session/";
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
    private static File ensureCorrectionsFileExists() {
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
                writer.write("# Google STT corrections (format: misheard=corrected)\n");
                writer.write("treaty=tritium\n");
                writer.write("tree=tritium\n");
                writer.write("3=tritium\n");
                writer.write("treat you=tritium\n");
                writer.write("treating=tritium\n");
                writer.write("thar goid=thargoid\n");
                writer.write("guard ian=guardian\n");
                writer.write("super cruise=supercruise\n");
                writer.write("display hard=display hud\n");
                writer.write("exit to head=display hud\n");
                writer.write("bunch it=punch it\n");
                writer.write("southwest=set voice to\n");
                log.info("Created default corrections file: {}", file.getAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to create default corrections file: {}. Error: {}", file.getPath(), e.getMessage());
            }
        }
        log.info("Corrections file: {}", file.getAbsolutePath());
        return file;
    }

    private static Map<String, String> loadCorrections() {
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
                            map.put(parts[0].trim().toLowerCase(), parts[1].trim().toLowerCase());
                        }
                    }
                }
                log.info("Loaded {} STT corrections from {}", map.size(), file.getAbsolutePath());
            } catch (IOException e) {
                log.warn("Failed to load STT corrections from {}: {}", file.getPath(), e.getMessage());
            }
        }
        // Fallback defaults (minimal, in case file fails)
        map.putIfAbsent("treaty", "tritium");
        map.putIfAbsent("treat you", "tritium");
        return map;
    }

    private String sanitizeGoogleMistakes(String transcript) {
        String sanitized = transcript.toLowerCase();
        for (Map.Entry<String, String> entry : GOOGLE_CORRECTIONS.entrySet()) {
            sanitized = sanitized.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue());
        }
        return sanitized;
    }
*
* */