# Text-to-Speech (TTS) Implementation Guide

This document provides guidance for contributors implementing a new Text-to-Speech (TTS) provider in the
`elite.intel.ai.mouth` package for the Elite Dangerous companion app. The app is a Terms of Service (ToS)-compliant
quality-of-life tool written in pure Java, designed to provide hands-free control and natural language interaction for
VR users.

The TTS system is responsible for converting text into spoken audio, supporting multiple voice choices and handling
events for both user-selected and random voice outputs, such as for radio transmission events.

## Overview

The `MouthInterface` defines the contract for TTS implementations. Your implementation must integrate with the app’s
event-driven architecture, handle a thread-safe queue for processing voice requests, and support the required voice
configurations. The implementation will be instantiated dynamically based on API key detection and must adhere to the
project’s programming principles.

## Project Principles

Your implementation must follow these principles:

- **DRY (Don’t Repeat Yourself)**: Avoid code duplication.
- **SRP (Single Responsibility Principle)**: Each class should have one responsibility.
- Avoid excessive `if/else-if` or `switch` statements.
- No dependency injection in constructors.
- No magic numbers or strings; use constants or enums.
- Use only pure Java; no platform-specific libraries, Python wrappers, or JNI.
- No game memory reading.
- No deprecated APIs.
- No plugin APIs that could violate ToS.

## Requirements

### 1. Implementing `MouthInterface`

Your TTS implementation must implement the `elite.intel.ai.mouth.MouthInterface` interface, which defines the
following methods:

```java
package elite.intel.ai.mouth;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.VoiceProcessEvent;

public interface MouthInterface {
    void start();

    void stop();

    void interruptAndClear(); // implement vocalization interrupt and clear the queue

    @Subscribe
    void onVoiceProcessEvent(VoiceProcessEvent event);
}
```

- **`start()`**: Initializes the TTS system, including any client setup and starting a processing thread for the voice
  queue. Check if the system is already running to avoid duplicate initialization.
- **`stop()`**: Gracefully shuts down the TTS system, stopping the processing thread and releasing resources (e.g.,
  closing API clients).
- **`onVoiceProcessEvent(VoiceProcessEvent event)`**: Handles `VoiceProcessEvent` events to process text for speech
  synthesis. The event includes:
    - `event.getText()`: The text to be spoken.
    - `event.isUseRandom()`: A boolean indicating whether to use a random voice (e.g., for radio transmissions) or the
      user-selected voice from `SystemSession.getInstance().getAIVoice()`.

Example implementation of `onVoiceProcessEvent`:

```java

@Subscribe
@Override
public void onVoiceProcessEvent(VoiceProcessEvent event) {
    if (event.isUseRandom()) {
        speak(event.getText(), getRandomVoice());
    } else {
        speak(event.getText());
    }
}
```

- If `event.isUseRandom()` is `true`, select a random voice from the available voices.
- Otherwise, use the user-selected voice from `SystemSession.getInstance().getAIVoice()`.

### 2. Constructor and Instantiation

Your implementation must provide a simple constructor or singleton pattern:

- **Non-singleton**: Use a default constructor, e.g., `new MyTTSImpl()`.
- **Singleton**: Provide a static `getInstance()` method, e.g., `MyTTSImpl.getInstance()`.

**No constructor dependency injection** is allowed. Any configuration (e.g., API keys) should be retrieved at runtime
using `ConfigManager.getInstance().getSystemKey(ConfigManager.TTS_API_KEY)`.

### 3. Voice Configuration

Your implementation must support the 14 voices defined in `AiVoices` enum (6 male, 8 female) with specific names, speech
rates, and accents (British, American, or Australian). Map these to your TTS provider’s equivalent voices.

```java
public enum AiVoices {
    ANNA("Anna", 1.1, false),      // Female, British
    BETTY("Betty", 1.2, false),    // Female, British
    CHARLES("Charles", 1.2, true), // Male, British
    EMMA("Emma", 1.1, false),      // Female, American
    JAKE("Jake", 1.2, false),      // Male, American
    JAMES("James", 1.1, true),     // Male, Australian
    JENNIFER("Jennifer", 1.2, false), // Female, American
    JOSEPH("Joseph", 1.2, true),   // Male, American
    KAREN("Karen", 1.2, false),    // Female, American
    MARY("Mary", 1.2, false),      // Female, British
    MICHAEL("Michael", 1.2, true), // Male, American
    OLIVIA("Olivia", 1.2, false),  // Female, British
    RACHEL("Rachel", 1.2, false),  // Female, American
    STEVE("Steve", 1.2, true),     // Male, American
}
```

- **Mapping**: Create a mapping (e.g., a `Map<String, YourVoiceType>`) from `AiVoices` names to your provider’s voice
  configurations (e.g., language codes, voice IDs).
- **Random Voice Selection**: Implement `getRandomVoice()` to return a random `AiVoices` entry for radio transmission
  events when `event.isUseRandom()` is `true`.
- **Speech Rate**: Apply the `speechRate` from `AiVoices` (e.g., 1.1 or 1.2) to control playback speed.
- **Accents**: Ensure voices align with the specified accents (British, American, Australian) as closely as possible.

### 4. Threading and Queue Management

- Use a `BlockingQueue` (e.g., `LinkedBlockingQueue`) to handle asynchronous voice requests.
- Run a dedicated processing thread to poll the queue and process requests.
- Ensure thread safety and graceful shutdown in the `stop()` method.
- Handle `InterruptedException` appropriately, restoring the interrupted status or exiting the thread.

Example queue processing:

