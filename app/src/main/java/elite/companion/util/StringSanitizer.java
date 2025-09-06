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

        return command.toLowerCase();
    }
}
