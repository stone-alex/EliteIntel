package elite.intel.ai.brain;

import java.util.List;

public class AIConstants {
    /// NOT FINAL, EDITED ON START
    public static List<String> passThroughWords = List.of("computer", "ship", "listen");

    public final static String TYPE_ACTION = "action";
    public final static String PROPERTY_RESPONSE_TEXT = "response_text";
    public final static String PROPERTY_CONTENT = "content";
    public final static String PROPERTY_MESSAGE = "message";

    public final static String ROLE_SYSTEM = "system";
    public final static String ROLE_USER = "user";
    public static final String PARAMS = "params";


    /// no longer used
    // public final static String ROLE_TOOL = "tool";
    // public final static String PROPERTY_ORIGINAL_QUERY = "original_query";
}
