# Speech-to-Text (STT) Integration Guide

## Overview

The Speech-to-Text (STT) component of the Elite Dangerous companion app enables hands-free voice control for VR users, providing a natural language interface to process player commands and queries. The STT system is part of the `comms/ears` package and is responsible for converting spoken input into
text, correcting misheard phrases using a dictionary, and forwarding the sanitized text to command or query handlers via the event bus.

This document guides contributors on implementing a new STT provider by adhering to the `EarsInterface` contract. The provided `GoogleSTTImpl` class serves as a reference implementation using the Google Speech-to-Text API. All implementations must comply with
the [Elite Dangerous Terms of Service (TOS)](https://www.elitedangerous.com/terms-of-service/), the project’s modular architecture, and its programming principles (e.g., DRY, SRP, no deprecated APIs, no platform-specific code).

## Project Structure

The STT implementation resides in the `app/src/main/java/elite/intel/comms/ears` package:

- **`EarsInterface.java`**: Defines the contract for all STT implementations with `start()` and `stop()` methods.
- **`google/GoogleSTTImpl.java`**: A reference implementation using the Google Speech-to-Text API.
- **`util/STTSanitizer.java`**: Handles correction of misheard phrases using the dictionary file at `APP_HOME/dictionary/daft-secretary-dictionary.txt`.
- **`util/AudioFormatDetector.java`**: Utility class for detecting supported audio formats to ensure high-quality audio input.
- **`util/AudioSettingsTuple.java`**: Utility class for returning paired audio format parameters (sample rate and buffer size).
- **`ai/KeyDetector.java`**: Detects the STT provider based on the API key format.
- **`ai/ApiFactory.java`**: Instantiates STT implementations based on the detected provider.

Related components:

- **`comms/handlers`**: Processes sanitized transcripts into commands (`command`) or queries (`query`).
- **`gameapi/EventBusManager`**: Publishes `UserInputEvent` for command/query handling.
- **`util/DictionaryLoader`**: Loads the dictionary file for phrase correction.
- **`logs`**: Stores application logs for debugging.

## EarsInterface

The `EarsInterface` defines the minimal contract for STT implementations:

```java
package elite.intel.ai.ears;

public interface EarsInterface {
    void stop();
    void start();
}
```

- **`start()`**: Initializes the STT service, sets up audio capture, and begins processing speech input. Implementations should handle threading and resource initialization here.
- **`stop()`**: Shuts down the STT service, releases resources (e.g., audio lines, API clients), and stops any running threads.

## Implementation Guidelines

To create a new STT provider (e.g., for Microsoft Azure, AWS Transcribe, or Deepgram), follow these steps:

### 1. Create a New STT Class

- Place the implementation in a subpackage under `comms/ears` (e.g., `comms/ears/microsoft`).
- Implement `EarsInterface` and ensure the class handles audio capture, API communication, and transcription processing.
- Example structure:
  ```java
  package elite.intel.comms.ears.microsoft;

  import elite.intel.ai.ears.EarsInterface;

  public class MicrosoftSTTImpl implements EarsInterface {
      @Override
      public void start() {
          // Initialize API client, audio capture, and threading
      }

      @Override
      public void stop() {
          // Clean up resources and stop threads
      }
  }
  ```

### 2. Audio Capture

- Use `javax.sound.sampled` for portable audio capture (no platform-specific libraries).
- Support mono 16-bit audio with common sample rates (e.g., 48kHz, 44.1kHz, 16kHz).
- **Crucial: Dynamic Audio Format Detection** - Use `AudioFormatDetector` to detect the supported sample rate from the default audio port. Mismatched sample rates will result in distorted or unusable audio, severely impacting STT performance. Call `AudioFormatDetector.detectSupportedFormat()` once
  and store the result to avoid redundant calls.
- Implement voice activity detection (VAD) to filter silence and optimize API usage (see `GoogleSTTImpl.calculateRMS`).
- Example:
  ```java
  private int sampleRateHertz;
  private int bufferSize;
  private static final int CHANNELS = 1; // Mono

  @Override
  public void start() {
      AudioSettingsTuple<Integer, Integer> formatResult = AudioFormatDetector.detectSupportedFormat();
      this.sampleRateHertz = formatResult.getSampleRate();
      this.bufferSize = formatResult.getBufferSize();
      AudioFormat format = new AudioFormat(sampleRateHertz, 16, CHANNELS, true, false);
      DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
      try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
          log.info("Using streaming format: SampleRate={}, Channels={}", sampleRateHertz, CHANNELS);
          line.open(format, bufferSize);
          line.start();
          byte[] buffer = new byte[bufferSize];
          // Streaming loop (see GoogleSTTImpl.startStreaming for details)
          while (isListening.get()) {
              // Read audio data, process, and send to STT API
          }
      } catch (LineUnavailableException e) {
          log.error("Audio capture failed: {}", e.getMessage());
          throw new RuntimeException("Failed to open audio line", e);
      }
  }
  ```

### 3. Reusable Audio Format Detection

To ensure high-quality audio and promote DRY, use the `AudioFormatDetector` utility in `app/src/main/java/elite/intel/util/AudioFormatDetector.java`. This class detects supported audio formats for mono 16-bit input, ensuring compatibility with the system’s default audio port. Correct sample rate
detection is critical, as mismatches result in distorted or unusable audio, severely impacting STT performance.

Implementation:

```java
package elite.intel.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import elite.intel.ai.ears.AudioSettingsTuple;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager; 

/**
 * Utility class for detecting supported audio formats for mono 16-bit input.
 */
public class AudioFormatDetector {
    private static final Logger log = LogManager.getLogger(elite.intel.ai.ears.AudioFormatDetector.class);
    private static final int CHANNELS = 1; // Mono
    private static final int[] POSSIBLE_RATES = {48000, 44100, 16000}; // Preferred rates in order

    /**
     * Detects a supported audio format for mono 16-bit input by checking available sample rates.
     *
     * @return AudioSettingsTuple containing the detected sample rate and buffer size
     * @throws RuntimeException if no supported format is found
     */
    public static AudioSettingsTuple<Integer, Integer> detectSupportedFormat() {
        for (int rate : POSSIBLE_RATES) {
            AudioFormat format = new AudioFormat(rate, 16, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (AudioSystem.isLineSupported(info)) {
                int bufferSize = (int) (rate * 0.1 * 2 * CHANNELS); // ~100ms buffer
                log.info("Detected supported sample rate: {} Hz, buffer size: {}", rate, bufferSize);
                return new AudioSettingsTuple<>(rate, bufferSize);
            }
        }
        throw new RuntimeException("No supported audio format found for mono 16-bit input");
    }
}
```

Tuple Implementation:

```java
package elite.intel.util;

public class AudioSettingsTuple<K, V> {
    private final K sampleRate;
    private final V bufferSize;

    public AudioSettingsTuple(K sampleRate, V bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
    }

    public K getSampleRate() {
        return sampleRate;
    }

    public V getBufferSize() {
        return bufferSize;
    }

    @Override
    public String toString() {
        return "(" + sampleRate + ", " + bufferSize + ")";
    }
}
```

- Call this utility in your `start()` method or constructor, ensuring it’s called only once:
  ```java
  AudioSettingsTuple<Integer, Integer> formatResult = AudioFormatDetector.detectSupportedFormat();
  this.sampleRateHertz = formatResult.getSampleRate();
  this.bufferSize = formatResult.getBufferSize();
  ```

### 4. Dictionary Integration

- Use `STTSanitizer` to correct misheard phrases based on `APP_HOME/dictionary/daft-secretary-dictionary.txt`. The sanitizer is a singleton, accessible via `getInstance()`, and requires no additional setup beyond calling `correctMistakes()`.
- The dictionary maps misheard phrases (e.g., "southwest") to corrected commands (e.g., "set voice to").
- Example:
  ```java
  String sanitizedTranscript = STTSanitizer.getInstance().correctMistakes(rawTranscript);
  ```

### 5. Event Publishing

- Publish sanitized transcripts as `UserInputEvent` via `EventBusManager` for processing by command/query handlers.
- Include confidence scores (if available) to allow handlers to filter low-quality transcriptions.
- Example from `GoogleSTTImpl`:
  ```java
  EventBusManager.publish(new UserInputEvent(sanitizedTranscript, confidence));
  ```

### 6. Threading

- Run audio capture and API streaming in a separate thread to avoid blocking the main application.
- Ensure thread safety for shared resources (e.g., transcription queues, API clients).
- Handle thread interruption gracefully in `stop()`.
- Example from `GoogleSTTImpl`:
  ```java
  private final AtomicBoolean isListening = new AtomicBoolean(true);

  @Override
  public void stop() {
      shutdown();
      this.processingThread.interrupt();
  }

  private void stopListening() {
      isListening.set(false);
  }

  public void shutdown() {
      stopListening();
      aiCommandInterface.stop();
      if (speechClient != null) {
          speechClient.close();
          log.info("SpeechClient closed");
      }
  }
  ```

### 7. Configuration

- Retrieve API keys from `ConfigManager` (e.g., `ConfigManager.getInstance().getSystemKey("STT_API_KEY")`).
- Avoid hard-coding API keys or configuration values.

### 8. Logging

- Use `org.slf4j.Logger` to log initialization, errors, and key events to `APP_HOME/logs`.
- Publish `AppLogEvent` via `EventBusManager` for UI logging of transcriptions.
- Example:
  ```java
  log.info("Final transcript: {} (confidence: {})", transcript, confidence);
  EventBusManager.publish(new AppLogEvent("STT Heard: [" + transcript + "]"));
  ```

### 9. TOS Compliance

- Do not read game memory or automate gameplay (e.g., no AFK automation or botting).
- Process only legally available data (e.g., microphone input, journal events).
- Respect streaming settings (e.g., `SystemSession.isStreamingModeOn`) for command activation.

### 10. Error Handling

- Handle API errors, audio line unavailability, and invalid configurations gracefully.
- Log errors and throw `RuntimeException` only for unrecoverable failures.
- Example:
  ```java
  try {
      // API initialization
  } catch (Exception e) {
      log.error("Failed to initialize STT: {}", e.getMessage());
      throw new RuntimeException("STT initialization failed", e);
  }
  ```

## Reference Implementation: GoogleSTTImpl

The `GoogleSTTImpl` class in `comms/ears/google` provides a complete example of an STT provider using the Google Speech-to-Text API. Key features include:

- **Dynamic Audio Format Detection**: Uses `AudioFormatDetector` to select a compatible sample rate (48kHz, 44.1kHz, 16kHz).
- **Voice Activity Detection (VAD)**: Uses RMS thresholds to detect voice and silence, optimizing API usage.
- **Streaming**: Manages long-running audio streams with periodic restarts to respect API limits.
- **Dictionary Correction**: Integrates with `STTSanitizer` to correct misheard phrases.
- **Event Integration**: Publishes transcriptions as `UserInputEvent` for command/query processing.
- **Thread Safety**: Uses `AtomicBoolean` and `BlockingQueue` for safe multithreaded operation.
- **Logging**: Logs to `APP_HOME/logs` and publishes `AppLogEvent` for UI feedback.

Review `GoogleSTTImpl.java` for implementation details, especially:

- `startStreaming()`: Handles audio capture and API streaming.
- `processStreamingRecognitionResult()`: Processes and sanitizes transcriptions.
- `getStreamingRecognitionConfig()`: Configures the API with context phrases and settings.

## Setup Instructions

1. **Place the Dictionary File**:
    - Ensure `APP_HOME/dictionary/daft-secretary-dictionary.txt` exists with mappings (e.g., `"southwest"="set voice to"`).
    - If the file doesn’t exist, a blank file will be created, but no mappings will be loaded. Current mappings are provided in the project.
    - Add new mappings to the file as needed for your STT provider, and test them to ensure accuracy. The dictionary is loaded on application start, so changes require a restart (current implementation, subject to future changes).

2. **Configure API Keys**:
    - Add the STT API key to `system.conf` under the key `STT_API_KEY` or enter it via the user interface.
    - Ensure the key format matches the expected pattern for your provider (e.g., `^AIzaSy[a-zA-Z0-9_-]{33}$` for Google STT).
    - Example: `STT_API_KEY=AIzaSyYourGoogleApiKeyHere`.
    - If your provider is not listed, add it to `KeyDetector.PATTERNS` and update `ProviderEnum`.

3. **Add Dependencies**:
    - Include the STT provider’s SDK in `build.gradle` (e.g., `com.google.cloud:google-cloud-speech:2.0.0` for Google STT).
    - Ensure no platform-specific or deprecated libraries are used.
    - The project must build with Gradle without errors and run from an IDE (run/debug) and as a JAR file.

4. **Register the Implementation**:
    - Update `ApiFactory.getEarsImpl()` to support your STT implementation by adding a new case for your provider in the `switch` statement.
    - Add a corresponding entry in `ProviderEnum` with the `STT` category.
    - Define the API key format in `KeyDetector.PATTERNS` to detect your provider if not already included.
    - Example for a new Microsoft STT implementation:
      ```java
      // In ProviderEnum.java
      public enum ProviderEnum {
          ...
          MICROSOFT_STT("STT"),
          ...
      }
      ```
      ```java
      // In KeyDetector.java
      private static final Map<ProviderEnum, Pattern> PATTERNS = Map.ofEntries(
          ...
          Map.entry(ProviderEnum.MICROSOFT_STT, Pattern.compile("^[0-9a-f]{32}$")),
          ...
      );
      ```
      ```java
      // In ApiFactory.java
      public EarsInterface getEarsImpl() {
          String apiKey = ConfigManager.getInstance().getSystemKey(ConfigManager.STT_API_KEY);
          ProviderEnum provider = KeyDetector.detectProvider(apiKey, "STT");
          switch (provider) {
              case GOOGLE_STT:
                  return new GoogleSTTImpl();
              case MICROSOFT_STT:
                  return new MicrosoftSTTImpl();
              default:
                  EventBusManager.publish(new AppLogEvent("Unknown STT key format"));
                  EventBusManager.publish(new VoiceProcessEvent("Using default Google STT—confirm?"));
                  return new GoogleSTTImpl();
          }
      }
      ```

## Testing

- **Unit Tests**: Create tests in `app/src/test/java/elite/intel/comms/ears` to verify audio capture, transcription, dictionary correction, and provider detection. Specifically, test `AudioFormatDetector` to ensure correct sample rate and buffer size selection.
- **Integration Tests**: Test with sample audio inputs, the dictionary file, and various API keys to ensure correct provider detection and phrase mapping.
- **Debugging**: Enable WAV recording (see `GoogleSTTImpl.startWavRecording`) to inspect audio quality. Verify sample rate detection logs in `APP_HOME/logs` to confirm `AudioFormatDetector` selects a supported format.
- **Logs**: Check `APP_HOME/logs` for errors, transcription details, and provider detection results.

## Best Practices

- **DRY**: Reuse `AudioFormatDetector`, `AudioSettingsTuple`, `STTSanitizer`, and `DictionaryLoader` across implementations.
- **SRP**: Ensure the STT class focuses only on speech recognition and delegates command processing to handlers.
- **No Magic Strings/Numbers**: Define constants for thresholds, file paths, and API settings.
- **Thread Safety**: Use thread-safe collections (e.g., `BlockingQueue`, `AtomicBoolean`) for shared state.
- **Portability**: Use `javax.sound.sampled` and avoid platform-specific APIs.
- **TOS Compliance**: Avoid game memory reading or automation.

## Contributing

- Submit pull requests with your STT implementation, including updates to `ApiFactory`, `KeyDetector`, and `ProviderEnum`.
- Include unit and integration tests, and update documentation as needed.
- Provide a temporary API key for testing your implementation, shared securely with maintainers (e.g., via a private channel).
- Ensure compliance with the project’s programming principles.
- Test with the provided dictionary file and common Elite Dangerous commands (e.g., "supercruise", "display hud").

For questions, contact the project maintainers or refer to `misc/notes` for additional context.