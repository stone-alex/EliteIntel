package elite.intel.ai.brain;

import java.util.List;

public class AIConstants {
    public final static List<String> passThroughWords = List.of("listen", "ignore", "ignoring", "wake", "wake up");

    /**
     * List of trash NVIDIA Parakeet likes to pre-pend and pollute the STT with.
     *
     */
    public final static List<String> blockWords = List.of(
            "mm-hmm.", "uh-huh.", "eh.", "yeah.", "did you", "wh", "now",
            "okay.", "got it.", "uh.", "the", "did", "you", "blow", " a ",
            "hello?", "did you", "it", "she can", "we", "?", "hmm.", "for",
            "he can", "you can", "my", "i'll", "did you", "mm.", "don't i",
            "fuck", "shit", "keep", "can", "click", "if", "do", "?"
    );


    public final static String TYPE_ACTION = "action";
    public final static String PROPERTY_TEXT_TO_SPEECH_RESPONSE = "text_to_speech_response";
    public final static String PROPERTY_CONTENT = "content";
    public final static String PROPERTY_MESSAGE = "message";

    public final static String ROLE_SYSTEM = "system";
    public final static String ROLE_USER = "user";
    public static final String PARAMS = "params";
}
