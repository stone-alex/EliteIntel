package elite.intel.ai.brain;

import java.util.*;
import java.util.stream.Collectors;

import static elite.intel.ai.brain.actions.Commands.IGNORE_NONSENSE;
import static elite.intel.ai.brain.actions.Queries.GENERAL_CONVERSATION;

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
     * Reduces a given map of key-value pairs based on a normalized input string and a specified conversation mode flag.
     * Filters out entries in the map unless the keys contain significant words from the normalized input.
     * Ensures a fallback behavior for empty or irrelevant inputs based on the conversation mode.
     *
     * @param normalizedInput the input string that is normalized and used to filter the map; null or blank input defaults to returning the original map
     * @param full the original map of key-value pairs to be filtered
     * @param isConversationMode a flag indicating whether the method is running in conversation mode; determines fallback behavior for empty input
     * @return a reduced map containing only the entries whose keys match significant words from the normalized input;
     *         may return a map with fallback values if the input is empty or irrelevant
     */
    public static Map<String, String> reduce(String normalizedInput, Map<String, String> full, boolean isConversationMode) {
        if (normalizedInput == null || normalizedInput.isBlank()) return full;

        Set<String> inputWords = Arrays.stream(normalizedInput.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .collect(Collectors.toSet());


        Map<String, String> result = new LinkedHashMap<>();
        /// Ensure an escape hatch is present
        if (inputWords.isEmpty()) {
            if (isConversationMode) {
                result.put(GENERAL_CONVERSATION.getAction(), GENERAL_CONVERSATION.getAction());
            } else {
                result.put(IGNORE_NONSENSE.getAction(), IGNORE_NONSENSE.getAction());
            }
        }

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
