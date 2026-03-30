# ai/ears - Speech-to-Text & Audio Input

This package owns everything from raw microphone audio to a sanitised text transcript. It detects the audio format, calibrates VAD thresholds against the ambient noise floor, applies real-time gain normalisation, runs the STT model, and publishes the result on the event bus.

---

## Package Structure

```
ai/ears/
├── EarsInterface.java           # Lifecycle contract (extends ManagedService)
├── AudioFormatDetector.java     # Probes the system for a usable audio format
├── AudioCalibrator.java         # Two-phase RMS calibration for VAD thresholds
├── StreamNormalizer.java        # Per-frame AGC (Automatic Gain Control)
├── Amplifier.java               # Two-pass peak normaliser (offline, for test audio)
├── Resampler.java               # Converts between sample rates
├── AudioSettingsTuple.java      # DTO: (sampleRate, bufferSize)
├── RmsTupple.java               # DTO: (rmsHigh, rmsLow) VAD thresholds
├── IsSpeakingEvent.java         # Published when VAD opens / closes the gate
├── AudioMonitorEvent.java       # Published for UI-level audio level metering
├── AudioFormatException.java    # Thrown when no supported format is found
├── DumpAudioForTesting.java     # Debug utility: writes raw PCM to disk
└── parakeet/
    ├── ParakeetSTTImpl.java     # Sherpa-onnx Parakeet streaming STT backend
    └── HotwordEncoder.java      # Wake-word detection encoder
```

---

## Core Interfaces & Classes

| Class                 | Responsibility                                                                                                                                                                                     |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `EarsInterface`       | Marker interface. Extend `ManagedService` (`start()` / `stop()`). All STT backends implement this.                                                                                                 |
| `AudioFormatDetector` | Tries `TargetDataLine` formats in order (48 kHz → 44.1 kHz → 16 kHz), mono 16-bit. Returns `AudioSettingsTuple` or throws `AudioFormatException`.                                                  |
| `AudioCalibrator`     | Phase 1: 5 s silence → 75th-percentile RMS = noise floor. Phase 2: 5 s speech prompt → average RMS. Returns `RmsTupple` used as VAD open/close thresholds.                                         |
| `StreamNormalizer`    | Per-frame AGC targeting −18 dBFS. Fast attack (α = 0.4 / 100 ms), slow release (α = 0.98). Hard-clips at ±32 767. Keeps the STT model input level consistent regardless of microphone sensitivity. |
| `Resampler`           | Linear-interpolation resampler. Used when the OS delivers audio at a rate other than what the STT model expects (typically 16 kHz).                                                                |
| `ParakeetSTTImpl`     | Wraps the sherpa-onnx Java bindings for the Parakeet CTC model. Pushes PCM frames into the streaming recogniser; on silence (VAD close) finalises the hypothesis and publishes `UserInputEvent`.   |
| `HotwordEncoder`      | Optional wake-word gate. Encodes audio frames through the hotword model; only lets speech through if the wake-word was detected within the current window.                                         |

---

## Audio Pipeline

```
OS / TargetDataLine
  └─► AudioFormatDetector.detectSupportedFormat()
        → AudioSettingsTuple (sampleRate, bufferSize)

Calibration (startup, once)
  └─► AudioCalibrator.calibrateRMS(line)
        → RmsTupple (rmsHigh=speech threshold, rmsLow=silence threshold)

Per-frame loop (running)
  raw PCM frame (16-bit mono)
    └─► StreamNormalizer.normalize(frame)         # AGC
          └─► Resampler.resample(frame)           # → 16 kHz if needed
                └─► VAD gate (RMS vs thresholds)
                      ├─ gate opens  → IsSpeakingEvent(true)   [mic gate open, TTS paused]
                      ├─ frame → ParakeetSTTImpl.acceptWaveform(frame)
                      └─ gate closes → IsSpeakingEvent(false)
                                       → ParakeetSTTImpl.finalise()
                                             → UserInputEvent(transcript, confidence)
```

---

## VAD Gate

The VAD (Voice Activity Detector) is threshold-based, not a neural VAD:

- **Gate opens** when a frame's RMS exceeds `rmsHigh` (speech baseline from calibration).
- **Gate stays open** as long as RMS stays above `rmsLow` (noise floor).
- **Gate closes** after a configurable silence timeout, triggering finalisation.

When the gate opens, `IsSpeakingEvent(true)` is published so the TTS (
`mouth`) can pause playback - preventing the microphone from picking up the assistant's own voice.

---

## Adding a New STT Backend

1. Implement `EarsInterface` (and `ManagedService`).
2. Use `AudioFormatDetector` to get the audio format - do not hard-code sample rates.
3. Use `AudioCalibrator` to obtain `RmsTupple` thresholds for the VAD gate.
4. Use `StreamNormalizer` for per-frame gain before feeding the model.
5. Implement VAD gate logic.
6. Normalize audio frames before sending to STT.
7. Publish `UserInputEvent(sanitisedTranscript, confidence)` when a phrase is complete.
8. Wire in to ApiFactory for instantiation based on key pattern (if cloud service)
9. Provide a consistent user interface option to switch to the STT service following the established pattern.

**NOTE** You can use distribution/parakeet/hotwords.txt to boost the common game related words,
but DO NOT change that file. Parakeet impl will not work if there are blank lines, more than
one word per line, or any hidden characters. If you choose to use that or a different file for
boosting, place it in to ROOT/distribution/ directory and implement a method in AppPaths to load it both in IDE
and while running from the fat jar.

All contents of the distribution directory are packaged in to installation / update.


---

## EventBus

| Direction | Event                | Notes                                                   |
|-----------|----------------------|---------------------------------------------------------|
| Published | `UserInputEvent`     | Sanitised transcript + confidence [0–1]                 |
| Published | `IsSpeakingEvent`    | `true` = gate open (mic active), `false` = gate closed  |
| Published | `AudioMonitorEvent`  | Current RMS level for UI metering                       |
| Published | `AiVoxResponseEvent` | Error messages (e.g. "No supported audio format found") |

---

## Configuration

| Parameter            | Value                       | Notes                                |
|----------------------|-----------------------------|--------------------------------------|
| Sample rates tried   | 48 000 → 44 100 → 16 000 Hz | First that the OS accepts is used    |
| Buffer duration      | 100 ms                      | `bufferSize = sampleRate / 10`       |
| Calibration duration | 5 s silence + 5 s speech    | Runs at startup before the main loop |
| AGC target           | −18 dBFS (≈ 3 277 RMS)      | Hard clip at ±32 767                 |
| STT model            | Parakeet CTC (sherpa-onnx)  | Model file path from config          |

---

## External Dependencies

- **sherpa-onnx** Java bindings - native library; path set via `-Djava.library.path`
- **Java Sound API** (`javax.sound.sampled`) - audio capture
