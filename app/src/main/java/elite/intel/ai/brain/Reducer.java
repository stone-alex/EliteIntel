package elite.intel.ai.brain;

import java.util.*;
import java.util.stream.Collectors;

public class Reducer {

    /// these words will pass through in "ignore" mode.
    public final static List<String> passThroughWords = List.of("listen", "wake", "wake up");


    /// trash
    public final static List<String> trashSttWords = List.of(
            "--", "mm-hmm", "uh-huh", "hmm", "mm", "uh", "um", "ah", "oh", "huh", "eh"
    );


    /// George Carlin list
    private static final Set<String> STOP_WORDS = Set.of(
            "blow", "fuck", "shit", "piss", "cunt", "cock", "cocksucker", "motherfucker"
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
