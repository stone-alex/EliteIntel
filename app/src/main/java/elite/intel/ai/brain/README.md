# Implementing a New LLM Backend in the Brain Package

The `brain` package is the extensible AI integration layer for the Elite Dangerous companion app. It routes user inputs (voice/text) to handlers for commands (e.g., key bindings like deploying landing gear), queries (e.g., analyzing ship loadout), and general chat (e.g., trivia or casual
conversation)—all while adhering to TOS compliance. It focuses on QoL features like hands-free VR control, on-demand analysis of legally available data (via journal/aux files), and natural language interaction, avoiding automation, botting, or memory reading.

The architecture follows DRY and SRP: Enums (`QueryActions`, `GameCommands`, `CustomCommands`) centralize supported actions, while factories (`QueryHandlerFactory`, `CommandHandlerFactory`) use reflection to auto-register handlers at runtime. This ensures adding a new command or query propagates to
all LLM backends without per-impl changes. No dependency injection, magic strings/numbers, or external languages—pure Java.

To add a new LLM (e.g., OpenAI, Anthropic), create a subpackage under `brain` (e.g., `openai`). Implement the required interfaces in classes mirroring the Grok structure (e.g., `OpenAiAnalysisEndpoint`, `OpenAiChatEndPoint`). Wire it via `ApiFactory` based on config. Keep it modular and thread-safe.

## Key Principles for Your Implementation

- **Modularity**: Each interface handles a single responsibility (e.g., routing vs. context generation).
- **Event-Driven**: Use `EventBusManager` for pub/sub (e.g., `@Subscribe` to `UserInputEvent` for voice commands).
- **JSON Structure**: All AI responses must use a standard schema:
    - `type`: Must be `"command"`, `"query"`, or `"chat"`. Determines handler path.
    - `action`: String matching a `GameCommand.getUserCommand()` value, `QueryActions.getAction()`, or `null` for chat.
    - `params`: `JsonObject` with action-specific data (e.g., `{"commodity": "painite"}`).
    - `response_text`: String for TTS output or feedback (can be empty for single-step commands).
    - `expect_followup`: Boolean (optional, default `false`) for queries needing re-query (e.g., analysis).
- **Handler Signature**: Command handlers (`CommandHandler`) use `handle(JsonObject params, String responseText)`. Query handlers (`QueryHandler`) use `handle(String action, JsonObject params, String originalUserInput)`. The router (`AIRouterInterface`) processes `JsonObject jsonResponse` with the
  above fields.
- **Tool Integration**: For follow-up queries, include a "tool" message with `role: "tool"`, `name: action`, and `content` instructing the LLM to format the response (e.g., set `type`, `action`, etc.).
- **Error Handling**: Log via SLF4J, return null or error JSON on failures. Sanitize inputs/outputs to avoid control characters or invalid JSON.
- **Config Integration**: Pull API keys from `ConfigManager` (e.g., `ConfigManager.getInstance().getSystemKey(ConfigManager.AI_API_KEY)`).
- **Prompting**: Use `AiContextFactory` for dynamic prompts with session state (e.g., player rank, ship data), enum-derived actions, and TOS reminders (no automation). Support game commands, game queries, and general chat.
- **TOS Compliance**: Prompts must emphasize user-initiated, real-time interactions.
- **Testing**: Handle low-confidence inputs (<0.8 → "Say again?"), follow-ups, and fallbacks to chat.

## Required Interfaces to Implement

Duplicate the Grok impl's structure, adapting API calls to your LLM. All are singletons for efficiency.

### 1. AiCommandInterface (e.g., `YourLlmCommandEndPoint`)

- **Role**: Lifecycle management and event handling for user inputs and sensor data.
- **Methods**:
    - `@Subscribe onUserInput(UserInputEvent event)`: Sanitize input, build prompt via `AiContextFactory.generatePlayerInstructions()`, send to LLM, parse into `{ "type": "...", "action": "...", "params": {...}, "response_text": "..." }`.
    - `@Subscribe onSensorDataEvent(SensorDataEvent event)`: Process sensor data, build prompt, send, format response.
- **Tips**: Use `ThreadLocal<JsonArray>` for chat history. Publish `VoiceProcessEvent` for TTS. Fallback to chat on errors. Example: "Weapons Hot!" → `{ "type": "command", "action": "weapons_hot", "params": {}, "response_text": "Hardpoints deployed." }`.
- **Grok Example**: Builds payload, extracts JSON after "\n\n{", publishes events.

### 2. AIRouterInterface (e.g., `YourLlmResponseRouter`)

- **Role**: Dispatches responses to handlers.
- **Methods**:
    - `processAiResponse(JsonObject jsonResponse, @Nullable String userInput)`: Extract `type`, `action`, `params`, `response_text`. Switch on `type`:
        - `"command"`: Call `handleCommand(action, params, responseText, jsonResponse)` using `action` matched to `GameCommand.getUserCommand()` values.
        - `"query"`: Call `handleQuery(action, params, userInput)` (handles follow-ups if `expect_followup` is true).
        - `"chat"`: Call `handleChat(response_text)`.
        - Default: Log warning, fallback to chat.
