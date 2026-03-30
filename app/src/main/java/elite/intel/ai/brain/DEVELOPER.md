# ai/brain - AI Decision Engine & Command Router

This package is the cognitive core of EliteIntel. It receives transcribed user speech, sends it to an LLM backend, interprets the structured JSON response, and dispatches the result to the appropriate command or query handler.

---

## Package Structure

```
ai/brain/
├── AiCommandInterface.java        # EventBus subscriber; owns the main input/response loop
├── AiAnalysisInterface.java       # Contract for data-analysis calls (contextual queries)
├── AIRouterInterface.java         # Routes parsed AI responses to handlers
├── AIChatInterface.java           # Sends message history to an LLM; returns JSON
├── Client.java                    # Shared HTTP client interface
│
├── anthropic/                     # Anthropic Claude backend
├── gemini/                        # Google Gemini backend
├── lmstudio/                      # LM Studio (local) backend
├── ollama/                        # Ollama dual-model backend
├── openai/                        # OpenAI backend
├── xai/                           # xAI Grok backend
├── commons/                       # Shared prompt builders, response parsers
│
└── handlers/
    ├── CommandHandlerFactory.java  # Auto-discovers & caches all CommandHandler impls
    ├── QueryHandlerFactory.java    # Auto-discovers & caches all QueryHandler impls
    ├── commands/
    │   ├── CommandHandler.java     # Interface: handle(action, params, responseText)
    │   ├── CommandOperator.java    # Optional base class with shared helpers
    │   ├── Commands.java           # Enum of every recognised command action name
    │   └── (90+ handler classes)  # One class per game command
    └── query/
        ├── QueryHandler.java       # Interface: handle(action, params, userInput) → JsonObject
        ├── Queries.java            # Enum of every recognised query action name
        ├── struct/
        │   └── AiData.java         # Context bundle passed into analysis calls
        └── (40+ handler classes)  # One class per query/analysis type
```

---

## Core Interfaces

| Interface             | Responsibility                                                                                                                                                   |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AiCommandInterface`  | Top-level subscriber. Receives `UserInputEvent`, builds the prompt with session context, calls the LLM, then calls `AIRouterInterface` to dispatch the response. |
| `AIRouterInterface`   | Parses the LLM JSON response (`type`, `action`, `params`) and calls the matching `CommandHandlerFactory` or `QueryHandlerFactory` entry.                         |
| `AIChatInterface`     | Low-level: sends a `JsonArray` message history to the LLM and returns raw `JsonObject`. Implemented separately per provider.                                     |
| `AiAnalysisInterface` | Used by query handlers that need a second LLM call with structured game-state context (`AiData`). Returns a `JsonObject` with the analysis result.               |

---

## LLM Provider Implementations

Each provider lives in its own sub-package and implements the relevant interfaces:

| Package      | Provider         | Notes                                                                    |
|--------------|------------------|--------------------------------------------------------------------------|
| `ollama/`    | Ollama (local)   | Dual-model: fast command model + larger query model running concurrently |
| `openai/`    | OpenAI           | GPT models via REST                                                      |
| `anthropic/` | Anthropic Claude | Claude models via REST                                                   |
| `xai/`       | xAI Grok         | Grok models via REST                                                     |
| `gemini/`    | Google Gemini    | Gemini models via REST                                                   |
| `lmstudio/`  | LM Studio        | OpenAI-compatible local endpoint                                         |

**Ollama dual-model design:** Two separate model instances run in parallel. The *command
model* (small, low-temperature, e.g. `tulu3:8b`) handles intent classification and ship-control decisions. The *query
model* (larger, higher latency acceptable, e.g.
`qwen2.5:14b`) handles data analysis and reasoning. Both are configured independently in settings.

**Adding a new provider:** Implement `AiCommandInterface` + `AIRouterInterface` + whichever of `AIChatInterface` /
`AiAnalysisInterface` your provider needs. Place it in a new sub-package. No other code changes are required - provider selection is config-driven.

---

## Handler System

### Command Handlers

Command handlers execute game actions (keystrokes, mode changes, navigation).

- Implement `CommandHandler`: `void handle(String action, JsonObject params, String responseText)`
- Optionally extend `CommandOperator` for shared key-execution helpers
- All actions are listed in `Commands.java` (enum)
- `CommandHandlerFactory` scans the
  `commands/` package at startup via reflection and caches one instance per handler class

### Query Handlers

Query handlers answer questions or perform analysis, returning structured data that gets spoken back to the user.

- Implement `QueryHandler`: `JsonObject handle(String action, JsonObject params, String originalUserInput)`
- All query types are listed in `Queries.java` (enum)
- `QueryHandlerFactory` scans the `query/` package at startup via reflection

### Adding a handler

1. Create a class in `handlers/commands/` or `handlers/query/`
2. Implement the corresponding interface
3. Add the action name to `Commands.java` or `Queries.java`
4. The factory picks it up automatically - no registration needed

---

## Data Flow

```
UserInputEvent (from ears)
  └─► AiCommandInterface.onUserInput()
        ├─ Attach session context (ship state, location, cargo, …)
        ├─ Build prompt (system + history + user message)
        └─ AIChatInterface.processAiPrompt(messages, temp)
              └─ LLM HTTP call → JsonObject response
                    └─ AIRouterInterface.processAiResponse(response, userInput)
                          ├─ type="command" → CommandHandlerFactory → CommandHandler.handle()
                          │     └─ hands/GameController (keystrokes)
                          │     └─ VocalisationRequestEvent (confirmation speech)
                          └─ type="query"   → QueryHandlerFactory  → QueryHandler.handle()
                                └─ search/ or db/ managers (data retrieval)
                                └─ [optional] AiAnalysisInterface second LLM call
                                └─ VocalisationRequestEvent (spoken answer)
```

---

## EventBus

| Direction | Event                      | Notes                                                   |
|-----------|----------------------------|---------------------------------------------------------|
| Consumed  | `UserInputEvent`           | Transcribed speech from ears                            |
| Consumed  | `SensorDataEvent`          | Real-time game telemetry injected into the prompt       |
| Published | `VocalisationRequestEvent` | Text to be spoken by mouth                              |
| Published | `AiVoxResponseEvent`       | AI-generated speech (routed through VocalisationRouter) |

---

## Key Design Rules

- **Never modify existing trigger phrases** in handler action name enums (`Commands`,
  `Queries`). They are matched against LLM output and were tuned over months; any change causes routing failures across all providers.
- **No Java pre-filtering
  ** of user input before it reaches the LLM other than prompt reducer logic. The LLM handles noisy STT output far better than regex.
- If you are adding a new query/command handler, your entry in the AiActionsMap must not clash / conflict with the existing entries.
- The LLM receives only the session state relevant to the current query - not the full conversation history - to minimise token usage and latency.
- To run successfully on small local LLMs we have to pre-digest the data. Do not rely on LLM to do complex calculations or reasoning. It will fail too often.
