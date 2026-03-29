package elite.intel.ai.brain;

import java.util.List;

public class AIConstants {
    public final static List<String> passThroughWords = List.of("listen", "ignore", "ignoring", "wake", "wake up");

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
            "it",
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
            "i got it", "don't i",
            // Hallucinated subject fragments
            "she can", "he can", "you can", "like they", "did you",
            "wh", "i'll", "like",
            // Pure garbage tokens
            "blow", "fuck", "shit", "just"
    );


    public final static String TYPE_ACTION = "action";
    public final static String PROPERTY_TEXT_TO_SPEECH_RESPONSE = "text_to_speech_response";
    public final static String PROPERTY_CONTENT = "content";
    public final static String PROPERTY_MESSAGE = "message";

    public final static String ROLE_SYSTEM = "system";
    public final static String ROLE_USER = "user";
    public static final String PARAMS = "params";
}
