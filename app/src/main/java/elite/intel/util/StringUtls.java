package elite.intel.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtls {

    private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:live/|watch\\?v=|v/|embed/|shorts/)|youtu\\.be/)([\\w-]{11})(?:\\?[^\\s]*)?"
    );


    public static String extractVideoId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        Matcher matcher = YOUTUBE_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1); // Group 1 is the video ID
        }
        return null;
    }


    public static String subtractString(String a, String b) {
        if (a == null || b == null) return "";
        return a.replace(b, "").replace("null", "").trim();
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


    public static String isFuelStarClause(String starClass) {
        if (starClass == null) {
            return "";
        }
        boolean isFuelStar = "KGBFOAM".contains(starClass);
        return isFuelStar ? " a Fuel Star. " : " Warning! - Not a Fuel Star! ";
    }

}