- **Tips**: Use `getAsStringOrEmpty` and `getAsObjectOrEmpty` for safe access. For follow-ups, append a tool message (e.g., `role: "tool"`) to re-query. Ensure `action` aligns with user commands (e.g., `"weapons_hot"`, not they key binding `"DeployHardpointToggle"`).
- **Grok Example**: Parses response, routes to `commandHandlers` or `queryHandlers`, handles follow-up logic.

### 3. AiContextFactory (e.g., `YourLlmContextFactory`)

- **Role**: Generates dynamic prompts with session context, behavior, and LLM-specific headers.
- **Methods**:
    - `generateSystemInstructions(String sensorInput)`: Build prompt with sensor data and session details.
    - `generatePlayerInstructions(String playerVoiceInput)`: Craft a prompt to classify input as "command", "query", or "chat", supporting game actions (e.g., "deploy landing gear"), game queries (e.g., "analyze my loadout"), and general chat (e.g., "who was the last man on the moon?").
    - `generateAnalysisPrompt(String userIntent, String dataJson)`: Prepare a prompt for analyzing game data with intent and JSON context.
    - `generateSystemPrompt()`: Define AI role, rules, and response format (e.g., JSON with `type`, `action`, `params`, `response_text`).
    - `generateQueryPrompt()`: Guide query classification for game and general topics.
    - `appendBehavior(StringBuilder sb)`: Add personality or cadence from session settings.
- **Tips**: Tailor prompts to your LLM’s strengths, including headers (e.g., model, temperature) via `ConfigManager`. Instruct the LLM to handle diverse inputs with the standard JSON structure. Refer to `GrokContextFactory` for examples of prompt construction, behavior integration, and chat
  support (e.g., trivia, math).
- **Grok Example**: Builds detailed prompts with classification clauses and response formatting.

### 4. AiQueryInterface (e.g., `YourLlmQueryEndPoint`)

- **Role**: Handles queries.
- **Methods**:
    - `sendToAi(JsonArray messages)`: Send prompt, parse response into standard JSON.
- **Tips**: Ensure response matches `{ "type": "query", "action": "...", "params": {...}, "response_text": "..." }` or triggers follow-up.
- **Grok Example**: Tailored for query-specific parsing.

### 5. AiAnalysisInterface (e.g., `YourLlmAnalysisEndpoint`)

- **Role**: Analyzes data.
- **Methods**:
    - `analyzeData(String userIntent, String dataJson)`: Build prompt, return `{ "type": "query", "action": "...", "params": {...}, "response_text": "..." }`.
- **Tips**: Focus on structured output for handlers.
- **Grok Example**: Extracts content as JsonObject.

### 6. AIChatInterface (e.g., `YourLlmChatEndPoint`)

- **Role**: General conversation.
- **Methods**:
    - `sendToAi(JsonArray messages)`: Return `{ "type": "chat", "action": null, "params": {}, "response_text": "..." }`.
- **Tips**: Fallback to chat type if no structured JSON. Maintain history for context.
- **Grok Example**: Routes non-JSON to chat type.

## Integration Steps

1. **Create Subpackage**: e.g., `brain/yourllm`.
2. **Implement Classes**: As above, making them singletons with `getInstance()`.
3. **Wire in ApiFactory**: Add getters like `getYourLlmRouter()`, returning instances based on `KeyDetector`’s provider detection for the LLM key in `system.conf`.
4. **Prompt Engineering**: Instruct your LLM to return JSON with `type`, `action`, `params`, `response_text`, and optionally `expect_followup`. Example: "For every response, format as { \"type\": \"command/query/chat\", \"action\": \"...\", \"params\": {...}, \"response_text\": \"...\",
   \"expect_followup\": true/false }". Include examples for chat (e.g., "What is six times seven?" → `{ "type": "chat", "action": null, "params": {}, "response_text": "Forty-two" }`).
5. **Testing**:
    - Voice command: "Power to engines" → Command handler triggers custom handler.
    - Voice command: "Set optimal speed" → Command handler triggers single action command (speed 75%).
    - Query: "Analyze my loadout" → Analysis, TTS response.
    - Query: "What is your favorite Motorhead album" → AI chooses her favorite Motorhead album, TTS response.
    - Chat: "Nothing interesting in this star system..." → Casual reply.
    - Add a test handler to enum, confirm auto-registration.
    - **NOTE**: Casual chat is disabled in PROFESSIONAL personality mode.

## Common Gotchas

- **API Differences**: Adapt HTTP setup (e.g., headers, body format) but keep response parsing robust (strip BOM, find JSON after text).
- **Rate Limits/Errors**: Implement retries or fallbacks in `sendToAi`.
- **Performance**: Keep prompts concise; use non-streaming for simplicity.
- **Extensibility**: If your LLM needs extra params, extend interfaces sparingly—propose upstream if core.

This setup ensures extensibility while enforcing principles. For specific class guidance, refer to the `Grok` implementation or provide your partial impl for review with temporary key for your chosen LLM!