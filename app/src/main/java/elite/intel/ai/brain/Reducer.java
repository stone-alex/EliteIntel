package elite.intel.ai.brain;

import java.util.*;
import java.util.stream.Collectors;

public class Reducer {

    /// these words will pass through in "ignore" mode.
    public final static List<String> passThroughWords = List.of("listen", "wake", "wake up");


    /// trash
    public final static List<String> trashSttWords = List.of(
            "--", "mm-hmm", "uh-huh", "hmm", "mm", "uh", "um", "ah", "oh", "huh", "eh",

            "yeah", "yep", "yup", "nope", "it", "an", "cool", "the",
            "okay", "ok", "got it", "alright", "alrighty", "sure", "right",
            "hello", "hi", "hey", "bye", "goodbye",
            "so", "well", "now", "anyway", "actually", "basically", "literally",
            "thanks", "thank you", "i'm sorry", "sorry", "excuse me", "pardon",
            "you know", "i see", "i mean", "of course", "no problem",
            "i got it", "don't i", "a ", "or ", "she can", "he can", "you can",
            "like they", "did you", "wh", "i'll", "like", "got a",
            "blow", "fuck", "shit", "just", "i "
    );


    /// George Carlin list
    private static final Set<String> STOP_WORDS = Set.of(
            /// George Carlin list
            "blow", "fuck", "shit", "piss", "cunt", "cock", "cocksucker", "motherfucker",

            /// Stop words
            "a", "an", "the", "to", "of", "in", "on", "at", "by", "for",
            "with", "and", "or", "is", "are", "am", "be", "do", "does",
            "what", "where", "how", "which", "any", "our", "my", "me",
            "we", "us", "i", "you", "it", "this", "that", "get", "have",
            "has", "can", "could", "would", "should", "not", "no", "up",
            "here", "there", "some", "much", "many"
    );


    /**
     * Filters a map of key-value pairs based on a normalized input string.
     * Input words are extracted from the normalized input, converted to lowercase,
     * and filtered to exclude short words and stop words. The method returns a
     * new map containing only entries whose keys contain at least one of the
     * filtered words from the input.
     *
     * @param normalizedInput the cleaned user input string used for filtering; can be null or blank,
     *                        in which case the full input map is returned.
     * @param full            the complete map of key-value pairs to be filtered.
     * @return a filtered map containing entries from the input map whose keys match the filtering criteria.
     */
    public static Map<String, String> reduce(String normalizedInput, Map<String, String> full) {
        if (normalizedInput == null || normalizedInput.isBlank()) return full;

        Set<String> inputWords = Arrays.stream(normalizedInput.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());

        if (inputWords.isEmpty()) return full;

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : full.entrySet()) {
            String keyLower = entry.getKey().toLowerCase();
            for (String word : inputWords) {
                if (keyLower.contains(word)) {
                    result.put(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
        return result;
    }

    public static String formatActions(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("ACTIONS (use ONLY these exact action names):\n\n");
        map.forEach((key, action) ->
                sb.append("  ").append(action).append(" ← ").append(key).append("\n"));
        return sb.toString();
    }
}