```java
private final BlockingQueue<VoiceRequest> voiceQueue = new LinkedBlockingQueue<>();
private volatile boolean running;

private void processVoiceQueue() {
    while (running) {
        try {
            VoiceRequest request = voiceQueue.poll(1, TimeUnit.SECONDS);
            if (request == null) continue;
            // Process request with your TTS provider
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (Exception e) {
            log.error("Error processing voice request", e);
        }
    }
}
```

### 5. Audio Playback

- Use pure Java’s `javax.sound.sampled` package for audio playback (no platform-specific libraries).
- Recommended audio format: 24kHz, 16-bit, mono, LINEAR16 encoding.
- **Fade-in/Fade-out**: To avoid popping sounds at the start or end of audio streams, apply a linear fade-in and
  fade-out (e.g., ~20ms). See `GoogleTTSImpl.applyFade` for an example.
- Ensure proper buffer management to avoid underruns (e.g., pre-buffer ~20ms of silence).

### 6. Event Subscription

Your implementation must subscribe to `VoiceProcessEvent` events using the app’s event bus:

- **Singleton**: Call `EventBusManager.register(this)` in your `getInstance()` method or constructor.
- **Non-singleton**: Add your package name to `SubscriberRegistration` for automatic registration. Ensure you have the
  `@Subscribe` annotation on your `onVoiceProcessEvent` method, or it will not be picked up.

To add your package to `SubscriberRegistration`, update the package list in
`SubscriberRegistration.registerSubscribers()`:

```java
Reflections reflections = new Reflections(
        "elite.intel.gameapi.journal.subscribers",
        "elite.intel.gameapi.gamestate.subscribers",
        "elite.intel.ai.mouth.yourpackage", // Add your package here
        new MethodAnnotationsScanner()
);
```

### 7. API Key Detection and Integration

Your implementation will be instantiated by `ApiFactory.getMouthImpl()` based on the API key provided in
`ConfigManager.TTS_API_KEY`. To support this:
If your TTS provider is not listed in `KeyDetector.PATTERNS`, you must:

- Define a unique `ProviderEnum` entry for your TTS provider (e.g., `MY_TTS("TTS")`).
- Add a regex pattern to `KeyDetector.PATTERNS` to match your API key format.
- Update `ApiFactory.getMouthImpl()` to return your implementation when the provider is detected:

```java
case MY_TTS:
        return MyTTSImpl.

getInstance(); // or new MyTTSImpl()
```

Example `ProviderEnum` and `KeyDetector` additions:

```java
// ProviderEnum.java
public enum ProviderEnum {
    MY_TTS("TTS"),
    // ... other providers
}

// KeyDetector.java
private static final Map<ProviderEnum, Pattern> PATTERNS = Map.ofEntries(
        Map.entry(ProviderEnum.MY_TTS, Pattern.compile("^your-key-pattern$")),
        // ... other patterns
);
```

### 8. Radio Transmissions Feature

When the “Radio Transmissions” feature is enabled (toggled via AI voice command), journal events related to radio
transmissions (e.g., NPC communications) are vocalized using random voices from the `AiVoices` enum. Ensure your
implementation supports this by checking `event.isUseRandom()` in `onVoiceProcessEvent`.

## Integration with the Application

- **Instantiation**: Your class is instantiated by `ApiFactory.getMouthImpl()` based on the API key. Ensure your
  constructor or `getInstance()` method is simple and retrieves the API key via `ConfigManager`.
- **Event Handling**: The app posts `VoiceProcessEvent` events to the event bus, which your implementation must handle.
- **Configuration**: The user-selected voice is stored in `SystemSession.getInstance().getAIVoice()`. Use this for
  non-random voice requests.
- **Logging**: Use `org.slf4j.Logger` for logging errors, warnings, and info (e.g.,
  `LoggerFactory.getLogger(MyTTSImpl.class)`).
- **Error Handling**: Handle API errors gracefully, logging issues and avoiding crashes. Post `AppLogEvent` or
  `VoiceProcessEvent` to notify the user if needed (e.g., invalid API key).

## Example Implementation

See `GoogleTTSImpl` (`elite.intel.ai.mouth.google.GoogleTTSImpl`) for a reference implementation. Key features to
emulate:

- Singleton pattern with `getInstance()`.
- Voice mapping to `AiVoices` using a `Map`.
- Thread-safe `BlockingQueue` for voice requests.
- Audio playback with fade-in/fade-out to prevent popping.
- Event bus registration via `EventBusManager.register(this)`.
- API key retrieval via `ConfigManager`.

## Troubleshooting

- **Popping Sounds**: If audio playback produces popping sounds, ensure you apply a ~20ms linear fade-in and fade-out to
  the audio data.
- **Thread Safety**: Use `synchronized` blocks or `volatile` variables for shared resources.
- **API Key Issues**: If your provider isn’t detected, verify your `ProviderEnum` and `KeyDetector` pattern.
- **Event Issues**: Ensure your class is registered with the event bus, either via `SubscriberRegistration` (add your
  package) or `EventBusManager.register(this)`. Verify the `@Subscribe` annotation on `onVoiceProcessEvent`.
- **Voice Mapping**: Ensure all 14 `AiVoices` are mapped correctly to avoid null voice selections.

## Testing

- Test voice mapping for all 14 `AiVoices` entries.
- Verify random voice selection for radio transmissions.
- Ensure thread shutdown is clean and resources are released.
- Test audio playback for clarity and absence of artifacts.
- Validate API key detection with various key formats.

For questions or clarification, consult the existing `GoogleTTSImpl` code or contact the project maintainers.