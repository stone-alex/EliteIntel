package elite.companion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (command.toLowerCase().contains("Australia".toLowerCase())) {
            command = command.replace("australia", Globals.AI_NAME);
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if(command.toLowerCase().contains("southwest")){
            command = command.replace("southwest", "set voice to");
            command = command.replace("southwest", "set voice to");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if(command.toLowerCase().contains("atlanta")){
            command = command.replace("atlanta", "lana");
            command = command.replace("Atlanta", "lana");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if(command.toLowerCase().contains("supercross")){
            command = command.replace("supercross", "supercruise");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if(command.toLowerCase().contains("hannah")){
            command = command.replace("Hannah", "Anna");
            command = command.replace("hannah", "Anna");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }

        if(command.toLowerCase().contains("privacy mode of")) {
            command = command.replace("privacy mode of", "privacy mode off");
            log.info("Sanitized transcript: {} -> {}", voiceCommand, command);
        }
        return command.toLowerCase();
    }



    // Very small Markdown -> HTML converter for common cases
    public static String markdownToHtml(String md) {
        String html = md
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        // Headers
        html = html.replaceAll("(?m)^######\\s*(.*)$", "<h6>$1</h6>");
        html = html.replaceAll("(?m)^#####\\s*(.*)$", "<h5>$1</h5>");
        html = html.replaceAll("(?m)^####\\s*(.*)$", "<h4>$1</h4>");
        html = html.replaceAll("(?m)^###\\s*(.*)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^##\\s*(.*)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^#\\s*(.*)$", "<h1>$1</h1>");

        // Bold/italic
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("__(.+?)__", "<b>$1</b>");
        html = html.replaceAll("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)", "<i>$1</i>");
        html = html.replaceAll("(?<!_)_(?!_)(.+?)(?<!_)_(?!_)", "<i>$1</i>");

        // Inline code
        html = html.replaceAll("`([^`]+)`", "<code>$1</code>");

        // Links [text](url)
        html = html.replaceAll("\\[(.+?)\\]\\((https?://[^\\s)]+)\\)", "<a href=\"$2\">$1</a>");

        // Lists
        html = html.replaceAll("(?m)^\\s*[-*]\\s+(.+)$", "<li>$1</li>");
        html = html.replaceAll("(?s)(<li>.*?</li>)", "<ul>$1</ul>");

        // Paragraphs (very naive)
        html = "<html><body style='font-family:Segoe UI, Sans-Serif;'>" +
                html.replaceAll("(?m)^(?!<h\\d>|<ul>|<li>|</ul>|<p>|</p>|<code>|</code>|<b>|</b>|<i>|</i>|<a |</a>)(.+)$", "<p>$1</p>") +
                "</body></html>";
        return html;
    }
}
