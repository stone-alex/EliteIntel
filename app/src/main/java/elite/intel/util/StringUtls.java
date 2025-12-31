package elite.intel.util;

import elite.intel.db.dao.CommodityDao;
import elite.intel.db.dao.MaterialNameDao;
import elite.intel.db.dao.SubSystemDao;
import elite.intel.db.util.Database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

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


    public static String isFuelStarClause(String starClass) {
        if (starClass == null) {
            return "";
        }
        boolean isFuelStar = "KGBFOAM".contains(starClass);
        return isFuelStar ? " a Fuel Star. " : " Warning! - Not a Fuel Star! ";
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


    public static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(s1.charAt(i - 1), s2.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
        return java.util.Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }


    public static String fuzzyCommodityMatch(String input, int similarity) {
        return fuzzyMatch(input, similarity, CommodityDao.class, CommodityDao::getAllNamesLowerCase, CommodityDao::getOriginalCase);
    }

    public static String fuzzyMaterialSearch(String input, int similarity) {
        return fuzzyMatch(input, similarity, MaterialNameDao.class, MaterialNameDao::getAllNamesLowerCase, MaterialNameDao::getOriginalCase);
    }

    public static String fuzzySubSystemSearch(String input, int similarity) {
        return fuzzyMatch(input, similarity, SubSystemDao.class, SubSystemDao::getAllNamesLowerCase, SubSystemDao::getOriginalCase);
    }


    /// re-use for other fuzzy search
    private static <T> String fuzzyMatch(String input, int similarity, Class<T> daoClass, Function<T, List<String>> candidatesProvider, BiFunction<T, String, String> originalCaseProvider) {
        if (input == null || input.isBlank()) return null;

        final String lowerInput = input.trim().toLowerCase();
        List<String> candidates = Database.withDao(daoClass, candidatesProvider);

        String bestLower = null;
        int bestDist = Integer.MAX_VALUE;

        for (String c : candidates) {
            int dist = levenshteinDistance(lowerInput, c);
            if (dist < bestDist) {
                bestDist = dist;
                bestLower = c;
            }
        }

        if (bestDist <= similarity && bestLower != null) {
            final String finalBestLower = bestLower;
            return Database.withDao(daoClass, dao -> originalCaseProvider.apply(dao, finalBestLower));
        }

        return null;
    }
}
