package elite.intel.ai.brain;

import java.util.List;

public class AIConstants {
    /// NOT FINAL, EDITED ON START
    public static List<String> passThroughWords = List.of("ship", "listen", "ignore", "stop ignoring");

    public final static String TYPE_ACTION = "action";
    public final static String PROPERTY_text_to_speech_response = "text_to_speech_response";
    public final static String PROPERTY_CONTENT = "content";
    public final static String PROPERTY_MESSAGE = "message";

    public final static String ROLE_SYSTEM = "system";
    public final static String ROLE_USER = "user";
    public static final String PARAMS = "params";
}
