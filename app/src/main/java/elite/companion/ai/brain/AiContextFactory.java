package elite.companion.ai.brain;

/**
 * The AiContextFactory interface defines a contract for creating various types of prompts and instructions
 * that guide the behavior of an AI system in different contexts. It also provides a method to append behaviors
 * or additional context to a StringBuilder for dynamic context generation. Implementations of this interface
 * are responsible for generating context-specific prompts based on inputs such as sensor data, user intents,
 * and user inputs.
 * <p>
 * Methods:
 * <p>
 * - generateSystemInstructions(String sensorInput): Generates system-level instructions tailored to the input
 * gathered from sensors. This can be used to provide the AI with situational context or state awareness.
 * <p>
 * - generateQueryPrompt(): Creates a prompt designed to elicit or clarify queries from the system or user.
 * <p>
 * - generateSystemPrompt(): Produces a prompt that aligns with the AI's strategic or operational goals.
 * This might involve setting up the AI's functional or procedural parameters.
 * <p>
 * - generateAnalysisPrompt(String userIntent, String dataJson): Constructs a prompt aimed at analyzing data
 * based on a specific user intent and raw data input encoded as a JSON string. This helps the AI to understand
 * the purpose and context of the analysis task.
 * <p>
 * - appendBehavior(StringBuilder sb): Appends behavior-specific information to the given StringBuilder,
 * allowing for the dynamic construction of context that reflects the AI's expected behavioral tone or cadence.
 * <p>
 * - generatePlayerInstructions(String playerVoiceInput): Generates instructions for the player based on
 * their voice input, enabling the system to respond contextually to the player's commands or inquiries.
 */
public interface AiContextFactory {
    String generateSystemInstructions(String sensorInput);
    String generateQueryPrompt();
    String generateSystemPrompt();
    String generateAnalysisPrompt(String userIntent, String dataJson);
    void appendBehavior(StringBuilder sb);
    String generatePlayerInstructions(String playerVoiceInput);
}
