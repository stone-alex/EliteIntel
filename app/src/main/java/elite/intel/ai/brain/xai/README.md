# Grok Implementation (`elite.intel.ai.brain.xai`)

This package provides the Grok-specific implementation for EliteIntel,
a TOS-compliant Quality-of-Life (QoL) companion app for *Elite Dangerous*.
It connects to xAI's Grok API (`https://api.x.ai/v1/chat/completions`)
to process user inputs (voice transcribed to text by STT impl) and deliver relevant responses.

## Purpose
The `grok` package handles communication with Grok’s API, routing user inputs to specific
endpoints (commands, queries, chats, or analysis) and integrating with the EliteIntel
AI pipeline (`elite.intel.ai.brain`). It:
- Sends HTTP POST requests to Grok’s chat completions endpoint.
- Builds JSON payloads and parses responses for TTS output or game actions (e.g., toggling cargo scoop).
- Supports commands and queries defined in `AiRequestHints` (e.g., `CARGO_SCOOP`, `ANALYZE_CURRENT_FUEL_STATUS`).
- Classifies input as chat if no game-relevant command or query is detected.

## Key Components
The package contains seven classes, enabling Grok integration:
- **`GrokCommandEndPoint.java`**: Processes commands (e.g., “Toggle cargo scoop”) via `@Subscribe` on `UserInputEvent`, aware of conversation history.
- **`GrokQueryEndPoint.java`**: Handles queries (e.g., “Do we have any drinks in our cargo?”), supports follow-up questions.
- **`GrokChatEndPoint.java`**: Manages conversational chats, maintaining history.
- **`GrokAnalysisEndpoint.java`**: Processes analytical queries (e.g., mission or scan analysis).
- **`GrokResponseRouter.java`**: Routes Grok responses to handlers in `elite.intel.ai.brain.handlers` (e.g., `AnalyzeCargoHoldHandler`).
- **`GrokContextFactory.java`**: Serves as a factory for generating prompts, instructions, and contextual responses for the AI system.
  It implements the contract of the `AiContextFactory` interface and includes additional methods for handling
  specific contextual data and operations. This class encapsulates utility methods and data to manage contextual
  information for AI-driven responses.
- **Utility Class** (e.g., client or config): Manages HTTP requests (using `java.net.http.HttpClient`) or authentication.

## How It Works
1. **Input Handling**: The AI pipeline (`elite.intel.ai.brain`) captures user inputs via `UserInputEvent`,
   analyzes, and routes them to the appropriate endpoint (command, query, chat, or analysis).
2. **Payload Construction**: `GrokContextFactory` builds prompts for commands, queries, chats, and analysis.
3. **API Interaction**: HTTP requests are sent to Grok’s API with xAI-specific headers (e.g., `Authorization: Bearer <key>`).
4. **Response Processing**: `GrokResponseRouter` directs responses to handlers that either handle game controls or aggregate and
   provide data for AI analysis. The results are sent to TTS processing by publishing a `VoiceProcessEvent`.
5. **Error Handling**: Failures (e.g., auth or JSON errors) are logged, with fallbacks to non-AI responses.

## For Contributors
To add an alternative LLM (e.g., for users with other LLM accounts):
1. **Use `ApiFactory`**: The `ApiFactory` singleton (`elite.intel.ai`) loads the LLM based on a configuration key.
   Create a new package (e.g., `elite.intel.ai.brain.newllm`) and register it via `ApiFactory`.
   NOTE: No constructor dependencies allowed. The factory will instantiate the class on demand via reflection.

2. **Support Commands and Queries**: Ensure your implementation is capable of supporting all commands and queries from
   `AiRequestHints.COMMANDS` and `AiRequestHints.QUERIES` (e.g., `CARGO_SCOOP`, `ANALYZE_CURRENT_FUEL_STATUS`).
   Use `generateSupportedCommandsCause()` to include supported commands in prompts, as shown
   in `GrokCommandEndPoint`, and `generateQueryPrompt()` for queries. Handler registration is handled automatically.
   Other commands and queries may be added to the list. Your implementation must be able to support future commands and queries.
   Do not hard-code any command or query specifics in your implementation.

3. **Implement Pipeline Contracts**: Adhere to interfaces in `elite.intel.ai.brain` for command, query, chat, and analysis handling. Your implementation must:
    - Support the existing JSON schema (e.g., `messages` array, `response_text` for vocalizing responses, `tool` calls for deep processing, and `action` for game actions).
    - Handle JSON-formatted data provided by the handlers.
    - Maintain conversation history for commands, queries, and chats.
    - Integrate with Handlers via `CommandHandler handler = commandHandlers.get(action);` and `QueryHandler handler = queryHandlers.get(action);`

4. **Test Thoroughly**: Validate with *Elite Dangerous* scenarios to ensure TOS compliance and seamless voice/game integration.

## <span style='color: red'>**Warning**:</span>

### **Do not** modify the endpoint URL in the Grok client (e.g., to another LLM’s API). This will cause:
- **Authentication Errors**: Grok uses xAI-specific Bearer tokens, incompatible with other APIs (e.g., 401 Unauthorized).
- **Schema Mismatches**: Other LLMs expect different JSON formats, leading to 400 Bad Request errors.
- **Parsing Failures**: Incorrect response formats may cause crashes or garbled voice output (e.g., `NullPointerException`).
  Instead, implement a new LLM via `ApiFactory` to maintain modularity.

## Development Notes
- **Dependencies**: Built with pure Java (`java.net.http`) and libraries in `app/build.gradle`. No JNI, DLLs, or Python allowed.
- **Commands and Queries**: Defined in `AiRequestHints` (`COMMANDS`, `QUERIES`) from `GameCommands`, `CustomCommands`, and `QueryActions`. Queries may require follow-ups.
- **Testing**: Test with *Elite Dangerous* journal data and voice output for stability and TOS compliance.

## Contact project owner for questions
Submit a pull request, open an issue on GitHub, or contact me via [Discord](https://discord.gg/3qAqBENsBm).