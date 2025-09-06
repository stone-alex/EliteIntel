package elite.companion.comms.brain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * AiQueryInterface defines a contract for sending messages, in the form of a JsonArray,
 * to an AI system and receiving a processed response as a JsonObject. Each message
 * in the array is expected to consist of a "role" and "content" property defining
 * the sender and the content of the message, respectively.
 * <p>
 * This interface is designed to enable structured communication with an AI service,
 * allowing the input message history to guide the AI's response generation while
 * encapsulating processing and error-handling logic.
 * <p>
 * Implementations of this interface may handle:
 * - Message sanitization and preparation prior to sending the request.
 * - Parsing of the AI service's response into a usable JsonObject format.
 * - Error handling to manage invalid requests, parsing issues, or unexpected responses.
 * <p>
 * The sendToAi method provides a singular entry point to facilitate the exchange of
 * input and output data with the AI system.
 */
public interface AiQueryInterface {
    JsonObject sendToAi(JsonArray messages);
}
