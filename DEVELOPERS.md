# Elite Intel Developer Guidelines

## Overview (video is dated, but still relevant)

[![Architecture Overview](https://img.youtube.com/vi/tpqcodcuIig/0.jpg)](https://youtu.be/tpqcodcuIig)


Welcome to developing for Elite Intel, a Java-based QoL app for *Elite Dangerous*. This guide ensures contributions align with the app’s architecture and Frontier’s TOS. The app uses an event-driven design (`elite.intel.gameapi`), Swing UI (`elite.intel.ui`), and integrates STT/TTS (via
`elite.intel.ai.ears` and `elite.intel.ai.mouth`) and AI (via `elite.intel.ai.brain`).

### How does the app work?
The app monitors journal and auxiliary files and builds local cache of relevant data stored in json format.
In addition Elite Intel uses custom API to access EDSM and Spansh data sources for specific features.
The user speaks in to a microphone, this audio is sent to STT for processing, and the resulting text is sent to AI for 
analysis and response. The AI figures out intent from the input. The input can be a command, a query or chat. 
AI will do its best attempt to map the input to a specific action such as a command or query. If non matches the AI will 
respond with a chat.

The commands are handled by command handlers and in most cases execute a keystroke, or a short keystroke sequence.
In other cases the command might require access to EDSM or Spansh for tasks such as finding a market or plotting a route.

If the input matches a query the AI will respond with a different format of data, which is in turn routed to the 
query handler for processing. The querry handler will aggregate data necessary to perform the action and send it to 
AI for analysis in a different prompt along with the original user input. AI will respond with the data analysis response.

## Getting Started

### Prerequisites

| Tool       | Version               | Notes                                                        |
|------------|-----------------------|--------------------------------------------------------------|
| **Java**   | **21 LTS**            | Required. Tested with Eclipse Temurin / OpenJDK 21.          |
| **Gradle** | **8.7** (via wrapper) | Do **not** install Gradle manually - always use `./gradlew`. |
| **Git**    | Any recent            | -                                                            |

> The build will fail on Java versions other than 21. Make sure `JAVA_HOME` points to a JDK 21 installation.

### Checkout

```bash
git clone https://github.com/<your-fork>/EliteIntel.git
cd EliteIntel
```

Or clone the upstream repo directly:

```bash
git clone https://github.com/stone-alex/EliteIntel.git
cd EliteIntel
```

### Build

```bash
./gradlew clean build          # Full build (both modules)
./gradlew app:build            # Build main app only
./gradlew shadowJar            # Create fat JAR → distribution/elite_intel.jar
```

The fat JAR is written to `distribution/elite_intel.jar`.

### Run

```bash
java -Djava.library.path=distribution/native/sherpa-onnx -jar distribution/elite_intel.jar
```

### IDE Configuration (IntelliJ IDEA)

1. **Open** the project root as a Gradle project (`File → Open`).
2. **SDK**: ensure the project SDK is set to Java 21 (`File → Project Structure → Project → SDK`).
3. **Run configuration**: edit the `App` run configuration and add the following to **VM options**:
   ```
   -Djava.library.path=distribution/native/sherpa-onnx
   ```
   This is required for the local Sherpa-ONNX STT/TTS native libraries to load. Without it the app will fail to run the local STT or TTS backend.
4. **Unused-declaration false positives**: see the *IntelliJ IDEA Note* section at the bottom of this file.

---

## Coding Standards

- **Language**: Pure Java (no external dependencies beyond configured APIs).
- **Architecture**:
    - Follow MVC in `elite.intel.ui` (controllers, views, models).
    - Use event-driven design in `elite.intel.gameapi` (e.g., `EventBusManager.publish`).
- **Modularity**: New features should subscribe to journal events (`elite.intel.gameapi.journal.events`) or custom events via `@Subscribe`.
- **TOS Compliance**: No AFK automation or game file modifications. All actions require user input.

The app's architecture is decoupled and modular. This means modules do not have a direct dependency or
knowledge of each other. If you want to add a new API integration to an alternative LLM, STT or TTS, all you
have to do is implement the contract in the interfaces and place your code in correct packages following the convention.
If you want to implement a new command or query follow the established patterns.

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
  - `AiVoxResponseEvent`: Initiates TTS processing (`elite.intel.ai.mouth`).
  - `AudioMonitorEvent`: Use this event to send the data to UI for visualization only. (`elite.intel.ai.erars`).
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
  - Run in a separate thread, stream mic input to STT, and publish `UserInputEvent` when a transcription is ready.
  - Use `AudioCalibrator` to automatically set RMS thresholds based on real-time audio analysis.
    - Use `AudioFormatDetector` to automatically detect the audio device, bitrate, and sample rate.
- **Current backend**: `elite.intel.ai.ears.whisper.WhisperSTTImpl` - offline, runs locally via
  `whisper-jni` (JNI bindings to whisper.cpp). Requires a 16 kHz mono audio stream; resampling is handled internally by
  `Resampler`.
- **Native dependency**: Whisper native libraries are loaded at runtime from the path specified by
  `-Djava.library.path` (see IDE Configuration above).

### TTS (Text-to-Speech)

- **Interface**: Implement `elite.intel.ai.mouth.MouthInterface`.
  - Methods: `start()`, `stop()`, and subscribe to relevant vocalisation events via `@Subscribe`.
  - Run in a separate thread, manage an internal queue, and register via `SubscriberRegistration` or
    `EventBusManager.register(this)` for singletons.
- **Current backends**:
  - `elite.intel.ai.mouth.kokoro.KokoroTTS` - **primary, offline**. Uses Kokoro via
    `sherpa-onnx` JNI. Two-queue pipeline: sentence splitting → synthesis queue → playback queue. Native libraries loaded from
    `-Djava.library.path`. No API key required.
  -
  `elite.intel.ai.mouth.google.GoogleTTSImpl` - cloud-based fallback via Google Cloud Text-to-Speech API. Requires a Google Cloud API key configured in the System settings tab.

### AI (LLM Integration)

- **Entry Point**: Implement `elite.intel.ai.brain.AiCommandInterface`.
    - Methods: `start()`, `stop()`, `@Subscribe void onUserInput(UserInputEvent event)`, `@Subscribe void onSensorDataEvent(SensorDataEvent event)`.
- **Routing**: Implement `elite.intel.ai.brain.AIRouterInterface`.
    - Methods: `start()`, `stop()`, `processAiResponse(JsonObject jsonResponse, @Nullable String userInput)`.
- **Endpoints**:
    - `AiQueryInterface`: `JsonObject sendToAi(JsonArray messages)` for queries.
    - `AiAnalysisInterface`: `JsonObject analyzeData(String userIntent, String dataJson)` for data analysis.
    - `AIChatInterface`: `JsonObject sendToAi(JsonArray messages)` for chats.
- **Example**: See `elite.intel.ai.brain.*` for various implementations.

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


## Intelli J IDEA Note:
There are classes instantiated via reflection. Intelli J IDEA will flag these as unused. To fix this, add @Subscribe to the list of annotations that are considered as usage.

1. Go to File > Settings (or IntelliJ IDEA > Preferences on macOS).
2. Navigate to Editor > Inspections.
3. In the search bar, type "Unused declaration" to find the inspection related to unused code.
4. Click on Unused declaration to expand it.
5. You'll see an option called Annotations. Click on the ellipsis (...) next to it.
6. In the dialog that appears, you can add @Subscribe to the list of annotations that are considered as usage.

## Contact

Submit a pull request. Open an issue on GitHub. Or contact me via Discord: [discord.gg/3qAqBENsBm](https://discord.gg/3qAqBENsBm).
