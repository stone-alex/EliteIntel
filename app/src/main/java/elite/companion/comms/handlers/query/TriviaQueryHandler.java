package elite.companion.comms.handlers.query;

import com.google.gson.JsonObject;

/**
 * TriviaQueryHandler is responsible for handling trivia-related queries. It generates a response
 * for general knowledge questions, utilizing information outside of the Elite Dangerous universe
 * unless explicitly stated in the query.
 * <p>
 * This handler transforms the user's original input into a properly formatted query,
 * intended for conversational purposes. The response includes properties to guide further
 * interactions, such as follow-up expectations and a placeholder for response text to avoid
 * immediate text-to-speech execution.
 * <p>
 * Implements the QueryHandler interface and must conform to its handle method, which processes
 * user input and generates an appropriate JSON response.
 * <p>
 * Key functionalities:
 * - Formats the user's input to focus on general trivia.
 * - Generates an empty response text to delay immediate TTS usage.
 * - Includes metadata like the original query and follow-up expectations.
 * - Marks the action type as "general_conversation."
 * <p>
 * Exceptions:
 * Throws {@code Exception} if any error occurs during the query processing.
 */
public class TriviaQueryHandler implements QueryHandler {

    /**
     * Handles a general trivia query by formatting the original user input into a structured
     * query and returning a JSON response. The response contains metadata including the formatted query,
     * an empty placeholder for response text (to prevent premature text-to-speech execution),
     * and indicators for follow-up actions.
     *
     * @param action the action type indicating the query being handled, expected to be "general_conversation"
     *               or similar context-related descriptor.
     * @param params a JSON object containing additional parameters relevant to the query, if any.
     * @param originalUserInput the raw input provided by the user, representing the trivia query.
     * @return a {@code JsonObject} containing the structured response including the formatted query,
     *         empty response text, action type, follow-up expectations, and other metadata.
     * @throws Exception if an error occurs during processing the query.
     */
    @Override public JsonObject handle(String action, JsonObject params, String originalUserInput) throws Exception {
        JsonObject response = new JsonObject();
        String query = "Answer using your general trivia knowledge, outside Elite Dangerous unless explicitly mentioned: " + originalUserInput;
        response.addProperty("response_text", ""); // Empty to avoid premature TTS
        response.addProperty("action", "general_conversation");
        response.add("params", new JsonObject());
        response.addProperty("expect_followup", true);
        response.addProperty("original_query", query);
        return response;
    }
}
