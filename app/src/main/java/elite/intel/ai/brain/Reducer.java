package elite.intel.ai.brain;

import java.util.*;
import java.util.stream.Collectors;

public class Reducer {

    /// these words will bass through in "ignore" mode.
    public final static List<String> passThroughWords = List.of("listen", "wake", "wake up");


    /**
     * Trash tokens NVIDIA Parakeet prepends to real utterances, or emits alone on noise.
     * Used by stripTrashPrefix(): tokens are stripped only from the START of the transcript.
     * Multi-word entries are matched as a unit (e.g. "got it" strips both tokens together).
     * Matching is punctuation-tolerant - trailing .,!?;: are ignored on both sides.
     */
    public final static List<String> trashSttWords = List.of(
            // Filler sounds
            "mm-hmm", "uh-huh", "hmm", "mm", "uh", "um", "ah", "oh", "huh", "eh",
            // Acknowledgements
            "yeah", "yep", "yup", "nope",
            "it", "an", "cool", "the",
            "okay", "ok", "got it", "alright", "alrighty",
            "sure", "right",
            // Greetings / closings Parakeet hallucinates
            "hello", "hi", "hey", "bye", "goodbye",
            // Discourse filler
            "so", "well", "now", "anyway", "actually", "basically", "literally",
            // Social phrases
            "thanks", "thank you",
            "i'm sorry", "sorry", "excuse me", "pardon",
            "you know", "i see", "i mean", "of course", "no problem",
            "i got it", "don't i", "a ", "or ",
            // Hallucinated subject fragments
            "she can", "he can", "you can", "like they", "did you",
            "wh", "i'll", "like", "got a",
            // Pure garbage tokens
            "blow", "fuck", "shit", "just", "i "
    );


    // Words too common to be useful for filtering
    private static final Set<String> STOP_WORDS = Set.of(
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
