package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;

public class ConversationalQueryHandler implements QueryHandler {
    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        JsonObject response = new JsonObject();
        String query = "Answer using your general knowledge, outside Elite Dangerous unless explicitly mentioned: " + originalUserInput;
        response.addProperty("response_text", ""); // Empty to avoid premature TTS
        response.addProperty("action", "general_conversation");
        response.add("params", new JsonObject());
        response.addProperty("expect_followup", true);
        response.addProperty("original_query", query);
        return response;
    }
}