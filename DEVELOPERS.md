# Elite Companion Developer Guidelines

## Overview

Welcome to developing for Elite Companion, a Java-based QoL app for *Elite Dangerous*. This guide ensures contributions align with the app’s architecture and Frontier’s TOS. The app uses an event-driven design (`elite.intel.gameapi`), Swing UI (`elite.intel.ui`), and integrates STT/TTS (via
`elite.intel.ai.ears` and `elite.intel.ai.mouth`) and AI (via `elite.intel.ai.brain`).

## Coding Standards

- **Language**: Pure Java (no external dependencies beyond configured APIs).
- **Architecture**:
    - Follow MVC in `elite.intel.ui` (controllers, views, models).
    - Use event-driven design in `elite.intel.gameapi` (e.g., `EventBusManager.publish`).
- **Modularity**: New features should subscribe to journal events (`elite.intel.gameapi.journal.events`) or custom events via `@Subscribe`.
- **TOS Compliance**: No AFK automation or game file modifications. All actions require user input.


## Contribution Workflow

1. **Fork**: Create a personal fork of the repo.
2. **Branch**: Use descriptive names (e.g., `feature/new-voice`).
3. **Code**: Implement in relevant packages (e.g., `elite.intel.ai.brain` for AI, `elite.intel.ui.controller` for UI).
4. **PR**: Submit a pull request to `master`. Include tests and documentation.
5. **Review**: Await maintainer approval (required due to branch protection).

## Event System

- **Event Types**:
    - `UserInputEvent`: Triggered by STT (`elite.intel.ai.ears`) for user voice input.
    - `SensorDataEvent`: Sent directly to AI for cadence or analysis, used in query/command handlers.
    - `VoiceProcessEvent`: Initiates TTS processing (`elite.intel.ai.mouth`).
    - `YourJournalEvent extends BaseEvent`: Journal file events, wrappers. Registered in `elite.intel.gameapi.journal.EventRegistry`.
- **Event Bus**:
    - Use `elite.intel.gameapi.EventBusManager` to publish events.
    - Use `elite.intel.gameapi.journal.EventRegistry` to register journal events.

- **Subscribers**:
    - Use `elite.intel.gameapi.EventBusManager` to subscribe to events.
    - Use `elite.intel.gameapi.journal.subscribers` for journal event subscribers.
    - Use `elite.intel.gameapi.journal.events` for journal event wrappers.
- **Registration**:
    - Use `elite.intel.util.SubscriberRegistration` to register event listeners by package.
    - For singletons, add `EventBusManager.register(this)` in the constructor.
    - Annotate methods with `@Subscribe` (e.g., `onUserInput`, `onSensorDataEvent`).

## Custom Integrations

### STT (Speech-to-Text)

- **Interface**: Implement `elite.intel.ai.ears.EarsInterface`.
    - Methods: `start()`, `stop()`, `getNextTranscription()`, `stopListening()`, `shutdown()`.
    - Run in a separate thread, stream mic input to STT, and throw `UserInputEvent` on API callback.
    - Use `AudioCalibrator` to automatically set up Root Mean Square (RMS) thresholds based on real time audio analysis.
    - Use `AudioFormatDetector` to automatically detect the audio device, bitrate, and sample rate.
- **Example**: See `elite.intel.ai.ears.google` for Google STT integration.

### TTS (Text-to-Speech)

- **Interface**: Implement `elite.intel.ai.mouth.MouthInterface`.
    - Methods: `start()`, `stop()`, `@Subscribe void onVoiceProcessEvent(VoiceProcessEvent event)`.
    - Run in a separate thread, manage an internal queue, and subscribe via `SubscriberRegistration` or `EventBusManager.register(this)` for singletons.
- **Example**: See `elite.intel.ai.mouth.google` for Google TTS integration.

### AI (LLM Integration)

- **Entry Point**: Implement `elite.intel.ai.brain.AiCommandInterface`.
    - Methods: `start()`, `stop()`, `@Subscribe void onUserInput(UserInputEvent event)`, `@Subscribe void onSensorDataEvent(SensorDataEvent event)`.
- **Routing**: Implement `elite.intel.ai.brain.AIRouterInterface`.
    - Methods: `start()`, `stop()`, `processAiResponse(JsonObject jsonResponse, @Nullable String userInput)`.
- **Endpoints**:
    - `AiQueryInterface`: `JsonObject sendToAi(JsonArray messages)` for queries.
    - `AiAnalysisInterface`: `JsonObject analyzeData(String userIntent, String dataJson)` for data analysis.
    - `AIChatInterface`: `JsonObject sendToAi(JsonArray messages)` for chats.
- **Example**: See `elite.intel.ai.brain.grok` for xAI Grok integration.

## New Journal Events

- **Registration**: Use `elite.intel.gameapi.journal.EventRegistry` to register new events.
- **Implementation**: Create event wrappers in `elite.intel.gameapi.journal.events` (e.g., extend `BaseEvent`).
- **Subscribers**: Annotate methods with `@Subscribe` in `elite.intel.gameapi.journal.subscribers`, and add your package to `elite.intel.util.SubscriberRegistration`.

## Security

- Sanitize voice inputs to prevent injection.
- Store API keys securely (UI "System" tab, locked fields).
- No internet-required features beyond APIs.

## Licensing

If open-sourced, code falls under Creative Commons (e.g., CC BY-NC-SA 4.0). Attribute third-party work.

## Contact

Submit a pull request. Open an issue on GitHub. Or contact me via Discord: [discord.gg/3qAqBENsBm](https://discord.gg/3qAqBENsBm).