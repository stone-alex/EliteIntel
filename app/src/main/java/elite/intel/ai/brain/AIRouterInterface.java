package elite.intel.ai.brain;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

/**
 * The AIRouterInterface defines a contract for managing AI routing operations,
 * including starting and stopping the AI router process, as well as handling
 * and processing AI-generated responses.
 * <p>
 * The responsibilities of the interface include:
 * <p>
 * - Starting the AI router process to initialize necessary components.
 * - Stopping the AI router process to release resources and shut down operations.
 * - Processing AI responses, which involves interpreting the data returned by the AI,
 * and optionally incorporating user-provided input for context when available.
 * <p>
 * Methods:
 * <p>
 * - start(): Initializes the AI router and its associated resources. This method
 * may throw an exception if initialization fails.
 * <p>
 * - stop(): Shuts down the AI router and cleans up resources. This method is
 * responsible for ensuring that the router ceases all operations cleanly.
 * <p>
 * - processAiResponse(JsonObject jsonResponse, @Nullable String userInput): Handles
 * the processing of an AI-generated response. The `jsonResponse` parameter contains
 * the data returned by the AI system, while the optional `userInput` parameter
 * provides additional context for processing.
 */
public interface AIRouterInterface {
    void processAiResponse(JsonObject jsonResponse, @Nullable String userInput);
}
