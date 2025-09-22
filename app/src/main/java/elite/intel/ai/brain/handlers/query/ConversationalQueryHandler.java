package elite.intel.ai.brain.handlers.query;

import com.google.gson.JsonObject;
import elite.intel.ai.brain.AIConstants;

public class ConversationalQueryHandler implements QueryHandler {
    /**
     * Handles the provided action and user input, generating a JSON response based on predefined
     * behavior for general conversational queries. The method constructs a response tailored for
     * general knowledge questions, integrating user input into a formatted query.
     *
     * @param action            The action string defining the type of query to be handled.
     *                          For this implementation, it is typically "general_conversation".
     * @param params            A JsonObject containing additional parameters for the query.
     *                          Typically empty or unused in this context.
     * @param originalUserInput The raw user input that forms the basis of the query response.
     *                          This input is processed into a general knowledge query.
     * @return A JsonObject containing the response structure, including placeholder
     * response text, action type, additional parameters, a flag indicating
     * follow-up requirements, and the original formatted query.
     * @throws Exception If an error occurs while handling the action or processing the input.
     */
    @Override
    public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        JsonObject response = new JsonObject();
        String query = "Answer using your general knowledge, outside Elite Dangerous unless explicitly mentioned: " + originalUserInput;
        response.addProperty(AIConstants.PROPERTY_RESPONSE_TEXT, ""); // Empty to avoid premature TTS
        response.addProperty(AIConstants.TYPE_ACTION, "general_conversation");
        response.add("params", new JsonObject());
        response.addProperty(AIConstants.PROPERTY_EXPECT_FOLLOWUP, true);
        response.addProperty(AIConstants.PROPERTY_ORIGINAL_QUERY, query);
        return response;
    }
}