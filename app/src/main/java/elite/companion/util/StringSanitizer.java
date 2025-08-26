package elite.companion.util;

import elite.companion.Globals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringSanitizer {
    private static final Logger log = LoggerFactory.getLogger(StringSanitizer.class);

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

        if (command.toLowerCase().contains("Australia".toLowerCase())) {
            command = command.replace("australia", Globals.AI_NAME);
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        return command;
    }

}
