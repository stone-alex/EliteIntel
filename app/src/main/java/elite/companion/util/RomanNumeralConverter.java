package elite.companion.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RomanNumeralConverter {

    public static String convertRomanInName(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Regular expression to match Roman numerals (I, V, X, L, C, D, M)
        // Matches standalone Roman numerals or those following "Mk"/"MK" (case-insensitive)
        Pattern pattern = Pattern.compile("\\b(MK|Mk|mk)?\\s*-?\\s*([IVXLCDM]+)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // Extract the Roman numeral part
            String roman = matcher.group(2);
            // Convert Roman numeral to integer
            int number = romanToInt(roman);
            // Get the prefix (e.g., "Mk", "MK", or empty)
            String prefix = matcher.group(1) != null ? matcher.group(1) + " " : "";
            // Replace the matched part with prefix + number
            matcher.appendReplacement(result, prefix + number);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static int romanToInt(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }

        // Map of Roman numeral values
        Map<Character, Integer> romanValues = new HashMap<>();
        romanValues.put('I', 1);
        romanValues.put('V', 5);
        romanValues.put('X', 10);
        romanValues.put('L', 50);
        romanValues.put('C', 100);
        romanValues.put('D', 500);
        romanValues.put('M', 1000);

        int result = 0;
        int prevValue = 0;

        // Iterate from right to left
        for (int i = s.length() - 1; i >= 0; i--) {
            char currentChar = s.charAt(i);
            if (!romanValues.containsKey(currentChar)) {
                throw new IllegalArgumentException("Invalid Roman numeral character: " + currentChar);
            }
            int currentValue = romanValues.get(currentChar);

            if (currentValue >= prevValue) {
                result += currentValue;
            } else {
                result -= currentValue;
            }
            prevValue = currentValue;
        }

        return result;
    }

    public static void main(String[] args) {
        // Test cases
        String[] tests = {
                "Viper Mk IV",
                "Cobra MK-III",
                "Anaconda Mk X",
                "MK IV Shield",
                "Type-IX Trader",
                "Invalid Mk IIX", // Invalid Roman numeral
                "No Roman Numeral"
        };

        for (String test : tests) {
            try {
                System.out.println(test + " -> " + convertRomanInName(test));
            } catch (IllegalArgumentException e) {
                System.out.println(test + " -> Error: " + e.getMessage());
            }
        }
    }
}