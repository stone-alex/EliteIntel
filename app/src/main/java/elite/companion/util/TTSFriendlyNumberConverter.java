package elite.companion.util;

/**
 * Utility class for converting numbers into text-to-speech (TTS)-friendly
 * representations for amounts. Primarily designed to assist in generating
 * easily understandable phrases for numerical bounties or values.
 */
public class TTSFriendlyNumberConverter {

    public static String formatBountyForSpeech(int bounty) {
        if (bounty <= 0) return "No bounty";

        // For small amounts, round to nearest hundred (easier to say)
        if (bounty < 1_000) {
            int rounded = Math.round(bounty / 100f) * 100;
            return "about " + smallNumberToWords(rounded) + " credits";
        }

        // Round to one significant digit for 1k and above
        int rounded = roundToOneSignificant(bounty);

        // Express in words with scale (thousand/million/billion)
        if (rounded >= 1_000_000_000) {
            int billions = rounded / 1_000_000_000;
            return "roughly " + smallNumberToWords(billions) + " billion credits";
        } else if (rounded >= 1_000_000) {
            int millions = rounded / 1_000_000;
            return "roughly " + smallNumberToWords(millions) + " million credits";
        } else {
            int thousands = rounded / 1_000;
            // thousands can be up to 999 after rounding to one significant digit
            return "roughly " + smallNumberToWords(thousands) + " thousand credits";
        }
    }

    // Rounds positive integer to 1 significant digit (e.g., 322540 -> 300000)
    private static int roundToOneSignificant(int n) {
        if (n <= 0) return 0;
        int magnitude = (int) Math.floor(Math.log10(n));
        int scale = (int) Math.pow(10, magnitude);
        int first = Math.round(n / (float) scale);
        return first * scale;
    }

    // Converts 1..999 that are multiples of 1, 10, or 100 into words (compact)
    private static String smallNumberToWords(int n) {
        if (n == 0) return "zero";
        // Handle exact hundreds and tens we produce via rounding
        if (n >= 100) {
            int hundreds = n / 100;
            int remainder = n % 100;
            String base = unitsWord(hundreds) + " hundred";
            if (remainder == 0) return base;
            return base + " " + tensWord(remainder); // remainder will be multiple of 10 in our usage
        } else if (n >= 20) {
            return tensWord(n);
        } else if (n >= 10) {
            return teensWord(n);
        } else {
            return unitsWord(n);
        }
    }

    private static String unitsWord(int n) {
        switch (n) {
            case 1:
                return "one";
            case 2:
                return "two";
            case 3:
                return "three";
            case 4:
                return "four";
            case 5:
                return "five";
            case 6:
                return "six";
            case 7:
                return "seven";
            case 8:
                return "eight";
            case 9:
                return "nine";
            default:
                return String.valueOf(n);
        }
    }

    private static String teensWord(int n) {
        switch (n) {
            case 10:
                return "ten";
            case 11:
                return "eleven";
            case 12:
                return "twelve";
            case 13:
                return "thirteen";
            case 14:
                return "fourteen";
            case 15:
                return "fifteen";
            case 16:
                return "sixteen";
            case 17:
                return "seventeen";
            case 18:
                return "eighteen";
            case 19:
                return "nineteen";
            default:
                return String.valueOf(n);
        }
    }

    private static String tensWord(int n) {
        // Assumes n is a multiple of 10 (common after rounding), but handles 20..99
        int tens = n / 10;
        int ones = n % 10;
        String tenWord;
        switch (tens) {
            case 2:
                tenWord = "twenty";
                break;
            case 3:
                tenWord = "thirty";
                break;
            case 4:
                tenWord = "forty";
                break;
            case 5:
                tenWord = "fifty";
                break;
            case 6:
                tenWord = "sixty";
                break;
            case 7:
                tenWord = "seventy";
                break;
            case 8:
                tenWord = "eighty";
                break;
            case 9:
                tenWord = "ninety";
                break;
            case 1:
                return teensWord(n); // 10..19
            default:
                return String.valueOf(n);
        }
        if (ones == 0) return tenWord;
        return tenWord + " " + unitsWord(ones);
    }

}
