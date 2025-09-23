package elite.intel.util;

public class StringUtls {

    public static String subtractString(String a, String b) {
        if (a == null || b == null) return "";
        return a.replace(b, "");
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
}
