package elite.intel.ai.brain;

import java.util.List;

public class AIConstants {
    public final static List<String> passThroughWords = List.of("listen", "ignore", "ignoring");
    public final static List<String> blockWords = List.of(
            "mm-hmm.", "uh-huh.", "eh.", "yeah.",
            "okay.", "got it.", "did you", "uh.",
            "hello?", "did you", "it", "she can",
            "he can", "you can", "my", "i'll"
    );

    public final static String TYPE_ACTION = "action";
    public final static String PROPERTY_TEXT_TO_SPEECH_RESPONSE = "text_to_speech_response";
    public final static String PROPERTY_CONTENT = "content";
    public final static String PROPERTY_MESSAGE = "message";

    public final static String ROLE_SYSTEM = "system";
    public final static String ROLE_USER = "user";
    public static final String PARAMS = "params";
}
