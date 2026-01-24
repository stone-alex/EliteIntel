package elite.intel.db;

import elite.intel.db.dao.CommodityDao;
import elite.intel.db.dao.MaterialNameDao;
import elite.intel.db.dao.SubSystemDao;
import elite.intel.db.util.Database;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FuzzySearch {


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
