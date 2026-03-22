package elite.intel.util;

import elite.intel.session.PlayerSession;

import java.time.LocalDateTime;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToNull;

public class StringUtls {


    public static String subtractString(String a, String b) {
        if (a == null || b == null) return "";
        return a.replace(b, "").replace("null", "").trim();
    }


    public static Integer getIntSafely(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }


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

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String isFuelStarClause(String starClass) {
        if (starClass == null) {
            return "";
        }
        boolean isFuelStar = "KGBFOAM".contains(starClass);
        return isFuelStar ? " Fuel Available. " : " Warning! - No fuel Available at next stop. ";
    }

    private static int getHourOfDay() {
        return LocalDateTime.now().getHour();
    }

    public static String greeting(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return "Hello, Commander!";
        }

        int hour = getHourOfDay();
        String timeGreeting;

        if (hour >= 5 && hour < 12) {
            timeGreeting = "Good morning";
        } else if (hour >= 12 && hour < 18) {
            timeGreeting = "Good afternoon";
        } else if (hour >= 18 && hour < 22) {
            timeGreeting = "Good evening";
        } else {
            timeGreeting = "Good evening";
        }

        return timeGreeting + ", " + playerName + "!";
    }


    /**
     * Converts a version string into a numeric format, preserving up to three version components
     * (e.g., major, minor, patch-build) while padding each component to four digits. The resulting
     * numeric representation is capped at 12 digits.
     *
     * @param version the version string to convert. If null, 0 is returned.
     * @return a long representing the numeric version, or 0 if input is null or cannot be processed.
     */
    public static long getNumericBuild(String version) {
        if (version == null) return 0L;
        // Remove non-digits and non-dots
        String cleaned = version.replaceAll("[^\\d.]", "");
        String[] parts = cleaned.split("\\.");
        // Take last up to 3 parts (major/minor/patch-build), pad to 4 digits each
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, parts.length - 3);
        for (int i = start; i < parts.length; i++) {
            sb.append(String.format("%4s", parts[i]).replace(' ', '0'));
        }
        // If less than 3, prepend zeros (e.g., 0172 -> 00000172)
        while (sb.length() < 12) {
            sb.insert(0, "0000");
        }
        return Long.parseLong(sb.substring(0, 12));
    }

    public static String sanitizeTts(String input) {
        if (input == null) return "";
        return input
                .replaceAll("[^\\x00-\\x7F]", "")               // strip non-ASCII (emojis, unicode)
                .replaceAll("\\*{1,2}([^*\n]*?)\\*{1,2}", "$1") // **bold** / *italic* → plain
                .replaceAll("_([^_\n]*?)_", "$1")                // _italic_ → plain
                .replaceAll("~~([^~\n]*?)~~", "$1")              // ~~strikethrough~~ → plain
                .replaceAll("`{1,3}[^`\n]*`{1,3}", "")          // `code` / ```block``` → remove
                .replaceAll("(?m)^#{1,6}\\s*", "")              // # headings → remove marker
                .replaceAll("(?m)^>\\s?", "")                   // > blockquotes → remove marker
                .replace("\\n", " ").replace("\\r", " ")        // literal escape sequences from LLM
                .replaceAll("[\\r\\n]+", " ")                    // actual newline characters → space
                .replaceAll("(?<=\\S)-(?=\\S)", " ")             // "ninety-five" → "ninety five" (hyphen between chars)
                .replace("*", " ")                              // any stray asterisks
                .replace("`", "")                               // any stray backticks
                .replace("\"", "")
                .replace("[", "").replace("]", "")
                .replace("ETA", ". E.T.A.")
                .replace(":", " - ")
                .replaceAll("\\s{2,}", " ")                     // collapse repeated spaces
                .trim();
    }

    public static String normalizeVersion(String v) {
        if (v == null) return "";
        return v.replaceAll("[\\r\\n]+", "").trim();
    }

    /*
    Remove underscores and seperate Camelcase
    */
    public static String humanizeBindingName(String gameBinding) {
        return gameBinding
                .replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .replace("HUD", "HUD ")
                .replaceAll("(?<=\\D)(?=\\d)", " ")
                .replaceAll("_", " ");
    }


    public static String toReadableModuleName(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String withSpaces = input.replace('_', ' ');

        String[] words = withSpaces.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }

        String result = sb.toString().trim();

        if (result.endsWith(" Fdl")) {
            result = result.substring(0, result.length() - 3) + "FDL";
        }

        return result;
    }

    public static String removeNameEnding(String missionName) {
        return missionName
                .replace("_name", "")       // Remove the _name
                .replaceAll("_\\d+$", "");  // Remove the _00x
    }


    public static String affirmative() {
        List<String> result = Arrays.stream(
                new String[]{"On it!", "affirmative!", "aye-aye!", "certainly!", "of course!", "rightaway!"}
        ).filter(Objects::nonNull).toList();
        if (result.isEmpty()) {
            return "Commander";
        }

        return result.get(new Random().nextInt(result.size()));
    }

    public static String player(PlayerSession playerSession) {
        String alternativeName = playerSession.getAlternativeName();
        String playerName = trimToNull(alternativeName) != null ? alternativeName : playerSession.getPlayerName();
        String playerMilitaryRank = playerSession.getPlayerHighestMilitaryRank();
        String playerHonorific = Ranks.getPlayerHonorific();

        List<String> result = Arrays.stream(
                new String[]{alternativeName, playerHonorific, playerName, playerMilitaryRank}
        ).filter(Objects::nonNull).toList();
        if (result.isEmpty()) {
            return "Commander";
        }

        return result.get(new Random().nextInt(result.size()));
    }
}
