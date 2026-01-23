package elite.intel.ai.brain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * AIChatInterface defines a contract for sending messages to an AI system and
 * receiving responses in JSON format. This interface is designed for use in
 * scenarios where structured communication with an AI service is necessary,
 * providing a method to handle message transmission and response parsing.
 * <p>
 * The `sendToAi` method accepts a message history encoded as a JsonArray,
 * where each message contains attributes such as role (e.g., system, user)
 * and content. The AI system processes this input and returns a parsed
 * JsonObject response that contains the AI's output.
 * <p>
 * Implementations of this interface may include error-handling mechanisms
 * to ensure null or invalid responses are properly managed. This makes it
 * suitable for integration in various applications requiring AI-assisted
 * communication or decision-making.
 */
public interface AIChatInterface {
    JsonObject processAiPrompt(JsonArray messages);
}
